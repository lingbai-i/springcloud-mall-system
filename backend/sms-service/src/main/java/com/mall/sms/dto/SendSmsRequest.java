package com.mall.sms.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送短信验证码请求DTO
 *
 * @author SMS Service
 * @since 2025-11-01
 */
@Data
public class SendSmsRequest {

    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phoneNumber;

    /**
     * 验证码用途
     */
    @NotBlank(message = "验证码用途不能为空")
    private String purpose;
}