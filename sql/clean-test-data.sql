-- ============================================
-- 数据清理脚本
-- 用途：清除系统中所有虚拟测试数据
-- 说明：此脚本会删除除管理员账号外的所有测试数据
-- 注意：执行前请务必备份重要数据
-- ============================================

-- 清理商品数据库
USE `mall_product`;

-- 删除所有SKU
DELETE FROM `product_sku` WHERE 1=1;

-- 删除所有SPU
DELETE FROM `product_spu` WHERE 1=1;

-- 清理商品分类（保留一级分类结构）
DELETE FROM `product_category` WHERE `parent_id` != 0;

-- 清理订单数据库
USE `mall_order`;

-- 删除所有订单商品
DELETE FROM `order_item` WHERE 1=1;

-- 删除所有订单
DELETE FROM `order_info` WHERE 1=1;

-- 删除所有购物车数据
DELETE FROM `shopping_cart` WHERE 1=1;

-- 清理商家数据库
USE `mall_merchant`;

-- 删除所有店铺
DELETE FROM `store` WHERE 1=1;

-- 删除所有商家（不删除可能用于关联的商家）
DELETE FROM `merchant` WHERE 1=1;

-- 清理用户数据库
USE `mall_user`;

-- 删除所有用户地址
DELETE FROM `user_address` WHERE 1=1;

-- 删除测试用户（保留管理员账号）
-- 根据您的需求，可以保留admin账号或特定用户
DELETE FROM `sys_user` WHERE `username` NOT IN ('admin');

-- 如果存在 users 表（普通用户表），也进行清理
-- 保留admin账号
DELETE FROM `users` WHERE `username` NOT IN ('admin');

-- 重置自增ID（可选，如果需要从1开始）
-- 注意：只有在确保没有外键依赖的情况下才执行此操作
-- ALTER TABLE `mall_product`.`product_sku` AUTO_INCREMENT = 1;
-- ALTER TABLE `mall_product`.`product_spu` AUTO_INCREMENT = 1;
-- ALTER TABLE `mall_order`.`order_info` AUTO_INCREMENT = 1;
-- ALTER TABLE `mall_order`.`order_item` AUTO_INCREMENT = 1;
-- ALTER TABLE `mall_order`.`shopping_cart` AUTO_INCREMENT = 1;
-- ALTER TABLE `mall_merchant`.`merchant` AUTO_INCREMENT = 1;
-- ALTER TABLE `mall_merchant`.`store` AUTO_INCREMENT = 1;
-- ALTER TABLE `mall_user`.`sys_user` AUTO_INCREMENT = 1;
-- ALTER TABLE `mall_user`.`user_address` AUTO_INCREMENT = 1;

-- 清理完成
SELECT '数据清理完成！' AS '状态';
SELECT '保留了管理员账号和基础分类数据' AS '提示';
