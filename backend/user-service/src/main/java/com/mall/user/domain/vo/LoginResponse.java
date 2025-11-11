package com.mall.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录响应VO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Schema(description = "登录响应")
public class LoginResponse {

    /** 访问令牌 */
    @Schema(description = "访问令牌")
    private String accessToken;

    /** 刷新令牌 */
    @Schema(description = "刷新令牌")
    private String refreshToken;

    /** 令牌类型 */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    /** 过期时间（秒） */
    @Schema(description = "过期时间（秒）", example = "3600")
    private Long expiresIn;

    /** 用户信息 */
    @Schema(description = "用户信息")
    private UserInfo userInfo;

    // 构造函数
    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public LoginResponse(String accessToken, Long expiresIn, UserInfo userInfo) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
    }

    // Getter和Setter方法
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", userInfo=" + userInfo +
                '}';
    }

    /**
     * 用户信息内部类
     */
    @Schema(description = "用户信息")
    public static class UserInfo {
        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "头像")
        private String avatar;

        @Schema(description = "性别")
        private Integer gender;

        @Schema(description = "生日")
        private String birthday;

        @Schema(description = "个人简介")
        private String bio;

        @Schema(description = "是否已设置密码")
        private Boolean hasSetPassword;

        // Getter和Setter方法
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
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

        public Boolean getHasSetPassword() {
            return hasSetPassword;
        }

        public void setHasSetPassword(Boolean hasSetPassword) {
            this.hasSetPassword = hasSetPassword;
        }
    }
}