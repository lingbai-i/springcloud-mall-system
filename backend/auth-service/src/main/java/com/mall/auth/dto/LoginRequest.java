package com.mall.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-11-11
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    /**
     * 用户名或手机号
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名或手机号", example = "admin")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    /**
     * 登录类型（password-密码登录，sms-短信登录）
     */
    @Schema(description = "登录类型", example = "password", allowableValues = {"password", "sms"})
    private String loginType = "password";
}
