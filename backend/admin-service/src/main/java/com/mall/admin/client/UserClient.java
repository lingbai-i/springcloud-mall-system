package com.mall.admin.client;

import com.mall.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户服务客户端
 * 用于调用用户服务的相关接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@FeignClient(name = "user-service", path = "/api/users")
public interface UserClient {
    
    /**
     * 分页查询用户列表
     * 
     * @param params 查询参数
     * @return 用户列表
     */
    @GetMapping("/list")
    R<Map<String, Object>> getUserList(@RequestParam Map<String, Object> params);
    
    /**
     * 根据ID获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    R<Map<String, Object>> getUserDetail(@PathVariable("userId") Long userId);
    
    /**
     * 禁用用户
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/{userId}/disable")
    R<Void> disableUser(@PathVariable("userId") Long userId);
    
    /**
     * 启用用户
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/{userId}/enable")
    R<Void> enableUser(@PathVariable("userId") Long userId);
    
    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    R<Void> deleteUser(@PathVariable("userId") Long userId);
    
    /**
     * 获取用户统计数据
     * 
     * @param params 查询参数
     * @return 统计数据
     */
    @GetMapping("/stats")
    R<Map<String, Object>> getUserStats(@RequestParam Map<String, Object> params);
}