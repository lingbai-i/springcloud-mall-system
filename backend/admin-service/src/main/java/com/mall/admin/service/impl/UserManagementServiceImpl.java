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
 * @author lingbai
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

    @Override
    public Map<String, Object> getUserStats() {
        try {
            log.info("开始调用用户服务获取统计数据");
            R<Map<String, Object>> result = userServiceClient.getUserStatistics();
            log.info("用户服务返回结果: success={}, message={}, data={}",
                    result != null ? result.isSuccess() : null,
                    result != null ? result.getMessage() : null,
                    result != null ? result.getData() : null);

            if (result == null || !result.isSuccess()) {
                String errorMsg = result != null ? result.getMessage() : "服务调用返回空值";
                log.error("获取用户统计数据失败: {}", errorMsg);
                throw new BusinessException("获取用户统计数据失败: " + errorMsg);
            }

            Map<String, Object> data = result.getData();
            if (data == null) {
                log.warn("用户统计数据为空，返回默认值");
                data = new java.util.HashMap<>();
                data.put("total", 0);
                data.put("active", 0);
                data.put("pending", 0);
                data.put("disabled", 0);
            }

            return data;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用用户服务异常", e);
            throw new BusinessException("系统异常，请联系管理员: " + e.getMessage());
        }
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
