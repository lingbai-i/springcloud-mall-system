package com.mall.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Schema(description = "登录请求")
public class LoginRequest {

    /** 用户名 */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /** 密码 */
    @Schema(description = "密码", example = "123456")
    private String password;

    /** 验证码 */
    @Schema(description = "验证码", example = "1234")
    private String captcha;

    /** 验证码UUID */
    @Schema(description = "验证码UUID", example = "uuid-1234")
    private String uuid;

    /** 手机号 */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /** 短信验证码 */
    @Schema(description = "短信验证码", example = "123456")
    private String smsCode;

    // 构造函数
    public LoginRequest() {
    }

    // Getter和Setter方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", captcha='" + captcha + '\'' +
                ", uuid='" + uuid + '\'' +
                ", phone='" + phone + '\'' +
                ", smsCode='" + smsCode + '\'' +
                '}';
    }
}
