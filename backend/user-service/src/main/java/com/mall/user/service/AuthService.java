package com.mall.user.service;

import com.mall.user.domain.dto.LoginRequest;
import com.mall.user.domain.dto.RegisterRequest;
import com.mall.user.domain.vo.LoginResponse;

/**
 * 认证服务接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
public interface AuthService {

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     * @throws Exception 登录异常
     */
    LoginResponse login(LoginRequest loginRequest) throws Exception;

    /**
     * 手机号验证码登录（未注册自动注册）
     * 
     * @param loginRequest 登录请求（必须包含phone和smsCode）
     * @return 登录响应
     * @throws Exception 登录异常
     */
    LoginResponse smsLogin(LoginRequest loginRequest) throws Exception;

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return 注册成功后的登录响应（包含token和用户信息）
     * @throws Exception 注册异常
     */
    LoginResponse register(RegisterRequest registerRequest) throws Exception;

    /**
     * 用户登出
     * 
     * @param token JWT令牌
     * @return 登出结果
     */
    boolean logout(String token);

    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     * @throws Exception 刷新异常
     */
    String refreshToken(String refreshToken) throws Exception;

    /**
     * 验证令牌
     * 
     * @param token JWT令牌
     * @return 验证结果
     */
    boolean validateToken(String token);
}