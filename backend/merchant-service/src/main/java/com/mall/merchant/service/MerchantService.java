package com.mall.merchant.service;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.Merchant;

import java.util.Map;

/**
 * 商家服务接口
 * 提供商家相关的业务逻辑处理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
public interface MerchantService {
    
    /**
     * 商家注册
     * 
     * @param merchant 商家信息
     * @return 注册结果
     */
    R<Void> register(Merchant merchant);
    
    /**
     * 商家登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含token等信息
     */
    R<Map<String, Object>> login(String username, String password);
    
    /**
     * 商家登出
     * 
     * @param merchantId 商家ID
     * @return 登出结果
     */
    R<Void> logout(Long merchantId);
    
    /**
     * 根据ID获取商家信息
     * 
     * @param merchantId 商家ID
     * @return 商家信息
     */
    R<Merchant> getMerchantById(Long merchantId);
    
    /**
     * 根据用户名获取商家信息
     * 
     * @param username 用户名
     * @return 商家信息
     */
    R<Merchant> getMerchantByUsername(String username);
    
    /**
     * 更新商家基本信息
     * 
     * @param merchant 商家信息
     * @return 更新结果
     */
    R<Void> updateMerchantInfo(Merchant merchant);
    
    /**
     * 更新商家密码
     * 
     * @param merchantId 商家ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 更新结果
     */
    R<Void> updatePassword(Long merchantId, String oldPassword, String newPassword);
    
    /**
     * 更新商家头像
     * 
     * @param merchantId 商家ID
     * @param avatar 头像URL
     * @return 更新结果
     */
    R<Void> updateAvatar(Long merchantId, String avatar);
    
    /**
     * 提交商家认证信息
     * 
     * @param merchantId 商家ID
     * @param merchant 认证信息
     * @return 提交结果
     */
    R<Void> submitAuthentication(Long merchantId, Merchant merchant);
    
    /**
     * 获取商家认证状态
     * 
     * @param merchantId 商家ID
     * @return 认证状态信息
     */
    R<Map<String, Object>> getAuthenticationStatus(Long merchantId);
    
    /**
     * 分页查询商家列表（管理员使用）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param shopName 店铺名称（可选）
     * @param merchantType 商家类型（可选）
     * @param auditStatus 审核状态（可选）
     * @param status 商家状态（可选）
     * @return 商家分页列表
     */
    R<PageResult<Merchant>> getMerchantList(Integer page, Integer size, String shopName, 
                                           Integer merchantType, Integer auditStatus, Integer status);
    
    /**
     * 审核商家
     * 
     * @param merchantId 商家ID
     * @param auditStatus 审核状态：2-通过，3-拒绝
     * @param auditRemark 审核备注
     * @return 审核结果
     */
    R<Void> auditMerchant(Long merchantId, Integer auditStatus, String auditRemark);
    
    /**
     * 禁用商家
     * 
     * @param merchantId 商家ID
     * @param reason 禁用原因
     * @return 禁用结果
     */
    R<Void> disableMerchant(Long merchantId, String reason);
    
    /**
     * 启用商家
     * 
     * @param merchantId 商家ID
     * @return 启用结果
     */
    R<Void> enableMerchant(Long merchantId);
    
    /**
     * 删除商家
     * 
     * @param merchantId 商家ID
     * @return 删除结果
     */
    R<Void> deleteMerchant(Long merchantId);
    
    /**
     * 获取商家统计数据
     * 
     * @return 统计数据
     */
    R<Map<String, Object>> getMerchantStatistics();
    
    /**
     * 导出商家数据
     * 
     * @param shopName 店铺名称（可选）
     * @param merchantType 商家类型（可选）
     * @param auditStatus 审核状态（可选）
     * @param status 商家状态（可选）
     * @return 导出数据
     */
    R<byte[]> exportMerchantData(String shopName, Integer merchantType, Integer auditStatus, Integer status);
    
    /**
     * 更新最后登录信息
     * 
     * @param merchantId 商家ID
     * @param loginIp 登录IP
     * @return 更新结果
     */
    R<Void> updateLastLoginInfo(Long merchantId, String loginIp);
    
    /**
     * 验证密码
     * 
     * @param merchantId 商家ID
     * @param password 密码
     * @return 验证结果
     */
    R<Boolean> verifyPassword(Long merchantId, String password);
    
    /**
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return 是否可用
     */
    R<Boolean> checkUsernameAvailable(String username);
    
    /**
     * 检查店铺名称是否可用
     * 
     * @param shopName 店铺名称
     * @return 是否可用
     */
    R<Boolean> checkShopNameAvailable(String shopName);
    
    /**
     * 检查手机号是否可用
     * 
     * @param phone 手机号
     * @return 是否可用
     */
    R<Boolean> checkPhoneAvailable(String phone);
    
    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱
     * @return 是否可用
     */
    R<Boolean> checkEmailAvailable(String email);
    
    /**
     * 重置密码
     * 
     * @param phone 手机号
     * @param verifyCode 验证码
     * @param newPassword 新密码
     * @return 重置结果
     */
    R<Void> resetPassword(String phone, String verifyCode, String newPassword);
    
    /**
     * 发送验证码
     * 
     * @param phone 手机号
     * @param type 验证码类型：1-注册，2-重置密码，3-修改手机号
     * @return 发送结果
     */
    R<Void> sendVerifyCode(String phone, Integer type);
}