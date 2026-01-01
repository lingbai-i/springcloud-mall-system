package com.mall.sms.service;

/**
 * SMS推送服务接口
 *
 * @author lingbai
 * @since 2025-09-23
 */
public interface SmsPushService {

    /**
     * 发送短信（验证码类型）
     *
     * @param phoneNumber 手机号
     * @param code 验证码
     * @param purpose 用途
     */
    void sendSms(String phoneNumber, String code, String purpose);

    /**
     * 发送通知短信（非验证码类型）
     * 用于发送审核结果、订单状态等通知类短信
     *
     * @param phoneNumber 手机号
     * @param message 短信内容
     * @param purpose 用途（如 MERCHANT_APPROVAL_PASS, MERCHANT_APPROVAL_REJECT）
     */
    void sendNotificationSms(String phoneNumber, String message, String purpose);
}