package com.mall.payment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义访问拒绝处理器
 * 处理已认证用户访问无权限资源的情况
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理访问拒绝异常
     * 当已认证用户尝试访问无权限资源时调用
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param accessDeniedException 访问拒绝异常
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        // 获取当前认证用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null ? authentication.getName() : "unknown";
        
        logger.warn("用户 {} 尝试访问无权限资源: {} {}", userId, request.getMethod(), request.getRequestURI());
        
        // 设置响应状态码为403 Forbidden
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "ACCESS_DENIED");
        errorResponse.put("message", "您没有权限访问此资源");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("method", request.getMethod());
        errorResponse.put("userId", userId);
        errorResponse.put("timestamp", new Date());
        
        // 添加详细错误信息（开发环境）
        if (isDevEnvironment()) {
            errorResponse.put("details", accessDeniedException.getMessage());
            errorResponse.put("requiredRoles", getRequiredRoles(request.getRequestURI()));
            errorResponse.put("userRoles", getCurrentUserRoles(authentication));
        }
        
        // 写入响应
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        
        logger.debug("返回访问拒绝响应: {}", jsonResponse);
    }

    /**
     * 检查是否为开发环境
     * 
     * @return 是否为开发环境
     */
    private boolean isDevEnvironment() {
        String profile = System.getProperty("spring.profiles.active", "dev");
        return "dev".equals(profile) || "development".equals(profile);
    }

    /**
     * 根据请求路径获取所需角色
     * 
     * @param requestURI 请求URI
     * @return 所需角色描述
     */
    private String getRequiredRoles(String requestURI) {
        if (requestURI.startsWith("/api/payment/risk/")) {
            return "ADMIN";
        } else if (requestURI.startsWith("/api/payment/statistics/")) {
            return "ADMIN";
        } else if (requestURI.startsWith("/api/payment/admin/")) {
            return "SUPER_ADMIN";
        } else if (requestURI.startsWith("/api/payment/orders/") || 
                   requestURI.startsWith("/api/payment/refunds/")) {
            return "USER, ADMIN";
        }
        return "AUTHENTICATED";
    }

    /**
     * 获取当前用户角色
     * 
     * @param authentication 认证对象
     * @return 用户角色列表
     */
    private String getCurrentUserRoles(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return "NONE";
        }
        
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .reduce((a, b) -> a + ", " + b)
                .orElse("NONE");
    }
}