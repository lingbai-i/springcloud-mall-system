package com.mall.order.service;

import com.mall.order.entity.Order;
import com.mall.order.enums.OrderStatus;
import com.mall.order.repository.OrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单指标监控服务
 * 负责收集和统计订单相关的业务指标
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，实现订单业务指标监控功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMetricsService {

    private final MeterRegistry meterRegistry;
    private final OrderRepository orderRepository;

    // 计数器 - 统计各种操作次数
    private Counter orderCreatedCounter;
    private Counter orderPaidCounter;
    private Counter orderCancelledCounter;
    private Counter orderCompletedCounter;
    private Counter orderTimeoutCounter;

    // 计时器 - 统计操作耗时
    private Timer orderCreateTimer;
    private Timer orderPaymentTimer;
    private Timer orderQueryTimer;

    // 实时指标缓存
    private final AtomicLong totalOrderCount = new AtomicLong(0);
    private final AtomicLong todayOrderCount = new AtomicLong(0);
    private final Map<OrderStatus, AtomicLong> statusCountMap = new ConcurrentHashMap<>();
    private volatile BigDecimal todayRevenue = BigDecimal.ZERO;
    private volatile BigDecimal totalRevenue = BigDecimal.ZERO;

    /**
     * 初始化监控指标
     */
    @PostConstruct
    public void initMetrics() {
        log.info("初始化订单监控指标");

        // 初始化计数器
        orderCreatedCounter = Counter.builder("order.created.total")
                .description("订单创建总数")
                .register(meterRegistry);

        orderPaidCounter = Counter.builder("order.paid.total")
                .description("订单支付成功总数")
                .register(meterRegistry);

        orderCancelledCounter = Counter.builder("order.cancelled.total")
                .description("订单取消总数")
                .register(meterRegistry);

        orderCompletedCounter = Counter.builder("order.completed.total")
                .description("订单完成总数")
                .register(meterRegistry);

        orderTimeoutCounter = Counter.builder("order.timeout.total")
                .description("订单超时总数")
                .register(meterRegistry);

        // 初始化计时器
        orderCreateTimer = Timer.builder("order.create.duration")
                .description("订单创建耗时")
                .register(meterRegistry);

        orderPaymentTimer = Timer.builder("order.payment.duration")
                .description("订单支付处理耗时")
                .register(meterRegistry);

        orderQueryTimer = Timer.builder("order.query.duration")
                .description("订单查询耗时")
                .register(meterRegistry);

        // 初始化状态计数器
        for (OrderStatus status : OrderStatus.values()) {
            statusCountMap.put(status, new AtomicLong(0));
        }

        // 注册实时指标
        Gauge.builder("order.total.count")
                .description("订单总数")
                .register(meterRegistry, this, OrderMetricsService::getTotalOrderCount);

        Gauge.builder("order.today.count")
                .description("今日订单数")
                .register(meterRegistry, this, OrderMetricsService::getTodayOrderCount);

        Gauge.builder("order.today.revenue")
                .description("今日营收")
                .register(meterRegistry, this, service -> service.todayRevenue.doubleValue());

        Gauge.builder("order.total.revenue")
                .description("总营收")
                .register(meterRegistry, this, service -> service.totalRevenue.doubleValue());

        // 为每个订单状态注册指标
        for (OrderStatus status : OrderStatus.values()) {
            Gauge.builder("order.status.count")
                    .description("各状态订单数量")
                    .tag("status", status.name())
                    .register(meterRegistry, status, s -> statusCountMap.get(s).get());
        }

        // 初始化数据
        refreshMetrics();
    }

    /**
     * 记录订单创建指标
     * 
     * @param order 订单
     * @param duration 创建耗时（毫秒）
     */
    public void recordOrderCreated(Order order, long duration) {
        orderCreatedCounter.increment();
        orderCreateTimer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        totalOrderCount.incrementAndGet();
        statusCountMap.get(OrderStatus.PENDING_PAYMENT).incrementAndGet();
        
        // 如果是今天创建的订单，增加今日计数
        if (isToday(order.getCreateTime())) {
            todayOrderCount.incrementAndGet();
        }
        
        log.debug("记录订单创建指标: orderId={}, duration={}ms", order.getId(), duration);
    }

    /**
     * 记录订单支付指标
     * 
     * @param order 订单
     * @param duration 支付处理耗时（毫秒）
     */
    public void recordOrderPaid(Order order, long duration) {
        orderPaidCounter.increment();
        orderPaymentTimer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        // 更新状态计数
        statusCountMap.get(OrderStatus.PENDING_PAYMENT).decrementAndGet();
        statusCountMap.get(OrderStatus.PAID).incrementAndGet();
        
        // 更新营收
        BigDecimal amount = order.getPayableAmount();
        totalRevenue = totalRevenue.add(amount);
        
        if (isToday(order.getPaymentTime())) {
            todayRevenue = todayRevenue.add(amount);
        }
        
        log.debug("记录订单支付指标: orderId={}, amount={}, duration={}ms", 
                order.getId(), amount, duration);
    }

    /**
     * 记录订单取消指标
     * 
     * @param order 订单
     */
    public void recordOrderCancelled(Order order) {
        orderCancelledCounter.increment();
        
        // 更新状态计数
        OrderStatus oldStatus = order.getStatus();
        if (statusCountMap.containsKey(oldStatus)) {
            statusCountMap.get(oldStatus).decrementAndGet();
        }
        statusCountMap.get(OrderStatus.CANCELLED).incrementAndGet();
        
        log.debug("记录订单取消指标: orderId={}", order.getId());
    }

    /**
     * 记录订单完成指标
     * 
     * @param order 订单
     */
    public void recordOrderCompleted(Order order) {
        orderCompletedCounter.increment();
        
        // 更新状态计数
        statusCountMap.get(OrderStatus.SHIPPED).decrementAndGet();
        statusCountMap.get(OrderStatus.COMPLETED).incrementAndGet();
        
        log.debug("记录订单完成指标: orderId={}", order.getId());
    }

    /**
     * 记录订单超时指标
     * 
     * @param orderId 订单ID
     */
    public void recordOrderTimeout(Long orderId) {
        orderTimeoutCounter.increment();
        
        // 更新状态计数
        statusCountMap.get(OrderStatus.PENDING_PAYMENT).decrementAndGet();
        statusCountMap.get(OrderStatus.CANCELLED).incrementAndGet();
        
        log.debug("记录订单超时指标: orderId={}", orderId);
    }

    /**
     * 记录订单查询指标
     * 
     * @param duration 查询耗时（毫秒）
     */
    public void recordOrderQuery(long duration) {
        orderQueryTimer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        log.debug("记录订单查询指标: duration={}ms", duration);
    }

    /**
     * 定时刷新指标数据
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void refreshMetrics() {
        log.info("开始刷新订单指标数据");
        
        try {
            // 刷新总订单数
            long totalCount = orderRepository.count();
            totalOrderCount.set(totalCount);
            
            // 刷新今日订单数
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            long todayCount = orderRepository.countByCreateTimeAfter(todayStart);
            todayOrderCount.set(todayCount);
            
            // 刷新各状态订单数
            for (OrderStatus status : OrderStatus.values()) {
                long count = orderRepository.countByStatus(status);
                statusCountMap.get(status).set(count);
            }
            
            // 刷新营收数据
            BigDecimal totalRev = orderRepository.sumPayableAmountByStatus(OrderStatus.PAID);
            totalRevenue = totalRev != null ? totalRev : BigDecimal.ZERO;
            
            BigDecimal todayRev = orderRepository.sumPayableAmountByStatusAndPaymentTimeAfter(
                    OrderStatus.PAID, todayStart);
            todayRevenue = todayRev != null ? todayRev : BigDecimal.ZERO;
            
            log.info("订单指标数据刷新完成: 总订单数={}, 今日订单数={}, 总营收={}, 今日营收={}", 
                    totalCount, todayCount, totalRevenue, todayRevenue);
            
        } catch (Exception e) {
            log.error("刷新订单指标数据失败", e);
        }
    }

    /**
     * 获取订单总数
     * 
     * @return 订单总数
     */
    public long getTotalOrderCount() {
        return totalOrderCount.get();
    }

    /**
     * 获取今日订单数
     * 
     * @return 今日订单数
     */
    public long getTodayOrderCount() {
        return todayOrderCount.get();
    }

    /**
     * 获取指定状态的订单数量
     * 
     * @param status 订单状态
     * @return 订单数量
     */
    public long getOrderCountByStatus(OrderStatus status) {
        return statusCountMap.getOrDefault(status, new AtomicLong(0)).get();
    }

    /**
     * 获取今日营收
     * 
     * @return 今日营收
     */
    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    /**
     * 获取总营收
     * 
     * @return 总营收
     */
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    /**
     * 判断时间是否为今天
     * 
     * @param dateTime 时间
     * @return 是否为今天
     */
    private boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return dateTime.toLocalDate().equals(now.toLocalDate());
    }
}