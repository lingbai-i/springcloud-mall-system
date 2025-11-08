package com.mall.user.security;

import com.mall.user.utils.JwtUtils;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private JwtUtils jwtUtils;

    // 定义不需要JWT认证的端点
    private static final List<String> PERMIT_ALL_PATHS = Arrays.asList(
            "/auth/**",
            "/api/auth/**",
            "/api/users/register",
            "/api/users/login",
            "/api/users/refresh-token",
            "/api/users/check-*",
            "/api/users/test",
            "/users/register",
            "/users/login",
            "/users/refresh-token",
            "/users/check-*",
            "/users/test",
            "/api/test/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        logger.debug("JWT Filter - Request URI: {}", requestPath);

        // 使用AntPathMatcher检查是否是permitAll的路径
        boolean isPermitAllPath = PERMIT_ALL_PATHS.stream()
                .anyMatch(pattern -> {
                    boolean matches = pathMatcher.match(pattern, requestPath);
                    if (matches) {
                        logger.debug("JWT Filter - Path '{}' matches pattern '{}'", requestPath, pattern);
                    }
                    return matches;
                });

        logger.debug("JWT Filter - Is permit all path: {}", isPermitAllPath);

        if (isPermitAllPath) {
            logger.debug("JWT Filter - Skipping JWT validation for permitAll path: {}", requestPath);
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = getTokenFromRequest(request);

            if (StringUtils.hasText(token)) {
                logger.debug("JWT Filter - Token exists, validating...");
                boolean isValid = jwtUtils.validateToken(token);
                logger.debug("JWT Filter - Token validation result: {}", isValid);

                if (isValid) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    logger.debug("JWT Filter - Username from token: {}", username);

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("JWT Filter - Authentication set for user: {}", username);
                } else {
                    logger.debug("JWT Filter - Token is invalid");
                }
            } else {
                logger.debug("JWT Filter - No token found in request");
            }
        } catch (Exception e) {
            logger.error("JWT Filter - Exception during JWT processing", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("JWT Filter - Authorization header: {}", bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            logger.debug("JWT Filter - Extracted token: {}", token);
            return token;
        }
        logger.debug("JWT Filter - No valid Authorization header found");
        return null;
    }
}