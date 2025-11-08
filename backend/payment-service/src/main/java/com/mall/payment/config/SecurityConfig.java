package com.mall.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security安全配置类
 * 配置支付服务的安全策略和权限控制
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    /**
     * 配置安全过滤器链
     * 定义URL访问权限和认证策略
     * 
     * @param http HTTP安全配置
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（API服务通常不需要）
            .csrf().disable()
            
            // 配置CORS
            .cors().configurationSource(corsConfigurationSource())
            
            .and()
            
            // 配置会话管理策略为无状态（适用于JWT等token认证）
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            
            .and()
            
            // 配置URL访问权限
            .authorizeHttpRequests(authz -> authz
                // 公开接口 - 不需要认证
                .requestMatchers(
                    "/api/payment/health",           // 健康检查
                    "/api/payment/callback/**",      // 支付回调（第三方调用）
                    "/actuator/**",                  // Spring Boot Actuator端点
                    "/swagger-ui/**",                // Swagger UI
                    "/v3/api-docs/**",               // OpenAPI文档
                    "/error"                         // 错误页面
                ).permitAll()
                
                // 支付相关接口 - 需要用户认证
                .requestMatchers(
                    "/api/payment/orders/**",        // 支付订单管理
                    "/api/payment/refunds/**"        // 退款管理
                ).hasAnyRole("USER", "ADMIN")
                
                // 风控相关接口 - 需要管理员权限
                .requestMatchers(
                    "/api/payment/risk/**"           // 风控管理
                ).hasRole("ADMIN")
                
                // 统计报表接口 - 需要管理员权限
                .requestMatchers(
                    "/api/payment/statistics/**"     // 统计报表
                ).hasRole("ADMIN")
                
                // 系统管理接口 - 需要超级管理员权限
                .requestMatchers(
                    "/api/payment/admin/**"          // 系统管理
                ).hasRole("SUPER_ADMIN")
                
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            
            // 配置异常处理
            .exceptionHandling()
            .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
            .accessDeniedHandler(new CustomAccessDeniedHandler())
            
            .and()
            
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器
     * 使用BCrypt算法进行密码加密
     * 
     * @return BCrypt密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS配置源
     * 配置跨域资源共享策略
     * 
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源域名
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许发送认证信息（如cookies）
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * JWT认证过滤器
     * 处理JWT token的验证和用户认证
     * 
     * @return JWT认证过滤器
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}