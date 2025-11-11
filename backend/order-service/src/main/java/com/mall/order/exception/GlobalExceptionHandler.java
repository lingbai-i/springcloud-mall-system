package com.mall.order.exception;

import com.mall.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理订单服务的异常，转换为标准响应格式
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理 Feign 调用异常
     */
    @ExceptionHandler(FeignClientException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public R<Void> handleFeignClientException(FeignClientException e) {
        log.error("远程服务调用失败: 服务={}, 方法={}, 错误={}", 
                e.getServiceName(), e.getMethod(), e.getMessage());
        return R.fail(503, e.getMessage());
    }
    
    /**
     * 处理订单不存在异常
     */
    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<Void> handleOrderNotFoundException(OrderNotFoundException e) {
        log.warn("订单不存在异常: {}", e.getMessage());
        return R.fail(404, e.getMessage());
    }
    
    /**
     * 处理订单状态异常
     */
    @ExceptionHandler(OrderStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleOrderStatusException(OrderStatusException e) {
        log.warn("订单状态异常: {}", e.getMessage());
        return R.fail(400, e.getMessage());
    }
    
    /**
     * 处理订单权限异常
     */
    @ExceptionHandler(OrderPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleOrderPermissionException(OrderPermissionException e) {
        log.warn("订单权限异常: {}", e.getMessage());
        return R.fail(403, e.getMessage());
    }
    
    /**
     * 处理库存不足异常
     */
    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleInsufficientStockException(InsufficientStockException e) {
        log.warn("库存不足异常: {}", e.getMessage());
        return R.fail(400, e.getMessage());
    }
    
    /**
     * 处理订单业务异常
     */
    @ExceptionHandler(OrderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleOrderException(OrderException e) {
        log.warn("订单业务异常: {}", e.getMessage());
        return R.fail(400, e.getMessage());
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return R.fail(400, "参数校验失败: " + message);
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return R.fail(400, "参数绑定失败: " + message);
    }
    
    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束违反: {}", message);
        return R.fail(400, "约束违反: " + message);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage());
        return R.fail(400, e.getMessage());
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return R.fail(500, "系统异常: " + e.getMessage());
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return R.fail(500, "系统异常，请稍后重试");
    }
}
