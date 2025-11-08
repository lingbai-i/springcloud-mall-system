package com.mall.user.controller;

import com.mall.common.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关测试控制器 - 处理网关转发后的请求
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@RestController
@RequestMapping("/users")
public class GatewayTestController {

    /**
     * 测试端点 - 处理网关转发后的路径 /users/test
     */
    @GetMapping("/test")
    public R<Map<String, Object>> test() {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("service", "user-service");
        data.put("message", "用户服务正常运行");
        data.put("timestamp", System.currentTimeMillis());
        data.put("path", "gateway-forwarded");
        
        return R.ok("测试成功", data);
    }

    /**
     * 注册测试端点 - 处理网关转发的 /users/register 请求
     * 原始请求: /api/users/register -> 网关StripPrefix=1 -> /users/register
     */
    @GetMapping("/register")
    public R<Map<String, Object>> registerTest() {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Gateway register test endpoint");
        data.put("service", "user-service");
        data.put("timestamp", System.currentTimeMillis());
        
        return R.ok("测试成功", data);
    }

    /**
     * 用户信息测试端点 - 处理网关转发的 /users/profile 请求
     * 原始请求: /api/users/profile -> 网关StripPrefix=1 -> /users/profile
     */
    @GetMapping("/profile")
    public R<Map<String, Object>> profileTest() {
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("message", "Gateway profile test endpoint");
        data.put("service", "user-service");
        data.put("timestamp", System.currentTimeMillis());
        data.put("path", "/users/profile");
        
        return R.ok("用户信息测试成功", data);
    }
}