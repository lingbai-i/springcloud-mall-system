package com.mall.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 登录日志实体
 */
@Data
@TableName("login_log")
public class LoginLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 管理员ID(失败时为空)
     */
    private Long adminId;
    
    /**
     * 登录账号
     */
    private String username;
    
    /**
     * 结果: 1-成功 0-失败
     */
    private Integer loginResult;
    
    /**
     * 失败原因
     */
    private String failureReason;
    
    /**
     * 登录IP
     */
    private String ipAddress;
    
    /**
     * 浏览器标识
     */
    private String userAgent;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
