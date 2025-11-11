package com.mall.user.controller;

import com.mall.common.core.domain.R;
import com.mall.user.domain.dto.LoginRequest;
import com.mall.user.domain.dto.RegisterRequest;
import com.mall.user.domain.vo.LoginResponse;
import com.mall.user.domain.vo.UserInfoResponse;
import com.mall.user.service.AuthService;
import com.mall.user.service.UserService;
import com.mall.user.utils.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制�?
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
// @Tag(name = "用户认证", description = "用户登录注册相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 手机号验证码登录（未注册自动注册）
     */
    // @Operation(summary = "手机号验证码登录", description = "手机号+验证码登录，未注册用户自动注册")
    @PostMapping("/sms-login")
    public R<LoginResponse> smsLogin(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.smsLogin(loginRequest);
            return R.ok("登录成功", response);
        } catch (Exception e) {
            logger.error("手机号登录失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    // @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return R.ok("登录成功", response);
        } catch (Exception e) {
            logger.error("用户登录失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    // @Operation(summary = "用户注册", description = "用户注册新账�?)
    @PostMapping("/register")
    public R<LoginResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            LoginResponse result = authService.register(registerRequest);
            return R.ok("注册成功", result);
        } catch (Exception e) {
            logger.error("用户注册失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    // @Operation(summary = "用户登出", description = "用户登出系统")
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader("Authorization") String authorization) {
        try {
            // 提取Token（去掉Bearer前缀�?
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }

            boolean result = authService.logout(token);
            return result ? R.<Void>ok("登出成功", null) : R.<Void>fail("登出失败");
        } catch (Exception e) {
            logger.error("用户登出失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 刷新Token
     */
    // @Operation(summary = "刷新Token", description = "刷新访问令牌")
    @PostMapping("/refresh")
    public R<String> refreshToken(@RequestHeader("Authorization") String authorization) {
        try {
            // 提取Token（去掉Bearer前缀�?
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }

            String newToken = authService.refreshToken(token);
            return R.ok("Token刷新成功", newToken);
        } catch (Exception e) {
            logger.error("Token刷新失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }

    /**
     * 验证Token
     */
    // @Operation(summary = "验证Token", description = "验证访问令牌是否有效")
    @GetMapping("/validate")
    public R<Boolean> validateToken(@RequestHeader("Authorization") String authorization) {
        try {
            // 提取Token（去掉Bearer前缀�?
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }

            boolean valid = authService.validateToken(token);
            return R.ok(valid ? "Token有效" : "Token无效", valid);
        } catch (Exception e) {
            logger.error("Token验证失败: {}", e.getMessage());
            return R.ok("Token验证失败", false);
        }
    }

    /**
     * 获取当前用户信息
     */
    // @Operation(summary = "获取当前用户信息", description = "根据Token获取当前登录用户信息")
    @GetMapping("/me")
    public R<UserInfoResponse> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        try {
            // 提取Token（去掉Bearer前缀�?
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }

            if (token == null) {
                return R.fail("Token不能为空");
            }

            // 验证Token并获取用户名
            if (!authService.validateToken(token)) {
                return R.fail("Token无效或已过期");
            }

            String username = jwtUtils.getUsernameFromToken(token);
            if (username == null) {
                return R.fail("无法从Token中获取用户信息");
            }

            // 根据用户名获取用户信息
            UserInfoResponse userInfo = userService.getUserInfo(username);
            if (userInfo == null) {
                return R.fail("用户不存在");
            }

            return R.ok("获取用户信息成功", userInfo);
        } catch (Exception e) {
            logger.error("获取用户信息失败: {}", e.getMessage());
            return R.fail("获取用户信息失败");
        }
    }

    /**
     * 临时测试接口：生成BCrypt密码
     * 访问: GET /user-service/auth/test-bcrypt?password=yourpassword
     */
    @GetMapping("/test-bcrypt")
    public R<String> testBcrypt(@RequestParam String password) {
        try {
            String encoded = passwordEncoder.encode(password);
            logger.info("生成BCrypt密码 - 明文: {}, 密文: {}", password, encoded);
            return R.ok("BCrypt密码生成成功", encoded);
        } catch (Exception e) {
            logger.error("生成BCrypt密码失败: {}", e.getMessage());
            return R.fail(e.getMessage());
        }
    }
}