-- 短信验证码服务数据库表结构
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS mall_sms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mall_sms;

-- 短信验证码记录表
CREATE TABLE IF NOT EXISTS sms_verification_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    mobile VARCHAR(20) NOT NULL COMMENT '手机号码',
    code VARCHAR(10) NOT NULL COMMENT '验证码',
    service_name VARCHAR(50) NOT NULL DEFAULT 'baichatmall' COMMENT '服务名称',
    purpose VARCHAR(50) NOT NULL COMMENT '验证码用途（register/login/reset_password等）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    used_time DATETIME NULL COMMENT '使用时间',
    ip_address VARCHAR(45) NULL COMMENT '请求IP地址',
    user_agent VARCHAR(500) NULL COMMENT '用户代理',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_mobile_purpose (mobile, purpose),
    INDEX idx_mobile_status (mobile, status),
    INDEX idx_expire_time (expire_time),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信验证码记录表';

-- 短信发送记录表
CREATE TABLE IF NOT EXISTS sms_send_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    mobile VARCHAR(20) NOT NULL COMMENT '手机号码',
    content TEXT NOT NULL COMMENT '短信内容',
    service_name VARCHAR(50) NOT NULL DEFAULT 'baichatmall' COMMENT '服务名称',
    purpose VARCHAR(50) NOT NULL COMMENT '发送目的',
    send_status TINYINT NOT NULL DEFAULT 0 COMMENT '发送状态：0-发送中，1-发送成功，2-发送失败',
    response_code VARCHAR(20) NULL COMMENT '第三方响应码',
    response_message TEXT NULL COMMENT '第三方响应消息',
    cost_time INT NULL COMMENT '发送耗时（毫秒）',
    ip_address VARCHAR(45) NULL COMMENT '请求IP地址',
    user_agent VARCHAR(500) NULL COMMENT '用户代理',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_mobile (mobile),
    INDEX idx_send_status (send_status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信发送记录表';

-- 频率限制记录表
CREATE TABLE IF NOT EXISTS sms_rate_limit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    mobile VARCHAR(20) NOT NULL COMMENT '手机号码',
    purpose VARCHAR(50) NOT NULL COMMENT '验证码用途',
    request_count INT NOT NULL DEFAULT 1 COMMENT '请求次数',
    last_request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后请求时间',
    reset_time DATETIME NOT NULL COMMENT '重置时间',
    ip_address VARCHAR(45) NULL COMMENT '请求IP地址',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_mobile_purpose (mobile, purpose),
    INDEX idx_reset_time (reset_time),
    INDEX idx_last_request_time (last_request_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信频率限制记录表';