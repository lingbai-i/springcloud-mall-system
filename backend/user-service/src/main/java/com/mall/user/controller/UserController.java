package com.mall.user.controller;

import com.mall.user.domain.dto.*;
import com.mall.user.domain.vo.*;
import com.mall.user.domain.entity.User;
import com.mall.user.service.AuthService;
import com.mall.user.service.MinioService;
import com.mall.user.service.UserService;
import com.mall.user.service.impl.UserServiceImpl;
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
import org.springframework.web.multipart.MultipartFile;

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
@RequestMapping({ "/api/users", "/users" })
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MinioService minioService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 测试端点 - 验证代码是否被加载
     */
    @GetMapping("/test-version")
    @Operation(summary = "测试版本", description = "验证代码版本")
    public ResponseEntity<Map<String, Object>> testVersion() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Code version: 2025-11-11 14:36 - Enhanced exception handling");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

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
            // 从 token 中获取用户名
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("未提供有效的认证令牌");
                response.put("success", false);
                response.put("message", "未提供有效的认证令牌");
                return ResponseEntity.status(401).body(response);
            }

            String token = authHeader.substring(7);
            String username;
            try {
                username = jwtUtils.getUsernameFromToken(token);
                boolean isValid = jwtUtils.validateToken(token);

                if (username == null || !isValid) {
                    logger.warn("无效的认证令牌");
                    response.put("success", false);
                    response.put("message", "无效的认证令牌");
                    return ResponseEntity.status(401).body(response);
                }
            } catch (Exception e) {
                logger.error("JWT token解析失败: {}", e.getMessage(), e);
                response.put("success", false);
                response.put("message", "令牌解析失败: " + e.getMessage());
                return ResponseEntity.status(401).body(response);
            }

            UserInfoResponse userInfo = null;
            try {
                userInfo = userService.getUserInfo(username);
                logger.info("从UserService获取的用户信息: {}", userInfo);
            } catch (Exception e) {
                logger.error("查询用户信息失败: username={}, error={}", username, e.getMessage(), e);
                response.put("success", false);
                response.put("message", "查询用户信息失败: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }

            if (userInfo == null) {
                logger.warn("用户信息不存在: username={}", username);
                response.put("success", false);
                response.put("message", "用户信息不存在");
                return ResponseEntity.status(404).body(response);
            }

            response.put("success", true);
            response.put("message", "获取用户信息成功");
            response.put("data", userInfo);

            logger.info("获取用户信息成功: username={}", username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取用户信息失败（未知异常）: error={}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "服务器内部错误: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 更新用户信息
     * 
     * @param request     更新请求
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
            // 从JWT中获取用户名
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
     * @param request     修改密码请求
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

    /**
     * 首次设置密码
     * 用于SMS登录用户首次设置自己的密码
     * 
     * @param request     设置密码请求（只包含newPassword和confirmPassword）
     * @param httpRequest HTTP请求
     * @return 设置结果
     */
    @PutMapping("/set-password")
    @Operation(summary = "首次设置密码", description = "SMS登录用户首次设置密码，不需要旧密码")
    public ResponseEntity<Map<String, Object>> setPassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        logger.info("首次设置密码请求");

        Map<String, Object> response = new HashMap<>();

        try {
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            // 验证密码
            if (newPassword == null || newPassword.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "新密码不能为空");
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }

            if (!newPassword.equals(confirmPassword)) {
                response.put("success", false);
                response.put("message", "两次输入的密码不一致");
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }

            // 从请求头中获取token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "未提供有效的认证令牌");
                response.put("code", 401);
                return ResponseEntity.badRequest().body(response);
            }

            String token = authHeader.substring(7);
            String username = jwtUtils.getUsernameFromToken(token);

            if (username == null || !jwtUtils.validateToken(token)) {
                response.put("success", false);
                response.put("message", "无效的认证令牌");
                response.put("code", 401);
                return ResponseEntity.badRequest().body(response);
            }

            // 调用service设置密码
            UserServiceImpl userServiceImpl = (UserServiceImpl) userService;
            userServiceImpl.setPassword(username, newPassword);

            response.put("success", true);
            response.put("message", "密码设置成功，现在可以使用账号密码登录了");
            response.put("code", 200);

            logger.info("密码设置成功: username={}", username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("密码设置失败: error={}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("code", 500);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 上传头像（MinIO 存储）
     * 
     * @param file        头像文件
     * @param httpRequest HTTP请求
     * @return 头像 URL
     */
    @PostMapping("/upload-avatar")
    @Operation(summary = "上传头像", description = "上传用户头像到 MinIO 存储")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {

        logger.info("=== 开始处理头像上传请求 ===");
        logger.info("文件名: {}", file.getOriginalFilename());
        logger.info("文件大小: {} bytes", file.getSize());
        logger.info("文件类型: {}", file.getContentType());

        Map<String, Object> response = new HashMap<>();

        try {
            // 获取当前用户
            String username = getCurrentUsername(httpRequest);
            if (username == null) {
                logger.warn("未获取到用户信息");
                response.put("success", false);
                response.put("message", "未登录或登录已过期");
                return ResponseEntity.status(401).body(response);
            }

            logger.info("当前用户: {}", username);

            // 查询用户
            User user = userService.findByUsername(username);
            if (user == null) {
                logger.error("用户不存在: {}", username);
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            logger.info("用户ID: {}", user.getId());

            // 上传到 MinIO
            String oldAvatar = user.getAvatar();
            logger.info("旧头像: {}", oldAvatar);

            String avatarUrl = minioService.uploadAvatar(file, user.getId());
            logger.info("新头像URL: {}", avatarUrl);

            // 更新数据库
            user.setAvatar(avatarUrl);
            userService.updateById(user);
            logger.info("✓ 数据库更新成功");

            // 删除旧头像（如果是 MinIO 存储的）
            if (oldAvatar != null && !oldAvatar.startsWith("data:image")) {
                minioService.deleteAvatar(oldAvatar);
                logger.info("✓ 已删除旧头像");
            }

            response.put("success", true);
            response.put("message", "头像上传成功");
            response.put("data", avatarUrl);

            logger.info("=== 头像上传完成 ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("头像上传失败", e);
            response.put("success", false);
            response.put("message", "头像上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        String username = jwtUtils.getUsernameFromToken(token);

        if (username == null || !jwtUtils.validateToken(token)) {
            return null;
        }

        return username;
    }

    /**
     * 通用文件上传接口（用于商家入驻等场景）
     * 
     * @param file 上传的文件
     * @return 文件URL
     */
    @PostMapping("/upload")
    @Operation(summary = "通用文件上传", description = "上传图片文件到MinIO存储，返回访问URL")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {

        logger.info("通用文件上传 - 文件名: {}, 大小: {} bytes",
                file.getOriginalFilename(), file.getSize());

        Map<String, Object> response = new HashMap<>();

        try {
            // 验证文件
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "文件不能为空");
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "只支持上传图片文件");
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }

            // 验证文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "文件大小不能超过2MB");
                response.put("code", 400);
                return ResponseEntity.badRequest().body(response);
            }

            // 生成唯一文件名
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = String.format("file_%d_%d%s",
                    System.currentTimeMillis(),
                    (int) (Math.random() * 10000),
                    extension);

            // 上传到MinIO (直接使用minioClient)
            String bucketName = "mall-avatars"; // 使用现有bucket
            minioService.minioClient.putObject(
                    io.minio.PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build());

            // 构建访问URL
            String fileUrl = String.format("http://localhost:9000/%s/%s", bucketName, fileName);

            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("code", 200);
            response.put("data", new HashMap<String, Object>() {
                {
                    put("url", fileUrl);
                    put("filename", fileName);
                }
            });

            logger.info("✅ 文件上传成功: {}", fileUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("文件上传失败", e);
            response.put("success", false);
            response.put("message", "文件上传失败: " + e.getMessage());
            response.put("code", 500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 获取用户统计数据
     * 供 admin-service Feign 调用
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取用户统计数据", description = "获取用户总数、活跃用户等统计信息")
    public com.mall.common.core.domain.R<Map<String, Object>> getUserStatistics() {
        logger.info("获取用户统计数据请求");

        try {
            Map<String, Object> stats = userService.getUserStatistics();
            return com.mall.common.core.domain.R.ok(stats);
        } catch (Exception e) {
            logger.error("获取用户统计数据失败", e);
            return com.mall.common.core.domain.R.fail("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户分布数据
     * 供 admin-service Feign 调用
     * 返回活跃度分布和注册时间分布
     */
    @GetMapping("/distribution")
    @Operation(summary = "获取用户分布数据", description = "获取用户活跃度和注册时间分布统计")
    public com.mall.common.core.domain.R<Map<String, Object>> getUserDistribution() {
        logger.info("获取用户分布数据请求");

        try {
            Map<String, Object> distribution = ((UserServiceImpl) userService).getUserDistribution();
            return com.mall.common.core.domain.R.ok(distribution);
        } catch (Exception e) {
            logger.error("获取用户分布数据失败", e);
            return com.mall.common.core.domain.R.fail("获取用户分布数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户列表（管理员接口）
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 关键词
     * @param status  状态
     * @return 用户列表
     */
    @GetMapping("")
    @Operation(summary = "获取用户列表", description = "管理员获取用户列表")
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status) {

        logger.info("管理员查询用户列表 - page: {}, size: {}, keyword: {}, status: {}", page, size, keyword, status);

        Map<String, Object> response = new HashMap<>();

        try {
            // 调用 service 获取用户列表
            com.mall.common.core.domain.PageResult<User> pageResult = ((UserServiceImpl) userService).getUserList(page,
                    size, keyword, status);

            response.put("code", 200);
            response.put("success", true);
            response.put("message", "获取用户列表成功");
            response.put("data", pageResult);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "获取用户列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取用户详情（管理员接口）
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "管理员获取用户详情")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable("id") Long id) {
        logger.info("管理员查询用户详情 - id: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            User user = ((UserServiceImpl) userService).getById(id);

            if (user == null) {
                response.put("code", 404);
                response.put("success", false);
                response.put("message", "用户不存在");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(404).body(response);
            }

            response.put("code", 200);
            response.put("success", true);
            response.put("message", "获取用户详情成功");
            response.put("data", user);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取用户详情失败", e);
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "获取用户详情失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 禁用用户（管理员接口）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用用户", description = "管理员禁用用户")
    public ResponseEntity<Map<String, Object>> disableUser(@PathVariable("id") Long id) {
        logger.info("管理员禁用用户 - id: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            User user = ((UserServiceImpl) userService).getById(id);
            if (user == null) {
                response.put("code", 404);
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            user.setStatus(0); // 0 = 禁用
            ((UserServiceImpl) userService).updateById(user);

            response.put("code", 200);
            response.put("success", true);
            response.put("message", "禁用用户成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("禁用用户失败", e);
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "禁用用户失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 启用用户（管理员接口）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用用户", description = "管理员启用用户")
    public ResponseEntity<Map<String, Object>> enableUser(@PathVariable("id") Long id) {
        logger.info("管理员启用用户 - id: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            User user = ((UserServiceImpl) userService).getById(id);
            if (user == null) {
                response.put("code", 404);
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.status(404).body(response);
            }

            user.setStatus(1); // 1 = 启用
            ((UserServiceImpl) userService).updateById(user);

            response.put("code", 200);
            response.put("success", true);
            response.put("message", "启用用户成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("启用用户失败", e);
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "启用用户失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}