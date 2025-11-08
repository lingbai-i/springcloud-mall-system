package com.mall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 购物车服务启动类
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-11-01
 * 
 *        修改日志：
 *        V1.1 2025-11-01：启用定时任务功能，支持购物车数据同步
 */
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
        System.out.println("购物车服务启动成功！");
    }
}