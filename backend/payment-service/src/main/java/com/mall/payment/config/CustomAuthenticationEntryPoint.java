package com.mall.payment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义认证入口点
 * 处理未认证用户访问受保护资源的情况
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理认证异常
     * 当未认证用户尝试访问受保护资源时调用
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param authException 认证异常
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, 
                        AuthenticationException authException) throws IOException, ServletException {
        
        logger.warn("未认证用户尝试访问受保护资源: {} {}", request.getMethod(), request.getRequestURI());
        
        // 设置响应状态码为401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "AUTHENTICATION_REQUIRED");
        errorResponse.put("message", "访问此资源需要身份认证");
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("method", request.getMethod());
        errorResponse.put("timestamp", new Date());
        
        // 添加详细错误信息（开发环境）
        if (isDevEnvironment()) {
            errorResponse.put("details", authException.getMessage());
            errorResponse.put("suggestions", "请在请求头中添加有效的Authorization Bearer token");
        }
        
        // 写入响应
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        
        logger.debug("返回认证失败响应: {}", jsonResponse);
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
}