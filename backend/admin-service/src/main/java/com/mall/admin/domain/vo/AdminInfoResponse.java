package com.mall.admin.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员信息响应VO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
public class AdminInfoResponse {
    
    /** 管理员ID */
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 真实姓名 */
    private String realName;
    
    /** 邮箱 */
    private String email;
    
    /** 手机号 */
    private String phone;
    
    /** 头像 */
    private String avatar;
    
    /** 角色 */
    private String role;
    
    /** 角色文本 */
    private String roleText;
    
    /** 状态 */
    private Integer status;
    
    /** 状态文本 */
    private String statusText;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    
    /** 最后登录IP */
    private String lastLoginIp;
    
    /** 登录次数 */
    private Integer loginCount;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 备注 */
    private String remark;
}