package com.mall.admin.controller;

import com.mall.admin.domain.dto.LoginRequest;
import com.mall.admin.domain.vo.LoginResponse;
import com.mall.admin.service.AuthService;
import com.mall.admin.util.IpUtil;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 管理员认证控制器
 * 
 * @author system
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 管理员登录
     * 
     * @param request 登录请求
     * @param httpRequest HTTP请求对象
     * @return 登录响应
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@Validated @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        String ipAddress = IpUtil.getIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        LoginResponse response = authService.login(request, ipAddress, userAgent);
        return R.ok(response);
    }
    
    /**
     * 管理员登出
     * 
     * @param httpRequest HTTP请求对象
     * @return 操作结果
     */
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest httpRequest) {
        String token = getTokenFromRequest(httpRequest);
        Long adminId = (Long) httpRequest.getAttribute("adminId");
        
        authService.logout(token, adminId);
        return R.ok();
    }
    
    /**
     * 刷新Token
     * 
     * @param httpRequest HTTP请求对象
     * @return 新Token
     */
    @PostMapping("/refresh")
    public R<String> refreshToken(HttpServletRequest httpRequest) {
        String oldToken = getTokenFromRequest(httpRequest);
        String newToken = authService.refreshToken(oldToken);
        return R.ok(newToken);
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
