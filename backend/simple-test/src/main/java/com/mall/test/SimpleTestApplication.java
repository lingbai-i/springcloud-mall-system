package com.mall.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单测试应用
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-10-21
 */
@SpringBootApplication
@RestController
public class SimpleTestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SimpleTestApplication.class, args);
        System.out.println("🚀 简单测试服务启动成功！");
    }
    
    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "在线商城后端服务测试");
        result.put("status", "运行正常");
        result.put("timestamp", LocalDateTime.now());
        result.put("service", "simple-test");
        return result;
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now());
        return result;
    }
}