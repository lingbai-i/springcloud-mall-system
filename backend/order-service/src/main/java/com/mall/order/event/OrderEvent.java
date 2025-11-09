package com.mall.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单事件基类
 * 用于订单状态变更的事件通知
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 事件类型
     */
    private OrderEventType eventType;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 订单金额
     */
    private BigDecimal amount;
    
    /**
     * 附加信息
     */
    private String message;
    
    /**
     * 事件时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 创建订单创建事件
     */
    public static OrderEvent createOrderCreatedEvent(Long orderId, String orderNo, 
                                                      Long userId, BigDecimal amount) {
        return OrderEvent.builder()
                .eventType(OrderEventType.ORDER_CREATED)
                .orderId(orderId)
                .orderNo(orderNo)
                .userId(userId)
                .amount(amount)
                .eventTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建订单支付事件
     */
    public static OrderEvent createOrderPaidEvent(Long orderId, String orderNo, 
                                                   Long userId, BigDecimal amount) {
        return OrderEvent.builder()
                .eventType(OrderEventType.ORDER_PAID)
                .orderId(orderId)
                .orderNo(orderNo)
                .userId(userId)
                .amount(amount)
                .eventTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建订单发货事件
     */
    public static OrderEvent createOrderShippedEvent(Long orderId, String orderNo, Long userId) {
        return OrderEvent.builder()
                .eventType(OrderEventType.ORDER_SHIPPED)
                .orderId(orderId)
                .orderNo(orderNo)
                .userId(userId)
                .eventTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建订单完成事件
     */
    public static OrderEvent createOrderCompletedEvent(Long orderId, String orderNo, Long userId) {
        return OrderEvent.builder()
                .eventType(OrderEventType.ORDER_COMPLETED)
                .orderId(orderId)
                .orderNo(orderNo)
                .userId(userId)
                .eventTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建订单取消事件
     */
    public static OrderEvent createOrderCancelledEvent(Long orderId, String orderNo, 
                                                        Long userId, String reason) {
        return OrderEvent.builder()
                .eventType(OrderEventType.ORDER_CANCELLED)
                .orderId(orderId)
                .orderNo(orderNo)
                .userId(userId)
                .message(reason)
                .eventTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建订单超时事件
     */
    public static OrderEvent createOrderTimeoutEvent(Long orderId, String orderNo, Long userId) {
        return OrderEvent.builder()
                .eventType(OrderEventType.ORDER_TIMEOUT)
                .orderId(orderId)
                .orderNo(orderNo)
                .userId(userId)
                .eventTime(LocalDateTime.now())
                .build();
    }
}
