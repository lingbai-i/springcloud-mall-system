package com.mall.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置类
 * 配置管理员系统的认证和授权规则
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    /**
     * 配置安全过滤器链
     * 定义哪些URL需要认证，哪些可以匿名访问
     * 
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（API接口通常不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置会话管理为无状态（使用JWT token）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置URL访问权限
            .authorizeHttpRequests(auth -> auth
                // 允许匿名访问的URL
                .requestMatchers("/api/admin/auth/login").permitAll()
                .requestMatchers("/api/admin/auth/logout").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/doc.html").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                
                // 系统管理相关接口需要超级管理员权限
                .requestMatchers("/api/admin/system/**").hasRole("SUPER_ADMIN")
                
                // 用户管理接口需要管理员权限
                .requestMatchers("/api/admin/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // 商家管理接口需要管理员权限
                .requestMatchers("/api/admin/merchants/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
    
    /**
     * 配置密码编码器
     * 使用BCrypt加密算法
     * 
     * @return PasswordEncoder 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}