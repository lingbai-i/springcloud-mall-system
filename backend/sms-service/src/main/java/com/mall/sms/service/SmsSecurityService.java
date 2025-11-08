package com.mall.sms.service;

/**
 * 短信安全服务接口
 *
 * @author SMS Service
 * @since 2024-01-01
 */
public interface SmsSecurityService {

    /**
     * 检查IP是否被暂时封禁
     *
     * @param ip IP地址
     * @return 是否被封禁
     */
    boolean isIpTemporarilyBlocked(String ip);

    /**
     * 检查手机号是否被暂时封禁
     *
     * @param phoneNumber 手机号
     * @return 是否被封禁
     */
    boolean isPhoneTemporarilyBlocked(String phoneNumber);

    /**
     * 记录失败尝试
     *
     * @param ip IP地址
     * @param phoneNumber 手机号
     */
    void recordFailedAttempt(String ip, String phoneNumber);

    /**
     * 清除失败记录
     *
     * @param ip IP地址
     * @param phoneNumber 手机号
     */
    void clearFailedAttempts(String ip, String phoneNumber);

    /**
     * 检查是否需要添加到黑名单
     *
     * @param ip IP地址
     * @param phoneNumber 手机号
     */
    void checkAndAddToBlacklist(String ip, String phoneNumber);
}