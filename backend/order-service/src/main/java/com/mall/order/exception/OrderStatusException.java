package com.mall.order.exception;

/**
 * 订单状态异常
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public class OrderStatusException extends OrderException {
    
    private static final String ERROR_CODE = "ORDER_002";
    
    public OrderStatusException(String message) {
        super(ERROR_CODE, message);
    }
    
    public OrderStatusException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
