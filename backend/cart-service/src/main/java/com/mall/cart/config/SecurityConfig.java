package com.mall.cart.config;

import com.mall.cart.security.JwtAuthenticationFilter;
import com.mall.cart.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Spring Security配置类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${security.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @PostConstruct
    public void init() {
        System.out.println("[Cart SecurityConfig] JWT认证已" + (jwtEnabled ? "启用" : "禁用") + " (security.jwt.enabled="
                + jwtEnabled + ")");
    }

    /**
     * 安全过滤器链配置
     * 
     * @param http HttpSecurity对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF
                .csrf(csrf -> csrf.disable())
                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 配置会话管理
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 开发模式：允许所有请求
        if (!jwtEnabled) {
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        } else {
            // 生产模式：启用JWT认证
            http
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .authorizeHttpRequests(authz -> authz
                            .requestMatchers(
                                    "/actuator/**",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/swagger-resources/**",
                                    "/webjars/**",
                                    "/doc.html",
                                    "/favicon.ico")
                            .permitAll()
                            .anyRequest().authenticated());
        }

        return http.build();
    }

    /**
     * CORS配置
     * 
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}