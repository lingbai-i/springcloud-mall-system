-- ======================================
-- 修复用户分布饼图测试数据
-- 创建时间: 2026-01-01
-- 用途: 修复订单号格式，确保订单显示在最新列表
-- ======================================

USE `mall_user`;

-- 1. 清理旧测试数据
DELETE FROM `users` WHERE `username` LIKE 'chart_user%';

-- 2. 插入新测试用户 (使用 chart_user 前缀避免冲突)
-- 活跃用户 (最近7天登录)
INSERT INTO `users` (`username`, `email`, `phone`, `password`, `nickname`, `status`, `last_login_time`, `created_time`) VALUES
('chart_user_active1', 'active1@chart.com', '13811110001', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '活跃用户A', 1, NOW(), DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('chart_user_active2', 'active2@chart.com', '13811110002', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '活跃用户B', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('chart_user_active3', 'active3@chart.com', '13811110003', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '活跃用户C', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 YEAR)),
('chart_user_active4', 'active4@chart.com', '13811110004', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '活跃用户D', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('chart_user_active5', 'active5@chart.com', '13811110005', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '活跃用户E', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY));

-- 不活跃用户 (7-30天登录)
INSERT INTO `users` (`username`, `email`, `phone`, `password`, `nickname`, `status`, `last_login_time`, `created_time`) VALUES
('chart_user_inactive1', 'inactive1@chart.com', '13811110011', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '不活跃A', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 6 MONTH)),
('chart_user_inactive2', 'inactive2@chart.com', '13811110012', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '不活跃B', 1, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 8 MONTH));

-- 沉睡用户 (30天+ 或 未登录)
INSERT INTO `users` (`username`, `email`, `phone`, `password`, `nickname`, `status`, `last_login_time`, `created_time`) VALUES
('chart_user_dormant1', 'dormant1@chart.com', '13811110021', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '沉睡A', 1, DATE_SUB(NOW(), INTERVAL 40 DAY), DATE_SUB(NOW(), INTERVAL 1 YEAR)),
('chart_user_dormant2', 'dormant2@chart.com', '13811110022', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '沉睡B', 1, NULL, DATE_SUB(NOW(), INTERVAL 2 YEAR));

-- 新用户 (注册 < 1月)
INSERT INTO `users` (`username`, `email`, `phone`, `password`, `nickname`, `status`, `last_login_time`, `created_time`) VALUES
('chart_user_new1', 'new1@chart.com', '13811110031', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '新用户A', 1, NOW(), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('chart_user_new2', 'new2@chart.com', '13811110032', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '新用户B', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));


USE `mall_order`;

-- 1. 清理旧测试数据
DELETE FROM `orders` WHERE `order_no` LIKE 'ORD2026%TEST%';
DELETE FROM `orders` WHERE `order_no` LIKE 'TEST%';

-- 2. 插入新测试订单 (使用标准格式 ORD + 时间 + 随机)
-- 确保有些订单非常新 (NOW())，以便在 dashboard 最新订单中显示

-- 高额订单 (VIP)
INSERT INTO `orders` (`order_no`, `user_id`, `merchant_id`, `product_amount`, `total_amount`, `pay_amount`, `status`, `receiver_name`, `receiver_phone`, `receiver_address`, `create_time`) VALUES
(CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '001'), (SELECT id FROM mall_user.users WHERE username = 'chart_user_active1' LIMIT 1), 1, 8888.00, 8888.00, 8888.00, 'PAID', 'VIP Customer', '13811110001', 'Beijing CBD', NOW()),
(CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '002'), (SELECT id FROM mall_user.users WHERE username = 'chart_user_active2' LIMIT 1), 1, 6666.00, 6666.00, 6666.00, 'COMPLETED', 'VIP Customer', '13811110002', 'Shanghai Bund', DATE_SUB(NOW(), INTERVAL 1 MINUTE));

-- 中额订单
INSERT INTO `orders` (`order_no`, `user_id`, `merchant_id`, `product_amount`, `total_amount`, `pay_amount`, `status`, `receiver_name`, `receiver_phone`, `receiver_address`, `create_time`) VALUES
(CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '003'), (SELECT id FROM mall_user.users WHERE username = 'chart_user_active3' LIMIT 1), 1, 1200.00, 1200.00, 1200.00, 'SHIPPED', 'Regular User', '13811110003', 'Guangzhou Tower', DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '004'), (SELECT id FROM mall_user.users WHERE username = 'chart_user_active4' LIMIT 1), 1, 1500.00, 1500.00, 1500.00, 'COMPLETED', 'Regular User', '13811110004', 'Shenzhen Park', DATE_SUB(NOW(), INTERVAL 10 MINUTE));

-- 低额订单
INSERT INTO `orders` (`order_no`, `user_id`, `merchant_id`, `product_amount`, `total_amount`, `pay_amount`, `status`, `receiver_name`, `receiver_phone`, `receiver_address`, `create_time`) VALUES
(CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '005'), (SELECT id FROM mall_user.users WHERE username = 'chart_user_new1' LIMIT 1), 1, 99.00, 99.00, 99.00, 'PENDING_PAYMENT', 'New User', '13811110031', 'Chengdu Panda Base', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), '006'), (SELECT id FROM mall_user.users WHERE username = 'chart_user_new2' LIMIT 1), 1, 299.00, 299.00, 299.00, 'PAID', 'New User', '13811110032', 'Hangzhou West Lake', DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- 验证数据
SELECT '修复数据插入完成' AS message;
