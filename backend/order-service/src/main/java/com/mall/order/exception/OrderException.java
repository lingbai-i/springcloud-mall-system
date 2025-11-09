package com.mall.order.exception;

/**
 * 订单业务异常基类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public class OrderException extends RuntimeException {
    
    private String code;
    
    public OrderException(String message) {
        super(message);
    }
    
    public OrderException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public OrderException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
