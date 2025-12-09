-- 用户地址表
CREATE TABLE IF NOT EXISTS `user_addresses` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `province_code` VARCHAR(20) DEFAULT NULL COMMENT '省份代码',
  `city_code` VARCHAR(20) DEFAULT NULL COMMENT '城市代码',
  `district_code` VARCHAR(20) DEFAULT NULL COMMENT '区县代码',
  `province` VARCHAR(50) NOT NULL COMMENT '省份名称',
  `city` VARCHAR(50) NOT NULL COMMENT '城市名称',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县名称',
  `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认地址:0-否,1-是',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';
