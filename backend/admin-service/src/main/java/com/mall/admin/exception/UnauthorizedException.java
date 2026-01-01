package com.mall.admin.exception;

/**
 * 未授权异常
 * 
 * @author lingbai
 * @since 2025-01-09
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
}
