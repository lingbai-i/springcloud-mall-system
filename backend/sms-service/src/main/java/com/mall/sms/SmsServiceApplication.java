package com.mall.sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SMS服务启动类
 *
 * @author lingbai
 * @since 2025-09-22
 */
@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class SmsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsServiceApplication.class, args);
        System.out.println("SMS服务启动成功！端口：8084");
    }

    @GetMapping("/health")
    public String health() {
        return "SMS Service is running!";
    }
}