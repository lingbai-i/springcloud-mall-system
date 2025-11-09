package com.mall.admin.controller;

import com.mall.admin.domain.dto.AdminLoginRequest;
import com.mall.admin.domain.entity.Admin;
import com.mall.admin.domain.vo.AdminInfoResponse;
import com.mall.admin.service.AdminService;
import com.mall.common.core.domain.R;
import com.mall.common.core.domain.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 管理员控制器
 * 处理管理员相关的HTTP请求
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理员管理", description = "管理员登录、信息管理等接口")
public class AdminController {
    
    private final AdminService adminService;
    
    /**
     * 管理员登录
     * 验证管理员身份并返回登录信息
     * 
     * @param request 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员用户名密码登录")
    public R<String> login(@Valid @RequestBody AdminLoginRequest request, HttpServletRequest httpRequest) {
        log.info("管理员登录请求，用户名：{}", request.getUsername());
        
        try {
            // 验证登录信息并获取JWT token
            String token = adminService.login(request);
            
            if (token == null || token.isEmpty()) {
                log.warn("管理员登录失败，用户名或密码错误：{}", request.getUsername());
                return R.fail("用户名或密码错误");
            }
            
            // 获取管理员信息并更新登录信息
            Admin admin = adminService.findByUsername(request.getUsername());
            if (admin != null) {
                String clientIp = getClientIp(httpRequest);
                adminService.updateLastLoginInfo(admin.getId(), clientIp);
            }
            
            log.info("管理员登录成功，用户名：{}", request.getUsername());
            return R.ok("登录成功", token);
            
        } catch (Exception e) {
            log.error("管理员登录异常，用户名：{}", request.getUsername(), e);
            return R.fail("登录失败，请稍后重试");
        }
    }
    
    /**
     * 管理员登出
     * 
     * @return 登出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "管理员登出", description = "管理员退出登录")
    public R<Void> logout() {
        log.info("管理员登出");
        
        try {
            // 这里可以添加登出逻辑，如清除缓存、记录登出日志等
            // 实际项目中可能需要清除JWT token或session
            
            log.info("管理员登出成功");
            return R.ok("登出成功");
            
        } catch (Exception e) {
            log.error("管理员登出异常", e);
            return R.fail("登出失败");
        }
    }
    
    /**
     * 获取管理员信息
     * 
     * @param adminId 管理员ID
     * @return 管理员信息
     */
    @GetMapping("/info/{adminId}")
    @Operation(summary = "获取管理员信息", description = "根据ID获取管理员详细信息")
    public R<AdminInfoResponse> getAdminInfo(@PathVariable Long adminId) {
        log.info("获取管理员信息，ID：{}", adminId);
        
        try {
            AdminInfoResponse response = adminService.getAdminInfo(adminId);
            
            if (response == null) {
                log.warn("管理员不存在，ID：{}", adminId);
                return R.fail("管理员不存在");
            }
            
            log.info("获取管理员信息成功，ID：{}", adminId);
            return R.ok(response);
            
        } catch (Exception e) {
            log.error("获取管理员信息异常，ID：{}", adminId, e);
            return R.fail("获取管理员信息失败");
        }
    }
    
    /**
     * 获取当前登录管理员信息
     * 
     * @return 当前管理员信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前管理员信息", description = "获取当前登录管理员的详细信息")
    public R<AdminInfoResponse> getCurrentAdmin() {
        log.info("获取当前管理员信息");
        
        try {
            // 这里应该从JWT token或session中获取当前管理员ID
            // 暂时使用固定ID进行演示
            Long currentAdminId = 1L; // 实际应该从认证上下文中获取
            
            return getAdminInfo(currentAdminId);
            
        } catch (Exception e) {
            log.error("获取当前管理员信息异常", e);
            return R.fail("获取管理员信息失败");
        }
    }
    
    /**
     * 修改管理员密码
     * 
     * @param adminId 管理员ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PutMapping("/{adminId}/password")
    @Operation(summary = "修改管理员密码", description = "管理员修改自己的密码")
    public R<Void> changePassword(
            @PathVariable Long adminId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        log.info("修改管理员密码，ID：{}", adminId);
        
        try {
            // 验证旧密码
            Admin admin = adminService.findById(adminId);
            if (admin == null) {
                log.warn("管理员不存在，ID：{}", adminId);
                return R.fail("管理员不存在");
            }
            
            if (!adminService.verifyPassword(oldPassword, admin.getPassword())) {
                log.warn("旧密码验证失败，管理员ID：{}", adminId);
                return R.fail("旧密码错误");
            }
            
            // 更新密码
            adminService.updatePassword(adminId, newPassword);
            
            log.info("管理员密码修改成功，ID：{}", adminId);
            return R.ok("密码修改成功");
            
        } catch (Exception e) {
            log.error("修改管理员密码异常，ID：{}", adminId, e);
            return R.fail("密码修改失败");
        }
    }
    
    /**
     * 更新管理员信息
     * 
     * @param adminId 管理员ID
     * @param admin 管理员信息
     * @return 更新结果
     */
    @PutMapping("/{adminId}")
    @Operation(summary = "更新管理员信息", description = "更新管理员的基本信息")
    public R<Void> updateAdmin(@PathVariable Long adminId, @Valid @RequestBody Admin admin) {
        log.info("更新管理员信息，ID：{}", adminId);
        
        try {
            admin.setId(adminId);
            adminService.updateAdmin(admin);
            
            log.info("管理员信息更新成功，ID：{}", adminId);
            return R.ok("管理员信息更新成功");
            
        } catch (Exception e) {
            log.error("更新管理员信息异常，ID：{}", adminId, e);
            return R.fail("管理员信息更新失败");
        }
    }
    
