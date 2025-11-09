package com.mall.admin.service;

import com.mall.admin.domain.dto.UserQueryRequest;
import com.mall.common.core.domain.PageResult;

import java.util.Map;

/**
 * 用户管理服务接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
public interface UserManagementService {
    
    /**
     * 分页查询用户列表
     * 
     * @param request 查询请求
     * @return 用户分页列表
     */
    PageResult<Map<String, Object>> getUserList(UserQueryRequest request);
    
    /**
     * 根据ID获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    Map<String, Object> getUserDetail(Long userId);
    
    /**
     * 禁用用户
     * 
     * @param userId 用户ID
     * @param adminId 操作管理员ID
     */
    void disableUser(Long userId, Long adminId);
    
    /**
     * 启用用户
     * 
     * @param userId 用户ID
     * @param adminId 操作管理员ID
     */
    void enableUser(Long userId, Long adminId);
    
    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @param adminId 操作管理员ID
     */
    void deleteUser(Long userId, Long adminId);
    
    /**
     * 获取用户统计数据
     * 
     * @param params 查询参数
     * @return 统计数据
     */
    Map<String, Object> getUserStats(Map<String, Object> params);
}