package com.mall.user.controller;

import com.mall.user.domain.dto.*;
import com.mall.user.domain.vo.*;
import com.mall.user.domain.entity.User;
import com.mall.user.service.AuthService;
import com.mall.user.service.UserService;
import com.mall.user.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@RestController
@RequestMapping({"/api/users", "/users"})
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册接口")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("用户注册请求: username={}", request.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查密码是否匹配
            if (!request.isPasswordMatch()) {
                response.put("success", false);
                response.put("message", "两次输入的密码不一致");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 执行注册，注册成功后自动生成JWT token
            LoginResponse loginResponse = authService.register(request);
            
            // 构建包含token和用户信息的响应
            response.put("success", true);
            response.put("message", "注册成功");
            
            // 将登录响应数据包装到data字段中
            Map<String, Object> data = new HashMap<>();
            data.put("token", loginResponse.getAccessToken());
            data.put("expiresIn", loginResponse.getExpiresIn());
            data.put("userInfo", loginResponse.getUserInfo());
            response.put("data", data);
            
            logger.info("用户注册并自动登录成功: username={}", request.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("用户注册失败: username={}, error={}", request.getUsername(), e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("用户登录请求: username={}", request.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            LoginResponse loginResponse = authService.login(request);
            
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("data", loginResponse);
            
            logger.info("用户登录成功: username={}", request.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("用户登录失败: username={}, error={}", request.getUsername(), e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestParam String refreshToken) {
        logger.info("刷新令牌请求");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String newToken = authService.refreshToken(refreshToken);
            
            if (newToken != null) {
                response.put("success", true);
                response.put("message", "令牌刷新成功");
                response.put("data", newToken);
            } else {
                response.put("success", false);
                response.put("message", "令牌刷新失败");
                return ResponseEntity.badRequest().body(response);
            }
            
            logger.info("令牌刷新成功");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("令牌刷新失败: error={}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户信息
     * 
     * @param request HTTP请求
     * @return 用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public ResponseEntity<Map<String, Object>> getUserProfile(HttpServletRequest request) {
        logger.info("获取用户信息请求");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 从请求头中获取token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "未提供有效的认证令牌");
                return ResponseEntity.badRequest().body(response);
            }
            
            String token = authHeader.substring(7);
            logger.info("获取到的token: {}", token);
            
            String username = jwtUtils.getUsernameFromToken(token);
            logger.info("从token中解析的用户名: {}", username);
            
            boolean isValid = jwtUtils.validateToken(token);
            logger.info("token验证结果: {}", isValid);
            
            if (username == null || !isValid) {
                response.put("success", false);
                response.put("message", "无效的认证令牌");
                return ResponseEntity.badRequest().body(response);
            }
            
            UserInfoResponse userInfo = userService.getUserInfo(username);
            logger.info("从UserService获取的用户信息: {}", userInfo);
            
            if (userInfo == null) {
                logger.warn("用户信息不存在: username={}", username);
                response.put("success", false);
                response.put("message", "用户信息不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("success", true);
            response.put("message", "获取用户信息成功");
            response.put("data", userInfo);
            
            logger.info("获取用户信息成功: username={}", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取用户信息失败: error={}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新用户信息
     * 
     * @param request 更新请求
     * @param httpRequest HTTP请求
     * @return 更新结果
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    public ResponseEntity<Map<String, Object>> updateUserProfile(
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        logger.info("更新用户信息请求");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 从请求头中获取token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "未提供有效的认证令牌");
                return ResponseEntity.badRequest().body(response);
            }
            
            String token = authHeader.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);
            
            if (username == null || !jwtUtils.validateToken(token)) {
                response.put("success", false);
                response.put("message", "无效的认证令牌");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 设置用户名到请求中
            request.setUsername(username);
            
            userService.updateUserInfo(request);
            
            response.put("success", true);
            response.put("message", "用户信息更新成功");
            
            logger.info("用户信息更新成功: username={}", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("用户信息更新失败: error={}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 修改密码
     * 
     * @param request 修改密码请求
     * @param httpRequest HTTP请求
     * @return 修改结果
     */
    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    public ResponseEntity<Map<String, Object>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        logger.info("修改密码请求");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 检查新密码和确认密码是否匹配
            if (!request.isPasswordMatch()) {
                response.put("success", false);
                response.put("message", "新密码和确认密码不一致");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 从请求头中获取token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "未提供有效的认证令牌");
                return ResponseEntity.badRequest().body(response);
            }
            
            String token = authHeader.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);
            
            if (username == null || !jwtUtils.validateToken(token)) {
                response.put("success", false);
                response.put("message", "无效的认证令牌");
                return ResponseEntity.badRequest().body(response);
            }
            
            request.setUsername(username);
            userService.changePassword(request);
            
            response.put("success", true);
            response.put("message", "密码修改成功");
            
            logger.info("密码修改成功: username={}", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("密码修改失败: error={}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否已被使用")
    public ResponseEntity<Map<String, Object>> checkUsername(
            @Parameter(description = "用户名") @RequestParam String username) {
        logger.info("检查用户名请求: username={}", username);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean available = userService.isUsernameAvailable(username);
            
            response.put("success", true);
            response.put("available", available);
            response.put("message", available ? "用户名可用" : "用户名已被使用");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("检查用户名失败: username={}, error={}", username, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱
     * @return 检查结果
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已被使用")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @Parameter(description = "邮箱") @RequestParam String email) {
        logger.info("检查邮箱请求: email={}", email);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean available = userService.isEmailAvailable(email);
            
            response.put("success", true);
            response.put("available", available);
            response.put("message", available ? "邮箱可用" : "邮箱已被使用");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("检查邮箱失败: email={}, error={}", email, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查手机号是否可用
     * 
     * @param phone 手机号
     * @return 检查结果
     */
    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号", description = "检查手机号是否已被使用")
    public ResponseEntity<Map<String, Object>> checkPhone(
            @Parameter(description = "手机号") @RequestParam String phone) {
        logger.info("检查手机号请求: phone={}", phone);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean available = userService.isPhoneAvailable(phone);
            
            response.put("success", true);
            response.put("available", available);
            response.put("message", available ? "手机号可用" : "手机号已被使用");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("检查手机号失败: phone={}, error={}", phone, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 用户登出
     * 
     * @param httpRequest HTTP请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest httpRequest) {
        logger.info("用户登出请求");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 从请求头中获取token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtils.getUsernameFromToken(token);
                
                if (username != null) {
                    authService.logout(token);
                    logger.info("用户登出成功: username={}", username);
                }
            }
            
            response.put("success", true);
            response.put("message", "登出成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("用户登出失败: error={}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 测试端点 - 用于验证网关转发
     * 
     * @return 测试结果
     */
    @GetMapping("/test")
    @Operation(summary = "测试端点", description = "用于验证网关转发的测试端点")
    public ResponseEntity<Map<String, Object>> test() {
        logger.info("收到测试请求");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "用户服务正常运行");
        response.put("service", "user-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}