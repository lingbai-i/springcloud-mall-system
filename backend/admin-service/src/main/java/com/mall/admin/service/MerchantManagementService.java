package com.mall.admin.service;

import com.mall.admin.domain.dto.MerchantApprovalRequest;
import com.mall.admin.domain.dto.MerchantQueryRequest;
import com.mall.admin.domain.entity.Merchant;
import com.mall.common.core.domain.PageResult;

import java.util.Map;

/**
 * 商家管理服务接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
public interface MerchantManagementService {
    
    /**
     * 分页查询商家列表
     * 
     * @param request 查询请求
     * @return 商家分页列表
     */
    PageResult<Merchant> getMerchantList(MerchantQueryRequest request);
    
    /**
     * 根据ID获取商家详情
     * 
     * @param merchantId 商家ID
     * @return 商家详情
     */
    Merchant getMerchantDetail(Long merchantId);
    
    /**
     * 审核商家
     * 
     * @param request 审核请求
     * @param adminId 操作管理员ID
     */
    void approveMerchant(MerchantApprovalRequest request, Long adminId);
    
    /**
     * 禁用商家
     * 
     * @param merchantId 商家ID
     * @param adminId 操作管理员ID
     */
    void disableMerchant(Long merchantId, Long adminId);
    
    /**
     * 启用商家
     * 
     * @param merchantId 商家ID
     * @param adminId 操作管理员ID
     */
    void enableMerchant(Long merchantId, Long adminId);
    
    /**
     * 删除商家
     * 
     * @param merchantId 商家ID
     * @param adminId 操作管理员ID
     */
    void deleteMerchant(Long merchantId, Long adminId);
    
    /**
     * 导出商家数据
     * 
     * @param params 导出参数
     * @return 导出文件字节数组
     */
    byte[] exportMerchants(Map<String, Object> params);
}