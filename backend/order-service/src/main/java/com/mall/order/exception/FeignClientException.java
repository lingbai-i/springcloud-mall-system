package com.mall.order.exception;

/**
 * Feign 调用异常
 * 用于封装远程服务调用失败的异常
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public class FeignClientException extends RuntimeException {
    
    private final String serviceName;
    private final String method;
    
    public FeignClientException(String serviceName, String method, String message) {
        super(String.format("调用 %s 服务失败 [%s]: %s", serviceName, method, message));
        this.serviceName = serviceName;
        this.method = method;
    }
    
    public FeignClientException(String serviceName, String method, String message, Throwable cause) {
        super(String.format("调用 %s 服务失败 [%s]: %s", serviceName, method, message), cause);
        this.serviceName = serviceName;
        this.method = method;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getMethod() {
        return method;
    }
}
