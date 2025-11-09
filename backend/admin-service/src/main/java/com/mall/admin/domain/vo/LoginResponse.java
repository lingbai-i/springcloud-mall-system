package com.mall.admin.domain.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginResponse {
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * 管理员信息
     */
    private AdminVO adminInfo;
}
