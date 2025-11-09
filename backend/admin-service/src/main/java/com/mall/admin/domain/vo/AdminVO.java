package com.mall.admin.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员信息VO
 */
@Data
public class AdminVO {
    
    private Long id;
    
    private String username;
    
    private String realName;
    
    private String email;
    
    private String phone;
    
    private String avatar;
    
    private Integer status;
    
    private LocalDateTime lastLoginTime;
    
    private LocalDateTime createdAt;
    
    /**
     * 角色列表
     */
    private List<RoleVO> roles;
    
    /**
     * 权限列表
     */
    private List<String> permissions;
}
