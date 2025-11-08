package com.mall.admin.client;

import com.mall.common.core.domain.R;
import com.mall.admin.domain.entity.Merchant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商家服务客户端
 * 用于调用商家服务的相关接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@FeignClient(name = "merchant-service", path = "/api/merchants")
public interface MerchantClient {
    
    /**
     * 分页查询商家列表
     * 
     * @param params 查询参数
     * @return 商家列表
     */
    @GetMapping("/list")
    R<Map<String, Object>> getMerchantList(@RequestParam Map<String, Object> params);
    
    /**
     * 根据ID获取商家详情
     * 
     * @param merchantId 商家ID
     * @return 商家详情
     */
    @GetMapping("/{merchantId}")
    R<Merchant> getMerchantDetail(@PathVariable("merchantId") Long merchantId);
    
    /**
     * 审核商家
     * 
     * @param merchantId 商家ID
     * @param params 审核参数
     * @return 操作结果
     */
    @PutMapping("/{merchantId}/approve")
    R<Void> approveMerchant(@PathVariable("merchantId") Long merchantId, @RequestBody Map<String, Object> params);
    
    /**
     * 禁用商家
     * 
     * @param merchantId 商家ID
     * @return 操作结果
     */
    @PutMapping("/{merchantId}/disable")
    R<Void> disableMerchant(@PathVariable("merchantId") Long merchantId);
    
    /**
     * 启用商家
     * 
     * @param merchantId 商家ID
     * @return 操作结果
     */
    @PutMapping("/{merchantId}/enable")
    R<Void> enableMerchant(@PathVariable("merchantId") Long merchantId);
    
    /**
     * 删除商家
     * 
     * @param merchantId 商家ID
     * @return 操作结果
     */
    @DeleteMapping("/{merchantId}")
    R<Void> deleteMerchant(@PathVariable("merchantId") Long merchantId);
    
    /**
     * 导出商家数据
     * 
     * @param params 查询参数
     * @return 导出数据
     */
    @GetMapping("/export")
    R<byte[]> exportMerchants(@RequestParam Map<String, Object> params);
}