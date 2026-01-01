package com.mall.admin.service;

import com.mall.admin.domain.dto.LoginRequest;
import com.mall.admin.domain.vo.LoginResponse;

/**
 * 管理员认证服务接口
 * 
 * @author lingbai
 * @since 2025-01-09
 */
public interface AuthService {
    
    /**
     * 管理员登录
     * 
     * @param request 登录请求
     * @param ipAddress 登录IP地址
     * @param userAgent 浏览器标识
     * @return 登录响应，包含Token和管理员信息
     */
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
    
    /**
     * 管理员登出
     * 
     * @param token JWT令牌
     * @param adminId 管理员ID
     */
    void logout(String token, Long adminId);
    
    /**
     * 刷新Token
     * 
     * @param oldToken 旧Token
     * @return 新Token
     */
    String refreshToken(String oldToken);
    
    /**
     * 验证Token是否有效
     * 
     * @param token JWT令牌
     * @return 是否有效
     */
    boolean validateToken(String token);
}