    /**
      * 启用/禁用管理员
      * 
      * @param adminId 管理员ID
      * @param status 状态：0-禁用，1-启用
      * @return 操作结果
      */
     @PutMapping("/{adminId}/status")
     @Operation(summary = "启用/禁用管理员", description = "修改管理员的启用状态")
     public R<Void> updateAdminStatus(@PathVariable Long adminId, @RequestParam Integer status) {
         log.info("修改管理员状态，ID：{}，状态：{}", adminId, status);
         
         try {
             adminService.updateAdminStatus(adminId, status);
             
             String statusText = status == 1 ? "启用" : "禁用";
             log.info("管理员状态修改成功，ID：{}，状态：{}", adminId, statusText);
             return R.ok("管理员" + statusText + "成功");
             
         } catch (Exception e) {
             log.error("修改管理员状态异常，ID：{}，状态：{}", adminId, status, e);
             return R.fail("管理员状态修改失败");
         }
     }
     
     /**
      * 分页查询管理员列表
      * 
      * @param page 页码
      * @param size 每页大小
      * @param username 用户名（可选）
      * @param realName 真实姓名（可选）
      * @param role 角色（可选）
      * @param status 状态（可选）
      * @return 管理员列表
      */
     @GetMapping("/list")
     @Operation(summary = "分页查询管理员列表", description = "根据条件分页查询管理员列表")
     public R<PageResult<AdminInfoResponse>> getAdminList(
             @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
             @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
             @Parameter(description = "用户名") @RequestParam(required = false) String username,
             @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
             @Parameter(description = "角色") @RequestParam(required = false) String role,
             @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
         log.info("分页查询管理员列表，页码：{}，每页大小：{}", page, size);
         
         try {
             PageResult<AdminInfoResponse> result = adminService.getAdminList(page, size, username, realName, role, status);
             
             log.info("查询管理员列表成功，总数：{}", result.getTotal());
             return R.ok(result);
             
         } catch (Exception e) {
             log.error("查询管理员列表异常", e);
             return R.fail("查询管理员列表失败");
         }
     }
     
     /**
      * 创建管理员
      * 
      * @param admin 管理员信息
      * @return 创建结果
      */
     @PostMapping
     @Operation(summary = "创建管理员", description = "创建新的管理员账户")
     public R<Void> createAdmin(@Valid @RequestBody Admin admin) {
         log.info("创建管理员，用户名：{}", admin.getUsername());
         
         try {
             // 检查用户名是否已存在
             Admin existingAdmin = adminService.findByUsername(admin.getUsername());
             if (existingAdmin != null) {
                 log.warn("管理员用户名已存在：{}", admin.getUsername());
                 return R.fail("用户名已存在");
             }
             
             adminService.createAdmin(admin);
             
             log.info("管理员创建成功，用户名：{}", admin.getUsername());
             return R.ok("管理员创建成功");
             
         } catch (Exception e) {
             log.error("创建管理员异常，用户名：{}", admin.getUsername(), e);
             return R.fail("管理员创建失败");
         }
     }
     
     /**
      * 删除管理员
      * 
      * @param adminId 管理员ID
      * @return 删除结果
      */
     @DeleteMapping("/{adminId}")
     @Operation(summary = "删除管理员", description = "删除指定的管理员账户")
     public R<Void> deleteAdmin(@Parameter(description = "管理员ID") @PathVariable @NotNull Long adminId) {
         log.info("删除管理员，ID：{}", adminId);
         
         try {
             // 检查管理员是否存在
             Admin admin = adminService.findById(adminId);
             if (admin == null) {
                 log.warn("管理员不存在，ID：{}", adminId);
                 return R.fail("管理员不存在");
             }
             
             // 不能删除超级管理员
             if (admin.isSuperAdmin()) {
                 log.warn("不能删除超级管理员，ID：{}", adminId);
                 return R.fail("不能删除超级管理员");
             }
             
             adminService.deleteAdmin(adminId);
             
             log.info("管理员删除成功，ID：{}", adminId);
             return R.ok("管理员删除成功");
             
         } catch (Exception e) {
             log.error("删除管理员异常，ID：{}", adminId, e);
             return R.fail("管理员删除失败");
         }
     }
    
    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
