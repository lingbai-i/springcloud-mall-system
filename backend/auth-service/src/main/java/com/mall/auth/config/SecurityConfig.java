package com.mall.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Spring Security 安全配置类
 * 
 * 配置认证服务的安全策略：
 * - 禁用Session，使用无状态JWT认证
 * - 配置允许匿名访问的路径
 * - 配置密码加密器
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-11-11
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 允许匿名访问的路径列表
     */
    @Value("${security.permit-all-paths:}")
    private String[] permitAllPaths;

    /**
     * 是否启用CSRF保护
     */
    @Value("${security.csrf-enabled:false}")
    private boolean csrfEnabled;

    /**
     * 配置安全过滤链
     * 
     * @param http HTTP安全配置对象
     * @return 配置好的安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("配置Spring Security安全策略...");
        
        // 配置HTTP安全
        http
            // 禁用CSRF（开发环境）
            .csrf(csrf -> {
                if (!csrfEnabled) {
                    csrf.disable();
                    log.debug("CSRF保护已禁用（开发环境）");
                }
            })
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> {
                // 配置允许匿名访问的路径
                if (permitAllPaths != null && permitAllPaths.length > 0) {
                    auth.requestMatchers(permitAllPaths).permitAll();
                    log.debug("配置允许匿名访问的路径: {}", String.join(", ", permitAllPaths));
                }
                
                // 其他请求需要认证
                auth.anyRequest().authenticated();
            })
            
            // 禁用Session，使用无状态JWT
            .sessionManagement(session -> {
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                log.debug("会话管理策略: 无状态（STATELESS）");
            })
            
            // 禁用默认登录页面
            .formLogin(AbstractHttpConfigurer::disable)
            
            // 禁用HTTP Basic认证
            .httpBasic(AbstractHttpConfigurer::disable);

        log.info("Spring Security配置完成");
        return http.build();
    }

    /**
     * 密码加密器Bean
     * 
     * 使用BCrypt算法进行密码加密
     * 
     * @return BCrypt密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("配置密码加密器: BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }
}
