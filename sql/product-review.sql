-- 商品评价表
-- V2.0 2025-12-28: 新增多维度评分字段（description_rating, service_rating, logistics_rating）
-- V2.1 2025-12-28: 移除测试数据，仅保留表结构
CREATE TABLE IF NOT EXISTS `product_review` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) DEFAULT NULL COMMENT '用户名称',
    `user_avatar` VARCHAR(500) DEFAULT NULL COMMENT '用户头像',
    `order_id` BIGINT(20) DEFAULT NULL COMMENT '订单ID',
    `rating` INT(1) NOT NULL DEFAULT 5 COMMENT '综合评分（1-5星）',
    `description_rating` INT(1) DEFAULT 5 COMMENT '描述相符评分（1-5星）',
    `service_rating` INT(1) DEFAULT 5 COMMENT '卖家服务评分（1-5星）',
    `logistics_rating` INT(1) DEFAULT 5 COMMENT '物流服务评分（1-5星）',
    `content` TEXT COMMENT '评价内容',
    `images` VARCHAR(2000) DEFAULT NULL COMMENT '评价图片（多张图片用逗号分隔）',
    `anonymous` TINYINT(1) DEFAULT 0 COMMENT '是否匿名（0-否，1-是）',
    `like_count` INT(11) DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态（0-待审核，1-已发布，2-已隐藏）',
    `merchant_reply` TEXT COMMENT '商家回复',
    `merchant_reply_time` DATETIME DEFAULT NULL COMMENT '商家回复时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- 如果表已存在，添加新字段
ALTER TABLE `product_review` 
    ADD COLUMN IF NOT EXISTS `description_rating` INT(1) DEFAULT 5 COMMENT '描述相符评分（1-5星）' AFTER `rating`,
    ADD COLUMN IF NOT EXISTS `service_rating` INT(1) DEFAULT 5 COMMENT '卖家服务评分（1-5星）' AFTER `description_rating`,
    ADD COLUMN IF NOT EXISTS `logistics_rating` INT(1) DEFAULT 5 COMMENT '物流服务评分（1-5星）' AFTER `service_rating`;

-- 清理测试数据（删除所有使用测试图片URL的评论）
DELETE FROM `product_review` WHERE `images` LIKE '%picsum.photos%' OR `user_avatar` LIKE '%dicebear.com%';

-- 注意：生产环境中不应包含测试数据，所有评论应来自真实用户提交
