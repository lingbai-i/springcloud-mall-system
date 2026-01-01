package com.mall.sms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短信黑名单实体
 *
 * @author lingbai
 * @since 2024-01-01
 */
@Data
@TableName("sms_blacklist")
public class SmsBlacklist {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 黑名单类型：phone-手机号，ip-IP地址
     */
    private String type;

    /**
     * 黑名单值
     */
    private String value;

    /**
     * 原因
     */
    private String reason;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}