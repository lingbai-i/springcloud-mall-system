package com.mall.order.exception;

/**
 * 库存不足异常
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public class InsufficientStockException extends OrderException {
    
    private static final String ERROR_CODE = "ORDER_004";
    
    public InsufficientStockException(Long productId) {
        super(ERROR_CODE, "商品库存不足，商品ID: " + productId);
    }
    
    public InsufficientStockException(String message) {
        super(ERROR_CODE, message);
    }
    
    public InsufficientStockException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
