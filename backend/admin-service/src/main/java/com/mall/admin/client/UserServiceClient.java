package com.mall.admin.client;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户服务Feign Client
 * 
 * @author system
 * @since 2025-01-09
 */
@FeignClient(name = "user-service", path = "/users")
public interface UserServiceClient {
    
    /**
     * 查询用户列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词
     * @param status 状态
     * @return 用户列表
     */
    @GetMapping("")
    R<PageResult<Map<String, Object>>> getUserList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status
    );
    
    /**
     * 获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    R<Map<String, Object>> getUserDetail(@PathVariable("id") Long userId);
    
    /**
     * 禁用用户
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/disable")
    R<Void> disableUser(@PathVariable("id") Long userId);
    
    /**
     * 启用用户
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    R<Void> enableUser(@PathVariable("id") Long userId);
    
    /**
     * 获取用户统计数据
     * 
     * @return 统计数据
     */
    @GetMapping("/statistics")
    R<Map<String, Object>> getUserStatistics();
}
