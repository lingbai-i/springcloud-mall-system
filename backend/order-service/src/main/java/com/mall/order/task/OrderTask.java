package com.mall.order.task;

import com.mall.order.service.DistributedLockService;
import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 订单定时任务
 * 处理订单相关的定时任务，如超时订单处理、自动确认收货等
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTask {
    
    private final OrderService orderService;
    private final DistributedLockService distributedLockService;
    
    private static final String TIMEOUT_TASK_LOCK = "order:task:timeout";
    private static final String AUTO_CONFIRM_TASK_LOCK = "order:task:auto-confirm";
    
    // 标记应用是否已完全启动
    private final AtomicBoolean applicationReady = new AtomicBoolean(false);
    
    /**
     * 应用启动完成事件监听
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        applicationReady.set(true);
        log.info("订单服务已完全启动，定时任务已激活");
    }
    
    /**
     * 处理超时订单
     * 每5分钟执行一次，取消超时未付款的订单
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 60 * 1000) // 5分钟执行一次，延迟1分钟启动
    public void processTimeoutOrders() {
        // 检查应用是否已完全启动
        if (!applicationReady.get()) {
            log.debug("应用尚未完全启动，跳过超时订单处理任务");
            return;
        }
        
        log.info("开始执行超时订单处理任务");
        
        // 使用分布式锁防止多实例重复执行
        String lockValue = String.valueOf(System.currentTimeMillis());
        Boolean acquired = distributedLockService.executeWithLock(
                TIMEOUT_TASK_LOCK, 
                lockValue, 
                300L, // 5分钟超时
                () -> {
                    try {
                        int processedCount = orderService.handleTimeoutOrders();
                        log.info("超时订单处理任务完成，处理订单数量: {}", processedCount);
                        return true;
                    } catch (Exception e) {
                        log.error("超时订单处理任务执行失败", e);
                        return false;
                    }
                }
        );
        
        if (Boolean.FALSE.equals(acquired)) {
            log.info("超时订单处理任务已在其他实例执行，跳过");
        }
    }
    
    /**
     * 自动确认收货
     * 每天凌晨2点执行，自动确认超过指定天数的已发货订单
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    public void autoConfirmOrders() {
        // 检查应用是否已完全启动
        if (!applicationReady.get()) {
            log.debug("应用尚未完全启动，跳过自动确认收货任务");
            return;
        }
        
        log.info("开始执行自动确认收货任务");
        
        // 使用分布式锁防止多实例重复执行
        String lockValue = String.valueOf(System.currentTimeMillis());
        Boolean acquired = distributedLockService.executeWithLock(
                AUTO_CONFIRM_TASK_LOCK, 
                lockValue, 
                600L, // 10分钟超时
                () -> {
                    try {
                        int processedCount = orderService.autoConfirmOrders();
                        log.info("自动确认收货任务完成，处理订单数量: {}", processedCount);
                        return true;
                    } catch (Exception e) {
                        log.error("自动确认收货任务执行失败", e);
                        return false;
                    }
                }
        );
        
        if (Boolean.FALSE.equals(acquired)) {
            log.info("自动确认收货任务已在其他实例执行，跳过");
        }
    }
}