package com.mall.order.exception;

/**
 * 订单权限异常
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public class OrderPermissionException extends OrderException {
    
    private static final String ERROR_CODE = "ORDER_003";
    
    public OrderPermissionException(String message) {
        super(ERROR_CODE, message);
    }
    
    public OrderPermissionException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
