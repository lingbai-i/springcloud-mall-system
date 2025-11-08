package com.mall.sms.service.impl;

import com.mall.sms.service.SmsRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * SMS Redis服务实现类
 *
 * @author SMS Service
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsRedisServiceImpl implements SmsRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String VERIFICATION_CODE_PREFIX = "sms:code:";
    private static final String PHONE_RATE_LIMIT_PREFIX = "sms:phone_rate:";
    private static final String IP_RATE_LIMIT_PREFIX = "sms:ip_rate:";
    private static final int CODE_EXPIRE_SECONDS = 180; // 3分钟
    private static final int PHONE_RATE_LIMIT_SECONDS = 60; // 1分钟
    private static final int IP_RATE_LIMIT_SECONDS = 60; // 1分钟

    @Override
    public void cacheVerificationCode(String phoneNumber, String code, String purpose) {
        String key = buildVerificationCodeKey(phoneNumber, purpose);
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.debug("缓存验证码 - 手机号: {}, 用途: {}, 过期时间: {}秒", phoneNumber, purpose, CODE_EXPIRE_SECONDS);
    }

    @Override
    public String getVerificationCode(String phoneNumber, String purpose) {
        String key = buildVerificationCodeKey(phoneNumber, purpose);
        String code = redisTemplate.opsForValue().get(key);
        log.debug("获取验证码 - 手机号: {}, 用途: {}, 结果: {}", phoneNumber, purpose, code != null ? "存在" : "不存在");
        return code;
    }

    @Override
    public void deleteVerificationCode(String phoneNumber, String purpose) {
        String key = buildVerificationCodeKey(phoneNumber, purpose);
        redisTemplate.delete(key);
        log.debug("删除验证码 - 手机号: {}, 用途: {}", phoneNumber, purpose);
    }

    @Override
    public boolean isPhoneRateLimited(String phoneNumber) {
        String key = buildPhoneRateLimitKey(phoneNumber);
        Boolean exists = redisTemplate.hasKey(key);
        boolean isLimited = Boolean.TRUE.equals(exists);
        log.debug("检查手机号频率限制 - 手机号: {}, 结果: {}", phoneNumber, isLimited ? "被限制" : "未限制");
        return isLimited;
    }

    @Override
    public void setPhoneRateLimit(String phoneNumber) {
        String key = buildPhoneRateLimitKey(phoneNumber);
        redisTemplate.opsForValue().set(key, "1", PHONE_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        log.debug("设置手机号频率限制 - 手机号: {}, 间隔: {}秒", phoneNumber, PHONE_RATE_LIMIT_SECONDS);
    }

    @Override
    public boolean isIpRateLimited(String ip) {
        String key = buildIpRateLimitKey(ip);
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr == null) {
            return false;
        }
        
        try {
            int count = Integer.parseInt(countStr);
            // IP每分钟最多10次请求
            boolean isLimited = count >= 10;
            log.debug("检查IP频率限制 - IP: {}, 当前请求数: {}, 结果: {}", ip, count, isLimited ? "被限制" : "未限制");
            return isLimited;
        } catch (NumberFormatException e) {
            log.warn("解析IP请求计数失败 - IP: {}, 值: {}", ip, countStr);
            return false;
        }
    }

    @Override
    public void incrementIpRequestCount(String ip) {
        String key = buildIpRateLimitKey(ip);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // 第一次设置过期时间
            redisTemplate.expire(key, IP_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);
        }
        log.debug("增加IP请求计数 - IP: {}, 当前计数: {}", ip, count);
    }

    @Override
    public void set(String key, String value, int expireSeconds) {
        redisTemplate.opsForValue().set(key, value, expireSeconds, TimeUnit.SECONDS);
        log.debug("设置Redis键值 - 键: {}, 过期时间: {}秒", key, expireSeconds);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("删除Redis键 - 键: {}", key);
    }

    /**
     * 构建验证码缓存键
     */
    private String buildVerificationCodeKey(String phoneNumber, String purpose) {
        return VERIFICATION_CODE_PREFIX + phoneNumber + ":" + purpose;
    }

    /**
     * 构建手机号频率限制键
     */
    private String buildPhoneRateLimitKey(String phoneNumber) {
        return PHONE_RATE_LIMIT_PREFIX + phoneNumber;
    }

    /**
     * 构建IP频率限制键
     */
    private String buildIpRateLimitKey(String ip) {
        return IP_RATE_LIMIT_PREFIX + ip;
    }
}