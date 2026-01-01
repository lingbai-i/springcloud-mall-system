package com.mall.sms.service;

/**
 * SMS Redis服务接口
 *
 * @author lingbai
 * @since 2025-09-22
 */
public interface SmsRedisService {

    /**
     * 缓存验证码
     *
     * @param phoneNumber 手机号
     * @param code 验证码
     * @param purpose 用途
     */
    void cacheVerificationCode(String phoneNumber, String code, String purpose);

    /**
     * 获取验证码
     *
     * @param phoneNumber 手机号
     * @param purpose 用途
     * @return 验证码
     */
    String getVerificationCode(String phoneNumber, String purpose);

    /**
     * 删除验证码
     *
     * @param phoneNumber 手机号
     * @param purpose 用途
     */
    void deleteVerificationCode(String phoneNumber, String purpose);

    /**
     * 检查手机号是否被频率限制
     *
     * @param phoneNumber 手机号
     * @return 是否被限制
     */
    boolean isPhoneRateLimited(String phoneNumber);

    /**
     * 设置手机号频率限制
     *
     * @param phoneNumber 手机号
     */
    void setPhoneRateLimit(String phoneNumber);

    /**
     * 检查IP是否被限制
     *
     * @param ip IP地址
     * @return 是否被限制
     */
    boolean isIpRateLimited(String ip);

    /**
     * 增加IP请求计数
     *
     * @param ip IP地址
     */
    void incrementIpRequestCount(String ip);

    /**
     * 通用设置键值
     *
     * @param key 键
     * @param value 值
     * @param expireSeconds 过期时间（秒）
     */
    void set(String key, String value, int expireSeconds);

    /**
     * 通用获取值
     *
     * @param key 键
     * @return 值
     */
    String get(String key);

    /**
     * 通用删除键
     *
     * @param key 键
     */
    void delete(String key);
}