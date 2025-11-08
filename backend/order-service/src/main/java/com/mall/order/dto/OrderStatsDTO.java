package com.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单统计数据传输对象
 * 用于订单统计信息的传输和展示
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
public class OrderStatsDTO {
    
    /**
     * 待付款订单数量
     */
    private Long pendingPayment;
    
    /**
     * 已付款订单数量
     */
    private Long paid;
    
    /**
     * 已发货订单数量
     */
    private Long shipped;
    
    /**
     * 已完成订单数量
     */
    private Long completed;
    
    /**
     * 已取消订单数量
     */
    private Long cancelled;
    
    /**
     * 已退款订单数量
     */
    private Long refunded;
    
    /**
     * 订单总数
     */
    private Long totalOrders;
    
    /**
     * 总消费金额
     */
    private BigDecimal totalAmount;
}