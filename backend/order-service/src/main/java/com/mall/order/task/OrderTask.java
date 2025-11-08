package com.mall.order.task;

import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    
    /**
     * 处理超时订单
     * 每5分钟执行一次，取消超时未付款的订单
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void processTimeoutOrders() {
        log.info("开始执行超时订单处理任务");
        
        try {
            int processedCount = orderService.handleTimeoutOrders();
            log.info("超时订单处理任务完成，处理订单数量: {}", processedCount);
        } catch (Exception e) {
            log.error("超时订单处理任务执行失败", e);
        }
    }
    
    /**
     * 自动确认收货
     * 每天凌晨2点执行，自动确认超过指定天数的已发货订单
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    public void autoConfirmOrders() {
        log.info("开始执行自动确认收货任务");
        
        try {
            int processedCount = orderService.autoConfirmOrders();
            log.info("自动确认收货任务完成，处理订单数量: {}", processedCount);
        } catch (Exception e) {
            log.error("自动确认收货任务执行失败", e);
        }
    }
}