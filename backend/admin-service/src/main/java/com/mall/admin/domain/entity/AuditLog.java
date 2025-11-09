package com.mall.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作审计日志实体
 */
@Data
@TableName("audit_log")
public class AuditLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 操作人ID
     */
    private Long adminId;
    
    /**
     * 操作人账号
     */
    private String adminUsername;
    
    /**
     * 操作类型
     */
    private String operationType;
    
    /**
     * 资源类型
     */
    private String resourceType;
    
    /**
     * 资源ID
     */
    private String resourceId;
    
    /**
     * 操作描述
     */
    private String operationDesc;
    
    /**
     * 请求方法
     */
    private String requestMethod;
    
    /**
     * 请求URL
     */
    private String requestUrl;
    
    /**
     * 请求参数(JSON)
     */
    private String requestParams;
    
    /**
     * 响应结果(JSON)
     */
    private String responseResult;
    
    /**
     * 操作IP
     */
    private String ipAddress;
    
    /**
     * 浏览器标识
     */
    private String userAgent;
    
    /**
     * 状态: 1-成功 0-失败
     */
    private Integer status;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 执行时间(ms)
     */
    private Integer executionTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
