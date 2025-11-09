package com.mall.admin.exception;

/**
 * 权限拒绝异常
 */
public class PermissionDeniedException extends RuntimeException {
    
    public PermissionDeniedException(String message) {
        super(message);
    }
}
