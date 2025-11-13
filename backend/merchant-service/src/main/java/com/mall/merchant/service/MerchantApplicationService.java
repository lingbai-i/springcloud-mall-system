package com.mall.merchant.service;

import com.mall.merchant.domain.dto.MerchantApplicationDTO;
import com.mall.merchant.domain.entity.MerchantApplication;
import com.mall.merchant.domain.vo.MerchantApplicationVO;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * 商家申请服务接口
 * 
 * @author system
 * @since 2025-11-11
 */
public interface MerchantApplicationService {
    
    /**
     * 提交商家入驻申请
     * 
     * @param applicationDTO 申请信息
     * @return 申请ID
     */
    Long submitApplication(MerchantApplicationDTO applicationDTO);
    
    /**
     * 查询申请详情
     * 
     * @param id 申请ID
     * @return 申请详情
     */
    MerchantApplication getApplicationDetail(Long id);
    
    /**
     * 获取申请列表（支持分页和筛选）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param status 审批状态
     * @param keyword 搜索关键词
     * @return 申请列表
     */
    Page<MerchantApplicationVO> getApplicationList(Integer page, Integer size, Integer status, String keyword);
    
    /**
     * 审核申请
     * 
     * @param id 申请ID
     * @param approved 是否通过
     * @param reason 审核原因
     * @param adminId 管理员ID
     * @param adminName 管理员名称
     */
    void auditApplication(Long id, Boolean approved, String reason, Long adminId, String adminName);
    
    /**
     * 获取申请统计
     * 
     * @return 统计数据
     */
    Map<String, Object> getApplicationStats();
}

