package com.mall.order.exception;

/**
 * 订单业务异常
 * 用于处理订单相关的业务异常
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public class OrderException extends RuntimeException {
    
    private final int code;
    
    public OrderException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public OrderException(String message) {
        super(message);
        this.code = 400;
    }
    
    public OrderException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
    }
    
    public int getCode() {
        return code;
    }
    
    // 常用的订单异常
    public static class OrderNotFoundException extends OrderException {
        public OrderNotFoundException(String message) {
            super(404, message);
        }
    }
    
    public static class OrderStatusException extends OrderException {
        public OrderStatusException(String message) {
            super(400, message);
        }
    }
    
    public static class InsufficientStockException extends OrderException {
        public InsufficientStockException(String message) {
            super(400, message);
        }
    }
    
    public static class PaymentException extends OrderException {
        public PaymentException(String message) {
            super(400, message);
        }
    }
}