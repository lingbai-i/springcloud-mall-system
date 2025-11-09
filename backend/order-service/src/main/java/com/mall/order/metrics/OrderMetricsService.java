package com.mall.order.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 订单指标服务
 * 使用Micrometer记录订单相关指标
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Slf4j
@Service
public class OrderMetricsService {
    
    private final Counter orderCreatedCounter;
    private final Counter orderCreatedFailedCounter;
    private final Timer orderCreateTimer;
    private final Counter orderCancelledCounter;
    private final Counter orderTimeoutCounter;
    private final Counter orderPaidCounter;
    private final Counter orderCompletedCounter;
    
    public OrderMetricsService(MeterRegistry meterRegistry) {
        // 订单创建成功计数器
        this.orderCreatedCounter = Counter.builder("order.created.count")
                .description("订单创建总数")
                .register(meterRegistry);
        
        // 订单创建失败计数器
        this.orderCreatedFailedCounter = Counter.builder("order.created.failed.count")
                .description("订单创建失败数")
                .register(meterRegistry);
        
        // 订单创建耗时计时器
        this.orderCreateTimer = Timer.builder("order.create.time")
                .description("订单创建耗时")
                .register(meterRegistry);
        
        // 订单取消计数器
        this.orderCancelledCounter = Counter.builder("order.cancelled.count")
                .description("订单取消数")
                .register(meterRegistry);
        
        // 订单超时计数器
        this.orderTimeoutCounter = Counter.builder("order.timeout.count")
                .description("订单超时数")
                .register(meterRegistry);
        
        // 订单支付计数器
        this.orderPaidCounter = Counter.builder("order.paid.count")
                .description("订单支付数")
                .register(meterRegistry);
        
        // 订单完成计数器
        this.orderCompletedCounter = Counter.builder("order.completed.count")
                .description("订单完成数")
                .register(meterRegistry);
    }
    
    /**
     * 记录订单创建成功
     */
    public void recordOrderCreated(BigDecimal amount) {
        try {
            orderCreatedCounter.increment();
            log.debug("记录订单创建成功指标，金额: {}", amount);
        } catch (Exception e) {
            log.warn("记录订单创建成功指标失败", e);
        }
    }
    
    /**
     * 记录订单创建失败
     */
    public void recordOrderCreateFailed() {
        try {
            orderCreatedFailedCounter.increment();
            log.debug("记录订单创建失败指标");
        } catch (Exception e) {
            log.warn("记录订单创建失败指标失败", e);
        }
    }
    
    /**
     * 记录订单创建耗时
     */
    public void recordOrderCreateTime(long milliseconds) {
        try {
            orderCreateTimer.record(milliseconds, TimeUnit.MILLISECONDS);
            log.debug("记录订单创建耗时: {} ms", milliseconds);
        } catch (Exception e) {
            log.warn("记录订单创建耗时失败", e);
        }
    }
    
    /**
     * 记录订单取消
     */
    public void recordOrderCancelled() {
        try {
            orderCancelledCounter.increment();
            log.debug("记录订单取消指标");
        } catch (Exception e) {
            log.warn("记录订单取消指标失败", e);
        }
    }
    
    /**
     * 记录订单超时
     */
    public void recordOrderTimeout() {
        try {
            orderTimeoutCounter.increment();
            log.debug("记录订单超时指标");
        } catch (Exception e) {
            log.warn("记录订单超时指标失败", e);
        }
    }
    
    /**
     * 记录订单支付
     */
    public void recordOrderPaid(BigDecimal amount) {
        try {
            orderPaidCounter.increment();
            log.debug("记录订单支付指标，金额: {}", amount);
        } catch (Exception e) {
            log.warn("记录订单支付指标失败", e);
        }
    }
    
    /**
     * 记录订单完成
     */
    public void recordOrderCompleted(BigDecimal amount) {
        try {
            orderCompletedCounter.increment();
            log.debug("记录订单完成指标，金额: {}", amount);
        } catch (Exception e) {
            log.warn("记录订单完成指标失败", e);
        }
    }
}
