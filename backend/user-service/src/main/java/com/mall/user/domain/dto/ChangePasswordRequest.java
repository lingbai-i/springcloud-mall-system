package com.mall.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {
    
    /** 用户名 */
    @Schema(description = "用户名", example = "john_doe")
    private String username;
    
    /** 原密码 */
    @Schema(description = "原密码", example = "oldPassword123")
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;
    
    /** 新密码 */
    @Schema(description = "新密码", example = "newPassword123")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$", 
            message = "密码必须包含字母和数字，长度6-20位")
    private String newPassword;
    
    /** 确认新密码 */
    @Schema(description = "确认新密码", example = "newPassword123")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    // 构造函数
    public ChangePasswordRequest() {}
    
    // Getter和Setter方法
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getOldPassword() {
        return oldPassword;
    }
    
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    public String getCurrentPassword() {
        return oldPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    public String getConfirmNewPassword() {
        return confirmPassword;
    }
    
    /**
     * 验证新密码和确认密码是否匹配
     */
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
    
    @Override
    public String toString() {
        return "ChangePasswordRequest{" +
                "username='" + username + '\'' +
                ", oldPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                ", confirmPassword='[PROTECTED]'" +
                '}';
    }
}
