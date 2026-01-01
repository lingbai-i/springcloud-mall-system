package com.mall.admin.controller;

import com.mall.admin.service.MerchantManagementService;
import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 商家管理控制器
 * 
 * @author lingbai
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/merchants")
@RequiredArgsConstructor
public class MerchantManagementController {
    
    private final MerchantManagementService merchantManagementService;
    
    /**
     * 查询商家列表
     */
    @GetMapping("")
    public R<PageResult<Map<String, Object>>> getMerchantList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status) {
        
        PageResult<Map<String, Object>> result = merchantManagementService.getMerchantList(page, size, keyword, status);
        return R.ok(result);
    }
    
    /**
     * 获取商家详情
     */
    @GetMapping("/{id}")
    public R<Map<String, Object>> getMerchantDetail(@PathVariable("id") Long merchantId) {
        Map<String, Object> result = merchantManagementService.getMerchantDetail(merchantId);
        return R.ok(result);
    }
    
    /**
     * 审核商家
     */
    @PutMapping("/{id}/audit")
    public R<Void> auditMerchant(
            @PathVariable("id") Long merchantId,
            @RequestParam("approved") Boolean approved,
            @RequestParam(value = "reason", required = false) String reason,
            HttpServletRequest request) {
        
        Long adminId = (Long) request.getAttribute("adminId");
        merchantManagementService.auditMerchant(merchantId, approved, reason, adminId);
        return R.ok();
    }
}
