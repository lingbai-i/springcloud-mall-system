package com.mall.payment.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理支付服务中的各种异常，提供标准化的错误响应格式
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理支付业务异常
     * 
     * @param e 支付异常
     * @return 错误响应
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentException(PaymentException e) {
        logger.warn("支付业务异常: 错误码={}, 错误信息={}", e.getErrorCode(), e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", e.getErrorCode());
        response.put("message", e.getMessage());
        
        if (e.getErrorDetails() != null) {
            response.put("details", e.getErrorDetails());
        }

        // 根据错误码确定HTTP状态码
        HttpStatus status = determineHttpStatus(e.getErrorCode());
        
        return ResponseEntity.status(status).body(response);
    }

    /**
     * 处理参数验证异常（@Valid注解）
     * 
     * @param e 方法参数验证异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.warn("参数验证异常: {}", e.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "VALIDATION_ERROR");
        response.put("message", "参数验证失败");
        response.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理绑定异常
     * 
     * @param e 绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException e) {
        logger.warn("参数绑定异常: {}", e.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "BINDING_ERROR");
        response.put("message", "参数绑定失败");
        response.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理约束违反异常（@Validated注解）
     * 
     * @param e 约束违反异常
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException e) {
        logger.warn("约束验证异常: {}", e.getMessage());

        String violations = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "CONSTRAINT_VIOLATION");
        response.put("message", "约束验证失败: " + violations);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理非法参数异常
     * 
     * @param e 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("非法参数异常: {}", e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "ILLEGAL_ARGUMENT");
        response.put("message", e.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理非法状态异常
     * 
     * @param e 非法状态异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException e) {
        logger.warn("非法状态异常: {}", e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "ILLEGAL_STATE");
        response.put("message", e.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理空指针异常
     * 
     * @param e 空指针异常
     * @return 错误响应
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointerException(NullPointerException e) {
        logger.error("空指针异常", e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "NULL_POINTER");
        response.put("message", "系统内部错误");

        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * 处理运行时异常
     * 
     * @param e 运行时异常
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常: {}", e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "RUNTIME_ERROR");
        response.put("message", "系统运行时错误");

        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * 处理通用异常
     * 
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        logger.error("未知异常: {}", e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "UNKNOWN_ERROR");
        response.put("message", "系统未知错误");

        return ResponseEntity.internalServerError().body(response);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据错误码确定HTTP状态码
     * 
     * @param errorCode 错误码
     * @return HTTP状态码
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        switch (errorCode) {
            // 400 Bad Request - 客户端请求错误
            case "INVALID_PARAMETER":
            case "INVALID_AMOUNT":
            case "INVALID_ORDER_STATUS":
            case "UNSUPPORTED_PAYMENT_METHOD":
            case "REFUND_AMOUNT_EXCEEDED":
            case "REFUND_NOT_SUPPORTED":
            case "DUPLICATE_PAYMENT":
            case "SIGNATURE_VERIFICATION_FAILED":
                return HttpStatus.BAD_REQUEST;

            // 404 Not Found - 资源不存在
            case "ORDER_NOT_FOUND":
                return HttpStatus.NOT_FOUND;

            // 402 Payment Required - 支付相关错误
            case "INSUFFICIENT_BALANCE":
                return HttpStatus.PAYMENT_REQUIRED;

            // 408 Request Timeout - 请求超时
            case "PAYMENT_TIMEOUT":
                return HttpStatus.REQUEST_TIMEOUT;

            // 502 Bad Gateway - 第三方服务错误
            case "THIRD_PARTY_PAYMENT_ERROR":
            case "NETWORK_ERROR":
                return HttpStatus.BAD_GATEWAY;

            // 500 Internal Server Error - 服务器内部错误
            case "SYSTEM_ERROR":
            case "CONFIGURATION_ERROR":
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}