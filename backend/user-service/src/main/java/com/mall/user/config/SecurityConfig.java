package com.mall.user.config;

import com.mall.user.security.JwtAuthenticationFilter;
import com.mall.user.security.JwtAuthenticationEntryPoint;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * Spring Security配置类 - 简化版
 * 
 * 支持通过配置文件控制是否启用JWT认证：
 * security.jwt.enabled=false // 开发模式：禁用JWT认证
 * security.jwt.enabled=true // 生产模式：启用JWT认证（默认）
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-01-27
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${security.jwt.enabled:false}")
    private boolean jwtEnabled;

    /**
     * 构造函数，记录JWT配置
     */
    public SecurityConfig() {
        System.out.println("[SecurityConfig] 初始化 SecurityConfig");
    }

    /**
     * Bean初始化后记录配置
     */
    @PostConstruct
    public void init() {
        System.out.println(
                "[SecurityConfig] JWT认证已" + (jwtEnabled ? "启用" : "禁用") + " (security.jwt.enabled=" + jwtEnabled + ")");
    }

    /**
     * 密码编码器
     * 
     * @return BCrypt密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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

        // 根据配置决定是否启用JWT认证
        if (jwtEnabled) {
            // 生产模式：启用JWT认证
            http
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                    .addFilterBefore(jwtAuthenticationFilter,
                            UsernamePasswordAuthenticationFilter.class)
                    .authorizeHttpRequests(authz -> authz
                            // 认证相关接口放行
                            .requestMatchers("/auth/**").permitAll()
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/user-service/auth/**").permitAll()
                            .requestMatchers("/api/user-service/auth/**").permitAll()
                            .requestMatchers("/users/register").permitAll()
                            .requestMatchers("/users/login").permitAll()
                            .requestMatchers("/api/users/register").permitAll()
                            .requestMatchers("/api/users/login").permitAll()
                            // 内部服务调用接口放行
                            .requestMatchers("/users/statistics").permitAll()
                            .requestMatchers("/api/users/statistics").permitAll()
                            // 唯一性检查接口放行
                            .requestMatchers("/users/check-*").permitAll()
                            .requestMatchers("/api/users/check-*").permitAll()
                            // 测试端点放行
                            .requestMatchers("/users/test").permitAll()
                            .requestMatchers("/api/users/test").permitAll()
                            // 监控端点放行
                            .requestMatchers("/actuator/**").permitAll()
                            // 其他接口需要认证
                            .anyRequest().authenticated());
        } else {
            // 开发模式：禁用JWT认证，所有接口放行
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        }

        return http.build();
    }

    /**
     * CORS配置源
     * 
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源（指定具体域名，支持本地开发和生产环境）
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:5174"));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 允许发送凭证
        configuration.setAllowCredentials(true);

        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}