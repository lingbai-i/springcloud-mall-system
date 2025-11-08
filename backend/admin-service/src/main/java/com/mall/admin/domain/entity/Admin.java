package com.mall.admin.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Admin extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    /** 管理员用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 真实姓名 */
    private String realName;
    
    /** 邮箱 */
    private String email;
    
    /** 手机号 */
    private String phone;
    
    /** 头像 */
    private String avatar;
    
    /** 角色：super_admin-超级管理员，admin-普通管理员 */
    private String role;
    
    /** 状态：0-禁用，1-正常 */
    private Integer status;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    
    /** 最后登录IP */
    private String lastLoginIp;
    
    /** 登录次数 */
    private Integer loginCount;
    
    /** 备注 */
    private String remark;
    
    /**
     * 获取角色文本
     */
    public String getRoleText() {
        if (role == null) {
            return "未知";
        }
        switch (role) {
            case "super_admin":
                return "超级管理员";
            case "admin":
                return "普通管理员";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取状态文本
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "禁用";
            case 1:
                return "正常";
            default:
                return "未知";
        }
    }
    
    /**
     * 是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return "super_admin".equals(role);
    }
}