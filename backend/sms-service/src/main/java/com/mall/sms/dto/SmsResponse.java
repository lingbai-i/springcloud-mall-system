package com.mall.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信服务统一响应DTO
 *
 * @author SMS Service
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponse<T> {

    /**
     * 响应码
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应
     */
    public static <T> SmsResponse<T> success(T data) {
        return new SmsResponse<>(200, "操作成功", data);
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> SmsResponse<T> success() {
        return new SmsResponse<>(200, "操作成功", null);
    }

    /**
     * 失败响应
     */
    public static <T> SmsResponse<T> error(int code, String message) {
        return new SmsResponse<>(code, message, null);
    }

    /**
     * 失败响应（默认错误码）
     */
    public static <T> SmsResponse<T> error(String message) {
        return new SmsResponse<>(500, message, null);
    }
}