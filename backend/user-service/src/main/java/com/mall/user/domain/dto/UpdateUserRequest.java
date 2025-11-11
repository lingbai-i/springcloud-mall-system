package com.mall.user.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.util.StringUtils;

/**
 * 更新用户信息请求DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Schema(description = "更新用户信息请求")
public class UpdateUserRequest {

    @Schema(description = "用户名", example = "john_doe")
    private String username;

    @Schema(description = "昵称", example = "新昵称")
    @Size(min = 2, max = 20, message = "昵称长度必须在2-20个字符之间")
    private String nickname;

    @Schema(description = "邮箱", example = "user@example.com")
    @Email(message = "邮箱格式不正确", regexp = "^$|^[\\w.-]+@[\\w.-]+\\.\\w+$")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "性别", example = "1", allowableValues = { "0", "1", "2" })
    private Integer gender; // 0-未知 1-男 2-女

    @Schema(description = "生日", example = "1990-01-01")
    private String birthday;

    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;

    @Schema(description = "个人简介", example = "这是个人简介")
    @Size(max = 200, message = "个人简介不能超过200个字符")
    private String bio;

    // 构造函数
    public UpdateUserRequest() {
    }

    // Getter和Setter方法
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public String toString() {
        return "UpdateUserRequest{" +
                "username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender=" + gender +
                ", birthday='" + birthday + '\'' +
                ", avatar='" + avatar + '\'' +
                ", bio='" + bio + '\'' +
                '}';
    }
}
