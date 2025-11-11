package com.mall.admin.controller;

import com.mall.admin.service.UserManagementService;
import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户管理控制器
 * 
 * @author system
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManagementController {
    
    private final UserManagementService userManagementService;
    
    /**
     * 查询用户列表
     */
    @GetMapping("")
    public R<PageResult<Map<String, Object>>> getUserList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status) {
        
        PageResult<Map<String, Object>> result = userManagementService.getUserList(page, size, keyword, status);
        return R.ok(result);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public R<Map<String, Object>> getUserDetail(@PathVariable("id") Long userId) {
        Map<String, Object> result = userManagementService.getUserDetail(userId);
        return R.ok(result);
    }
    
    /**
     * 禁用用户
     */
    @PutMapping("/{id}/disable")
    public R<Void> disableUser(@PathVariable("id") Long userId, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        userManagementService.disableUser(userId, adminId);
        return R.ok();
    }
    
    /**
     * 启用用户
     */
    @PutMapping("/{id}/enable")
    public R<Void> enableUser(@PathVariable("id") Long userId, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        userManagementService.enableUser(userId, adminId);
        return R.ok();
    }
}
