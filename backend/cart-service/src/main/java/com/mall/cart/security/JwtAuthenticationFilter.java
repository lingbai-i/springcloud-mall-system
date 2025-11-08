package com.mall.cart.security;

import com.mall.cart.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    // 定义不需要JWT认证的端点
    private static final List<String> PERMIT_ALL_PATHS = Arrays.asList(
        "/actuator/",
        "/swagger-ui/",
        "/v3/api-docs/",
        "/swagger-resources/",
        "/webjars/",
        "/doc.html",
        "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        logger.debug("JWT Filter - Processing request: '{}'", requestPath);
        
        // 检查是否是permitAll的路径
        boolean isPermitAllPath = PERMIT_ALL_PATHS.stream()
            .anyMatch(path -> {
                if (path.endsWith("/")) {
                    // 对于以/结尾的路径，使用startsWith匹配
                    return requestPath.startsWith(path);
                } else {
                    // 对于精确路径，使用equals匹配
                    return requestPath.equals(path);
                }
            });
        
        if (isPermitAllPath) {
            logger.debug("JWT Filter - Skipping JWT validation for: {}", requestPath);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        
        logger.debug("JWT Filter - Processing JWT validation for: {}", requestPath);
        
        try {
            String token = getTokenFromRequest(request);
            logger.debug("JWT Filter - Token from request: {}", token != null ? "present" : "absent");

            // 只有当token存在且有效时才设置认证信息
            if (StringUtils.hasText(token)) {
                logger.debug("JWT Filter - Token exists, validating...");
                boolean isValid = jwtUtils.validateToken(token);
                logger.debug("JWT Filter - Token validation result: {}", isValid);
                
                if (isValid) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    Long userId = jwtUtils.getUserIdFromToken(token);
                    logger.debug("JWT Filter - Username from token: {}, UserId: {}", username, userId);

                    // 创建认证对象，将用户ID存储在principal中
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId != null ? userId : username, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("JWT Filter - Authentication set for user: {}", username);
                } else {
                    logger.warn("JWT Filter - Token is invalid");
                }
            } else {
                logger.warn("JWT Filter - No token found in request");
            }
        } catch (Exception e) {
            logger.error("JWT Filter - Exception during JWT processing: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取JWT令牌
     * 
     * @param request HTTP请求
     * @return JWT令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("JWT Filter - Authorization header: {}", bearerToken != null ? "present" : "absent");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            logger.debug("JWT Filter - Extracted token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            return token;
        }
        logger.debug("JWT Filter - No valid Authorization header found");
        return null;
    }
}