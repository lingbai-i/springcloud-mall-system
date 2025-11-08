package com.mall.sms.service;

/**
 * SMS服务接口
 *
 * @author SMS Service
 * @since 2024-01-01
 */
public interface SmsService {

    /**
     * 发送验证码
     *
     * @param phoneNumber 手机号
     * @param purpose 用途
     * @param clientIp 客户端IP
     * @return 发送结果
     */
    boolean sendVerificationCode(String phoneNumber, String purpose, String clientIp);

    /**
     * 验证验证码
     *
     * @param phoneNumber 手机号
     * @param code 验证码
     * @param purpose 用途
     * @param clientIp 客户端IP
     * @return 验证结果
     */
    boolean verifyCode(String phoneNumber, String code, String purpose, String clientIp);

    /**
     * 生成验证码
     *
     * @return 6位数字验证码
     */
    String generateVerificationCode();
}