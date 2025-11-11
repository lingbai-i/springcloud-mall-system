package com.mall.order.service;

import com.mall.order.entity.Order;
import com.mall.order.enums.OrderStatus;
import com.mall.order.exception.OrderPermissionException;
import com.mall.order.exception.OrderStatusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订单验证器
 * 提供订单相关的验证逻辑
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Slf4j
@Component
public class OrderValidator {
    
    /**
     * 验证订单所有者
     * 
     * @param order 订单
     * @param userId 用户ID
     * @throws OrderPermissionException 权限异常
     */
    public void validateOrderOwner(Order order, Long userId) {
        if (!order.getUserId().equals(userId)) {
            log.warn("用户无权限操作订单，userId: {}, orderId: {}, orderUserId: {}", 
                    userId, order.getId(), order.getUserId());
            throw new OrderPermissionException("无权限操作此订单");
        }
    }
    
    /**
     * 验证订单是否可以取消
     * 
     * @param order 订单
     * @throws OrderStatusException 状态异常
     */
    public void validateCancellable(Order order) {
        if (order.getStatus() != OrderStatus.PENDING && 
            order.getStatus() != OrderStatus.PAID) {
            log.warn("订单状态不允许取消，orderId: {}, status: {}", 
                    order.getId(), order.getStatus());
            throw new OrderStatusException("当前订单状态不允许取消");
        }
    }
    
    /**
     * 验证订单是否可以支付
     * 
     * @param order 订单
     * @throws OrderStatusException 状态异常
     */
    public void validatePayable(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("订单状态不允许支付，orderId: {}, status: {}", 
                    order.getId(), order.getStatus());
            throw new OrderStatusException("当前订单状态不允许支付");
        }
    }
    
    /**
     * 验证订单是否可以确认收货
     * 
     * @param order 订单
     * @throws OrderStatusException 状态异常
     */
    public void validateConfirmable(Order order) {
        if (order.getStatus() != OrderStatus.SHIPPED) {
            log.warn("订单状态不允许确认收货，orderId: {}, status: {}", 
                    order.getId(), order.getStatus());
            throw new OrderStatusException("当前订单状态不允许确认收货");
        }
    }
    
    /**
     * 验证订单是否可以申请退款
     * 
     * @param order 订单
     * @throws OrderStatusException 状态异常
     */
    public void validateRefundable(Order order) {
        if (order.getStatus() != OrderStatus.PAID && 
            order.getStatus() != OrderStatus.SHIPPED && 
            order.getStatus() != OrderStatus.COMPLETED) {
            log.warn("订单状态不允许申请退款，orderId: {}, status: {}", 
                    order.getId(), order.getStatus());
            throw new OrderStatusException("当前订单状态不允许申请退款");
        }
    }
    
    /**
     * 验证订单是否已发货（用于查看物流）
     * 
     * @param order 订单
     * @throws IllegalArgumentException 参数异常
     */
    public void validateShipped(Order order) {
        if (order.getStatus() != OrderStatus.SHIPPED && 
            order.getStatus() != OrderStatus.COMPLETED) {
            log.warn("订单尚未发货，无物流信息，orderId: {}, status: {}", 
                    order.getId(), order.getStatus());
            throw new IllegalArgumentException("订单尚未发货，暂无物流信息");
        }
    }
}
