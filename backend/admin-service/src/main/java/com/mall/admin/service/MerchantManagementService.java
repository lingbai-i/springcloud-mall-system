package com.mall.admin.service;

import com.mall.common.core.domain.PageResult;

import java.util.Map;

/**
 * 商家管理服务接口
 * 
 * @author system
 * @since 2025-01-09
 */
public interface MerchantManagementService {
    
    /**
     * 查询商家列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词
     * @param status 状态
     * @return 商家列表
     */
    PageResult<Map<String, Object>> getMerchantList(Integer page, Integer size, String keyword, Integer status);
    
    /**
     * 获取商家详情
     * 
     * @param merchantId 商家ID
     * @return 商家详情
     */
    Map<String, Object> getMerchantDetail(Long merchantId);
    
    /**
     * 审核商家
     * 
     * @param merchantId 商家ID
     * @param approved 是否通过
     * @param reason 审核备注
     * @param adminId 操作管理员ID
     */
    void auditMerchant(Long merchantId, Boolean approved, String reason, Long adminId);
}
