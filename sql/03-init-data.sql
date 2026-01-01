-- ============================================
-- 店铺和商品初始化数据脚本
-- 创建10个正规店铺及其商品
-- @since 2025-12-29
-- ============================================

-- 设置客户端编码为 utf8mb4，防止中文乱码
SET NAMES utf8mb4;

-- ============================================
-- 第一部分：商家和店铺数据 (mall_merchant)
-- ============================================
USE `mall_merchant`;

-- 清理已有测试数据（可选，谨慎使用）
-- DELETE FROM merchant WHERE id >= 1001;

-- 插入10个商家
-- 密码统一为 admin123 ($2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q)
INSERT INTO `merchant` (`id`, `merchant_code`, `username`, `password`, `shop_name`, `company_name`, `merchant_type`, `contact_name`, `contact_phone`, `contact_email`, `id_number`, `bank_card_number`, `bank_name`, `shop_description`, `shop_logo`, `approval_status`, `status`, `deposit_paid`, `created_time`) VALUES
(1001, 'MC20251229001', 'yeston', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', '盈通官方旗舰店', '盈通科技有限公司', 2, '李明', '13800138001', 'yeston@example.com', '440300198001010001', '6222021234567890001', '中国工商银行深圳分行', '盈通科技官方旗舰店，专注显卡、主板等电脑硬件产品，提供正品保障和优质售后服务。', '/images/store/yeston-logo.png', 1, 1, 1, NOW()),
(1002, 'MC20251229002', 'vivo', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', 'vivo官方旗舰店', '维沃移动通信有限公司', 2, '王芳', '13800138002', 'vivo@example.com', '440300198001010002', '6222021234567890002', '中国建设银行东莞分行', 'vivo官方旗舰店，专注智能手机研发与销售，为用户提供极致的拍照和音乐体验。', '/images/store/vivo-logo.png', 1, 1, 1, NOW()),
(1003, 'MC20251229003', 'huawei', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', '华为官方旗舰店', '华为技术有限公司', 2, '赵强', '13800138003', 'huawei@example.com', '440300198001010003', '6222021234567890003', '中国银行深圳分行', '华为官方旗舰店，全球领先的ICT解决方案提供商，提供智能手机、平板、笔记本等产品。', '/images/store/huawei-logo.png', 1, 1, 1, NOW()),
(1004, 'MC20251229004', 'apple', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', 'Apple官方旗舰店', '苹果电子产品商贸（北京）有限公司', 2, '陈静', '13800138004', 'apple@example.com', '110000198001010004', '6222021234567890004', '招商银行北京分行', 'Apple官方旗舰店，提供iPhone、iPad、Mac等苹果全系列产品，享受官方品质保障。', '/images/store/apple-logo.png', 1, 1, 1, NOW()),
(1005, 'MC20251229005', 'oppo', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', 'OPPO官方旗舰店', 'OPPO广东移动通信有限公司', 2, '刘洋', '13800138005', 'oppo@example.com', '440300198001010005', '6222021234567890005', '中国农业银行东莞分行', 'OPPO官方旗舰店，专注美颜拍照手机，为年轻用户打造时尚科技产品。', '/images/store/oppo-logo.png', 1, 1, 1, NOW()),
(1006, 'MC20251229006', 'sony', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', 'Sony官方旗舰店', '索尼（中国）有限公司', 2, '孙丽', '13800138006', 'sony@example.com', '310000198001010006', '6222021234567890006', '交通银行上海分行', 'Sony官方旗舰店，提供相机、耳机、游戏机等消费电子产品，传承日本匠心品质。', '/images/store/sony-logo.png', 1, 1, 1, NOW()),
(1007, 'MC20251229007', 'sennheiser', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', '森海塞尔官方旗舰店', '森海塞尔电子（北京）有限公司', 2, '周杰', '13800138007', 'sennheiser@example.com', '110000198001010007', '6222021234567890007', '中信银行北京分行', '森海塞尔官方旗舰店，德国专业音频设备制造商，提供高端耳机和麦克风产品。', '/images/store/sennheiser-logo.png', 1, 1, 1, NOW()),
(1008, 'MC20251229008', 'nike', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', 'Nike官方旗舰店', '耐克体育（中国）有限公司', 2, '吴敏', '13800138008', 'nike@example.com', '310000198001010008', '6222021234567890008', '浦发银行上海分行', 'Nike官方旗舰店，全球知名运动品牌，提供运动鞋、运动服饰等专业运动装备。', '/images/store/nike-logo.png', 1, 1, 1, NOW()),
(1009, 'MC20251229009', 'samsung', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', '三星官方旗舰店', '三星电子（中国）有限公司', 2, '金秀贤', '13800138009', 'samsung@example.com', '110000198001010009', '6222021234567890009', '建设银行北京分行', '三星官方旗舰店，全球领先的电子产品制造商，提供手机、平板、家电等全系列产品。', '/images/store/samsung-logo.png', 1, 1, 1, NOW()),
(1010, 'MC20251229010', 'xiaomi', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', '小米官方旗舰店', '小米科技有限责任公司', 2, '卢伟冰', '13800138010', 'xiaomi@example.com', '110000198001010010', '6222021234567890010', '招商银行北京分行', '小米官方旗舰店，让每个人都能享受科技的乐趣，提供手机、智能家居等高性价比产品。', '/images/store/xiaomi-logo.png', 1, 1, 1, NOW());


-- ============================================
-- 第二部分：商品分类数据 (mall_product)
-- ============================================
USE `mall_product`;

-- 插入商品分类（如果不存在）
INSERT IGNORE INTO `categories` (`id`, `parent_id`, `name`, `icon`, `sort_order`, `status`) VALUES
(1, 0, '电子产品', 'electronics', 1, 1),
(2, 0, '服装鞋帽', 'clothing', 2, 1),
(3, 0, '家居生活', 'home', 3, 1),
(11, 1, '手机通讯', 'phone', 1, 1),
(12, 1, '电脑硬件', 'computer', 2, 1),
(13, 1, '平板电脑', 'tablet', 3, 1),
(14, 1, '智能穿戴', 'wearable', 4, 1),
(15, 1, '耳机音响', 'audio', 5, 1),
(21, 2, '运动鞋', 'shoes', 1, 1),
(22, 2, '运动服饰', 'sportswear', 2, 1);

-- ============================================
-- 第三部分：商品数据 (mall_product)
-- 使用新的 products 表结构
-- ============================================

-- 清理已有测试数据（可选，谨慎使用）
-- DELETE FROM products WHERE merchant_id >= 1001;

-- ============================================
-- 店铺1：盈通官方旗舰店 - 显卡产品 (merchant_id=1001)
-- ============================================
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `category_id`, `brand_name`, `main_image`, `detail_images`, `created_time`) VALUES
(1001, '盈通 RTX5070Ti 16G D7 樱瞳水着 Atlantis OC', 
'盈通RTX5070Ti樱瞳水着显卡，采用NVIDIA最新Blackwell架构，16GB GDDR7显存，支持DLSS 4技术和光线追踪。独特的二次元樱瞳水着主题设计，RGB灯效炫酷，三风扇散热系统，静音高效。适合高端游戏玩家和内容创作者，畅玩4K光追大作。', 
6999.00, 7499.00, 500, 1, 12, '盈通', '/images/盈通 RTX5070Ti - 16G D7 樱瞳水着 Atlantis OC.png', 
'["/images/盈通水着介绍1.jpg", "/images/盈通水着介绍2.jpg", "/images/盈通水着介绍3.jpg", "/images/盈通水着介绍4.jpg", "/images/盈通水着介绍5.jpg"]', NOW()),

(1001, '盈通 RX9070XT 16G D6 樱瞳花嫁 OC', 
'盈通RX9070XT樱瞳花嫁显卡，基于AMD RDNA 4架构，16GB GDDR6显存，支持FSR 3技术。浪漫花嫁主题外观设计，白色PCB搭配粉色点缀，三风扇大面积散热，RGB灯效可自定义。性能强劲，功耗优化，是AMD平台玩家的理想选择。', 
5999.00, 6299.00, 300, 1, 12, '盈通', '/images/盈通 RX9070XT-16GD6 樱瞳花嫁 OC.png', 
'["/images/花嫁介绍1.jpg", "/images/花嫁介绍2.jpg", "/images/花嫁介绍3.jpg", "/images/花嫁介绍4.jpg", "/images/花嫁介绍5.jpg"]', NOW()),

(1001, '盈通 RTX4060 8G D6 樱瞳 OC', 
'盈通RTX4060樱瞳显卡，采用Ada Lovelace架构，8GB GDDR6显存，支持DLSS 3和光线追踪。入门级光追显卡，功耗仅115W，无需外接供电。樱瞳系列经典设计，双风扇静音散热，适合1080P游戏玩家和轻度创作者使用。', 
2499.00, 2699.00, 800, 1, 12, '盈通', '/images/盈通水着介绍6.jpg', 
'["/images/盈通水着介绍7.jpg", "/images/盈通水着介绍8.jpg", "/images/盈通水着介绍9.jpg"]', NOW()),

(1001, '盈通 RTX4070 12G D6X 樱瞳水着 OC', 
'盈通RTX4070樱瞳水着显卡，Ada Lovelace架构，12GB GDDR6X高速显存，支持DLSS 3帧生成技术。中高端定位，2K游戏流畅运行，光追效果出色。水着主题设计，三风扇散热，RGB灯效，是性价比之选。', 
4599.00, 4999.00, 400, 1, 12, '盈通', '/images/盈通水着介绍10.jpg', 
'["/images/盈通水着介绍11.jpg", "/images/花嫁介绍6.jpg", "/images/花嫁介绍7.jpg"]', NOW()),

(1001, '盈通 RX7800XT 16G D6 樱瞳花嫁 OC', 
'盈通RX7800XT樱瞳花嫁显卡，RDNA 3架构，16GB大显存，支持FSR 2.0技术。2K游戏性能出色，显存充足适合高分辨率材质。花嫁主题白色外观，三风扇散热设计，静音运行，是AMD中高端显卡的优质选择。', 
3999.00, 4299.00, 350, 1, 12, '盈通', '/images/花嫁介绍8.jpg', 
'["/images/花嫁介绍9.jpg", "/images/花嫁介绍10.jpg", "/images/花嫁介绍11.jpg"]', NOW());


-- ============================================
-- 店铺2：vivo官方旗舰店 - 手机产品 (merchant_id=1002)
-- ============================================
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `category_id`, `brand_name`, `main_image`, `detail_images`, `created_time`) VALUES
(1002, 'vivo X200 Pro 5G智能手机', 
'vivo X200 Pro旗舰手机，搭载天玑9400处理器，蔡司光学镜头系统，2亿像素主摄，支持100倍数字变焦。6.78英寸2K AMOLED屏幕，120Hz刷新率，5400mAh大电池，100W闪充。OriginOS 5系统，流畅智能体验。', 
5999.00, 6299.00, 600, 1, 11, 'vivo', '/images/vivio X200pro.png', 
'["/images/vivo x200 ultra.jpg", "/images/vivo X200s.png"]', NOW()),

(1002, 'vivo X200s 5G智能手机', 
'vivo X200s轻薄旗舰，天玑9300+处理器，蔡司影像系统，5000万像素三摄。6.67英寸AMOLED曲面屏，1.5K分辨率，轻薄机身仅7.19mm。5000mAh电池，90W快充，续航持久。适合追求轻薄手感的用户。', 
4299.00, 4599.00, 700, 1, 11, 'vivo', '/images/vivo X200s.png', 
'["/images/vivio X200pro.png", "/images/vivo x100s pro.png"]', NOW()),

(1002, 'vivo X200 Pro mini 5G智能手机', 
'vivo X200 Pro mini小屏旗舰，6.31英寸小尺寸屏幕，单手操作舒适。天玑9400处理器，蔡司影像系统，性能不妥协。5700mAh电池，90W快充，小身材大能量。专为喜欢小屏手机的用户打造。', 
4999.00, 5299.00, 450, 1, 11, 'vivo', '/images/vivo X200pro mini.png', 
'["/images/vivo x100s.png", "/images/vivo x100s pro.png"]', NOW()),

(1002, 'vivo X Fold5 折叠屏手机', 
'vivo X Fold5折叠屏旗舰，8.03英寸内屏+6.53英寸外屏，骁龙8 Gen3处理器。蔡司影像系统，5500mAh大电池，100W快充。航空级铰链设计，折叠无痕，商务办公与娱乐兼顾的高端选择。', 
9999.00, 10999.00, 200, 1, 11, 'vivo', '/images/vivo  X Fold5轮播图.jpg', 
'["/images/vivio X200pro.png", "/images/vivo X200s.png"]', NOW()),

(1002, 'vivo X100s Pro 5G智能手机', 
'vivo X100s Pro影像旗舰，天玑9300处理器，蔡司APO超级长焦，2亿像素潜望式长焦镜头。6.78英寸AMOLED屏幕，5400mAh电池，100W快充。专业影像能力，记录生活每一个精彩瞬间。', 
4999.00, 5299.00, 500, 1, 11, 'vivo', '/images/vivo x100s pro.png', 
'["/images/vivo x100s.png", "/images/vivo X200s.png"]', NOW()),

(1002, 'vivo X100s 5G智能手机', 
'vivo X100s标准版，天玑9300处理器，蔡司影像系统，5000万像素主摄。6.67英寸AMOLED屏幕，5000mAh电池，100W快充。均衡配置，性价比出色，是日常使用的理想选择。', 
3999.00, 4299.00, 800, 1, 11, 'vivo', '/images/vivo x100s.png', 
'["/images/vivo x100s pro.png", "/images/vivio X200pro.png"]', NOW());


-- ============================================
-- 店铺3：华为官方旗舰店 - 手机/平板/手表 (merchant_id=1003)
-- ============================================
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `category_id`, `brand_name`, `main_image`, `detail_images`, `created_time`) VALUES
(1003, '华为 Mate 70 Pro+ 5G智能手机', 
'华为Mate 70 Pro+旗舰手机，搭载麒麟9100处理器，鸿蒙HarmonyOS 5系统。超感知影像系统，可变光圈镜头，AI摄影大师。6.9英寸LTPO OLED屏幕，5600mAh电池，100W有线+50W无线快充。商务旗舰，科技巅峰。', 
8999.00, 9499.00, 300, 1, 11, '华为', '/images/huawei-mate70-pro-plus.png', 
'["/images/huawei-mate70-pro.png", "/images/huawei-mate70-rs-ultimate-design.png"]', NOW()),

(1003, '华为 Mate 70 Pro 5G智能手机', 
'华为Mate 70 Pro旗舰手机，麒麟9100处理器，鸿蒙HarmonyOS 5系统。超感知影像系统，5000万像素主摄，支持10倍光学变焦。6.8英寸OLED屏幕，5500mAh电池，100W快充。旗舰性能，商务首选。', 
7499.00, 7999.00, 400, 1, 11, '华为', '/images/huawei-mate70-pro.png', 
'["/images/huawei-mate70-pro-plus.png", "/images/HUAWEI-pura80-pro.png"]', NOW()),

(1003, '华为 Pura 80 Ultra 5G智能手机', 
'华为Pura 80 Ultra影像旗舰，1英寸大底主摄，可变光圈f/1.4-f/4.0，超光变长焦镜头。麒麟9100处理器，6.8英寸LTPO OLED屏幕。5200mAh电池，100W快充。专业影像，记录光影艺术。', 
9999.00, 10499.00, 250, 1, 11, '华为', '/images/HUAWEI-pura80-ultra.png', 
'["/images/HUAWEI-pura80-pro.png", "/images/HUAWEI-pura80.png"]', NOW()),

(1003, '华为 Pura 80 Pro+ 5G智能手机', 
'华为Pura 80 Pro+高端旗舰，超感知影像系统，可变光圈主摄，潜望式长焦镜头。麒麟9100处理器，鸿蒙HarmonyOS 5系统。6.8英寸OLED屏幕，5000mAh电池，100W快充。影像与性能的完美结合。', 
8499.00, 8999.00, 350, 1, 11, '华为', '/images/HUAWEI pura80 pro+.jpg', 
'["/images/HUAWEI-pura80-ultra.png", "/images/HUAWEI-pura80-pro.png"]', NOW()),

(1003, '华为 Mate X6 折叠屏手机', 
'华为Mate X6折叠屏旗舰，7.93英寸内屏+6.4英寸外屏，麒麟9100处理器。超感知影像系统，5500mAh大电池，66W快充。航天级铰链设计，折叠无痕，商务精英的身份象征。', 
13999.00, 14999.00, 150, 1, 11, '华为', '/images/huawei-mate-x6.png', 
'["/images/huawei-mate-xt-ultimate-design.png", "/images/huawei-pocket-2.png"]', NOW()),

(1003, '华为 MatePad Pro 13.2英寸平板电脑', 
'华为MatePad Pro 13.2英寸旗舰平板，13.2英寸OLED柔性屏，2.8K分辨率，144Hz刷新率。麒麟9000S处理器，鸿蒙HarmonyOS 4系统。支持M-Pencil手写笔，生产力工具，创作利器。', 
6499.00, 6999.00, 400, 1, 13, '华为', '/images/HUAWEI MatePad Pro 13,2英寸轮播图.jpeg', 
'["/images/HUAWEI-matepad-pro-13.2-id.jpeg", "/images/huawei-MateBook E 系列轮播图.jpeg"]', NOW()),

(1003, '华为 WATCH 5 智能手表', 
'华为WATCH 5智能手表，1.43英寸AMOLED屏幕，钛金属表壳，蓝宝石玻璃镜面。支持血氧、心率、睡眠监测，100+运动模式。14天超长续航，5ATM防水，健康管理专家。', 
2499.00, 2699.00, 600, 1, 14, '华为', '/images/HUAWIE-watch-5.jpg', 
'["/images/HUAWEI-watch-4-pro-space-edition.png"]', NOW()),

(1003, '华为 MateBook 14 笔记本电脑', 
'华为MateBook 14轻薄本，14英寸2K触控屏，酷睿Ultra处理器，16GB内存+512GB SSD。金属机身，轻薄便携，多屏协同功能。70Wh大电池，65W快充，商务办公首选。', 
6999.00, 7499.00, 350, 1, 12, '华为', '/images/huawei-laptops-shelf-matebook-14.jpeg', 
'["/images/huawei-laptops-shelf-matebook-gt-14.jpeg", "/images/huawei-MateBook E 系列轮播图.jpeg"]', NOW());


-- ============================================
-- 店铺4：Apple官方旗舰店 - 苹果产品 (merchant_id=1004)
-- ============================================
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `category_id`, `brand_name`, `main_image`, `detail_images`, `created_time`) VALUES
(1004, 'iPhone 16 Pro Max 256GB', 
'Apple iPhone 16 Pro Max，A18 Pro仿生芯片，6.9英寸超视网膜XDR显示屏，ProMotion自适应刷新率。4800万像素Pro级摄像头系统，5倍光学变焦。钛金属设计，USB-C接口，全天候电池续航。', 
10999.00, 11499.00, 500, 1, 11, 'Apple', '/images/iphone16promaxjpg.jpg', 
'["/images/iphone16pro.jpg", "/images/iphone16jpg.jpg"]', NOW()),

(1004, 'iPhone 16 Pro 256GB', 
'Apple iPhone 16 Pro，A18 Pro仿生芯片，6.3英寸超视网膜XDR显示屏。4800万像素Pro级摄像头系统，支持ProRes视频录制。钛金属边框，轻盈耐用，专业级影像创作工具。', 
8999.00, 9499.00, 600, 1, 11, 'Apple', '/images/iphone16pro.jpg', 
'["/images/iphone16promaxjpg.jpg", "/images/iphone16jpg.jpg"]', NOW()),

(1004, 'iPhone 16 128GB', 
'Apple iPhone 16，A18仿生芯片，6.1英寸超视网膜XDR显示屏。4800万像素双摄系统，支持4K杜比视界HDR录制。航空级铝金属边框，多彩配色，日常使用的理想选择。', 
6299.00, 6799.00, 800, 1, 11, 'Apple', '/images/iphone16jpg.jpg', 
'["/images/iphone16pro.jpg", "/images/iphone15.png"]', NOW()),

(1004, 'iPhone 15 128GB', 
'Apple iPhone 15，A16仿生芯片，6.1英寸超视网膜XDR显示屏。4800万像素主摄，支持2倍光学变焦。灵动岛设计，USB-C接口，经典之选，性价比出众。', 
5499.00, 5999.00, 700, 1, 11, 'Apple', '/images/iphone15.png', 
'["/images/iphone16jpg.jpg", "/images/iphone.jpg"]', NOW()),

(1004, 'MacBook Pro 14英寸 M3 Pro', 
'Apple MacBook Pro 14英寸，M3 Pro芯片，12核CPU+18核GPU，18GB统一内存。14.2英寸Liquid视网膜XDR显示屏，ProMotion技术。MagSafe充电，长达17小时电池续航，专业创作者的生产力工具。', 
16999.00, 17999.00, 300, 1, 12, 'Apple', '/images/macbook.jpg', 
'["/images/tablet.jpg", "/images/iphone16pro.jpg"]', NOW()),

(1004, 'iPad Pro 11英寸 M4芯片', 
'Apple iPad Pro 11英寸，M4芯片，超薄设计仅5.3mm。Ultra Retina XDR显示屏，ProMotion技术，支持Apple Pencil Pro。12MP超广角前置摄像头，Face ID解锁，专业级平板电脑。', 
8499.00, 8999.00, 400, 1, 13, 'Apple', '/images/tablet.jpg', 
'["/images/macbook.jpg", "/images/iphone16pro.jpg"]', NOW());


-- ============================================
-- 店铺5：OPPO官方旗舰店 - 手机产品 (merchant_id=1005)
-- ============================================
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `category_id`, `brand_name`, `main_image`, `detail_images`, `created_time`) VALUES
(1005, 'OPPO Find X8 Ultra 5G智能手机', 
'OPPO Find X8 Ultra影像旗舰，天玑9400处理器，哈苏影像系统，1英寸大底主摄。6.82英寸2K LTPO AMOLED屏幕，5800mAh电池，100W闪充+50W无线充。专业影像，旗舰性能。', 
6999.00, 7499.00, 400, 1, 11, 'OPPO', '/images/oppo Find X8 Ultra.png', 
'["/images/oppo find N5.png"]', NOW()),

(1005, 'OPPO Find N5 折叠屏手机', 
'OPPO Find N5折叠屏旗舰，7.82英寸内屏+6.31英寸外屏，骁龙8 Gen3处理器。哈苏影像系统，5700mAh大电池，67W快充。超轻薄设计，折叠厚度仅9.3mm，时尚与科技的完美融合。', 
8999.00, 9499.00, 250, 1, 11, 'OPPO', '/images/oppo find N5.png', 
'["/images/oppo Find X8 Ultra.png"]', NOW()),

(1005, 'OPPO Reno12 Pro 5G智能手机', 
'OPPO Reno12 Pro，天玑9200+处理器，5000万像素AI人像三摄。6.7英寸AMOLED曲面屏，120Hz刷新率。5000mAh电池，80W闪充，轻薄时尚，年轻人的潮流之选。', 
3499.00, 3799.00, 600, 1, 11, 'OPPO', '/images/oppo Find X8 Ultra.png', 
'["/images/oppo find N5.png"]', NOW()),

(1005, 'OPPO A3 Pro 5G智能手机', 
'OPPO A3 Pro，天玑7050处理器，6400万像素AI三摄。6.7英寸LCD屏幕，120Hz刷新率。5000mAh大电池，67W闪充，IP65防尘防水，耐用可靠的中端选择。', 
1999.00, 2199.00, 900, 1, 11, 'OPPO', '/images/oppo find N5.png', 
'["/images/oppo Find X8 Ultra.png"]', NOW()),

(1005, 'OPPO Pad 3 平板电脑', 
'OPPO Pad 3平板电脑，11.61英寸2.8K LCD屏幕，144Hz刷新率。天玑9200处理器，8GB+256GB存储。9510mAh大电池，67W闪充，支持OPPO Pencil，学习办公好帮手。', 
2999.00, 3299.00, 500, 1, 13, 'OPPO', '/images/tablet.jpg', 
'["/images/oppo Find X8 Ultra.png"]', NOW());


-- ============================================
-- 店铺6：Sony官方旗舰店 - 耳机/相机 (merchant_id=1006)
-- ============================================
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `category_id`, `brand_name`, `main_image`, `detail_images`, `created_time`) VALUES
(1006, 'Sony WH-1000XM5 无线降噪耳机', 
'Sony WH-1000XM5旗舰降噪耳机，行业领先的主动降噪技术，8个麦克风精准降噪。30mm驱动单元，Hi-Res Audio认证，LDAC高清传输。30小时续航，快充3分钟听歌3小时，商务出行必备。', 
2999.00, 3299.00, 500, 1, 15, 'Sony', '/images/headphones.jpg', 
'["/images/headphones.jpg"]', NOW()),

(1006, 'Sony WF-1000XM5 真无线降噪耳机', 
'Sony WF-1000XM5真无线旗舰，全球最小最轻的降噪豆。集成处理器V2，HD降噪处理器QN2e，双芯降噪。8.4mm驱动单元，LDAC/DSEE Extreme，8小时续航+充电盒16小时。', 
2299.00, 2499.00, 600, 1, 15, 'Sony', '/images/headphones.jpg', 
'["/images/headphones.jpg"]', NOW()),

(1006, 'Sony LinkBuds S 真无线耳机', 
'Sony LinkBuds S轻巧降噪耳机，仅4.8g超轻单耳重量。集成处理器V1，自适应声音控制。5mm驱动单元，LDAC高清传输，6小时续航+充电盒14小时。日常通勤的舒适选择。', 
1299.00, 1499.00, 700, 1, 15, 'Sony', '/images/headphones.jpg', 
'["/images/headphones.jpg"]', NOW()),

(1006, 'Sony Alpha 7 IV 全画幅微单相机', 
'Sony A7M4全画幅微单，3300万像素Exmor R CMOS传感器，BIONZ XR处理器。759点相位检测自动对焦，10fps连拍。4K 60p视频录制，5轴防抖，专业摄影师的创作利器。', 
16999.00, 17999.00, 200, 1, 12, 'Sony', '/images/headphones.jpg', 
'["/images/headphones.jpg"]', NOW()),

(1006, 'Sony ZV-E10 II Vlog相机', 
'Sony ZV-E10 II Vlog相机，2600万像素APS-C传感器，可换镜头设计。4K 60p视频，S-Cinetone色彩科学。侧翻屏设计，产品展示模式，内容创作者的理想选择。', 
5999.00, 6499.00, 350, 1, 12, 'Sony', '/images/headphones.jpg', 
'["/images/headphones.jpg"]', NOW());


-- ============================================
-- 店铺7：森海塞尔官方旗舰店 - 专业音频 (merchant_id=1007)
-- ============================================
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `category_id`, `brand_name`, `main_image`, `detail_images`, `created_time`) VALUES
(1007, 'Sennheiser HD 660S2 开放式耳机', '森海塞尔HD 660S2开放式参考级耳机，德国原产，手工组装。42mm换能器，阻抗300Ω，频响范围8Hz-41.5kHz。天鹅绒耳垫，轻量化设计，发烧友的HiFi之选。', 4999.00, 5499.00, 200, 1, 15, 'Sennheiser', '/images/headphones.jpg', '["/images/headphones.jpg"]', NOW()),
(1007, 'Sennheiser HD 600 经典开放式耳机', '森海塞尔HD 600经典开放式耳机，传奇型号，录音室标准。42mm换能器，阻抗300Ω，频响范围12Hz-39kHz。德国制造，经久耐用，音频工程师的参考标准。', 2999.00, 3299.00, 300, 1, 15, 'Sennheiser', '/images/headphones.jpg', '["/images/headphones.jpg"]', NOW()),
(1007, 'Sennheiser Momentum 4 无线耳机', '森海塞尔Momentum 4无线耳机，自适应降噪技术，42mm换能器。aptX Adaptive高清传输，60小时超长续航。真皮头梁，记忆海绵耳垫，奢华音质体验。', 2999.00, 3499.00, 400, 1, 15, 'Sennheiser', '/images/headphones.jpg', '["/images/headphones.jpg"]', NOW()),
(1007, 'Sennheiser IE 600 入耳式耳机', '森海塞尔IE 600旗舰入耳式耳机，7mm TrueResponse换能器，德国设计制造。锆合金外壳，MMCX可换线设计。频响范围4Hz-46.5kHz，发烧级便携HiFi。', 5499.00, 5999.00, 150, 1, 15, 'Sennheiser', '/images/headphones.jpg', '["/images/headphones.jpg"]', NOW()),
(1007, 'Sennheiser Profile USB 麦克风', '森海塞尔Profile USB播客麦克风，心形指向性，24bit/48kHz采样。内置增益控制，3.5mm耳机监听。即插即用，适合播客、直播、会议等场景。', 1299.00, 1499.00, 500, 1, 15, 'Sennheiser', '/images/headphones.jpg', '["/images/headphones.jpg"]', NOW());


-- ============================================
-- 店铺8：Nike官方旗舰店 - 运动产品 (merchant_id=1008)
-- ============================================
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `category_id`, `brand_name`, `main_image`, `detail_images`, `created_time`) VALUES
(1008, 'Nike Air Max 270 男子运动鞋', 'Nike Air Max 270男子运动鞋，标志性大气垫设计，270度可视Air气垫单元。网眼鞋面透气舒适，泡棉中底缓震回弹。经典配色，街头潮流与运动性能的完美结合，日常穿搭百搭之选。', 1299.00, 1499.00, 800, 1, 21, 'Nike', '/images/shoes.jpg', '["/images/shoes.jpg"]', NOW()),
(1008, 'Nike Air Force 1 07 经典板鞋', 'Nike Air Force 1 07经典板鞋，1982年诞生的传奇鞋款。全粒面皮革鞋面，Air气垫缓震科技。经典白色设计，百搭不过时，街头文化的标志性单品。', 899.00, 999.00, 1000, 1, 21, 'Nike', '/images/shoes.jpg', '["/images/shoes.jpg"]', NOW()),
(1008, 'Nike Dunk Low 复古板鞋', 'Nike Dunk Low复古板鞋，1985年篮球鞋经典复刻。皮革鞋面，泡棉中底，橡胶外底。多彩配色可选，复古潮流风格，年轻人的时尚宣言。', 799.00, 899.00, 900, 1, 21, 'Nike', '/images/shoes.jpg', '["/images/shoes.jpg"]', NOW()),
(1008, 'Nike Zoom Pegasus 41 跑步鞋', 'Nike Zoom Pegasus 41专业跑步鞋，Zoom Air气垫前后掌缓震。ReactX泡棉中底，能量回馈出色。Flywire飞线技术锁定足部，透气网眼鞋面，马拉松训练的可靠伙伴。', 999.00, 1099.00, 700, 1, 21, 'Nike', '/images/shoes.jpg', '["/images/shoes.jpg"]', NOW()),
(1008, 'Nike Pro Dri-FIT 男子紧身衣', 'Nike Pro Dri-FIT男子紧身训练上衣，Dri-FIT速干科技，快速排汗保持干爽。弹力面料贴合身形，平锁缝线减少摩擦。适合健身、跑步、球类运动等多种场景。', 299.00, 349.00, 600, 1, 22, 'Nike', '/images/shoes.jpg', '["/images/shoes.jpg"]', NOW()),
(1008, 'Nike Sportswear Club 男子卫衣', 'Nike Sportswear Club男子连帽卫衣，柔软抓绒内里，保暖舒适。经典Swoosh标志，袋鼠口袋设计。休闲百搭，运动与日常穿搭皆宜。', 499.00, 599.00, 500, 1, 22, 'Nike', '/images/shoes.jpg', '["/images/shoes.jpg"]', NOW());


-- ============================================
-- 店铺9：三星官方旗舰店 - 手机/平板 (merchant_id=1009)
-- ============================================
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `category_id`, `brand_name`, `main_image`, `detail_images`, `created_time`) VALUES
(1009, 'Samsung Galaxy S24 Ultra 5G智能手机', '三星Galaxy S24 Ultra旗舰手机，骁龙8 Gen3 for Galaxy处理器，Galaxy AI智能功能。2亿像素主摄，100倍空间变焦。6.8英寸QHD+ Dynamic AMOLED屏幕，S Pen手写笔，商务旗舰之选。', 9999.00, 10999.00, 400, 1, 11, 'Samsung', '/images/iphone.jpg', '["/images/tablet.jpg"]', NOW()),
(1009, 'Samsung Galaxy S24+ 5G智能手机', '三星Galaxy S24+大屏旗舰，骁龙8 Gen3处理器，Galaxy AI智能功能。5000万像素三摄系统，3倍光学变焦。6.7英寸QHD+ Dynamic AMOLED屏幕，4900mAh电池，45W快充。', 7999.00, 8499.00, 500, 1, 11, 'Samsung', '/images/iphone.jpg', '["/images/tablet.jpg"]', NOW()),
(1009, 'Samsung Galaxy Z Fold6 折叠屏手机', '三星Galaxy Z Fold6折叠屏旗舰，7.6英寸内屏+6.3英寸外屏。骁龙8 Gen3处理器，Galaxy AI智能功能。IPX8防水，装甲铝框架，折叠屏技术领导者。', 14999.00, 15999.00, 200, 1, 11, 'Samsung', '/images/iphone.jpg', '["/images/tablet.jpg"]', NOW()),
(1009, 'Samsung Galaxy Z Flip6 折叠屏手机', '三星Galaxy Z Flip6小折叠旗舰，6.7英寸内屏+3.4英寸外屏。骁龙8 Gen3处理器，Galaxy AI智能功能。FlexCam灵动拍摄，时尚小巧，潮流人士的个性之选。', 8999.00, 9499.00, 350, 1, 11, 'Samsung', '/images/iphone.jpg', '["/images/tablet.jpg"]', NOW()),
(1009, 'Samsung Galaxy Tab S9 Ultra 平板电脑', '三星Galaxy Tab S9 Ultra旗舰平板，14.6英寸Dynamic AMOLED 2X屏幕，120Hz刷新率。骁龙8 Gen2处理器，IP68防水防尘。S Pen手写笔，DeX桌面模式，生产力巅峰。', 9999.00, 10999.00, 250, 1, 13, 'Samsung', '/images/tablet.jpg', '["/images/iphone.jpg"]', NOW());


-- ============================================
-- 店铺10：小米官方旗舰店 - 手机/智能家居 (merchant_id=1010)
-- ============================================
INSERT INTO `products` (`merchant_id`, `name`, `description`, `price`, `original_price`, `stock`, `status`, `category_id`, `brand_name`, `main_image`, `detail_images`, `created_time`) VALUES
(1010, '小米15 Pro 5G智能手机', '小米15 Pro旗舰手机，骁龙8至尊版处理器，徕卡光学镜头系统。5000万像素主摄，支持10倍光学变焦。6.73英寸2K LTPO AMOLED屏幕，5400mAh电池，120W快充，性能怪兽。', 5299.00, 5599.00, 600, 1, 11, '小米', '/images/iphone.jpg', '["/images/tablet.jpg"]', NOW()),
(1010, '小米15 5G智能手机', '小米15标准版，骁龙8至尊版处理器，徕卡影像系统。5000万像素三摄，光学防抖。6.36英寸1.5K AMOLED屏幕，5400mAh电池，90W快充。小尺寸旗舰，单手操作舒适。', 4499.00, 4799.00, 700, 1, 11, '小米', '/images/iphone.jpg', '["/images/tablet.jpg"]', NOW()),
(1010, '小米MIX Fold 4 折叠屏手机', '小米MIX Fold 4折叠屏旗舰，7.98英寸内屏+6.56英寸外屏。骁龙8 Gen3处理器，徕卡影像系统。5100mAh电池，67W快充，超轻薄设计，折叠厚度仅9.47mm。', 8999.00, 9499.00, 300, 1, 11, '小米', '/images/iphone.jpg', '["/images/tablet.jpg"]', NOW()),
(1010, '小米Pad 6S Pro 平板电脑', '小米Pad 6S Pro旗舰平板，12.4英寸3K LCD屏幕，144Hz刷新率。骁龙8 Gen2处理器，10000mAh大电池，120W快充。支持小米灵感触控笔，生产力与娱乐兼顾。', 3499.00, 3799.00, 500, 1, 13, '小米', '/images/tablet.jpg', '["/images/iphone.jpg"]', NOW()),
(1010, '小米手环9 智能手环', '小米手环9智能手环，1.62英寸AMOLED屏幕，60Hz刷新率。支持血氧、心率、睡眠监测，150+运动模式。21天超长续航，5ATM防水，健康管理入门之选。', 249.00, 279.00, 1000, 1, 14, '小米', '/images/HUAWIE-watch-5.jpg', '["/images/HUAWEI-watch-4-pro-space-edition.png"]', NOW()),
(1010, '小米Watch S3 智能手表', '小米Watch S3智能手表，1.43英寸AMOLED屏幕，可更换表圈设计。支持eSIM独立通话，血氧、心率、睡眠监测。15天续航，5ATM防水，时尚与功能兼备。', 999.00, 1099.00, 600, 1, 14, '小米', '/images/HUAWIE-watch-5.jpg', '["/images/HUAWEI-watch-4-pro-space-edition.png"]', NOW()),
(1010, '小米路由器AX9000', '小米路由器AX9000三频WiFi6旗舰，12根高增益天线，覆盖面积达600㎡。2.5G网口，USB 3.0接口，支持Mesh组网。游戏加速，智能家居中枢，极客玩家的网络利器。', 999.00, 1099.00, 400, 1, 3, '小米', '/images/headphones.jpg', '["/images/headphones.jpg"]', NOW());

-- ============================================
-- 数据同步：将 mall_product.products 数据同步到 mall_merchant.merchant_product
-- ============================================
USE `mall_merchant`;

INSERT INTO `merchant_product` 
(merchant_id, product_name, category_id, brand, price, stock_quantity, main_image, images, description, status, is_recommended, is_new, is_hot, created_time)
SELECT 
    p.merchant_id,
    p.name,
    p.category_id,
    p.brand_name,
    p.price,
    p.stock,
    p.main_image,
    p.detail_images,
    p.description,
    1, -- status: 1-上架
    1, -- is_recommended
    1, -- is_new
    0, -- is_hot
    p.created_time
FROM `mall_product`.`products` p
WHERE p.merchant_id >= 1001;

-- ============================================
-- 完成
-- ============================================
SELECT '商家、分类、商品及同步数据初始化完成！' AS message;
