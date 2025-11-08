package com.mall.admin.service;

import com.mall.admin.domain.dto.AdminLoginRequest;
import com.mall.admin.domain.entity.Admin;
import com.mall.admin.domain.vo.AdminInfoResponse;
import com.mall.common.core.domain.PageResult;

/**
 * 管理员服务接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
public interface AdminService {
    
    /**
     * 管理员登录
     * 
     * @param request 登录请求
     * @return JWT token
     */
    String login(AdminLoginRequest request);
    
    /**
     * 根据用户名查找管理员
     * 
     * @param username 用户名
     * @return 管理员信息
     */
    Admin findByUsername(String username);
    
    /**
     * 根据ID查找管理员
     * 
     * @param id 管理员ID
     * @return 管理员信息
     */
    Admin findById(Long id);
    
    /**
     * 获取管理员信息
     * 
     * @param adminId 管理员ID
     * @return 管理员信息响应
     */
    AdminInfoResponse getAdminInfo(Long adminId);
    
    /**
     * 更新最后登录信息
     * 
     * @param adminId 管理员ID
     * @param ip 登录IP
     */
    void updateLastLoginInfo(Long adminId, String ip);
    
    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密密码
     * @return 是否匹配
     */
    boolean verifyPassword(String rawPassword, String encodedPassword);
    
    /**
     * 更新管理员密码
     * 
     * @param adminId 管理员ID
     * @param newPassword 新密码
     */
    void updatePassword(Long adminId, String newPassword);
    
    /**
     * 更新管理员信息
     * 
     * @param admin 管理员信息
     */
    void updateAdmin(Admin admin);
    
    /**
     * 更新管理员状态
     * 
     * @param adminId 管理员ID
     * @param status 状态
     */
    void updateAdminStatus(Long adminId, Integer status);
    
    /**
     * 分页查询管理员列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param username 用户名
     * @param realName 真实姓名
     * @param role 角色
     * @param status 状态
     * @return 管理员列表
     */
    PageResult<AdminInfoResponse> getAdminList(Integer page, Integer size, String username, String realName, String role, Integer status);
    
    /**
     * 创建管理员
     * 
     * @param admin 管理员信息
     */
    void createAdmin(Admin admin);
    
    /**
     * 删除管理员
     * 
     * @param adminId 管理员ID
     */
    void deleteAdmin(Long adminId);
}