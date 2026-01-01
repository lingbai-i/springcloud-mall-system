-- ======================================
-- 用户分布饼图测试数据插入脚本
-- 创建时间: 2026-01-01
-- 用途: 为用户分布饼图功能提供支撑数据
-- ======================================

-- 使用用户数据库
USE `mall_user`;

-- 插入不同注册时间和最后登录时间的测试用户
-- 活跃用户：7天内登录过
INSERT INTO `users` (`username`, `email`, `phone`, `password`, `nickname`, `status`, `last_login_time`, `created_time`) VALUES
('active_user1', 'active1@test.com', '13800000001', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '活跃用户1', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 MONTH)),
('active_user2', 'active2@test.com', '13800000002', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '活跃用户2', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 6 MONTH)),
('active_user3', 'active3@test.com', '13800000003', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '活跃用户3', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 MONTH)),
('active_user4', 'active4@test.com', '13800000004', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '活跃用户4', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 2 MONTH)),
('active_user5', 'active5@test.com', '13800000005', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '活跃用户5', 1, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY));

-- 不活跃用户：7-30天内登录过
INSERT INTO `users` (`username`, `email`, `phone`, `password`, `nickname`, `status`, `last_login_time`, `created_time`) VALUES
('inactive_user1', 'inactive1@test.com', '13800000011', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '不活跃用户1', 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 4 MONTH)),
('inactive_user2', 'inactive2@test.com', '13800000012', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '不活跃用户2', 1, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 5 MONTH)),
('inactive_user3', 'inactive3@test.com', '13800000013', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '不活跃用户3', 1, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 7 MONTH));

-- 沉睡用户：30天以上未登录
INSERT INTO `users` (`username`, `email`, `phone`, `password`, `nickname`, `status`, `last_login_time`, `created_time`) VALUES
('dormant_user1', 'dormant1@test.com', '13800000021', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '沉睡用户1', 1, DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_SUB(NOW(), INTERVAL 8 MONTH)),
('dormant_user2', 'dormant2@test.com', '13800000022', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '沉睡用户2', 1, DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_SUB(NOW(), INTERVAL 9 MONTH)),
('dormant_user3', 'dormant3@test.com', '13800000023', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '沉睡用户3', 1, NULL, DATE_SUB(NOW(), INTERVAL 10 MONTH)), -- 从未登录
('dormant_user4', 'dormant4@test.com', '13800000024', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '沉睡用户4', 1, DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_SUB(NOW(), INTERVAL 12 MONTH));

