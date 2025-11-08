-- 订单服务数据库索引优化脚本
-- 用于提升订单查询性能

-- 订单表索引优化
-- 1. 用户ID + 状态 复合索引（用于用户订单列表查询）
CREATE INDEX IF NOT EXISTS idx_order_user_status ON orders(user_id, status);

-- 2. 用户ID + 创建时间 复合索引（用于按时间排序的用户订单查询）
CREATE INDEX IF NOT EXISTS idx_order_user_create_time ON orders(user_id, create_time DESC);

-- 3. 订单号唯一索引（用于订单号查询）
CREATE UNIQUE INDEX IF NOT EXISTS idx_order_no ON orders(order_no);

-- 4. 状态 + 创建时间 复合索引（用于超时订单处理）
CREATE INDEX IF NOT EXISTS idx_order_status_create_time ON orders(status, create_time);

-- 5. 支付时间索引（用于支付相关查询）
CREATE INDEX IF NOT EXISTS idx_order_payment_time ON orders(payment_time);

-- 6. 更新时间索引（用于数据同步和监控）
CREATE INDEX IF NOT EXISTS idx_order_update_time ON orders(update_time);

-- 订单项表索引优化
-- 1. 订单ID索引（用于根据订单查询订单项）
CREATE INDEX IF NOT EXISTS idx_order_item_order_id ON order_items(order_id);

-- 2. 商品ID索引（用于根据商品查询相关订单）
CREATE INDEX IF NOT EXISTS idx_order_item_product_id ON order_items(product_id);

-- 3. 订单ID + 商品ID 复合索引（用于防重复和快速查询）
CREATE INDEX IF NOT EXISTS idx_order_item_order_product ON order_items(order_id, product_id);

-- 性能优化建议注释：
-- 1. 定期分析表统计信息：ANALYZE TABLE orders, order_items;
-- 2. 监控慢查询日志，识别需要进一步优化的查询
-- 3. 考虑按时间分区存储历史订单数据
-- 4. 对于大数据量场景，可考虑读写分离
-- 5. 缓存热点数据，减少数据库查询压力

-- 查询性能监控视图（可选）
-- CREATE VIEW order_performance_stats AS
-- SELECT 
--     DATE(create_time) as order_date,
--     status,
--     COUNT(*) as order_count,
--     AVG(payable_amount) as avg_amount,
--     SUM(payable_amount) as total_amount
-- FROM orders 
-- WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
-- GROUP BY DATE(create_time), status
-- ORDER BY order_date DESC, status;