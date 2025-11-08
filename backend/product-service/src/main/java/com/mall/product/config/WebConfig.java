package com.mall.product.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置跨域访问等Web相关设置
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-01-22
 * 修改日志：V1.2 2025-10-22：修复CORS配置问题，禁用allowCredentials以支持通配符
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域访问
     * 允许前端应用访问后端API
     * 修复CORS配置问题：禁用allowCredentials以支持通配符域名
     * 
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 使用allowedOriginPatterns支持通配符
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(false) // 禁用凭证以支持通配符域名
                .maxAge(3600); // 预检请求的缓存时间（秒）
    }
}