-- ============================================
-- 初始化数据库脚本
-- 说明：仅创建数据库结构和基础分类数据
-- 注意：不包含任何测试数据
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `mall_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_product` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mall_merchant` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用用户数据库
USE `mall_user`;

-- 用户表
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(30) NOT NULL COMMENT '用户账号',
  `nickname` varchar(30) DEFAULT NULL COMMENT '用户昵称',
  `email` varchar(50) DEFAULT NULL COMMENT '用户邮箱',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号码',
  `sex` char(1) DEFAULT '2' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) DEFAULT NULL COMMENT '头像地址',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `login_ip` varchar(128) DEFAULT NULL COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `version` int DEFAULT '0' COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 用户地址表
CREATE TABLE `user_address` (
  `id` bigint NOT NULL COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `detail_address` varchar(200) NOT NULL COMMENT '详细地址',
  `postal_code` varchar(10) DEFAULT NULL COMMENT '邮政编码',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认地址（0否 1是）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

-- 使用商品数据库
USE `mall_product`;

-- 商品分类表
CREATE TABLE `product_category` (
  `id` bigint NOT NULL COMMENT '分类ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(100) DEFAULT NULL COMMENT '分类图标',
  `image` varchar(200) DEFAULT NULL COMMENT '分类图片',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态（0禁用 1启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 商品SPU表
CREATE TABLE `product_spu` (
  `id` bigint NOT NULL COMMENT 'SPU ID',
  `spu_code` varchar(50) NOT NULL COMMENT 'SPU编码',
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `description` text COMMENT '商品描述',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `brand_id` bigint DEFAULT NULL COMMENT '品牌ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态（0下架 1上架）',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_code` (`spu_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SPU表';

-- 商品SKU表
CREATE TABLE `product_sku` (
  `id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_code` varchar(50) NOT NULL COMMENT 'SKU编码',
  `spu_id` bigint NOT NULL COMMENT 'SPU ID',
  `name` varchar(200) NOT NULL COMMENT 'SKU名称',
  `image` varchar(200) DEFAULT NULL COMMENT 'SKU图片',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `cost_price` decimal(10,2) DEFAULT NULL COMMENT '成本价',
  `stock` int DEFAULT '0' COMMENT '库存',
  `sales` int DEFAULT '0' COMMENT '销量',
  `weight` decimal(10,2) DEFAULT NULL COMMENT '重量(kg)',
  `volume` decimal(10,2) DEFAULT NULL COMMENT '体积(m³)',
  `specs` json DEFAULT NULL COMMENT '规格属性JSON',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态（0禁用 1启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- 使用订单数据库
USE `mall_order`;

-- 购物车表
CREATE TABLE `shopping_cart` (
  `id` bigint NOT NULL COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `quantity` int NOT NULL COMMENT '数量',
  `selected` tinyint(1) DEFAULT '1' COMMENT '是否选中（0否 1是）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- 订单表
CREATE TABLE `order_info` (
  `id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `status` varchar(20) NOT NULL COMMENT '订单状态',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `freight_amount` decimal(10,2) DEFAULT '0.00' COMMENT '运费',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `receiver_address` varchar(500) NOT NULL COMMENT '收货地址',
  `remark` varchar(500) DEFAULT NULL COMMENT '订单备注',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 订单商品表
CREATE TABLE `order_item` (
  `id` bigint NOT NULL COMMENT '订单商品ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `sku_name` varchar(200) NOT NULL COMMENT 'SKU名称',
  `sku_image` varchar(200) DEFAULT NULL COMMENT 'SKU图片',
  `sku_price` decimal(10,2) NOT NULL COMMENT 'SKU价格',
  `quantity` int NOT NULL COMMENT '数量',
  `total_amount` decimal(10,2) NOT NULL COMMENT '小计金额',
  `specs` json DEFAULT NULL COMMENT '规格属性JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单商品表';

-- 使用商家数据库
USE `mall_merchant`;

-- 商家表
CREATE TABLE `merchant` (
  `id` bigint NOT NULL COMMENT '商家ID',
  `merchant_code` varchar(32) NOT NULL COMMENT '商家编码',
  `company_name` varchar(100) NOT NULL COMMENT '公司名称',
  `business_license` varchar(50) DEFAULT NULL COMMENT '营业执照号',
  `legal_person` varchar(50) DEFAULT NULL COMMENT '法人代表',
  `contact_name` varchar(50) NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) NOT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `bank_account` varchar(50) DEFAULT NULL COMMENT '银行账户',
  `bank_name` varchar(100) DEFAULT NULL COMMENT '开户银行',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态（PENDING待审核 APPROVED已通过 REJECTED已拒绝 SUSPENDED已暂停）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_code` (`merchant_code`),
  UNIQUE KEY `uk_business_license` (`business_license`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家表';

-- 店铺表
CREATE TABLE `store` (
  `id` bigint NOT NULL COMMENT '店铺ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `store_name` varchar(100) NOT NULL COMMENT '店铺名称',
  `store_logo` varchar(200) DEFAULT NULL COMMENT '店铺LOGO',
  `store_description` text COMMENT '店铺简介',
  `business_hours` varchar(100) DEFAULT NULL COMMENT '营业时间',
  `status` varchar(20) DEFAULT 'OPEN' COMMENT '状态（OPEN营业中 CLOSED已关闭 SUSPENDED已暂停）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- ============================================
-- 初始化管理员账号
-- 默认管理员：admin / password
-- ============================================
USE `mall_user`;

-- 插入默认管理员账号
INSERT INTO `sys_user` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `status`) VALUES
(1, 'admin', '系统管理员', 'admin@mall.com', '13800138000', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '0');

-- 如果存在 users 表，也插入管理员账号
INSERT INTO `users` (`id`, `username`, `nickname`, `email`, `phone`, `password`, `status`) 
VALUES (1, 'admin', '系统管理员', 'admin@mall.com', '13800138888', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 1)
ON DUPLICATE KEY UPDATE username=username;

USE `mall_product`;

-- 插入商品分类
INSERT INTO `product_category` (`id`, `parent_id`, `name`, `icon`, `sort_order`) VALUES
(1, 0, '手机数码', 'Cellphone', 1),
(2, 0, '电脑办公', 'Computer', 2),
(3, 0, '家居家装', 'House', 3),
(4, 0, '服饰内衣', 'Shirt', 4),
(11, 1, '手机通讯', NULL, 1),
(12, 1, '数码配件', NULL, 2),
(21, 2, '电脑整机', NULL, 1),
(22, 2, '电脑配件', NULL, 2);

-- ============================================
-- 注意：不再插入任何测试数据
-- 如需测试数据，请在应用启动后通过前端添加
-- 或使用单独的测试数据脚本
-- ============================================