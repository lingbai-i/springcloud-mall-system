package com.mall.sms.service.impl;

import com.mall.sms.service.SmsRedisService;
import com.mall.sms.service.SmsService;
import com.mall.sms.service.SmsPushService;
import com.mall.sms.service.SmsBlacklistService;
import com.mall.sms.service.SmsLogService;
import com.mall.sms.service.SmsSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * 短信服务实现类
 *
 * @author SMS Service
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final SmsRedisService redisService;
    private final SmsPushService pushService;
    private final SmsBlacklistService blacklistService;
    private final SmsLogService logService;
    private final SmsSecurityService securityService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public boolean sendVerificationCode(String phoneNumber, String purpose, String clientIp) {
        try {
            // 检查黑名单
            if (blacklistService.isPhoneBlacklisted(phoneNumber)) {
                log.warn("手机号在黑名单中 - 手机号: {}", phoneNumber);
                logService.logSend(phoneNumber, null, purpose, clientIp, 0, "手机号在黑名单中", null);
                return false;
            }

            if (blacklistService.isIpBlacklisted(clientIp)) {
                log.warn("IP在黑名单中 - IP: {}", clientIp);
                logService.logSend(phoneNumber, null, purpose, clientIp, 0, "IP在黑名单中", null);
                return false;
            }

            // 检查安全封禁
            if (securityService.isIpTemporarilyBlocked(clientIp)) {
                log.warn("IP被暂时封禁 - IP: {}", clientIp);
                logService.logSend(phoneNumber, null, purpose, clientIp, 0, "IP被暂时封禁", null);
                return false;
            }

            if (securityService.isPhoneTemporarilyBlocked(phoneNumber)) {
                log.warn("手机号被暂时封禁 - 手机号: {}", phoneNumber);
                logService.logSend(phoneNumber, null, purpose, clientIp, 0, "手机号被暂时封禁", null);
                return false;
            }

            // 检查IP频率限制
            if (redisService.isIpRateLimited(clientIp)) {
                log.warn("IP频率限制 - IP: {}", clientIp);
                logService.logSend(phoneNumber, null, purpose, clientIp, 0, "IP频率限制", null);
                securityService.recordFailedAttempt(clientIp, phoneNumber);
                return false;
            }

            // 检查手机号频率限制
            if (redisService.isPhoneRateLimited(phoneNumber)) {
                log.warn("手机号频率限制 - 手机号: {}", phoneNumber);
                logService.logSend(phoneNumber, null, purpose, clientIp, 0, "手机号频率限制", null);
                securityService.recordFailedAttempt(clientIp, phoneNumber);
                return false;
            }

            // 生成验证码
            String code = generateVerificationCode();

            // 发送短信
            pushService.sendSms(phoneNumber, code, purpose);

            // 缓存验证码
            redisService.cacheVerificationCode(phoneNumber, code, purpose);

            // 设置频率限制
            redisService.setPhoneRateLimit(phoneNumber);
            redisService.incrementIpRequestCount(clientIp);

            // 记录成功日志
            logService.logSend(phoneNumber, code, purpose, clientIp, 1, null, "发送成功");

            log.info("验证码发送成功 - 手机号: {}, 用途: {}", phoneNumber, purpose);
            return true;
        } catch (Exception e) {
            log.error("发送验证码异常 - 手机号: {}, 用途: {}, 错误: {}", phoneNumber, purpose, e.getMessage(), e);
            logService.logSend(phoneNumber, null, purpose, clientIp, 0, "系统异常: " + e.getMessage(), null);
            securityService.recordFailedAttempt(clientIp, phoneNumber);
            return false;
        }
    }

    @Override
    public boolean verifyCode(String phoneNumber, String code, String purpose, String clientIp) {
        try {
            // 检查黑名单
            if (blacklistService.isPhoneBlacklisted(phoneNumber)) {
                log.warn("验证失败：手机号在黑名单中 - 手机号: {}", phoneNumber);
                securityService.recordFailedAttempt(clientIp, phoneNumber);
                return false;
            }

            if (blacklistService.isIpBlacklisted(clientIp)) {
                log.warn("验证失败：IP在黑名单中 - IP: {}", clientIp);
                securityService.recordFailedAttempt(clientIp, phoneNumber);
                return false;
            }

            // 检查安全封禁
            if (securityService.isIpTemporarilyBlocked(clientIp)) {
                log.warn("验证失败：IP被暂时封禁 - IP: {}", clientIp);
                return false;
            }

            if (securityService.isPhoneTemporarilyBlocked(phoneNumber)) {
                log.warn("验证失败：手机号被暂时封禁 - 手机号: {}", phoneNumber);
                return false;
            }

            // 从Redis获取验证码
            String cachedCode = redisService.getVerificationCode(phoneNumber, purpose);
            if (cachedCode == null) {
                log.warn("验证码不存在或已过期 - 手机号: {}, 用途: {}", phoneNumber, purpose);
                securityService.recordFailedAttempt(clientIp, phoneNumber);
                return false;
            }

            // 验证码比较
            if (!cachedCode.equals(code)) {
                log.warn("验证码错误 - 手机号: {}, 用途: {}", phoneNumber, purpose);
                securityService.recordFailedAttempt(clientIp, phoneNumber);
                return false;
            }

            // 验证成功，删除验证码
            redisService.deleteVerificationCode(phoneNumber, purpose);
            
            // 清除失败记录
            securityService.clearFailedAttempts(clientIp, phoneNumber);

            log.info("验证码验证成功 - 手机号: {}, 用途: {}", phoneNumber, purpose);
            return true;
        } catch (Exception e) {
            log.error("验证码验证异常 - 手机号: {}, 用途: {}, 错误: {}", phoneNumber, purpose, e.getMessage(), e);
            securityService.recordFailedAttempt(clientIp, phoneNumber);
            return false;
        }
    }

    @Override
    public String generateVerificationCode() {
        // 生成6位随机数字验证码
        int code = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}