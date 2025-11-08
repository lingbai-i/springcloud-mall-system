package com.mall.order.service;

import com.mall.order.entity.Order;
import com.mall.order.enums.OrderStatus;
import com.mall.order.event.OrderEvent;
import com.mall.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时处理服务
 * 负责处理订单支付超时的自动取消逻辑
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，实现订单超时处理功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTimeoutService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderMetricsService orderMetricsService;

    /**
     * 订单支付超时时间（分钟）
     */
    @Value("${mall.order.payment-timeout-minutes:30}")
    private int paymentTimeoutMinutes;

    /**
     * 定时检查并处理超时订单
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    @Transactional
    public void processTimeoutOrders() {
        log.info("开始处理超时订单，超时时间：{}分钟", paymentTimeoutMinutes);
        
        try {
            // 计算超时时间点
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(paymentTimeoutMinutes);
            
            // 查询超时的待支付订单
            List<Order> timeoutOrders = orderRepository.findByStatusAndCreateTimeBefore(
                    OrderStatus.PENDING_PAYMENT, timeoutTime);
            
            if (timeoutOrders.isEmpty()) {
                log.debug("没有发现超时订单");
                return;
            }
            
            log.info("发现{}个超时订单，开始处理", timeoutOrders.size());
            
            int processedCount = 0;
            int failedCount = 0;
            
            for (Order order : timeoutOrders) {
                try {
                    // 处理单个超时订单
                    processTimeoutOrder(order);
                    processedCount++;
                    
                } catch (Exception e) {
                    log.error("处理超时订单失败: orderId={}, orderNo={}, error={}", 
                            order.getId(), order.getOrderNo(), e.getMessage(), e);
                    failedCount++;
                }
            }
            
            log.info("超时订单处理完成，成功处理：{}个，失败：{}个", processedCount, failedCount);
            
            // 记录订单超时指标
            timeoutOrders.forEach(order -> orderMetricsService.recordOrderTimeout());
            
        } catch (Exception e) {
            log.error("处理超时订单时发生异常", e);
        }
    }

    /**
     * 处理单个超时订单
     * 
     * @param order 超时订单
     */
    @Transactional
    public void processTimeoutOrder(Order order) {
        log.info("处理超时订单: orderId={}, orderNo={}, userId={}, createTime={}", 
                order.getId(), order.getOrderNo(), order.getUserId(), order.getCreateTime());
        
        // 检查订单状态，确保只处理待支付状态的订单
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("订单状态不是待支付，跳过处理: orderId={}, status={}", 
                    order.getId(), order.getStatus());
            return;
        }
        
        // 更新订单状态为已取消
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason("支付超时自动取消");
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // 保存订单状态变更
        orderRepository.save(order);
        
        log.info("订单超时自动取消成功: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
        
        // 发布订单超时事件
        OrderEvent timeoutEvent = OrderEvent.createOrderTimeoutEvent(
                order.getId(), order.getOrderNo(), order.getUserId());
        orderEventPublisher.publishOrderTimeoutEvent(timeoutEvent);
        
        // 发布订单取消事件
        OrderEvent cancelEvent = OrderEvent.createStatusChangeEvent(
                order.getId(), order.getOrderNo(), order.getUserId(),
                oldStatus, OrderStatus.CANCELLED,
                OrderEvent.OrderEventType.ORDER_CANCELLED,
                "支付超时自动取消");
        orderEventPublisher.publishOrderCancelledEvent(cancelEvent);
    }

    /**
     * 手动触发超时订单处理
     * 用于测试或紧急情况下的手动处理
     */
    public void manualProcessTimeoutOrders() {
        log.info("手动触发超时订单处理");
        processTimeoutOrders();
    }

    /**
     * 检查指定订单是否超时
     * 
     * @param orderId 订单ID
     * @return 是否超时
     */
    public boolean isOrderTimeout(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("订单不存在: orderId={}", orderId);
            return false;
        }
        
        // 只有待支付状态的订单才需要检查超时
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return false;
        }
        
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(paymentTimeoutMinutes);
        boolean isTimeout = order.getCreateTime().isBefore(timeoutTime);
        
        log.debug("检查订单超时: orderId={}, createTime={}, timeoutTime={}, isTimeout={}", 
                orderId, order.getCreateTime(), timeoutTime, isTimeout);
        
        return isTimeout;
    }

    /**
     * 获取订单剩余支付时间（分钟）
     * 
     * @param orderId 订单ID
     * @return 剩余支付时间，如果已超时返回0，如果订单不存在或状态不对返回-1
     */
    public long getRemainingPaymentTime(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("订单不存在: orderId={}", orderId);
            return -1;
        }
        
        // 只有待支付状态的订单才有剩余时间
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            return -1;
        }
        
        LocalDateTime expireTime = order.getCreateTime().plusMinutes(paymentTimeoutMinutes);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(expireTime)) {
            return 0; // 已超时
        }
        
        // 计算剩余分钟数
        long remainingMinutes = java.time.Duration.between(now, expireTime).toMinutes();
        
        log.debug("订单剩余支付时间: orderId={}, remainingMinutes={}", orderId, remainingMinutes);
        
        return Math.max(0, remainingMinutes);
    }
}