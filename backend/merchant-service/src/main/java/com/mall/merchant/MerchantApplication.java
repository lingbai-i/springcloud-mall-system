package com.mall.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 商家服务启动类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-10-21
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class MerchantApplication {

    /**
     * 商家服务主入口方法
     * 启动Spring Boot应用程序并注册到Nacos服务发现
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(MerchantApplication.class, args);
        System.out.println("🚀 商家服务启动成功！");
    }
}