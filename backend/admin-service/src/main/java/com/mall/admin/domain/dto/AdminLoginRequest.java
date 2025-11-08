package com.mall.admin.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 管理员登录请求DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
public class AdminLoginRequest {
    
    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
    
    /** 验证码 */
    private String captcha;
    
    /** 记住登录 */
    private Boolean remember = false;
}