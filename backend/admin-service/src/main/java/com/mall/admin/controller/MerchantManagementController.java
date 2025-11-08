package com.mall.admin.controller;

import com.mall.admin.domain.dto.MerchantApprovalRequest;
import com.mall.admin.domain.dto.MerchantQueryRequest;
import com.mall.admin.domain.entity.Merchant;
import com.mall.admin.service.MerchantManagementService;
import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * 商家管理控制器
 * 处理管理员对商家的管理操作
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/merchants")
@RequiredArgsConstructor
@Tag(name = "商家管理", description = "管理员对商家的管理操作接口")
public class MerchantManagementController {
    
    private final MerchantManagementService merchantManagementService;
    
    /**
     * 分页查询商家列表
     * 
     * @param request 查询请求参数
     * @return 分页商家列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询商家列表", description = "管理员分页查询商家列表，支持多条件筛选")
    public R<PageResult<Merchant>> getMerchantList(@Valid MerchantQueryRequest request) {
        log.info("管理员查询商家列表，参数：{}", request);
        
        try {
            PageResult<Merchant> result = merchantManagementService.getMerchantList(request);
            
            log.info("商家列表查询成功，总数：{}", result.getTotal());
            return R.success(result);
            
        } catch (Exception e) {
            log.error("查询商家列表异常", e);
            return R.error("查询商家列表失败");
        }
    }
    
    /**
     * 获取商家详情
     * 
     * @param merchantId 商家ID
     * @return 商家详情
     */
    @GetMapping("/{merchantId}")
    @Operation(summary = "获取商家详情", description = "根据商家ID获取商家详细信息")
    public R<Merchant> getMerchantDetail(@PathVariable Long merchantId) {
        log.info("管理员查询商家详情，商家ID：{}", merchantId);
        
        try {
            Merchant merchant = merchantManagementService.getMerchantDetail(merchantId);
            
            if (merchant == null) {
                log.warn("商家不存在，ID：{}", merchantId);
                return R.error("商家不存在");
            }
            
            log.info("商家详情查询成功，商家ID：{}", merchantId);
            return R.success(merchant);
            
        } catch (Exception e) {
            log.error("查询商家详情异常，商家ID：{}", merchantId, e);
            return R.error("查询商家详情失败");
        }
    }
    
    /**
     * 审核商家
     * 
     * @param request 审核请求参数
     * @return 操作结果
     */
    @PostMapping("/approve")
    @Operation(summary = "审核商家", description = "管理员审核商家入驻申请")
    public R<Void> approveMerchant(@Valid @RequestBody MerchantApprovalRequest request) {
        log.info("管理员审核商家，请求：{}", request);
        
        try {
            // 获取当前管理员ID
            Long adminId = getCurrentAdminId();
            
            merchantManagementService.approveMerchant(request, adminId);
            
            String result = request.getApproved() ? "通过" : "拒绝";
            log.info("商家审核成功，商家ID：{}，审核结果：{}，操作管理员ID：{}", 
                    request.getMerchantId(), result, adminId);
            return R.success("商家审核" + result + "成功");
            
        } catch (Exception e) {
            log.error("审核商家异常，商家ID：{}", request.getMerchantId(), e);
            return R.error("审核商家失败");
        }
    }
    
    /**
     * 禁用商家
     * 
     * @param merchantId 商家ID
     * @return 操作结果
     */
    @PutMapping("/{merchantId}/disable")
    @Operation(summary = "禁用商家", description = "管理员禁用指定商家")
    public R<Void> disableMerchant(@PathVariable Long merchantId) {
        log.info("管理员禁用商家，商家ID：{}", merchantId);
        
        try {
            // 获取当前管理员ID
            Long adminId = getCurrentAdminId();
            
            merchantManagementService.disableMerchant(merchantId, adminId);
            
            log.info("商家禁用成功，商家ID：{}，操作管理员ID：{}", merchantId, adminId);
            return R.success("商家禁用成功");
            
        } catch (Exception e) {
            log.error("禁用商家异常，商家ID：{}", merchantId, e);
            return R.error("禁用商家失败");
        }
    }
    
    /**
     * 启用商家
     * 
     * @param merchantId 商家ID
     * @return 操作结果
     */
    @PutMapping("/{merchantId}/enable")
    @Operation(summary = "启用商家", description = "管理员启用指定商家")
    public R<Void> enableMerchant(@PathVariable Long merchantId) {
        log.info("管理员启用商家，商家ID：{}", merchantId);
        
        try {
            // 获取当前管理员ID
            Long adminId = getCurrentAdminId();
            
            merchantManagementService.enableMerchant(merchantId, adminId);
            
            log.info("商家启用成功，商家ID：{}，操作管理员ID：{}", merchantId, adminId);
            return R.success("商家启用成功");
            
        } catch (Exception e) {
            log.error("启用商家异常，商家ID：{}", merchantId, e);
            return R.error("启用商家失败");
        }
    }
    
    /**
     * 删除商家
     * 
     * @param merchantId 商家ID
     * @return 操作结果
     */
    @DeleteMapping("/{merchantId}")
    @Operation(summary = "删除商家", description = "管理员删除指定商家")
    public R<Void> deleteMerchant(@PathVariable Long merchantId) {
        log.info("管理员删除商家，商家ID：{}", merchantId);
        
        try {
            // 获取当前管理员ID
            Long adminId = getCurrentAdminId();
            
            merchantManagementService.deleteMerchant(merchantId, adminId);
            
            log.info("商家删除成功，商家ID：{}，操作管理员ID：{}", merchantId, adminId);
            return R.success("商家删除成功");
            
        } catch (Exception e) {
            log.error("删除商家异常，商家ID：{}", merchantId, e);
            return R.error("删除商家失败");
        }
    }
    
    /**
     * 导出商家数据
     * 
     * @param request 查询请求参数
     * @return 导出文件
     */
    @GetMapping("/export")
    @Operation(summary = "导出商家数据", description = "导出商家数据为Excel文件")
    public ResponseEntity<byte[]> exportMerchants(@Valid MerchantQueryRequest request) {
        log.info("管理员导出商家数据，参数：{}", request);
        
        try {
            // 构建导出参数
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("request", request);
            
            byte[] data = merchantManagementService.exportMerchants(params);
            
            if (data.length == 0) {
                log.warn("导出商家数据为空");
                return ResponseEntity.noContent().build();
            }
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "merchants.xlsx");
            headers.setContentLength(data.length);
            
            log.info("商家数据导出成功，文件大小：{} bytes", data.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(data);
            
        } catch (Exception e) {
            log.error("导出商家数据异常", e);
            return ResponseEntity.internalServerError().build();
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