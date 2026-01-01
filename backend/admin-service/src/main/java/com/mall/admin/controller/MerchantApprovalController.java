package com.mall.admin.controller;

import com.mall.admin.domain.dto.ApprovalRequest;
import com.mall.admin.service.MerchantApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商家审批控制器（管理员）
 * 
 * @author lingbai
 * @since 2025-11-11
 */
@Slf4j
@RestController
@RequestMapping({"/api/admin/merchants", "/admin/merchants"})
@RequiredArgsConstructor
public class MerchantApprovalController {
    
    private final MerchantApprovalService approvalService;
    
    /**
     * 获取商家申请列表
     */
    @GetMapping("/applications")
    public ResponseEntity<Map<String, Object>> getApplications(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword) {
        
        log.info("管理员查询申请列表 - page: {}, size: {}, status: {}", page, size, status);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Page<Map<String, Object>> applications = approvalService.getPendingApplications(page, size, status, keyword);
            
            Map<String, Object> data = new HashMap<>();
            data.put("total", applications.getTotalElements());
            data.put("page", page);
            data.put("size", size);
            data.put("records", applications.getContent());
            
            response.put("code", 200);
            response.put("success", true);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("查询申请列表失败", e);
            response.put("code", 500);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取申请详情
     */
    @GetMapping("/applications/{id}")
    public ResponseEntity<Map<String, Object>> getApplicationDetail(@PathVariable Long id) {
        
        log.info("查询申请详情 - ID: {}", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> application = approvalService.getApplicationDetail(id);
            
            response.put("code", 200);
            response.put("success", true);
            response.put("data", application);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("查询申请详情失败", e);
            response.put("code", 500);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 审批商家申请
     */
    @PutMapping("/applications/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("审批商家申请 - ID: {}, 结果: {}", id, request.getApproved() ? "通过" : "拒绝");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 从请求中获取管理员信息（实际应从JWT token中获取）
            Long adminId = 1L; // TODO: 从token中获取真实管理员ID
            String adminUsername = "admin"; // TODO: 从token中获取真实管理员用户名
            String ipAddress = getClientIp(httpRequest);
            
            Map<String, Object> result = approvalService.approveApplication(
                    id, request, adminId, adminUsername, ipAddress);
            
            response.put("code", 200);
            response.put("success", true);
            response.put("message", request.getApproved() ? "审批通过成功" : "已拒绝申请");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("审批操作失败", e);
            response.put("code", 500);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}







