package com.mall.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 管理员服务启动类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@SpringBootApplication
@EnableFeignClients
public class AdminServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}