-- ========================================
-- 用户收藏表初始化脚本
-- 创建时间: 2025-12-30
-- 作者: Kiro
-- 版本: 1.0
-- ========================================

USE `mall_user`;

-- 用户收藏表
CREATE TABLE IF NOT EXISTS `user_favorites` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(255) COMMENT '商品名称（冗余）',
    `product_image` VARCHAR(500) COMMENT '商品图片（冗余）',
    `product_price` DECIMAL(10,2) COMMENT '商品价格（冗余）',
    `product_desc` VARCHAR(500) COMMENT '商品描述（冗余）',
    `original_price` DECIMAL(10,2) COMMENT '商品原价（冗余）',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    `version` INT DEFAULT 0 COMMENT '版本号（乐观锁）',
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';
