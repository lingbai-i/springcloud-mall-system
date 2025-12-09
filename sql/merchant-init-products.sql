-- 初始化商品测试数据
-- 快速创建10个测试商品供开发使用

INSERT INTO merchant_product (
    merchant_id, product_name, description, price, market_price, cost_price,
    stock_quantity, warning_stock, sales_count, status, is_recommended, is_new, is_hot,
    category_id, brand, main_image, images, unit, sort_order,
    create_time, update_time
) VALUES
-- 商品1: iPhone 15 Pro
(1, 'iPhone 15 Pro 256GB 钛金属', 
 'Apple iPhone 15 Pro，A17 Pro芯片，钛金属设计，专业级摄像系统', 
 8999.00, 9999.00, 7999.00, 
 100, 10, 0, 1, 1, 1, 1,
 1, 'Apple', 'https://via.placeholder.com/300', 'https://via.placeholder.com/300,https://via.placeholder.com/300', '台', 1,
 NOW(), NOW()),

-- 商品2: 小米13 Ultra
(1, '小米13 Ultra 16GB+512GB 黑色', 
 '骁龙8 Gen2，徕卡光学镜头，2K曲面屏', 
 5999.00, 6499.00, 4999.00,
 150, 10, 0, 1, 1, 1, 1,
 1, '小米', 'https://via.placeholder.com/300', 'https://via.placeholder.com/300,https://via.placeholder.com/300', '台', 2,
 NOW(), NOW()),

-- 商品3: 华为Mate 60 Pro
(1, '华为Mate 60 Pro 12GB+512GB', 
 '星闪通信，超可靠玄武架构，昆仑玻璃', 
 6999.00, 7499.00, 5999.00,
 80, 10, 0, 1, 1, 1, 1,
 1, '华为', 'https://via.placeholder.com/300', 'https://via.placeholder.com/300,https://via.placeholder.com/300', '台', 3,
 NOW(), NOW()),

-- 商品4: MacBook Pro 14
(1, 'MacBook Pro 14英寸 M3 Pro芯片', 
 'Apple M3 Pro芯片，18小时续航，Liquid视网膜XDR显示屏', 
 15999.00, 17999.00, 13999.00,
 50, 5, 0, 1, 1, 0, 1,
 2, 'Apple', 'https://via.placeholder.com/300', 'https://via.placeholder.com/300,https://via.placeholder.com/300', '台', 4,
 NOW(), NOW()),

-- 商品5: 小米笔记本Pro 15
(1, '小米笔记本Pro 15 2024款', 
 '第14代英特尔酷睿i7，RTX 4060，3.2K OLED屏', 
 7999.00, 8999.00, 6999.00,
 60, 10, 0, 1, 1, 1, 0,
 2, '小米', 'https://via.placeholder.com/300', 'https://via.placeholder.com/300,https://via.placeholder.com/300', '台', 5,
 NOW(), NOW()),

-- 商品6: AirPods Pro 2
(1, 'AirPods Pro 第二代', 
 '主动降噪，空间音频，MagSafe充电盒', 
 1899.00, 1999.00, 1599.00,
 200, 20, 0, 1, 1, 1, 1,
 3, 'Apple', 'https://via.placeholder.com/300', 'https://via.placeholder.com/300,https://via.placeholder.com/300', '副', 6,
 NOW(), NOW()),

-- 商品7: 索尼WH-1000XM5
(1, '索尼WH-1000XM5 无线降噪耳机', 
 '业界领先降噪，30小时续航，多点连接', 
 2399.00, 2699.00, 1999.00,
 120, 10, 0, 1, 1, 0, 1,
 3, '索尼', 'https://via.placeholder.com/300', 'https://via.placeholder.com/300,https://via.placeholder.com/300', '副', 7,
 NOW(), NOW()),

-- 商品8: iPad Air 第五代
(1, 'iPad Air 第五代 10.9英寸 256GB', 
 'Apple M1芯片，Liquid视网膜显示屏，支持第二代Apple Pencil', 
 5199.00, 5699.00, 4499.00,
 90, 10, 0, 1, 1, 1, 0,
 4, 'Apple', 'https://via.placeholder.com/300', 'https://via.placeholder.com/300,https://via.placeholder.com/300', '台', 8,
 NOW(), NOW()),

-- 商品9: 小米平板6 Pro
(1, '小米平板6 Pro 12.4英寸 12GB+256GB', 
 '骁龙8+ Gen1，3K分辨率，144Hz高刷，8扬声器', 
 2999.00, 3299.00, 2499.00,
 110, 10, 0, 1, 1, 1, 1,
 4, '小米', 'https://via.placeholder.com/300', 'https://via.placeholder.com/300,https://via.placeholder.com/300', '台', 9,
 NOW(), NOW()),

-- 商品10: Apple Watch Series 9
(1, 'Apple Watch Series 9 GPS 45mm', 
 'S9芯片，全天候视网膜显示屏，血氧检测，心电图', 
 3199.00, 3499.00, 2699.00,
 75, 10, 0, 1, 1, 1, 1,
 5, 'Apple', 'https://via.placeholder.com/300', 'https://via.placeholder.com/300,https://via.placeholder.com/300', '只', 10,
 NOW(), NOW());
