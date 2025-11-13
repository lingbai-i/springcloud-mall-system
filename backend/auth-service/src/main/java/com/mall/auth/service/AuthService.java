package com.mall.auth.service;

import com.mall.auth.dto.LoginRequest;
import com.mall.auth.dto.LoginResponse;
import com.mall.auth.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务业务层
 * 
 * 负责用户认证、令牌管理等核心业务逻辑
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-11-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient.Builder webClientBuilder;

    /**
     * 用户服务URL
     */
    @Value("${user-service.url}")
    private String userServiceUrl;

    /**
     * 访问令牌有效期（毫秒）
     */
    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应（包含访问令牌和刷新令牌）
     */
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录请求: username={}, loginType={}", request.getUsername(), request.getLoginType());

        // 调用用户服务验证用户凭据
        Map<String, Object> userInfo = validateUserCredentials(request);
        
        if (userInfo == null) {
            log.warn("用户验证失败: username={}", request.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }

        Long userId = ((Number) userInfo.get("id")).longValue();
        String username = (String) userInfo.get("username");

        log.info("用户验证成功: userId={}, username={}", userId, username);

        // 生成访问令牌和刷新令牌
        String accessToken = jwtUtils.generateAccessToken(userId, username);
        String refreshToken = jwtUtils.generateRefreshToken(userId, username);

        // 存储刷新令牌到Redis（用于令牌刷新验证）
        String refreshTokenKey = "refresh_token:" + userId;
        redisTemplate.opsForValue().set(
            refreshTokenKey, 
            refreshToken, 
            7, 
            TimeUnit.DAYS
        );

        log.info("令牌生成成功: userId={}", userId);

        // 构建登录响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // 转换为秒
                .userId(userId)
                .username(username)
                .build();
    }

    /**
     * 刷新访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的登录响应
     */
    public LoginResponse refreshToken(String refreshToken) {
        log.info("令牌刷新请求");

        // 验证刷新令牌
        if (!jwtUtils.validateToken(refreshToken)) {
            log.warn("无效的刷新令牌");
            throw new RuntimeException("无效的刷新令牌");
        }

        // 从令牌中提取用户信息
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        String username = jwtUtils.getUsernameFromToken(refreshToken);

        // 验证Redis中的刷新令牌
        String refreshTokenKey = "refresh_token:" + userId;
        String storedToken = (String) redisTemplate.opsForValue().get(refreshTokenKey);
        
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("刷新令牌不匹配或已过期: userId={}", userId);
            throw new RuntimeException("刷新令牌已失效");
        }

        log.info("刷新令牌验证成功: userId={}", userId);

        // 生成新的访问令牌
        String newAccessToken = jwtUtils.generateAccessToken(userId, username);

        log.info("新访问令牌生成成功: userId={}", userId);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 刷新令牌保持不变
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .userId(userId)
                .username(username)
                .build();
    }

    /**
     * 验证访问令牌
     * 
     * @param token 访问令牌
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }

    /**
     * 用户登出
     * 
     * @param token 访问令牌
     */
    public void logout(String token) {
        Long userId = jwtUtils.getUserIdFromToken(token);
        log.info("用户登出: userId={}", userId);

        // 将令牌加入黑名单
        long remainingValidity = jwtUtils.getRemainingValidity(token);
        if (remainingValidity > 0) {
            String blacklistKey = "token_blacklist:" + token;
            redisTemplate.opsForValue().set(
                blacklistKey, 
                "1", 
                remainingValidity, 
                TimeUnit.MILLISECONDS
            );
        }

        // 删除刷新令牌
        String refreshTokenKey = "refresh_token:" + userId;
        redisTemplate.delete(refreshTokenKey);

        log.info("用户登出成功: userId={}", userId);
    }

    /**
     * 验证用户凭据（调用用户服务）
     * 
     * @param request 登录请求
     * @return 用户信息Map，验证失败返回null
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> validateUserCredentials(LoginRequest request) {
        try {
            // 调用用户服务的验证接口
            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(userServiceUrl + "/api/users/validate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return (Map<String, Object>) response.get("data");
            }

            return null;
        } catch (Exception e) {
            log.error("调用用户服务验证失败: {}", e.getMessage(), e);
            throw new RuntimeException("用户服务不可用，请稍后重试");
        }
    }
}
