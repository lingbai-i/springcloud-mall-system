package com.mall.sms.service.impl;

import com.mall.sms.entity.SmsLog;
import com.mall.sms.mapper.SmsLogMapper;
import com.mall.sms.service.SmsLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信日志服务实现类
 *
 * @author lingbai
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsLogServiceImpl implements SmsLogService {

    private final SmsLogMapper smsLogMapper;

    @Override
    public void logSend(String phoneNumber, String code, String purpose, String clientIp, 
                        Integer status, String errorMessage, String response) {
        SmsLog smsLog = new SmsLog();
        smsLog.setPhoneNumber(phoneNumber);
        smsLog.setCode(code);
        smsLog.setPurpose(purpose);
        smsLog.setClientIp(clientIp);
        smsLog.setStatus(status);
        smsLog.setErrorMessage(errorMessage);
        smsLog.setResponse(response);
        
        save(smsLog);
    }

    @Override
    public void save(SmsLog smsLog) {
        try {
            smsLogMapper.insert(smsLog);
            log.debug("短信日志保存成功 - 手机号: {}, 用途: {}", smsLog.getPhoneNumber(), smsLog.getPurpose());
        } catch (Exception e) {
            log.error("短信日志保存失败 - 手机号: {}, 用途: {}, 错误: {}", 
                    smsLog.getPhoneNumber(), smsLog.getPurpose(), e.getMessage(), e);
        }
    }
}