package com.mall.sms.service;

import com.mall.sms.entity.SmsLog;

/**
 * 短信日志服务接口
 *
 * @author SMS Service
 * @since 2024-01-01
 */
public interface SmsLogService {

    /**
     * 记录发送日志
     *
     * @param phoneNumber 手机号
     * @param code 验证码
     * @param purpose 用途
     * @param clientIp 客户端IP
     * @param status 状态
     * @param errorMessage 错误信息
     * @param response 第三方响应
     */
    void logSend(String phoneNumber, String code, String purpose, String clientIp, 
                 Integer status, String errorMessage, String response);

    /**
     * 保存日志
     *
     * @param smsLog 日志对象
     */
    void save(SmsLog smsLog);
}