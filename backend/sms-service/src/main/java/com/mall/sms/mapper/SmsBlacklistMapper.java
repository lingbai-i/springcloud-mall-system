package com.mall.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.sms.entity.SmsBlacklist;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信黑名单Mapper
 *
 * @author SMS Service
 * @since 2024-01-01
 */
@Mapper
public interface SmsBlacklistMapper extends BaseMapper<SmsBlacklist> {
}