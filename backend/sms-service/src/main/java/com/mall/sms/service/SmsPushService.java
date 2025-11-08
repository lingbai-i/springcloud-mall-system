package com.mall.sms.service;

/**
 * SMS推送服务接口
 *
 * @author SMS Service
 * @since 2024-01-01
 */
public interface SmsPushService {

    /**
     * 发送短信
     *
     * @param phoneNumber 手机号
     * @param code 验证码
     * @param purpose 用途
     */
    void sendSms(String phoneNumber, String code, String purpose);
}