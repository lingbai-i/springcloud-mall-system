package com.mall.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员角色关联实体
 */
@Data
@TableName("admin_role")
public class AdminRole {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 管理员ID
     */
    private Long adminId;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
