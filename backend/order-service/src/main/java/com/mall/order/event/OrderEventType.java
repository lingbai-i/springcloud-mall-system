package com.mall.order.event;

/**
 * 订单事件类型枚举
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public enum OrderEventType {
    
    /**
     * 订单创建
     */
    ORDER_CREATED("order.created", "订单创建"),
    
    /**
     * 订单支付
     */
    ORDER_PAID("order.paid", "订单支付"),
    
    /**
     * 订单发货
     */
    ORDER_SHIPPED("order.shipped", "订单发货"),
    
    /**
     * 订单完成
     */
    ORDER_COMPLETED("order.completed", "订单完成"),
    
    /**
     * 订单取消
     */
    ORDER_CANCELLED("order.cancelled", "订单取消"),
    
    /**
     * 订单超时
     */
    ORDER_TIMEOUT("order.timeout", "订单超时");
    
    private final String routingKey;
    private final String description;
    
    OrderEventType(String routingKey, String description) {
        this.routingKey = routingKey;
        this.description = description;
    }
    
    public String getRoutingKey() {
        return routingKey;
    }
    
    public String getDescription() {
        return description;
    }
}
