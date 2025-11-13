package com.mall.auth.controller;

import com.mall.auth.dto.LoginRequest;
import com.mall.auth.dto.LoginResponse;
import com.mall.auth.dto.RefreshTokenRequest;
import com.mall.auth.dto.TokenValidationRequest;
import com.mall.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 
 * 提供用户认证相关的RESTful API：
 * - 用户登录
 * - 令牌刷新
 * - 令牌验证
 * - 用户登出
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证与令牌管理接口")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应（包含访问令牌和刷新令牌）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名和密码进行登录，返回访问令牌和刷新令牌")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求: username={}", request.getUsername());
        
        try {
            LoginResponse response = authService.login(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 刷新访问令牌
     * 
     * @param request 刷新令牌请求
     * @return 新的登录响应
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("收到令牌刷新请求");
        
        try {
            LoginResponse response = authService.refreshToken(request.getRefreshToken());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "令牌刷新成功");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("令牌刷新失败: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 验证访问令牌
     * 
     * @param request 令牌验证请求
     * @return 验证结果
     */
    @PostMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证访问令牌是否有效（供其他服务调用）")
    public ResponseEntity<Map<String, Object>> validateToken(@Valid @RequestBody TokenValidationRequest request) {
        log.debug("收到令牌验证请求");
        
        boolean isValid = authService.validateToken(request.getToken());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", Map.of("valid", isValid));
        
        return ResponseEntity.ok(result);
    }

    /**
     * 用户登出
     * 
     * @param authorization Authorization请求头（格式: Bearer {token}）
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "使当前访问令牌和刷新令牌失效")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("收到登出请求");
        
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new RuntimeException("缺少有效的访问令牌");
            }
            
            String token = authorization.substring(7);
            authService.logout(token);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "登出成功");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 健康检查
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查认证服务是否正常运行")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("service", "auth-service");
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(result);
    }
}
