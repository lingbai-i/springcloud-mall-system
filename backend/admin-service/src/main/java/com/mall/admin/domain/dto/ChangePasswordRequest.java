package com.mall.admin.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 修改密码请求DTO
 */
@Data
public class ChangePasswordRequest {
    
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;
    
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9@#$%^&*]{8,20}$", message = "密码长度8-20位,只能包含字母、数字和特殊字符")
    private String newPassword;
}
