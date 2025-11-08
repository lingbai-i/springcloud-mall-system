package com.mall.order.event;

import com.mall.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单事件基类
 * 用于订单状态变更时的消息传递
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，实现订单事件基类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID，用于幂等性控制
     */
    private String eventId;

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
     * 订单状态（变更前）
     */
    private OrderStatus oldStatus;

    /**
     * 订单状态（变更后）
     */
    private OrderStatus newStatus;

    /**
     * 订单金额
     */
    private BigDecimal totalAmount;

    /**
     * 实际支付金额
     */
    private BigDecimal actualAmount;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 事件描述
     */
    private String description;

    /**
     * 扩展数据（JSON格式）
     */
    private String extData;

    /**
     * 订单事件类型枚举
     */
    public enum OrderEventType {
        /**
         * 订单创建
         */
        ORDER_CREATED,
        
        /**
         * 订单支付
         */
        ORDER_PAID,
        
        /**
         * 订单发货
         */
        ORDER_SHIPPED,
        
        /**
         * 订单完成
         */
        ORDER_COMPLETED,
        
        /**
         * 订单取消
         */
        ORDER_CANCELLED,
        
        /**
         * 订单退款
         */
        ORDER_REFUNDED,
        
        /**
         * 库存扣减失败
         */
        STOCK_DEDUCTION_FAILED,
        
        /**
         * 订单超时
         */
        ORDER_TIMEOUT
    }

    /**
     * 创建订单创建事件
     * 
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param totalAmount 订单总金额
     * @param actualAmount 实际支付金额
     * @return 订单创建事件
     */
    public static OrderEvent createOrderCreatedEvent(Long orderId, String orderNo, Long userId, 
                                                   BigDecimal totalAmount, BigDecimal actualAmount) {
        OrderEvent event = new OrderEvent();
        event.setEventId(generateEventId());
        event.setEventType(OrderEventType.ORDER_CREATED);
        event.setOrderId(orderId);
        event.setOrderNo(orderNo);
        event.setUserId(userId);
        event.setOldStatus(null);
        event.setNewStatus(OrderStatus.PENDING_PAYMENT);
        event.setTotalAmount(totalAmount);
        event.setActualAmount(actualAmount);
        event.setEventTime(LocalDateTime.now());
        event.setDescription("订单创建成功");
        return event;
    }

    /**
     * 创建订单状态变更事件
     * 
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param eventType 事件类型
     * @param description 事件描述
     * @return 订单状态变更事件
     */
    public static OrderEvent createStatusChangeEvent(Long orderId, String orderNo, Long userId,
                                                   OrderStatus oldStatus, OrderStatus newStatus,
                                                   OrderEventType eventType, String description) {
        OrderEvent event = new OrderEvent();
        event.setEventId(generateEventId());
        event.setEventType(eventType);
        event.setOrderId(orderId);
        event.setOrderNo(orderNo);
        event.setUserId(userId);
        event.setOldStatus(oldStatus);
        event.setNewStatus(newStatus);
        event.setEventTime(LocalDateTime.now());
        event.setDescription(description);
        return event;
    }

    /**
     * 创建库存扣减失败事件
     * 
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param reason 失败原因
     * @return 库存扣减失败事件
     */
    public static OrderEvent createStockDeductionFailedEvent(Long orderId, String orderNo, Long userId, String reason) {
        OrderEvent event = new OrderEvent();
        event.setEventId(generateEventId());
        event.setEventType(OrderEventType.STOCK_DEDUCTION_FAILED);
        event.setOrderId(orderId);
        event.setOrderNo(orderNo);
        event.setUserId(userId);
        event.setEventTime(LocalDateTime.now());
        event.setDescription("库存扣减失败：" + reason);
        return event;
    }

    /**
     * 创建订单超时事件
     * 
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 订单超时事件
     */
    public static OrderEvent createOrderTimeoutEvent(Long orderId, String orderNo, Long userId) {
        OrderEvent event = new OrderEvent();
        event.setEventId(generateEventId());
        event.setEventType(OrderEventType.ORDER_TIMEOUT);
        event.setOrderId(orderId);
        event.setOrderNo(orderNo);
        event.setUserId(userId);
        event.setEventTime(LocalDateTime.now());
        event.setDescription("订单支付超时");
        return event;
    }

    /**
     * 生成事件ID
     * 
     * @return 事件ID
     */
    private static String generateEventId() {
        return "EVENT_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
}