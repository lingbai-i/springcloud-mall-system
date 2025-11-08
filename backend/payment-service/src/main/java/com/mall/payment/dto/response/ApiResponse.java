package com.mall.payment.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用API响应DTO
 * 用于统一API响应格式的通用响应对象
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 * @param <T> 数据类型
 */
@Data
public class ApiResponse<T> {

    /**
     * 响应状态码
     * 200: 成功
     * 400: 客户端错误
     * 500: 服务器错误
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 请求追踪ID
     */
    private String traceId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 默认构造函数
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数
     * 
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = code != null && code == 200;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数
     * 
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     * @param traceId 追踪ID
     */
    public ApiResponse(Integer code, String message, T data, String traceId) {
        this(code, message, data);
        this.traceId = traceId;
    }

    /**
     * 创建成功响应
     * 
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null);
    }

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    /**
     * 创建成功响应
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 创建失败响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }

    /**
     * 创建失败响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 创建失败响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    /**
     * 创建客户端错误响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 客户端错误响应
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null);
    }

    /**
     * 创建未授权响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message != null ? message : "未授权访问", null);
    }

    /**
     * 创建禁止访问响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 禁止访问响应
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message != null ? message : "禁止访问", null);
    }

    /**
     * 创建资源未找到响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 资源未找到响应
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message != null ? message : "资源未找到", null);
    }

    /**
     * 创建参数验证失败响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 参数验证失败响应
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return new ApiResponse<>(422, message != null ? message : "参数验证失败", null);
    }

    /**
     * 创建业务逻辑错误响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 业务逻辑错误响应
     */
    public static <T> ApiResponse<T> businessError(String message) {
        return new ApiResponse<>(600, message, null);
    }

    /**
     * 创建支付相关错误响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 支付相关错误响应
     */
    public static <T> ApiResponse<T> paymentError(String message) {
        return new ApiResponse<>(700, message, null);
    }

    /**
     * 判断响应是否成功
     * 
     * @return 如果成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return success != null && success;
    }

    /**
     * 判断响应是否失败
     * 
     * @return 如果失败返回true，否则返回false
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 设置追踪ID
     * 
     * @param traceId 追踪ID
     * @return 当前响应对象
     */
    public ApiResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 设置时间戳
     * 
     * @param timestamp 时间戳
     * @return 当前响应对象
     */
    public ApiResponse<T> withTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * 获取错误信息摘要
     * 
     * @return 错误信息摘要
     */
    public String getErrorSummary() {
        if (isSuccess()) {
            return null;
        }
        return String.format("错误码: %d, 错误信息: %s", code, message);
    }

    /**
     * 获取响应状态描述
     * 
     * @return 响应状态描述
     */
    public String getStatusDescription() {
        if (code == null) {
            return "未知状态";
        }
        
        return switch (code) {
            case 200 -> "成功";
            case 400 -> "客户端错误";
            case 401 -> "未授权";
            case 403 -> "禁止访问";
            case 404 -> "资源未找到";
            case 422 -> "参数验证失败";
            case 500 -> "服务器内部错误";
            case 600 -> "业务逻辑错误";
            case 700 -> "支付相关错误";
            default -> "其他错误";
        };
    }

    /**
     * 转换数据类型
     * 
     * @param newData 新数据
     * @param <R> 新数据类型
     * @return 新的响应对象
     */
    public <R> ApiResponse<R> map(R newData) {
        ApiResponse<R> newResponse = new ApiResponse<>(this.code, this.message, newData);
        newResponse.setTraceId(this.traceId);
        newResponse.setTimestamp(this.timestamp);
        return newResponse;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}