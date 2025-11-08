package com.mall.admin.controller;

import com.mall.admin.domain.dto.UserQueryRequest;
import com.mall.admin.service.UserManagementService;
import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;

/**
 * 用户管理控制器
 * 处理管理员对用户的管理操作
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "管理员对用户的管理操作接口")
public class UserManagementController {
    
    private final UserManagementService userManagementService;
    
    /**
     * 分页查询用户列表
     * 
     * @param request 查询请求参数
     * @return 分页用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表", description = "管理员分页查询用户列表，支持关键词搜索和状态筛选")
    public R<PageResult<User>> getUserList(@Valid UserQueryRequest request) {
        log.info("管理员查询用户列表，参数：{}", request);
        
        try {
            PageResult<User> result = userManagementService.getUserList(request);
            
            log.info("用户列表查询成功，总数：{}", result.getTotal());
            return R.success(result);
            
        } catch (Exception e) {
            log.error("查询用户列表异常", e);
            return R.error("查询用户列表失败");
        }
    }
    
    /**
     * 获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public R<User> getUserDetail(@PathVariable Long userId) {
        log.info("管理员查询用户详情，用户ID：{}", userId);
        
        try {
            User user = userManagementService.getUserDetail(userId);
            
            if (user == null) {
                log.warn("用户不存在，ID：{}", userId);
                return R.error("用户不存在");
            }
            
            log.info("用户详情查询成功，用户ID：{}", userId);
            return R.success(user);
            
        } catch (Exception e) {
            log.error("查询用户详情异常，用户ID：{}", userId, e);
            return R.error("查询用户详情失败");
        }
    }
    
    /**
     * 禁用用户
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/{userId}/disable")
    @Operation(summary = "禁用用户", description = "管理员禁用指定用户")
    public R<Void> disableUser(@PathVariable Long userId) {
        log.info("管理员禁用用户，用户ID：{}", userId);
        
        try {
            // 获取当前管理员ID
            Long adminId = getCurrentAdminId();
            
            userManagementService.disableUser(userId, adminId);
            
            log.info("用户禁用成功，用户ID：{}，操作管理员ID：{}", userId, adminId);
            return R.success("用户禁用成功");
            
        } catch (Exception e) {
            log.error("禁用用户异常，用户ID：{}", userId, e);
            return R.error("禁用用户失败");
        }
    }
    
    /**
     * 启用用户
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/{userId}/enable")
    @Operation(summary = "启用用户", description = "管理员启用指定用户")
    public R<Void> enableUser(@PathVariable Long userId) {
        log.info("管理员启用用户，用户ID：{}", userId);
        
        try {
            // 获取当前管理员ID
            Long adminId = getCurrentAdminId();
            
            userManagementService.enableUser(userId, adminId);
            
            log.info("用户启用成功，用户ID：{}，操作管理员ID：{}", userId, adminId);
            return R.success("用户启用成功");
            
        } catch (Exception e) {
            log.error("启用用户异常，用户ID：{}", userId, e);
            return R.error("启用用户失败");
        }
    }
    
    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "管理员删除指定用户")
    public R<Void> deleteUser(@PathVariable Long userId) {
        log.info("管理员删除用户，用户ID：{}", userId);
        
        try {
            // 获取当前管理员ID
            Long adminId = getCurrentAdminId();
            
            userManagementService.deleteUser(userId, adminId);
            
            log.info("用户删除成功，用户ID：{}，操作管理员ID：{}", userId, adminId);
            return R.success("用户删除成功");
            
        } catch (Exception e) {
            log.error("删除用户异常，用户ID：{}", userId, e);
            return R.error("删除用户失败");
        }
    }
    
    /**
     * 获取用户统计数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取用户统计数据", description = "获取指定时间范围内的用户统计数据")
    public R<Map<String, Object>> getUserStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        log.info("管理员获取用户统计数据，时间范围：{} - {}", startTime, endTime);
        
        try {
            // 构建查询参数
            Map<String, Object> params = new java.util.HashMap<>();
            if (startTime != null) {
                params.put("startTime", startTime);
            }
            if (endTime != null) {
                params.put("endTime", endTime);
            }
            
            Map<String, Object> stats = userManagementService.getUserStats(params);
            
            log.info("用户统计数据获取成功");
            return R.success(stats);
            
        } catch (Exception e) {
            log.error("获取用户统计数据异常", e);
            return R.error("获取用户统计数据失败");
        }
    }
    
    /**
     * 获取当前管理员ID
     * 从认证上下文中获取当前登录的管理员ID
     * 
     * @return 管理员ID
     */
    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return Long.parseLong((String) authentication.getPrincipal());
        }
        throw new RuntimeException("未获取到管理员认证信息");
    }
}