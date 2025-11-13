-- 商品评价表
CREATE TABLE IF NOT EXISTS `product_review` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) DEFAULT NULL COMMENT '用户名称',
    `user_avatar` VARCHAR(500) DEFAULT NULL COMMENT '用户头像',
    `order_id` BIGINT(20) DEFAULT NULL COMMENT '订单ID',
    `rating` INT(1) NOT NULL DEFAULT 5 COMMENT '评分（1-5星）',
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

-- 插入测试数据
INSERT INTO `product_review` 
    (`product_id`, `user_id`, `user_name`, `user_avatar`, `rating`, `content`, `anonymous`, `like_count`, `status`, `create_time`)
VALUES
    (202, 1, '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=1', 5, '商品质量非常好，物流也很快，非常满意！', 0, 12, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (202, 2, '李四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=2', 4, '整体不错，就是包装有点简陋，建议改进一下。', 0, 8, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (202, 3, '王五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=3', 5, '第二次购买了，一如既往的好，强烈推荐！', 0, 15, 1, DATE_SUB(NOW(), INTERVAL 7 DAY)),
    (202, 4, '赵六', 'https://api.dicebear.com/7.x/avataaars/svg?seed=4', 3, '价格有点贵，性价比一般，但质量还可以。', 0, 3, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
    (202, 5, '匿名用户', NULL, 5, '非常棒的购物体验，下次还会再来！', 1, 20, 1, DATE_SUB(NOW(), INTERVAL 15 DAY));
