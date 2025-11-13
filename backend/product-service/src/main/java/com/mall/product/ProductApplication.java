package com.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品服务启动类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@SpringBootApplication
@MapperScan("com.mall.product.mapper")
@EnableFeignClients
public class ProductApplication {

    /**
     * 主方法，启动商品服务应用
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}