package com.mall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 订单服务启动类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableTransactionManagement
@EnableScheduling
public class OrderServiceApplication {
    
    /**
     * 订单服务主入口方法
     * 启动Spring Boot应用程序并注册到Nacos服务发现
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
        System.out.println("🚀 订单服务启动成功！");
    }
}