package com.mall.sms.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 验证短信验证码请求DTO
 *
 * @author lingbai
 * @since 2024-01-01
 */
@Data
public class VerifySmsRequest {

    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phoneNumber;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    private String code;

    /**
     * 验证码用途
     */
    @NotBlank(message = "验证码用途不能为空")
    private String purpose;
}