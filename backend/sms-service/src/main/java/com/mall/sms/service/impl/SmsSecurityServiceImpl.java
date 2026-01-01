package com.mall.sms.service.impl;

import com.mall.sms.service.SmsRedisService;
import com.mall.sms.service.SmsBlacklistService;
import com.mall.sms.service.SmsSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短信安全服务实现类
 *
 * @author lingbai
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsSecurityServiceImpl implements SmsSecurityService {

    private final SmsRedisService redisService;
    private final SmsBlacklistService blacklistService;

    // 安全配置常量
    private static final int MAX_FAILED_ATTEMPTS = 5; // 最大失败次数
    private static final int BLOCK_DURATION_MINUTES = 30; // 封禁时长（分钟）
    private static final int BLACKLIST_THRESHOLD = 10; // 加入黑名单阈值

    @Override
    public boolean isIpTemporarilyBlocked(String ip) {
        try {
            String key = "sms:security:ip_block:" + ip;
            String blocked = redisService.get(key);
            boolean isBlocked = "1".equals(blocked);
            
            if (isBlocked) {
                log.warn("IP被暂时封禁 - IP: {}", ip);
            }
            
            return isBlocked;
        } catch (Exception e) {
            log.error("检查IP封禁状态失败 - IP: {}, 错误: {}", ip, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isPhoneTemporarilyBlocked(String phoneNumber) {
        try {
            String key = "sms:security:phone_block:" + phoneNumber;
            String blocked = redisService.get(key);
            boolean isBlocked = "1".equals(blocked);
            
            if (isBlocked) {
                log.warn("手机号被暂时封禁 - 手机号: {}", phoneNumber);
            }
            
            return isBlocked;
        } catch (Exception e) {
            log.error("检查手机号封禁状态失败 - 手机号: {}, 错误: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void recordFailedAttempt(String ip, String phoneNumber) {
        try {
            // 记录IP失败次数
            String ipKey = "sms:security:ip_failed:" + ip;
            String ipCountStr = redisService.get(ipKey);
            int ipCount = ipCountStr != null ? Integer.parseInt(ipCountStr) : 0;
            ipCount++;
            
            redisService.set(ipKey, String.valueOf(ipCount), 3600); // 1小时过期
            
            // 记录手机号失败次数
            String phoneKey = "sms:security:phone_failed:" + phoneNumber;
            String phoneCountStr = redisService.get(phoneKey);
            int phoneCount = phoneCountStr != null ? Integer.parseInt(phoneCountStr) : 0;
            phoneCount++;
            
            redisService.set(phoneKey, String.valueOf(phoneCount), 3600); // 1小时过期
            
            log.info("记录失败尝试 - IP: {} ({}次), 手机号: {} ({}次)", ip, ipCount, phoneNumber, phoneCount);
            
            // 检查是否需要暂时封禁
            if (ipCount >= MAX_FAILED_ATTEMPTS) {
                blockIp(ip);
            }
            
            if (phoneCount >= MAX_FAILED_ATTEMPTS) {
                blockPhone(phoneNumber);
            }
            
            // 检查是否需要加入黑名单
            checkAndAddToBlacklist(ip, phoneNumber);
            
        } catch (Exception e) {
            log.error("记录失败尝试异常 - IP: {}, 手机号: {}, 错误: {}", ip, phoneNumber, e.getMessage(), e);
        }
    }

    @Override
    public void clearFailedAttempts(String ip, String phoneNumber) {
        try {
            String ipKey = "sms:security:ip_failed:" + ip;
            String phoneKey = "sms:security:phone_failed:" + phoneNumber;
            
            redisService.delete(ipKey);
            redisService.delete(phoneKey);
            
            log.info("清除失败记录 - IP: {}, 手机号: {}", ip, phoneNumber);
        } catch (Exception e) {
            log.error("清除失败记录异常 - IP: {}, 手机号: {}, 错误: {}", ip, phoneNumber, e.getMessage(), e);
        }
    }

    @Override
    public void checkAndAddToBlacklist(String ip, String phoneNumber) {
        try {
            // 检查IP失败次数
            String ipKey = "sms:security:ip_failed:" + ip;
            String ipCountStr = redisService.get(ipKey);
            int ipCount = ipCountStr != null ? Integer.parseInt(ipCountStr) : 0;
            
            if (ipCount >= BLACKLIST_THRESHOLD) {
                blacklistService.addIpToBlacklist(ip, "频繁失败尝试，自动加入黑名单");
                log.warn("IP加入黑名单 - IP: {}, 失败次数: {}", ip, ipCount);
            }
            
            // 检查手机号失败次数
            String phoneKey = "sms:security:phone_failed:" + phoneNumber;
            String phoneCountStr = redisService.get(phoneKey);
            int phoneCount = phoneCountStr != null ? Integer.parseInt(phoneCountStr) : 0;
            
            if (phoneCount >= BLACKLIST_THRESHOLD) {
                blacklistService.addPhoneToBlacklist(phoneNumber, "频繁失败尝试，自动加入黑名单");
                log.warn("手机号加入黑名单 - 手机号: {}, 失败次数: {}", phoneNumber, phoneCount);
            }
            
        } catch (Exception e) {
            log.error("检查黑名单异常 - IP: {}, 手机号: {}, 错误: {}", ip, phoneNumber, e.getMessage(), e);
        }
    }

    /**
     * 封禁IP
     */
    private void blockIp(String ip) {
        try {
            String key = "sms:security:ip_block:" + ip;
            redisService.set(key, "1", BLOCK_DURATION_MINUTES * 60); // 转换为秒
            log.warn("IP被暂时封禁 - IP: {}, 时长: {}分钟", ip, BLOCK_DURATION_MINUTES);
        } catch (Exception e) {
            log.error("封禁IP失败 - IP: {}, 错误: {}", ip, e.getMessage(), e);
        }
    }

    /**
     * 封禁手机号
     */
    private void blockPhone(String phoneNumber) {
        try {
            String key = "sms:security:phone_block:" + phoneNumber;
            redisService.set(key, "1", BLOCK_DURATION_MINUTES * 60); // 转换为秒
            log.warn("手机号被暂时封禁 - 手机号: {}, 时长: {}分钟", phoneNumber, BLOCK_DURATION_MINUTES);
        } catch (Exception e) {
            log.error("封禁手机号失败 - 手机号: {}, 错误: {}", phoneNumber, e.getMessage(), e);
        }
    }
}