package com.mall.admin.service;

import com.mall.common.core.domain.PageResult;

import java.util.Map;

/**
 * 用户管理服务接口
 * 
 * @author lingbai
 * @since 2025-01-09
 */
public interface UserManagementService {

    /**
     * 查询用户列表
     * 
     * @param page    页码
     * @param size    每页大小
     * @param keyword 关键词
     * @param status  状态
     * @return 用户列表
     */
    PageResult<Map<String, Object>> getUserList(Integer page, Integer size, String keyword, Integer status);

    /**
     * 获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    Map<String, Object> getUserDetail(Long userId);

    /**
     * 禁用用户
     * 
     * @param userId  用户ID
     * @param adminId 操作管理员ID
     */
    void disableUser(Long userId, Long adminId);

    /**
     * 启用用户
     * 
     * @param userId  用户ID
     * @param adminId 操作管理员ID
     */
    void enableUser(Long userId, Long adminId);

    /**
     * 获取用户统计数据
     * 
     * @return 统计数据
     */
    Map<String, Object> getUserStats();
}
