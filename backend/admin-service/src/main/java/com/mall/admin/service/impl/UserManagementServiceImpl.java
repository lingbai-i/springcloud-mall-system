package com.mall.admin.service.impl;

import com.mall.admin.client.UserServiceClient;
import com.mall.admin.domain.entity.AuditLog;
import com.mall.admin.exception.BusinessException;
import com.mall.admin.mapper.AuditLogMapper;
import com.mall.admin.service.UserManagementService;
import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户管理服务实现
 * 
 * @author system
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {
    
    private final UserServiceClient userServiceClient;
    private final AuditLogMapper auditLogMapper;
    
    @Override
    public PageResult<Map<String, Object>> getUserList(Integer page, Integer size, String keyword, Integer status) {
        R<PageResult<Map<String, Object>>> result = userServiceClient.getUserList(page, size, keyword, status);
        if (!result.isSuccess()) {
            throw new BusinessException("查询用户列表失败: " + result.getMessage());
        }
        return result.getData();
    }
    
    @Override
    public Map<String, Object> getUserDetail(Long userId) {
        R<Map<String, Object>> result = userServiceClient.getUserDetail(userId);
        if (!result.isSuccess()) {
            throw new BusinessException("查询用户详情失败: " + result.getMessage());
        }
        return result.getData();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long userId, Long adminId) {
        R<Void> result = userServiceClient.disableUser(userId);
        if (!result.isSuccess()) {
            throw new BusinessException("禁用用户失败: " + result.getMessage());
        }
        
        // 记录审计日志
        recordAuditLog(adminId, "禁用用户", "user", userId, "禁用用户", true);
        
        log.info("禁用用户成功: userId={}, adminId={}", userId, adminId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long userId, Long adminId) {
        R<Void> result = userServiceClient.enableUser(userId);
        if (!result.isSuccess()) {
            throw new BusinessException("启用用户失败: " + result.getMessage());
        }
        
        // 记录审计日志
        recordAuditLog(adminId, "启用用户", "user", userId, "启用用户", true);
        
        log.info("启用用户成功: userId={}, adminId={}", userId, adminId);
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
