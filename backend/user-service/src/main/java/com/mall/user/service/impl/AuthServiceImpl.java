package com.mall.user.service.impl;

import com.mall.user.domain.dto.LoginRequest;
import com.mall.user.domain.dto.RegisterRequest;
import com.mall.user.domain.entity.User;
import com.mall.user.domain.vo.LoginResponse;
import com.mall.user.service.AuthService;
import com.mall.user.service.UserService;
import com.mall.user.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    /** Token黑名单前缀 */
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";

    /** SMS服务基础URL - Docker环境下使用服务名 */
    @Value("${sms.service.url:http://sms-service:8083}")
    private String smsServiceUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final WebClient webClient;

    // @Autowired
    // private RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数，初始化WebClient
     * 
     * @author lingbai
     * @since 2025-01-27
     */
    public AuthServiceImpl() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    /**
     * 验证手机验证码
     * 
     * @param phone   手机号
     * @param captcha 验证码
     * @param purpose 验证码用途（默认为register）
     * @return 验证结果
     * @author lingbai
     * @since 2025-10-27
     */
    private boolean verifySmsCode(String phone, String captcha, String purpose) {
        try {
            logger.info("开始验证手机验证码: phone={}, captcha={}, purpose={}", phone, captcha, purpose);

            // 构建验证请求参数
            Map<String, String> verifyRequest = new HashMap<>();
            verifyRequest.put("phoneNumber", phone);
            verifyRequest.put("code", captcha);
            verifyRequest.put("purpose", purpose != null ? purpose.toUpperCase() : "REGISTER");

            // 调用SMS服务验证接口（直接调用SMS服务，不经过网关）
            Mono<Map> response = webClient.post()
                    .uri(smsServiceUrl + "/verify")
                    .bodyValue(verifyRequest)
                    .retrieve()
                    .bodyToMono(Map.class);

            // 同步获取结果
            Map<String, Object> result = response.block();

            if (result != null) {
                Integer code = (Integer) result.get("code");
                String message = (String) result.get("message");
                String data = (String) result.get("data");

                logger.info("验证码验证结果: code={}, message={}, data={}", code, message, data);

                // 验证成功的条件：code为200且data包含"验证码"和"正确/成功"
                return code != null && code == 200 && data != null &&
                        (data.contains("验证码正确") || data.contains("验证码验证成功"));
            }

            logger.warn("验证码验证失败: 响应为空");
            return false;

        } catch (Exception e) {
            logger.error("验证码验证异常: phone={}, captcha={}, purpose={}", phone, captcha, purpose, e);
            return false;
        }
    }

    /**
     * 用户登录
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("用户登录: {}", loginRequest.getUsername());

        // 参数校验
        if (!StringUtils.hasText(loginRequest.getUsername()) ||
                !StringUtils.hasText(loginRequest.getPassword())) {
            throw new RuntimeException("用户名和密码不能为空");
        }

        // 根据用户名或手机号查询用户
        User user = userService.findByUsername(loginRequest.getUsername());
        if (user == null) {
            // 如果用户名找不到，尝试用手机号查找
            user = userService.findByPhone(loginRequest.getUsername());
        }

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态(0-禁用，1-正常)
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getUsername());

        // 更新登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        // 构建用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setGender(user.getGender());
        userInfo.setBirthday(user.getBirthday());
        userInfo.setBio(user.getBio());
        userInfo.setHasSetPassword(user.getPasswordSetTime() != null);

        // 构建响应
        LoginResponse response = new LoginResponse(token, jwtUtils.getExpirationSeconds(), userInfo);

        logger.info("用户登录成功: {}", user.getUsername());
        return response;
    }

    /**
     * 手机号验证码登录（未注册自动注册）
     * 实现智能登录逻辑：
     * 1. 检查手机号是否已注册
     * 2. 如果已注册，验证验证码后直接登录
     * 3. 如果未注册，验证验证码后自动注册并登录
     * 
     * @param loginRequest 登录请求（必须包含phone和smsCode）
     * @return 登录响应
     */
    @Override
    public LoginResponse smsLogin(LoginRequest loginRequest) {
        logger.info("手机号验证码登录: {}", loginRequest.getPhone());

        // 参数校验
        if (!StringUtils.hasText(loginRequest.getPhone())) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!StringUtils.hasText(loginRequest.getSmsCode())) {
            throw new RuntimeException("验证码不能为空");
        }

        // 验证手机验证码（使用LOGIN用途）
        boolean isValidCaptcha = verifySmsCode(loginRequest.getPhone(), loginRequest.getSmsCode(), "LOGIN");
        if (!isValidCaptcha) {
            logger.warn("验证码验证失败: phone={}, code={}",
                    loginRequest.getPhone(), loginRequest.getSmsCode());
            throw new RuntimeException("验证码错误或已过期");
        }

        // 查找用户
        User user = userService.findByPhone(loginRequest.getPhone());

        if (user == null) {
            // 未注册，自动注册
            logger.info("手机号未注册，自动创建账户: {}", loginRequest.getPhone());

            user = new User();
            // 用手机号作为用户名
            user.setUsername("user_" + loginRequest.getPhone());
            // 设置默认密码（手机号后6位）
            user.setPassword(passwordEncoder.encode(loginRequest.getPhone().substring(
                    loginRequest.getPhone().length() - 6)));
            user.setPhone(loginRequest.getPhone());
            user.setNickname("用户" + loginRequest.getPhone().substring(
                    loginRequest.getPhone().length() - 4));
            user.setStatus(1); // 默认启用
            user.setGender(0); // 默认未知
            user.setDeleted(0);

            boolean result = userService.insertUser(user);
            if (!result) {
                throw new RuntimeException("自动注册失败");
            }
            logger.info("自动注册成功: {}", user.getUsername());
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成JWT令牌
        String token = jwtUtils.generateToken(user.getUsername());

        // 更新登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        // 构建用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setGender(user.getGender());
        userInfo.setBirthday(user.getBirthday());
        userInfo.setBio(user.getBio());
        userInfo.setHasSetPassword(user.getPasswordSetTime() != null);

        // 构建响应
        LoginResponse response = new LoginResponse(token, jwtUtils.getExpirationSeconds(), userInfo);

        logger.info("手机号登录成功: {}", user.getUsername());
        return response;
    }

    /**
     * 用户注册
     * 注册成功后自动生成JWT token并返回完整的登录响应
     * 
     * @param registerRequest 注册请求
     * @return 注册成功后的登录响应（包含token和用户信息）
     * @throws Exception 注册异常
     * @author lingbai
     * @version 1.1 2025-01-27：修改返回类型为LoginResponse，实现注册后自动登录
     */
    @Override
    public LoginResponse register(RegisterRequest registerRequest) {
        logger.info("用户注册: {}", registerRequest.getUsername());

        // 参数校验
        validateRegisterRequest(registerRequest);

        // 检查用户名是否已存在
        if (!userService.isUsernameUnique(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(registerRequest.getEmail()) &&
                !userService.isEmailUnique(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(registerRequest.getPhone()) &&
                !userService.isPhoneUnique(registerRequest.getPhone())) {
            throw new RuntimeException("手机号已被使用");
        }

        // 验证手机验证码（手机注册必须验证验证码）
        if (StringUtils.hasText(registerRequest.getPhone()) &&
                StringUtils.hasText(registerRequest.getCaptcha())) {

            logger.info("开始验证手机验证码: phone={}", registerRequest.getPhone());

            // 验证手机验证码 - 使用大写的REGISTER以匹配SMS服务
            boolean isValidCaptcha = verifySmsCode(registerRequest.getPhone(), registerRequest.getCaptcha(),
                    "REGISTER");

            if (!isValidCaptcha) {
                logger.warn("手机验证码验证失败: phone={}, captcha={}",
                        registerRequest.getPhone(), registerRequest.getCaptcha());
                throw new RuntimeException("验证码错误或已过期");
            }

            logger.info("手机验证码验证成功: phone={}", registerRequest.getPhone());
        } else if (StringUtils.hasText(registerRequest.getPhone())) {
            // 如果提供了手机号但没有验证码信息，则要求验证码
            throw new RuntimeException("手机注册需要提供验证码");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setNickname(StringUtils.hasText(registerRequest.getNickname()) ? registerRequest.getNickname()
                : registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setStatus(1); // 默认启用(1-正常，0-禁用)
        user.setGender(0); // 默认未知
        user.setDeleted(0); // 设置删除标志为0（未删除）

        // 保存用户
        boolean result = userService.insertUser(user);

        if (!result) {
            logger.error("用户注册失败: {}", user.getUsername());
            throw new RuntimeException("用户注册失败");
        }

        logger.info("用户注册成功: {}", user.getUsername());

        // 注册成功后，自动生成JWT token并构建登录响应
        String token = jwtUtils.generateToken(user.getUsername());

        // 构建用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setGender(user.getGender());
        userInfo.setBirthday(user.getBirthday());
        userInfo.setBio(user.getBio());

        // 构建登录响应
        LoginResponse response = new LoginResponse(token, jwtUtils.getExpirationSeconds(), userInfo);

        logger.info("用户注册并自动登录成功: {}", user.getUsername());
        return response;
    }

    /**
     * 用户登出
     */
    @Override
    public boolean logout(String token) {
        logger.info("用户登出");

        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            // 将Token加入黑名单 - 临时禁用Redis功能
            // String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            // Long expiration = jwtUtils.getExpirationSeconds();
            // redisTemplate.opsForValue().set(blacklistKey, "1", expiration,
            // TimeUnit.SECONDS);

            logger.info("用户登出成功");
            return true;
        } catch (Exception e) {
            logger.error("用户登出失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新Token
     */
    @Override
    public String refreshToken(String token) {
        logger.info("刷新Token");

        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("Token不能为空");
        }

        // 检查Token是否在黑名单中
        if (isTokenBlacklisted(token)) {
            throw new RuntimeException("Token已失效");
        }

        // 刷新Token
        String newToken = jwtUtils.refreshToken(token);
        if (newToken == null) {
            throw new RuntimeException("Token刷新失败");
        }

        // 将旧Token加入黑名单
        logout(token);

        logger.info("Token刷新成功");
        return newToken;
    }

    /**
     * 验证Token
     */
    @Override
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        // 检查Token是否在黑名单中
        if (isTokenBlacklisted(token)) {
            return false;
        }

        // 验证Token
        String username = jwtUtils.getUsernameFromToken(token);
        if (username == null) {
            return false;
        }

        // 验证Token有效性
        return jwtUtils.validateToken(token);
    }

    /**
     * 校验注册请求参数
     */
    private void validateRegisterRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            throw new RuntimeException("用户名不能为空");
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }

        if (!StringUtils.hasText(request.getConfirmPassword())) {
            throw new RuntimeException("确认密码不能为空");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        if (request.getUsername().length() < 3 || request.getUsername().length() > 20) {
            throw new RuntimeException("用户名长度必须在3-20个字符之间");
        }

        if (request.getPassword().length() < 6 || request.getPassword().length() > 20) {
            throw new RuntimeException("密码长度必须在6-20个字符之间");
        }
    }

    /**
     * 检查Token是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            // 临时禁用Redis功能
            // String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            // return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
            return false; // 临时返回false，表示Token未被拉黑
        } catch (Exception e) {
            logger.error("检查Token黑名单失败: {}", e.getMessage());
            return false;
        }
    }
}
