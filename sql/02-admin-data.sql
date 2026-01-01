USE `mall_admin`;

-- 初始化角色数据
INSERT INTO `role` (`role_code`, `role_name`, `description`, `status`) VALUES
('SUPER_ADMIN', '超级管理员', '拥有所有权限', 1),
('USER_ADMIN', '用户管理员', '用户管理权限', 1),
('MERCHANT_ADMIN', '商家管理员', '商家审核权限', 1),
('ORDER_ADMIN', '订单管理员', '订单处理权限', 1),
('VIEWER', '只读管理员', '仅查看权限', 1);

-- 初始化权限数据
INSERT INTO `permission` (`permission_code`, `permission_name`, `resource_type`, `resource`, `action`, `description`) VALUES
-- 用户管理权限
('user:user:view', '查看用户', 'user', 'user', 'view', '查看用户列表和详情'),
('user:user:update', '修改用户', 'user', 'user', 'update', '修改用户状态'),
('user:user:export', '导出用户', 'user', 'user', 'export', '导出用户数据'),

-- 商家管理权限
('merchant:application:view', '查看商家申请', 'merchant', 'application', 'view', '查看商家申请列表'),
('merchant:application:approve', '审批商家申请', 'merchant', 'application', 'approve', '审批商家入驻申请'),
('merchant:merchant:view', '查看商家信息', 'merchant', 'merchant', 'view', '查看商家详情'),
('merchant:merchant:update', '修改商家信息', 'merchant', 'merchant', 'update', '修改商家状态和信息'),

-- 订单管理权限
('order:order:view', '查看订单', 'order', 'order', 'view', '查看订单列表和详情'),
('order:order:update', '修改订单', 'order', 'order', 'update', '修改订单状态'),
('order:refund:approve', '审批退款', 'order', 'refund', 'approve', '审批订单退款申请'),

-- 系统监控权限
('system:stats:view', '查看统计数据', 'system', 'stats', 'view', '查看系统统计数据'),
('system:health:view', '查看服务健康', 'system', 'health', 'view', '查看服务健康状态'),

-- 权限管理权限
('permission:role:view', '查看角色', 'permission', 'role', 'view', '查看角色列表'),
('permission:role:create', '创建角色', 'permission', 'role', 'create', '创建新角色'),
('permission:role:update', '修改角色', 'permission', 'role', 'update', '修改角色信息'),
('permission:role:delete', '删除角色', 'permission', 'role', 'delete', '删除角色'),
('permission:admin:assign', '分配管理员角色', 'permission', 'admin', 'assign', '给管理员分配角色'),

-- 审计日志权限
('audit:log:view', '查看审计日志', 'audit', 'log', 'view', '查看操作审计日志');

-- 为超级管理员角色分配所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM `role` WHERE role_code = 'SUPER_ADMIN'),
    id
FROM `permission`;

-- 为用户管理员分配用户相关权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM `role` WHERE role_code = 'USER_ADMIN'),
    id
FROM `permission`
WHERE permission_code LIKE 'user:%' OR permission_code = 'audit:log:view';

-- 为商家管理员分配商家相关权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM `role` WHERE role_code = 'MERCHANT_ADMIN'),
    id
FROM `permission`
WHERE permission_code LIKE 'merchant:%' OR permission_code = 'audit:log:view';

-- 为订单管理员分配订单相关权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM `role` WHERE role_code = 'ORDER_ADMIN'),
    id
FROM `permission`
WHERE permission_code LIKE 'order:%' OR permission_code = 'audit:log:view';

-- 为只读管理员分配查看权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 
    (SELECT id FROM `role` WHERE role_code = 'VIEWER'),
    id
FROM `permission`
WHERE action = 'view';

-- 创建超级管理员账号 (密码: admin123, BCrypt加密后的hash)
-- $2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q
INSERT INTO `admin` (`username`, `password`, `real_name`, `email`, `phone`, `status`) VALUES
('admin', '$2a$10$iXNc/Y/3w0dqWslabSZt5.Op7sGly9TD8TatZ0ITvahge2I774w2q', '超级管理员', 'admin@mall.com', '13800138000', 1);

-- 为超级管理员分配角色
INSERT INTO `admin_role` (`admin_id`, `role_id`)
VALUES (
    (SELECT id FROM `admin` WHERE username = 'admin'),
    (SELECT id FROM `role` WHERE role_code = 'SUPER_ADMIN')
);
