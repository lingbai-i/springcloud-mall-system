package com.mall.admin.service.impl;

import com.mall.admin.client.MerchantServiceClient;
import com.mall.admin.domain.entity.AuditLog;
import com.mall.admin.exception.BusinessException;
import com.mall.admin.mapper.AuditLogMapper;
import com.mall.admin.service.MerchantManagementService;
import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 商家管理服务实现
 * 
 * @author system
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantManagementServiceImpl implements MerchantManagementService {
    
    private final MerchantServiceClient merchantServiceClient;
    private final AuditLogMapper auditLogMapper;
    
    @Override
    public PageResult<Map<String, Object>> getMerchantList(Integer page, Integer size, String keyword, Integer status) {
        R<PageResult<Map<String, Object>>> result = merchantServiceClient.getMerchantList(page, size, keyword, status);
        if (!result.isSuccess()) {
            throw new BusinessException("查询商家列表失败: " + result.getMessage());
        }
        return result.getData();
    }
    
    @Override
    public Map<String, Object> getMerchantDetail(Long merchantId) {
        R<Map<String, Object>> result = merchantServiceClient.getMerchantDetail(merchantId);
        if (!result.isSuccess()) {
            throw new BusinessException("查询商家详情失败: " + result.getMessage());
        }
        return result.getData();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditMerchant(Long merchantId, Boolean approved, String reason, Long adminId) {
        R<Void> result = merchantServiceClient.auditMerchant(merchantId, approved, reason);
        if (!result.isSuccess()) {
            throw new BusinessException("审核商家失败: " + result.getMessage());
        }
        
        // 记录审计日志
        String operationDesc = approved ? "通过商家审核" : "拒绝商家审核";
        if (reason != null && !reason.isEmpty()) {
            operationDesc += ": " + reason;
        }
        recordAuditLog(adminId, "商家审核", "merchant", merchantId, operationDesc, true);
        
        log.info("审核商家成功: merchantId={}, approved={}, adminId={}", merchantId, approved, adminId);
    }
    
    /**
     * 记录审计日志
     */
    private void recordAuditLog(Long adminId, String operationType, String resourceType, 
                                 Long resourceId, String operationDesc, boolean success) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAdminId(adminId);
        auditLog.setOperationType(operationType);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(String.valueOf(resourceId));
        auditLog.setOperationDesc(operationDesc);
        auditLog.setStatus(success ? 1 : 0);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(auditLog);
    }
}
