package com.mall.sms.service;

/**
 * 短信黑名单服务接口
 *
 * @author SMS Service
 * @since 2024-01-01
 */
public interface SmsBlacklistService {

    /**
     * 检查手机号是否在黑名单中
     *
     * @param phoneNumber 手机号
     * @return 是否在黑名单中
     */
    boolean isPhoneBlacklisted(String phoneNumber);

    /**
     * 检查IP是否在黑名单中
     *
     * @param ip IP地址
     * @return 是否在黑名单中
     */
    boolean isIpBlacklisted(String ip);

    /**
     * 添加手机号到黑名单
     *
     * @param phoneNumber 手机号
     * @param reason 原因
     */
    void addPhoneToBlacklist(String phoneNumber, String reason);

    /**
     * 添加IP到黑名单
     *
     * @param ip IP地址
     * @param reason 原因
     */
    void addIpToBlacklist(String ip, String reason);
}