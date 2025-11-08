package com.mall.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单统计响应DTO
 * 用于返回用户订单统计信息
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，包含各状态订单数量和总消费金额统计
 */
@Data
public class OrderStatsResponse {
    
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
     * 总订单数量
     */
    private Long totalOrders;
    
    /**
     * 总消费金额（已完成订单）
     */
    private BigDecimal totalAmount;
    
    /**
     * 本月订单数量
     */
    private Long monthlyOrders;
    
    /**
     * 本月消费金额
     */
    private BigDecimal monthlyAmount;
    
    /**
     * 平均订单金额
     */
    private BigDecimal averageOrderAmount;
    
    /**
     * 最近一次下单时间
     */
    private String lastOrderTime;
    
    /**
     * 构造函数 - 创建空的统计对象
     */
    public OrderStatsResponse() {
        this.pendingPayment = 0L;
        this.paid = 0L;
        this.shipped = 0L;
        this.completed = 0L;
        this.cancelled = 0L;
        this.refunded = 0L;
        this.totalOrders = 0L;
        this.totalAmount = BigDecimal.ZERO;
        this.monthlyOrders = 0L;
        this.monthlyAmount = BigDecimal.ZERO;
        this.averageOrderAmount = BigDecimal.ZERO;
    }
}