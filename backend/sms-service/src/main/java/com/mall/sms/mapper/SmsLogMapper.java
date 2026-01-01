package com.mall.sms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.sms.entity.SmsLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信日志Mapper
 *
 * @author lingbai
 * @since 2024-01-01
 */
@Mapper
public interface SmsLogMapper extends BaseMapper<SmsLog> {
}