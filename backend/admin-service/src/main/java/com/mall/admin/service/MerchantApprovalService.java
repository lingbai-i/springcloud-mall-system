package com.mall.admin.service;

import com.mall.admin.domain.dto.ApprovalRequest;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * 商家审批服务接口
 * 
 * @author system
 * @since 2025-11-11
 */
public interface MerchantApprovalService {
    
    /**
     * 获取待审批申请列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param status 审批状态
     * @param keyword 搜索关键词
     * @return 申请列表
     */
    Page<Map<String, Object>> getPendingApplications(Integer page, Integer size, Integer status, String keyword);
    
    /**
     * 审批商家申请
     * 
     * @param applicationId 申请ID
     * @param request 审批请求
     * @param adminId 管理员ID
     * @param adminUsername 管理员用户名
     * @param ipAddress 操作IP
     * @return 审批结果
     */
    Map<String, Object> approveApplication(Long applicationId, ApprovalRequest request, 
                                           Long adminId, String adminUsername, String ipAddress);
    
    /**
     * 获取申请详情
     * 
     * @param applicationId 申请ID
     * @return 申请详情
     */
    Map<String, Object> getApplicationDetail(Long applicationId);
}







