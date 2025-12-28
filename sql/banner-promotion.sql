-- 轮播图投流系统数据库表
-- V1.0 2025-12-28: 初始版本，包含轮播图申请表和统计表
-- 数据库: mall_merchant

-- ============================================
-- 轮播图申请表 (banner_application)
-- 用途: 存储商家提交的轮播图投流申请
-- ============================================
CREATE TABLE IF NOT EXISTS `banner_application` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `merchant_id` BIGINT(20) NOT NULL COMMENT '商家ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '轮播图图片URL',
    `title` VARCHAR(100) NOT NULL COMMENT '标题（最多100字）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述（最多500字）',
    `target_url` VARCHAR(500) NOT NULL COMMENT '跳转链接',
    `start_date` DATE NOT NULL COMMENT '展示开始日期',
    `end_date` DATE NOT NULL COMMENT '展示结束日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝, EXPIRED-已过期, CANCELLED-已取消',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `reviewer_id` BIGINT(20) DEFAULT NULL COMMENT '审核人ID',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序权重（数值越大越靠前）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '删除标志（0-存在，1-删除）',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`) COMMENT '商家ID索引，用于商家查询自己的申请',
    KEY `idx_status` (`status`) COMMENT '状态索引，用于按状态筛选',
    KEY `idx_start_date` (`start_date`) COMMENT '开始日期索引，用于自动上架查询',
    KEY `idx_end_date` (`end_date`) COMMENT '结束日期索引，用于自动下架查询',
    KEY `idx_review_time` (`review_time`) COMMENT '审核时间索引，用于优先级排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图申请表';

-- ============================================
-- 轮播图统计表 (banner_statistics)
-- 用途: 存储轮播图的曝光和点击统计数据（按日汇总）
-- ============================================
CREATE TABLE IF NOT EXISTS `banner_statistics` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    `banner_id` BIGINT(20) NOT NULL COMMENT '轮播图申请ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `impressions` INT(11) DEFAULT 0 COMMENT '曝光次数',
    `clicks` INT(11) DEFAULT 0 COMMENT '点击次数',
    `unique_impressions` INT(11) DEFAULT 0 COMMENT '独立曝光数（去重UV）',
    `unique_clicks` INT(11) DEFAULT 0 COMMENT '独立点击数（去重UV）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_banner_date` (`banner_id`, `stat_date`) COMMENT '轮播图ID+日期唯一索引，防止重复统计',
    KEY `idx_banner_id` (`banner_id`) COMMENT '轮播图ID索引，用于查询单个轮播图的统计',
    KEY `idx_stat_date` (`stat_date`) COMMENT '统计日期索引，用于按日期范围查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图统计表';

-- ============================================
-- 索引说明
-- ============================================
-- banner_application 表索引:
--   1. idx_merchant_id: 商家查询自己的申请列表
--   2. idx_status: 管理员按状态筛选申请（如查询所有待审核）
--   3. idx_start_date: 定时任务查询需要自动上架的轮播图
--   4. idx_end_date: 定时任务查询需要自动下架的轮播图
--   5. idx_review_time: 当活跃轮播图超过10个时，按审核时间排序优先展示
--
-- banner_statistics 表索引:
--   1. uk_banner_date: 确保每个轮播图每天只有一条统计记录
--   2. idx_banner_id: 查询单个轮播图的历史统计数据
--   3. idx_stat_date: 按日期范围查询统计数据

-- ============================================
-- 状态流转说明
-- ============================================
-- PENDING (待审核) -> APPROVED (已通过) : 管理员审核通过
-- PENDING (待审核) -> REJECTED (已拒绝) : 管理员审核拒绝
-- PENDING (待审核) -> CANCELLED (已取消) : 商家主动取消
-- REJECTED (已拒绝) -> PENDING (待审核) : 商家修改后重新提交
-- APPROVED (已通过) -> EXPIRED (已过期) : 展示结束日期已过（系统自动更新）
