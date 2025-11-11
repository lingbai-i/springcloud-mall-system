package com.mall.admin.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色信息VO
 */
@Data
public class RoleVO {
    
    private Long id;
    
    private String roleCode;
    
    private String roleName;
    
    private String description;
    
    private Integer status;
    
    private LocalDateTime createdAt;
    
    /**
     * 关联的权限列表
     */
    private List<PermissionVO> permissions;
}
