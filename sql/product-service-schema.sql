-- ============================================
-- Product Service 数据库表结构
-- 用于商品服务架构重构
-- @author lingbai
-- @since 2025-12-01
-- ============================================

USE `mall_product`;

-- 商品表（统一商品数据源）
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `description` text COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '销售价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价/市场价',
  `cost_price` decimal(10,2) DEFAULT NULL COMMENT '成本价',
  `stock` int DEFAULT 0 COMMENT '库存数量',
  `stock_warning` int DEFAULT 10 COMMENT '库存预警阈值',
  `sales` int DEFAULT 0 COMMENT '销量',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `is_recommend` tinyint(1) DEFAULT 0 COMMENT '是否推荐',
  `is_new` tinyint(1) DEFAULT 0 COMMENT '是否新品',
  `is_hot` tinyint(1) DEFAULT 0 COMMENT '是否热销',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `brand_name` varchar(100) DEFAULT NULL COMMENT '品牌名称',
  `main_image` varchar(500) DEFAULT NULL COMMENT '主图URL',
  `detail_images` text DEFAULT NULL COMMENT '详情图URL，逗号分隔',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT 0 COMMENT '删除标志',
  PRIMARY KEY (`id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_sales` (`sales` DESC),
  INDEX `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 库存操作日志表
CREATE TABLE IF NOT EXISTS `stock_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型：DEDUCT-扣减，RESTORE-恢复，UPDATE-更新',
  `quantity` int NOT NULL COMMENT '变更数量',
  `before_stock` int NOT NULL COMMENT '变更前库存',
  `after_stock` int NOT NULL COMMENT '变更后库存',
  `order_no` varchar(50) DEFAULT NULL COMMENT '关联订单号',
  `reason` varchar(200) DEFAULT NULL COMMENT '变更原因',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_order_no` (`order_no`),
  INDEX `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存操作日志表';

-- 价格变更历史表
CREATE TABLE IF NOT EXISTS `price_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `old_price` decimal(10,2) NOT NULL COMMENT '旧价格',
  `new_price` decimal(10,2) NOT NULL COMMENT '新价格',
  `reason` varchar(200) DEFAULT NULL COMMENT '变更原因',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格变更历史表';
