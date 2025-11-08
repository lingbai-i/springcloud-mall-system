package com.mall.merchant.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT token
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Component
public class JwtUtil {

    // 日志记录：记录JWT生成与解析的关键路径，便于问题诊断
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    
    /**
     * JWT密钥
     */
    @Value("${jwt.secret:mall-merchant-secret-key-for-jwt-token-generation}")
    private String secret;
    
    /**
     * JWT过期时间（毫秒）
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;
    
    /**
     * 生成JWT token
     * 
     * @param merchantId 商家ID
     * @param username 用户名
     * @param status 状态
     * @return JWT token字符串
     */
    public String generateToken(Long merchantId, String username, String status) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("merchantId", merchantId);
        claims.put("username", username);
        claims.put("status", status);
        claims.put("role", "MERCHANT");
        
        return createToken(claims, username);
    }
    
    /**
     * 创建JWT token
     * 
     * @param claims 声明信息
     * @param subject 主题（通常是用户名）
     * @return JWT token字符串
     */
    /**
     * 创建JWT token
     * 修改日志：V1.1 2025-11-05 将签名算法由 HS512 调整为 HS256，以兼容默认密钥长度，
     * 避免因密钥长度不足（<64字节）导致的 WeakKeyException，引发登录失败。
     * 设计说明：HS256对默认字符串密钥更友好，测试环境无需额外配置即可通过；生产环境建议使用足够长度的密钥并考虑更强算法。
     *
     * @param claims  声明信息
     * @param subject 主题（通常是用户名）
     * @return JWT token字符串
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        log.debug("创建JWT，subject={}, claimsKeys={}, expiresAt={}", subject, claims.keySet(), expiryDate);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                // 调整为HS256以兼容默认密钥长度，避免WeakKeyException
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 从token中获取用户名
     * 
     * @param token JWT token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
    
    /**
     * 从token中获取商家ID
     * 
     * @param token JWT token
     * @return 商家ID
     */
    public Long getMerchantIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Long.valueOf(claims.get("merchantId").toString());
    }
    
    /**
     * 从token中获取状态
     * 
     * @param token JWT token
     * @return 状态
     */
    public String getStatusFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("status").toString();
    }
    
    /**
     * 从token中获取过期时间
     * 
     * @param token JWT token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }
    
    /**
     * 从token中获取声明信息
     * 
     * @param token JWT token
     * @return 声明信息
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 检查token是否过期
     * 
     * @param token JWT token
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 验证token
     * 
     * @param token JWT token
     * @param username 用户名
     * @return 是否有效
     */
    public Boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }
    
    /**
     * 获取签名密钥
     * 
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        // 说明：使用提供的字符串密钥生成HMAC密钥；若生产环境使用HS512，请确保密钥长度>=64字节
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}