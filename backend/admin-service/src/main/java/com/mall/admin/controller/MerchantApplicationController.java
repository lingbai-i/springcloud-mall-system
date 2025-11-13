package com.mall.admin.controller;

import com.mall.admin.client.MerchantServiceClient;
import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家申请管理控制器
 * 
 * @author system
 * @since 2025-11-12
 */
@Slf4j
@RestController
/**
 * V1.1 2025-11-12T18:56:16+08:00：修正路由前缀，与网关 StripPrefix=2 对齐，将
 * 类级映射从 "/admin/merchants/applications" 改为 "/merchants/applications"，避免 404。
 */
@RequestMapping("/merchants/applications")
@RequiredArgsConstructor
public class MerchantApplicationController {
    
    private final MerchantServiceClient merchantServiceClient;
    
    /**
     * 获取申请列表
     */
    @GetMapping("")
    public R<Map<String, Object>> getApplicationList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword) {
        
        log.info("管理员查询商家申请列表 - page: {}, size: {}, status: {}, keyword: {}", 
                page, size, status, keyword);
        
        try {
            R<Map<String, Object>> result = merchantServiceClient.getApplicationList(page, size, status, keyword);
            return result;
        } catch (Exception e) {
            log.error("查询申请列表失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取申请详情
     */
    @GetMapping("/{id}")
    public R<Map<String, Object>> getApplicationDetail(@PathVariable("id") Long id) {
        log.info("管理员查询申请详情 - id: {}", id);
        
        try {
            R<Map<String, Object>> result = merchantServiceClient.getApplicationDetail(id);
            return result;
        } catch (Exception e) {
            log.error("查询申请详情失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 审批申请
     */
    @PutMapping("/{id}/approve")
    public R<Void> approveApplication(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> data) {
        
        Boolean approved = (Boolean) data.get("approved");
        String reason = (String) data.get("reason");
        Long adminId = 1L; // TODO: 从当前登录用户获取
        String adminName = "管理员"; // TODO: 从当前登录用户获取
        
        log.info("管理员审批申请 - id: {}, approved: {}, adminId: {}", id, approved, adminId);
        
        try {
            R<Void> result = merchantServiceClient.auditApplication(id, approved, reason, adminId, adminName);
            return result;
        } catch (Exception e) {
            log.error("审批申请失败", e);
            return R.fail("审批失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取申请统计
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> getApplicationStats() {
        log.info("管理员查询申请统计");
        
        try {
            R<Map<String, Object>> result = merchantServiceClient.getApplicationStats();
            return result;
        } catch (Exception e) {
            log.error("查询统计失败", e);
            return R.fail("查询失败: " + e.getMessage());
        }
    }
}








