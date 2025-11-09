package com.mall.order.exception;

/**
 * 订单不存在异常
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public class OrderNotFoundException extends OrderException {
    
    private static final String ERROR_CODE = "ORDER_001";
    
    public OrderNotFoundException(Long orderId) {
        super(ERROR_CODE, "订单不存在: " + orderId);
    }
    
    public OrderNotFoundException(String orderNo) {
        super(ERROR_CODE, "订单不存在: " + orderNo);
    }
    
    public OrderNotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
