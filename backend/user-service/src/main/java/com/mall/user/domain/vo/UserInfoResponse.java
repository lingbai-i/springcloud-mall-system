package com.mall.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 用户信息响应VO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Schema(description = "用户信息响应")
public class UserInfoResponse {
    
    /** 用户ID */
    @Schema(description = "用户ID")
    private Long userId;
    
    /** 用户名 */
    @Schema(description = "用户名")
    private String username;
    
    /** 昵称 */
    @Schema(description = "昵称")
    private String nickname;
    
    /** 邮箱 */
    @Schema(description = "邮箱")
    private String email;
    
    /** 手机号 */
    @Schema(description = "手机号")
    private String phone;
    
    /** 头像 */
    @Schema(description = "头像")
    private String avatar;
    
    /** 性别：0-未知，1-男，2-女 */
    @Schema(description = "性别", example = "1")
    private Integer gender;
    
    /** 生日 */
    @Schema(description = "生日")
    private String birthday;
    
    /** 个人简介 */
    @Schema(description = "个人简介")
    private String bio;
    
    /** 状态：0-禁用，1-正常 */
    @Schema(description = "状态", example = "1")
    private Integer status;
    
    /** 创建时间 */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /** 最后登录时间 */
    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    
    /** 是否已设置密码（用于区分SMS自动注册用户） */
    @Schema(description = "是否已设置密码")
    private Boolean hasSetPassword;
    
    // 构造函数
    public UserInfoResponse() {}
    
    // Getter和Setter方法
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public Integer getGender() {
        return gender;
    }
    
    public void setGender(Integer gender) {
        this.gender = gender;
    }
    
    public String getBirthday() {
        return birthday;
    }
    
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public Boolean getHasSetPassword() {
        return hasSetPassword;
    }
    
    public void setHasSetPassword(Boolean hasSetPassword) {
        this.hasSetPassword = hasSetPassword;
    }
    
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
    
    @Override
    public String toString() {
        return "UserInfoResponse{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", avatar='" + avatar + '\'' +
                ", gender=" + gender +
                ", birthday='" + birthday + '\'' +
                ", bio='" + bio + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", lastLoginTime=" + lastLoginTime +
                '}';
    }
}


