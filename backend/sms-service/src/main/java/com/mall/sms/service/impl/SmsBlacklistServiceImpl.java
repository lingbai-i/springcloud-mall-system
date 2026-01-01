package com.mall.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.sms.entity.SmsBlacklist;
import com.mall.sms.mapper.SmsBlacklistMapper;
import com.mall.sms.service.SmsBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 短信黑名单服务实现类
 *
 * @author lingbai
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsBlacklistServiceImpl implements SmsBlacklistService {

    private final SmsBlacklistMapper blacklistMapper;

    @Override
    public boolean isPhoneBlacklisted(String phoneNumber) {
        return isBlacklisted("phone", phoneNumber);
    }

    @Override
    public boolean isIpBlacklisted(String ip) {
        return isBlacklisted("ip", ip);
    }

    @Override
    public void addPhoneToBlacklist(String phoneNumber, String reason) {
        addToBlacklist("phone", phoneNumber, reason);
    }

    @Override
    public void addIpToBlacklist(String ip, String reason) {
        addToBlacklist("ip", ip, reason);
    }

    /**
     * 检查是否在黑名单中
     */
    private boolean isBlacklisted(String type, String value) {
        try {
            LambdaQueryWrapper<SmsBlacklist> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SmsBlacklist::getType, type)
                   .eq(SmsBlacklist::getValue, value)
                   .eq(SmsBlacklist::getStatus, 1)
                   .and(w -> w.isNull(SmsBlacklist::getExpireTime)
                           .or()
                           .gt(SmsBlacklist::getExpireTime, LocalDateTime.now()));
            
            long count = blacklistMapper.selectCount(wrapper);
            boolean isBlacklisted = count > 0;
            
            log.debug("检查黑名单 - 类型: {}, 值: {}, 结果: {}", type, value, isBlacklisted ? "在黑名单中" : "不在黑名单中");
            return isBlacklisted;
        } catch (Exception e) {
            log.error("检查黑名单失败 - 类型: {}, 值: {}, 错误: {}", type, value, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 添加到黑名单
     */
    private void addToBlacklist(String type, String value, String reason) {
        try {
            // 检查是否已存在
            if (isBlacklisted(type, value)) {
                log.warn("黑名单已存在 - 类型: {}, 值: {}", type, value);
                return;
            }
            
            SmsBlacklist blacklist = new SmsBlacklist();
            blacklist.setType(type);
            blacklist.setValue(value);
            blacklist.setReason(reason);
            blacklist.setStatus(1);
            
            blacklistMapper.insert(blacklist);
            log.info("添加黑名单成功 - 类型: {}, 值: {}, 原因: {}", type, value, reason);
        } catch (Exception e) {
            log.error("添加黑名单失败 - 类型: {}, 值: {}, 原因: {}, 错误: {}", 
                    type, value, reason, e.getMessage(), e);
        }
    }
}