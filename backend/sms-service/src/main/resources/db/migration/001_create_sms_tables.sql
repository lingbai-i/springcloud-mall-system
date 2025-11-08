-- SMS服务数据库初始化脚本
-- 创建短信日志表和黑名单表

-- 创建短信日志表
CREATE TABLE IF NOT EXISTS sms_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    phone_number VARCHAR(20) NOT NULL COMMENT '手机号',
    code VARCHAR(10) COMMENT '验证码',
    purpose VARCHAR(50) NOT NULL COMMENT '用途',
    client_ip VARCHAR(45) COMMENT '客户端IP',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-失败，1-成功',
    error_message VARCHAR(500) COMMENT '错误信息',
    response TEXT COMMENT '第三方响应',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone_number (phone_number),
    INDEX idx_client_ip (client_ip),
    INDEX idx_purpose (purpose),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信日志表';

-- 创建短信黑名单表
CREATE TABLE IF NOT EXISTS sms_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：phone-手机号，ip-IP地址',
    value VARCHAR(50) NOT NULL COMMENT '值',
    reason VARCHAR(200) COMMENT '加入黑名单原因',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    expire_time DATETIME COMMENT '过期时间，NULL表示永久',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_type_value (type, value),
    INDEX idx_type (type),
    INDEX idx_value (value),
    INDEX idx_status (status),
    INDEX idx_expire_time (expire_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信黑名单表';

-- 插入一些初始黑名单数据（示例）
INSERT IGNORE INTO sms_blacklist (type, value, reason, status) VALUES
('phone', '10000000000', '测试黑名单手机号', 1),
('ip', '127.0.0.1', '本地测试IP', 0);

-- 创建索引优化查询性能
-- 短信日志表的复合索引
CREATE INDEX IF NOT EXISTS idx_phone_purpose_time ON sms_log (phone_number, purpose, create_time);
CREATE INDEX IF NOT EXISTS idx_ip_time ON sms_log (client_ip, create_time);

-- 黑名单表的复合索引
CREATE INDEX IF NOT EXISTS idx_type_status_expire ON sms_blacklist (type, status, expire_time);