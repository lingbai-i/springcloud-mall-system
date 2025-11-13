package com.mall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 认证服务启动类
 * 
 * 提供统一的身份认证、授权和令牌管理功能
 * - JWT令牌生成与验证
 * - 用户登录认证
 * - 令牌刷新
 * - 访问控制
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-11-11
 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
})
public class AuthApplication {

    /**
     * 应用程序入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
        System.out.println("认证服务启动成功 (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧");
    }
}