-- 新用户：注册时间小于1个月
INSERT INTO `users` (`username`, `email`, `phone`, `password`, `nickname`, `status`, `last_login_time`, `created_time`) VALUES
('new_user1', 'new1@test.com', '13800000031', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '新用户1', 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('new_user2', 'new2@test.com', '13800000032', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '新用户2', 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
('new_user3', 'new3@test.com', '13800000033', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MQ', '新用户3', 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY));

-- 查询确认插入成功
SELECT '用户数据插入完成' AS message;
SELECT 
  (SELECT COUNT(*) FROM users WHERE deleted = 0) AS total_users,
  (SELECT COUNT(*) FROM users WHERE deleted = 0 AND last_login_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)) AS active_users,
  (SELECT COUNT(*) FROM users WHERE deleted = 0 AND created_time >= DATE_SUB(NOW(), INTERVAL 1 MONTH)) AS new_users;

-- ======================================
-- 使用订单数据库
USE `mall_order`;

-- 获取用户ID列表用于创建订单（使用子查询）
-- 为测试用户创建不同金额的订单

-- 低消费用户订单 (总额 < 500)
INSERT INTO `orders` (`order_no`, `user_id`, `merchant_id`, `product_amount`, `total_amount`, `payment_amount`, `status`, `receiver_name`, `receiver_phone`, `receiver_address`, `created_time`) VALUES
('TEST20260101000001', (SELECT id FROM mall_user.users WHERE username = 'active_user1' LIMIT 1), 1, 100.00, 100.00, 100.00, 'COMPLETED', '测试收货人', '13900000001', '测试地址1', DATE_SUB(NOW(), INTERVAL 5 DAY)),
('TEST20260101000002', (SELECT id FROM mall_user.users WHERE username = 'active_user1' LIMIT 1), 1, 150.00, 150.00, 150.00, 'COMPLETED', '测试收货人', '13900000001', '测试地址1', DATE_SUB(NOW(), INTERVAL 4 DAY)),
('TEST20260101000003', (SELECT id FROM mall_user.users WHERE username = 'inactive_user1' LIMIT 1), 1, 200.00, 200.00, 200.00, 'COMPLETED', '测试收货人', '13900000002', '测试地址2', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('TEST20260101000004', (SELECT id FROM mall_user.users WHERE username = 'dormant_user1' LIMIT 1), 1, 50.00, 50.00, 50.00, 'PAID', '测试收货人', '13900000003', '测试地址3', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 中等消费用户订单 (500 <= 总额 < 2000)
INSERT INTO `orders` (`order_no`, `user_id`, `merchant_id`, `product_amount`, `total_amount`, `payment_amount`, `status`, `receiver_name`, `receiver_phone`, `receiver_address`, `created_time`) VALUES
('TEST20260101000011', (SELECT id FROM mall_user.users WHERE username = 'active_user2' LIMIT 1), 1, 600.00, 600.00, 600.00, 'COMPLETED', '测试收货人', '13900000011', '测试地址11', DATE_SUB(NOW(), INTERVAL 10 DAY)),
('TEST20260101000012', (SELECT id FROM mall_user.users WHERE username = 'active_user2' LIMIT 1), 1, 400.00, 400.00, 400.00, 'SHIPPED', '测试收货人', '13900000011', '测试地址11', DATE_SUB(NOW(), INTERVAL 8 DAY)),
('TEST20260101000013', (SELECT id FROM mall_user.users WHERE username = 'inactive_user2' LIMIT 1), 1, 800.00, 800.00, 800.00, 'COMPLETED', '测试收货人', '13900000012', '测试地址12', DATE_SUB(NOW(), INTERVAL 7 DAY)),
('TEST20260101000014', (SELECT id FROM mall_user.users WHERE username = 'new_user1' LIMIT 1), 1, 1500.00, 1500.00, 1500.00, 'PAID', '测试收货人', '13900000013', '测试地址13', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 高消费用户订单 (2000 <= 总额 < 5000)
INSERT INTO `orders` (`order_no`, `user_id`, `merchant_id`, `product_amount`, `total_amount`, `payment_amount`, `status`, `receiver_name`, `receiver_phone`, `receiver_address`, `created_time`) VALUES
('TEST20260101000021', (SELECT id FROM mall_user.users WHERE username = 'active_user3' LIMIT 1), 1, 2500.00, 2500.00, 2500.00, 'COMPLETED', '测试收货人', '13900000021', '测试地址21', DATE_SUB(NOW(), INTERVAL 15 DAY)),
('TEST20260101000022', (SELECT id FROM mall_user.users WHERE username = 'active_user3' LIMIT 1), 1, 1000.00, 1000.00, 1000.00, 'SHIPPED', '测试收货人', '13900000021', '测试地址21', DATE_SUB(NOW(), INTERVAL 12 DAY)),
('TEST20260101000023', (SELECT id FROM mall_user.users WHERE username = 'inactive_user3' LIMIT 1), 1, 3000.00, 3000.00, 3000.00, 'COMPLETED', '测试收货人', '13900000022', '测试地址22', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- VIP消费用户订单 (总额 >= 5000)
INSERT INTO `orders` (`order_no`, `user_id`, `merchant_id`, `product_amount`, `total_amount`, `payment_amount`, `status`, `receiver_name`, `receiver_phone`, `receiver_address`, `created_time`) VALUES
('TEST20260101000031', (SELECT id FROM mall_user.users WHERE username = 'active_user4' LIMIT 1), 1, 3000.00, 3000.00, 3000.00, 'COMPLETED', '测试收货人', '13900000031', '测试地址31', DATE_SUB(NOW(), INTERVAL 20 DAY)),
('TEST20260101000032', (SELECT id FROM mall_user.users WHERE username = 'active_user4' LIMIT 1), 1, 2500.00, 2500.00, 2500.00, 'SHIPPED', '测试收货人', '13900000031', '测试地址31', DATE_SUB(NOW(), INTERVAL 18 DAY)),
('TEST20260101000033', (SELECT id FROM mall_user.users WHERE username = 'active_user5' LIMIT 1), 1, 6000.00, 6000.00, 6000.00, 'COMPLETED', '测试收货人', '13900000032', '测试地址32', DATE_SUB(NOW(), INTERVAL 15 DAY)),
('TEST20260101000034', (SELECT id FROM mall_user.users WHERE username = 'new_user2' LIMIT 1), 1, 8000.00, 8000.00, 8000.00, 'PAID', '测试收货人', '13900000033', '测试地址33', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- 查询确认插入成功
SELECT '订单数据插入完成' AS message;
SELECT 
  (SELECT COUNT(*) FROM orders) AS total_orders,
  (SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED') AS completed_orders,
  (SELECT COUNT(*) FROM orders WHERE status = 'PAID') AS paid_orders;

COMMIT;

SELECT '测试数据插入完成！' AS final_message;
