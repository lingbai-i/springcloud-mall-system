package com.mall.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mall.user.client")
@SpringBootApplication(exclude = { RedisRepositoriesAutoConfiguration.class })
@MapperScan("com.mall.user.mapper")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        System.out.println("用户服务启动成功 (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧");
    }
}