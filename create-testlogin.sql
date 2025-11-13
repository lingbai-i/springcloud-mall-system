USE mall_user;

-- 如果testlogin已存在则删除
DELETE FROM users WHERE username = 'testlogin';

-- 创建testlogin用户 (密码: nacos)
-- 密码哈希: $2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu
INSERT INTO users (username, password, nickname, email, phone, gender, status, password_set_time, created_time, updated_time) 
VALUES ('testlogin', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', 'testlogin', 'test@mall.com', '13800138889', 0, 1, NOW(), NOW(), NOW());

-- 验证
SELECT username, password, status, password_set_time FROM users WHERE username = 'testlogin';

