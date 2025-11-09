package com.mall.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Data
@TableName("permission")
public class Permission {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 权限编码
     */
    private String permissionCode;
    
    /**
     * 权限名称
     */
    private String permissionName;
    
    /**
     * 资源类型
     */
    private String resourceType;
    
    /**
     * 资源标识
     */
    private String resource;
    
    /**
     * 操作类型
     */
    private String action;
    
    /**
     * 权限描述
     */
    private String description;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
