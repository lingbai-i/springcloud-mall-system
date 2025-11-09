package com.mall.admin.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 创建角色请求DTO
 */
@Data
public class CreateRoleRequest {
    
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    
    /**
     * 角色描述
     */
    private String description;
    
    @NotEmpty(message = "权限ID列表不能为空")
    private List<Long> permissionIds;
}
