-- ========================================
-- 在线商城数据库初始化脚本
-- 创建时间: 2025-10-21 23:01:58
-- 作者: lingbai
-- 版本: 1.2
-- 修改日志:
-- V1.2 2025-12-30: 修正 products 表结构以匹配 Product 实体类 (brand_name, detail_images, is_recommend 等)
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `mall_gateway` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_product` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_cart` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_payment` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_merchant` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_admin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_sms` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 显示创建的数据库
SHOW DATABASES LIKE 'mall_%';

-- 使用用户数据库创建基础表结构
USE `mall_user`;

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `email` VARCHAR(100) NULL COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `password_set_time` DATETIME NULL COMMENT '密码设置时间',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    `birthday` DATE COMMENT '生日',
    `bio` VARCHAR(500) COMMENT '个人简介',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `version` INT DEFAULT 0 COMMENT '版本号（乐观锁）',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_phone` (`phone`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户地址表
CREATE TABLE IF NOT EXISTS `user_addresses` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `province_code` VARCHAR(20) DEFAULT NULL COMMENT '省份代码',
    `city_code` VARCHAR(20) DEFAULT NULL COMMENT '城市代码',
    `district_code` VARCHAR(20) DEFAULT NULL COMMENT '区县代码',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `district` VARCHAR(50) NOT NULL COMMENT '区县',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `postal_code` VARCHAR(10) COMMENT '邮政编码',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址: 0-否, 1-是',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

-- 使用商品数据库
USE `mall_product`;

-- 商品分类表
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
    `level` TINYINT DEFAULT 1 COMMENT '分类层级',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `icon` VARCHAR(255) COMMENT '分类图标',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `description` VARCHAR(500) COMMENT '分类描述',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_level` (`level`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 商品表 (修正结构以匹配 Product.java)
CREATE TABLE IF NOT EXISTS `products` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    `name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `brand_name` VARCHAR(100) COMMENT '品牌名称',
    `description` TEXT COMMENT '商品描述',
    `main_image` VARCHAR(255) COMMENT '主图片',
    `detail_images` JSON COMMENT '商品详情图片列表',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `original_price` DECIMAL(10,2) COMMENT '原价',
    `cost_price` DECIMAL(10,2) COMMENT '成本价',
    `stock` INT DEFAULT 0 COMMENT '库存',
    `stock_warning` INT DEFAULT 10 COMMENT '库存预警值',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `weight` DECIMAL(8,2) COMMENT '重量(kg)',
    `unit` VARCHAR(20) COMMENT '单位',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `is_recommend` TINYINT DEFAULT 0 COMMENT '是否推荐',
    `is_new` TINYINT DEFAULT 0 COMMENT '是否新品',
    `is_hot` TINYINT DEFAULT 0 COMMENT '是否热销',
    `sort_order` INT DEFAULT 0 COMMENT '排序值',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标志: 0-未删除, 1-已删除',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_merchant_id` (`merchant_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_price` (`price`),
    INDEX `idx_sales` (`sales`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 商品SKU表
CREATE TABLE IF NOT EXISTS `product_skus` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `product_id` BIGINT NOT NULL,
    `sku_code` VARCHAR(64),
    `sku_name` VARCHAR(255),
    `price` DECIMAL(10,2),
    `original_price` DECIMAL(10,2),
    `cost_price` DECIMAL(10,2),
    `price_version` BIGINT,
    `stock` INT,
    `stock_warning` INT,
    `weight` INT,
    `barcode` VARCHAR(64),
    `image` VARCHAR(255),
    `spec_values` JSON,
    `status` TINYINT,
    `sales` INT DEFAULT 0,
    `sort` INT DEFAULT 0,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- 商品评价表
CREATE TABLE IF NOT EXISTS `product_review` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `product_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `user_name` VARCHAR(50),
    `user_avatar` VARCHAR(255),
    `order_id` BIGINT,
    `rating` INT,
    `description_rating` INT,
    `service_rating` INT,
    `logistics_rating` INT,
    `content` TEXT,
    `images` TEXT,
    `anonymous` TINYINT DEFAULT 0,
    `like_count` INT DEFAULT 0,
    `status` TINYINT DEFAULT 0,
    `merchant_reply` TEXT,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- 评价点赞表
CREATE TABLE IF NOT EXISTS `review_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `review_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价点赞表';

-- 库存日志表
CREATE TABLE IF NOT EXISTS `stock_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `product_id` BIGINT NOT NULL,
    `sku_id` BIGINT,
    `before_stock` INT,
    `after_stock` INT,
    `quantity` INT,
    `operation_type` VARCHAR(20),
    `reason` VARCHAR(255),
    `order_no` VARCHAR(64),
    `operator_id` BIGINT,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存日志表';

-- 价格历史表
CREATE TABLE IF NOT EXISTS `price_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `product_id` BIGINT NOT NULL,
    `sku_id` BIGINT,
    `old_price` DECIMAL(10,2),
    `new_price` DECIMAL(10,2),
    `price_type` VARCHAR(20),
    `reason` VARCHAR(255),
    `change_reason` VARCHAR(255),
    `audit_status` VARCHAR(20),
    `audit_reason` VARCHAR(255),
    `audit_time` DATETIME,
    `auditor_id` BIGINT,
    `operator_id` BIGINT,
    `operator_name` VARCHAR(50),
    `product_name` VARCHAR(255),
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格历史表';

-- 价格审批表
CREATE TABLE IF NOT EXISTS `price_approval` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `price_history_id` BIGINT,
    `applicant_id` BIGINT,
    `applicant_name` VARCHAR(50),
    `approver_id` BIGINT,
    `approver_name` VARCHAR(50),
    `status` VARCHAR(20),
    `urgency_level` INT,
    `business_reason` VARCHAR(500),
    `approval_comment` VARCHAR(500),
    `application_time` DATETIME,
    `approval_time` DATETIME,
    `expected_time` DATETIME,
    `product_id` BIGINT,
    `sku_id` BIGINT,
    `product_name` VARCHAR(255),
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    INDEX `idx_price_history_id` (`price_history_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格审批表';

-- 使用订单数据库
USE `mall_order`;

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `product_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品总金额',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `payment_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `freight_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '运费',
    `discount_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态',
    `payment_status` TINYINT DEFAULT 0 COMMENT '支付状态: 0-未支付, 1-已支付',
    `delivery_status` TINYINT DEFAULT 0 COMMENT '发货状态: 0-未发货, 1-已发货, 2-已收货',
    `pay_method` VARCHAR(20) COMMENT '支付方式',
    `pay_time` DATETIME COMMENT '支付时间',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `receiver_address` VARCHAR(500) NOT NULL COMMENT '收货地址',
    `ship_time` DATETIME COMMENT '发货时间',
    `confirm_time` DATETIME COMMENT '确认收货时间',
    `cancel_time` DATETIME COMMENT '取消时间',
    `cancel_reason` VARCHAR(200) COMMENT '取消原因',
    `logistics_company` VARCHAR(50) COMMENT '物流公司',
    `logistics_no` VARCHAR(50) COMMENT '物流单号',
    `tracking_no` VARCHAR(50) COMMENT '物流跟踪号',
    `payment_id` VARCHAR(50) COMMENT '支付ID',
    `refund_time` DATETIME COMMENT '退款时间',
    `refund_reason` VARCHAR(200) COMMENT '退款原因',
    `refund_apply_time` DATETIME COMMENT '退款申请时间',
    `remark` VARCHAR(500) COMMENT '订单备注',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_merchant_id` (`merchant_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单商品表
CREATE TABLE IF NOT EXISTS `order_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单商品ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `product_image` VARCHAR(255) COMMENT '商品图片',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品表';

-- 使用支付数据库
USE `mall_payment`;

CREATE TABLE IF NOT EXISTS `payment_orders` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '支付订单ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '业务订单ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(15,2) NOT NULL COMMENT '支付金额',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式',
    `status` VARCHAR(20) NOT NULL COMMENT '支付状态',
    `description` VARCHAR(255) COMMENT '支付描述',
    `return_url` VARCHAR(500) COMMENT '返回URL',
    `notify_url` VARCHAR(500) COMMENT '异步通知URL',
    `expire_time` DATETIME COMMENT '过期时间',
    `third_party_order_no` VARCHAR(64) COMMENT '第三方订单号',
    `pay_time` DATETIME COMMENT '支付时间',
    `actual_amount` DECIMAL(15,2) COMMENT '实际支付金额',
    `fee_amount` DECIMAL(15,2) COMMENT '手续费',
    `channel_response` TEXT COMMENT '渠道响应',
    `failure_reason` VARCHAR(500) COMMENT '失败原因',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单表';

CREATE TABLE IF NOT EXISTS `payment_records` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '支付记录ID',
    `payment_order_id` VARCHAR(36) NOT NULL COMMENT '支付订单ID',
    `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式',
    `status` VARCHAR(20) NOT NULL COMMENT '支付状态',
    `amount` DECIMAL(15,2) NOT NULL COMMENT '支付金额',
    `request_params` TEXT COMMENT '请求参数',
    `response_data` TEXT COMMENT '响应数据',
    `third_party_trade_no` VARCHAR(64) COMMENT '第三方交易号',
    `payment_channel` VARCHAR(50) COMMENT '支付渠道',
    `pay_time` DATETIME COMMENT '支付完成时间',
    `fee_amount` DECIMAL(15,2) COMMENT '手续费',
    `error_code` VARCHAR(50) COMMENT '错误代码',
    `error_message` VARCHAR(500) COMMENT '错误信息',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `client_ip` VARCHAR(45) COMMENT '客户端IP',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `device_info` VARCHAR(200) COMMENT '设备信息',
    `action` VARCHAR(100) COMMENT '操作类型',
    `description` VARCHAR(500) COMMENT '操作描述',
    `remark` VARCHAR(500) COMMENT '备注信息',
    `created_at` DATETIME NOT NULL COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL COMMENT '更新时间',
    INDEX `idx_payment_order_id` (`payment_order_id`),
    INDEX `idx_third_party_trade_no` (`third_party_trade_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- 使用购物车数据库
USE `mall_cart`;

CREATE TABLE IF NOT EXISTS `cart_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车项ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(255) COMMENT '商品名称',
    `product_image` VARCHAR(255) COMMENT '商品图片',
    `price` DECIMAL(10,2) COMMENT '商品价格',
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `selected` TINYINT(1) DEFAULT 1 COMMENT '是否选中',
    `specifications` VARCHAR(500) COMMENT '规格信息',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `version` INT DEFAULT 0 COMMENT '版本号',
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车项表';

-- 使用商家数据库
USE `mall_merchant`;

CREATE TABLE IF NOT EXISTS `merchant` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    `merchant_code` VARCHAR(32) UNIQUE COMMENT '商家编码',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '商家用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '商家密码',
    `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `company_name` VARCHAR(100) NOT NULL COMMENT '公司名称',
    `merchant_type` INT NOT NULL COMMENT '商家类型：1-个人商家，2-企业商家',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系人手机号',
    `contact_email` VARCHAR(100) COMMENT '联系人邮箱',
    `id_number` VARCHAR(50) NOT NULL COMMENT '身份证号/营业执照号',
    `id_front_image` VARCHAR(200) COMMENT '身份证正面照/营业执照照片',
    `id_back_image` VARCHAR(200) COMMENT '身份证反面照',
    `bank_card_number` VARCHAR(30) COMMENT '银行卡号',
    `bank_name` VARCHAR(100) COMMENT '开户银行',
    `bank_account_name` VARCHAR(50) COMMENT '开户人姓名',
    `shop_description` VARCHAR(500) COMMENT '店铺描述',
    `shop_logo` VARCHAR(200) COMMENT '店铺logo',
    `business_category` VARCHAR(100) COMMENT '经营类目',
    `address` VARCHAR(200) COMMENT '详细地址',
    `province` VARCHAR(50) COMMENT '省份',
    `city` VARCHAR(50) COMMENT '城市',
    `district` VARCHAR(50) COMMENT '区县',
    `approval_status` INT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝',
    `approval_reason` VARCHAR(200) COMMENT '审核意见',
    `approval_time` DATETIME COMMENT '审核时间',
    `approval_by` BIGINT COMMENT '审核人ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '商家状态：0-禁用，1-正常',
    `deposit_amount` DECIMAL(10,2) COMMENT '保证金金额',
    `deposit_paid` INT NOT NULL DEFAULT 0 COMMENT '是否已缴纳保证金：0-未缴纳，1-已缴纳',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `version` INT DEFAULT 0 COMMENT '版本号',
    INDEX `idx_username` (`username`),
    INDEX `idx_merchant_code` (`merchant_code`),
    INDEX `idx_approval_status` (`approval_status`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家表';

CREATE TABLE IF NOT EXISTS `merchant_product` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `sku` VARCHAR(100) COMMENT '商品SKU编码',
    `category_id` BIGINT NOT NULL COMMENT '商品分类ID',
    `brand` VARCHAR(100) COMMENT '商品品牌',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `market_price` DECIMAL(10,2) COMMENT '市场价格',
    `cost_price` DECIMAL(10,2) COMMENT '成本价格',
    `stock_quantity` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `warning_stock` INT NOT NULL DEFAULT 10 COMMENT '预警库存',
    `main_image` VARCHAR(200) COMMENT '商品主图',
    `images` VARCHAR(1000) COMMENT '商品图片',
    `description` TEXT COMMENT '商品详情',
    `specifications` TEXT COMMENT '商品规格参数',
    `attributes` TEXT COMMENT '商品属性',
    `weight` INT COMMENT '商品重量',
    `dimensions` VARCHAR(50) COMMENT '商品尺寸',
    `status` INT NOT NULL DEFAULT 2 COMMENT '商品状态：0-下架，1-上架，2-草稿',
    `is_recommended` INT NOT NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
    `is_new` INT NOT NULL DEFAULT 0 COMMENT '是否新品：0-否，1-是',
    `is_hot` INT NOT NULL DEFAULT 0 COMMENT '是否热销：0-否，1-是',
    `sales_count` INT NOT NULL DEFAULT 0 COMMENT '销售数量',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
    `rating` DECIMAL(3,2) COMMENT '评分',
    `review_count` INT NOT NULL DEFAULT 0 COMMENT '评价数量',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序值',
    `seo_keywords` VARCHAR(200) COMMENT 'SEO关键词',
    `seo_description` VARCHAR(500) COMMENT 'SEO描述',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `version` INT DEFAULT 0 COMMENT '版本号',
    INDEX `idx_merchant_id` (`merchant_id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家商品表';

CREATE TABLE IF NOT EXISTS `merchant_order` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `product_image` VARCHAR(200) COMMENT '商品图片',
    `product_spec` VARCHAR(200) COMMENT '商品规格',
    `product_price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `paid_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `discount_amount` DECIMAL(10,2) COMMENT '优惠金额',
    `shipping_fee` DECIMAL(10,2) COMMENT '运费',
    `status` INT NOT NULL DEFAULT 1 COMMENT '订单状态：1-待付款，2-待发货，3-待收货，4-待评价，5-已完成，6-已取消，7-退款中，8-已退款',
    `payment_method` INT COMMENT '支付方式：1-微信支付，2-支付宝，3-银行卡',
    `payment_time` DATETIME COMMENT '支付时间',
    `ship_time` DATETIME COMMENT '发货时间',
    `receive_time` DATETIME COMMENT '收货时间',
    `finish_time` DATETIME COMMENT '完成时间',
    `cancel_time` DATETIME COMMENT '取消时间',
    `cancel_reason` VARCHAR(200) COMMENT '取消原因',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    `receiver_address` VARCHAR(200) NOT NULL COMMENT '收货地址',
    `receiver_province` VARCHAR(50) COMMENT '省份',
    `receiver_city` VARCHAR(50) COMMENT '城市',
    `receiver_district` VARCHAR(50) COMMENT '区县',
    `logistics_company` VARCHAR(50) COMMENT '物流公司',
    `logistics_no` VARCHAR(50) COMMENT '物流单号',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `version` INT DEFAULT 0 COMMENT '版本号',
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_merchant_id` (`merchant_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家订单表';

CREATE TABLE IF NOT EXISTS `merchant_statistics` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `stat_type` INT NOT NULL COMMENT '统计类型：1-日统计，2-月统计，3-年统计',
    `total_orders` INT NOT NULL DEFAULT 0 COMMENT '订单总数',
    `completed_orders` INT NOT NULL DEFAULT 0 COMMENT '已完成订单数',
    `cancelled_orders` INT NOT NULL DEFAULT 0 COMMENT '已取消订单数',
    `refund_orders` INT NOT NULL DEFAULT 0 COMMENT '退款订单数',
    `total_sales` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '总销售额',
    `actual_income` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '实际收入',
    `refund_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额',
    `product_sales_count` INT NOT NULL DEFAULT 0 COMMENT '商品销售数量',
    `new_products` INT NOT NULL DEFAULT 0 COMMENT '新增商品数',
    `online_products` INT NOT NULL DEFAULT 0 COMMENT '上架商品数',
    `offline_products` INT NOT NULL DEFAULT 0 COMMENT '下架商品数',
    `page_views` INT NOT NULL DEFAULT 0 COMMENT '访问量',
    `unique_visitors` INT NOT NULL DEFAULT 0 COMMENT '独立访客数',
    `product_views` INT NOT NULL DEFAULT 0 COMMENT '商品浏览量',
    `product_favorites` INT NOT NULL DEFAULT 0 COMMENT '商品收藏数',
    `cart_additions` INT NOT NULL DEFAULT 0 COMMENT '购物车添加数',
    `conversion_rate` DECIMAL(5,4) DEFAULT 0.0000 COMMENT '转化率',
    `avg_order_value` DECIMAL(10,2) DEFAULT 0.00 COMMENT '客单价',
    `refund_rate` DECIMAL(5,4) DEFAULT 0.0000 COMMENT '退款率',
    `positive_rate` DECIMAL(5,4) DEFAULT 0.0000 COMMENT '好评率',
    `avg_rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '平均评分',
    `total_reviews` INT NOT NULL DEFAULT 0 COMMENT '评价总数',
    `positive_reviews` INT NOT NULL DEFAULT 0 COMMENT '好评数',
    `neutral_reviews` INT NOT NULL DEFAULT 0 COMMENT '中评数',
    `negative_reviews` INT NOT NULL DEFAULT 0 COMMENT '差评数',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `version` INT DEFAULT 0 COMMENT '版本号',
    INDEX `idx_merchant_id` (`merchant_id`),
    INDEX `idx_stat_date` (`stat_date`),
    INDEX `idx_stat_type` (`stat_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家统计数据表';

CREATE TABLE IF NOT EXISTS `banner_statistics` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    `banner_id` BIGINT NOT NULL COMMENT '轮播图申请ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `impressions` INT DEFAULT 0 COMMENT '曝光次数',
    `clicks` INT DEFAULT 0 COMMENT '点击次数',
    `unique_impressions` INT DEFAULT 0 COMMENT '独立曝光数',
    `unique_clicks` INT DEFAULT 0 COMMENT '独立点击数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_banner_date` (`banner_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图统计表';

CREATE TABLE IF NOT EXISTS `merchant_approval_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `before_status` INT COMMENT '审核前状态',
    `after_status` INT NOT NULL COMMENT '审核后状态',
    `approval_reason` VARCHAR(200) COMMENT '审核意见',
    `approval_by` BIGINT COMMENT '审核人ID',
    `approver_name` VARCHAR(50) COMMENT '审核人名称',
    `approval_time` DATETIME COMMENT '审核时间',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `version` INT DEFAULT 0 COMMENT '版本号',
    INDEX `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家审核日志表';

CREATE TABLE IF NOT EXISTS `banner_application` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家ID',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `description` VARCHAR(500) COMMENT '描述',
    `target_url` VARCHAR(500) NOT NULL COMMENT '跳转链接',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    `reject_reason` VARCHAR(500) COMMENT '拒绝原因',
    `review_time` DATETIME COMMENT '审核时间',
    `reviewer_id` BIGINT COMMENT '审核人ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记',
    `version` INT DEFAULT 0 COMMENT '版本号',
    INDEX `idx_merchant_id` (`merchant_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图申请表';

CREATE TABLE IF NOT EXISTS `merchant_applications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    `entity_type` VARCHAR(20) NOT NULL COMMENT '主体类型',
    `shop_type` VARCHAR(20) COMMENT '店铺类型',
    `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `email` VARCHAR(100) COMMENT '邮箱',
    `company_name` VARCHAR(100) COMMENT '公司名称',
    `credit_code` VARCHAR(18) COMMENT '信用代码',
    `legal_person` VARCHAR(50) COMMENT '法人',
    `business_license` VARCHAR(500) COMMENT '营业执照',
    `id_card` VARCHAR(18) COMMENT '身份证号',
    `id_card_front` VARCHAR(500) COMMENT '身份证正面',
    `id_card_back` VARCHAR(500) COMMENT '身份证反面',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    `reject_reason` VARCHAR(500) COMMENT '拒绝原因',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_contact_phone` (`contact_phone`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家入驻申请表';

-- 使用管理后台数据库
USE `mall_admin`;

CREATE TABLE IF NOT EXISTS `admin` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `real_name` VARCHAR(50),
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `avatar` VARCHAR(255),
    `status` TINYINT DEFAULT 1,
    `last_login_time` DATETIME,
    `last_login_ip` VARCHAR(50),
    `failed_login_count` INT DEFAULT 0,
    `locked_until` DATETIME,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_code` VARCHAR(50) NOT NULL UNIQUE,
    `role_name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(200),
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `permission_code` VARCHAR(100) NOT NULL UNIQUE,
    `permission_name` VARCHAR(100) NOT NULL,
    `resource_type` VARCHAR(50),
    `resource` VARCHAR(100),
    `action` VARCHAR(50),
    `description` VARCHAR(200),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

CREATE TABLE IF NOT EXISTS `admin_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_admin_id` (`admin_id`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员角色关联表';

CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT,
    `admin_username` VARCHAR(50),
    `operation_type` VARCHAR(50),
    `resource_type` VARCHAR(50),
    `resource_id` VARCHAR(50),
    `operation_desc` VARCHAR(255),
    `request_method` VARCHAR(10),
    `request_url` VARCHAR(255),
    `request_params` TEXT,
    `response_result` TEXT,
    `ip_address` VARCHAR(50),
    `user_agent` VARCHAR(255),
    `status` TINYINT,
    `error_msg` TEXT,
    `execution_time` INT,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_admin_id` (`admin_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作审计日志表';

CREATE TABLE IF NOT EXISTS `login_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `admin_id` BIGINT,
    `username` VARCHAR(50),
    `login_result` TINYINT,
    `failure_reason` VARCHAR(255),
    `ip_address` VARCHAR(50),
    `user_agent` VARCHAR(255),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 使用短信服务数据库
USE `mall_sms`;

CREATE TABLE IF NOT EXISTS `sms_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `phone_number` VARCHAR(20),
    `code` VARCHAR(10),
    `purpose` VARCHAR(50),
    `client_ip` VARCHAR(50),
    `status` INT,
    `error_message` VARCHAR(500),
    `response` TEXT,
    `create_time` DATETIME,
    `update_time` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信日志表';

CREATE TABLE IF NOT EXISTS `sms_blacklist` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `type` VARCHAR(20),
    `value` VARCHAR(50),
    `reason` VARCHAR(200),
    `status` INT,
    `expire_time` DATETIME,
    `create_time` DATETIME,
    `update_time` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信黑名单表';

-- ============================================
-- 注意：不再插入任何测试数据
-- 所有数据应通过系统前端或API添加
-- 管理员账号请参考 sql/init.sql
-- ============================================

COMMIT;

-- 显示初始化完成信息
SELECT '数据库初始化完成！' AS message;
