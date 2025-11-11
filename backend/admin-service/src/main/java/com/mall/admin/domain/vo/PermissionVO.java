package com.mall.admin.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 权限信息VO
 */
@Data
public class PermissionVO {
    
    private Long id;
    
    private String permissionCode;
    
    private String permissionName;
    
    private String resourceType;
    
    private String resource;
    
    private String action;
    
    private String description;
    
    private LocalDateTime createdAt;
}
