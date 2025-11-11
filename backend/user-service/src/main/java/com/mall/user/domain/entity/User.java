package com.mall.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 密码设置时间 */
    private LocalDateTime passwordSetTime;
    
    /** 昵称 */
    private String nickname;
    
    /** 邮箱 */
    private String email;
    
    /** 手机号 */
    private String phone;
    
    /** 头像 */
    private String avatar;
    
    /** 性别：0-未知，1-男，2-女 */
    private Integer gender;
    
    /** 生日 */
    private String birthday;
    
    /** 个人简介 */
    private String bio;
    
    /** 状态：0-禁用，1-正常 */
    private Integer status;
    
    /** 版本号 */
    private Integer version;
    
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    
    /** 创建时间 - 覆盖父类字段以映射到数据库的created_time */
    @TableField("created_time")
    private LocalDateTime createTime;
    
    /** 更新时间 - 覆盖父类字段以映射到数据库的updated_time */
    @TableField("updated_time")
    private LocalDateTime updateTime;
    
    /**
     * 获取性别文本
     */
    public String getGenderText() {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
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
}



