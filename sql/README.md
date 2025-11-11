# 数据清理说明

## 概述

本目录包含用于清理系统测试数据的 SQL 脚本。执行这些脚本将移除系统中的虚拟测试数据，为正式使用做准备。

## 文件说明

### 1. clean-test-data.sql

**用途：** 清除系统中所有虚拟测试数据

**清理范围：**

- ✅ 所有商品数据（SKU、SPU）
- ✅ 所有订单数据（订单、订单商品）
- ✅ 所有购物车数据
- ✅ 所有商家数据（商家、店铺）
- ✅ 所有测试用户数据
- ✅ 所有用户地址数据

**保留内容：**

- ✅ 管理员账号（admin）
- ✅ 基础商品分类数据
- ✅ 数据库表结构

### 2. init.sql（已更新）

**说明：** 初始化数据库脚本已更新，不再包含测试数据

**包含内容：**

- ✅ 数据库结构创建
- ✅ 基础商品分类数据
- ✅ 默认管理员账号（admin / password）

## 使用方法

### 方式一：使用命令行（推荐）

```bash
# Windows PowerShell
Get-Content sql/clean-test-data.sql | docker exec -i mall-mysql-dev mysql -uroot -p123456

# Linux/Mac
cat sql/clean-test-data.sql | docker exec -i mall-mysql-dev mysql -uroot -p123456
```

### 方式二：使用 MySQL 客户端

```bash
# 登录MySQL
docker exec -it mall-mysql-dev mysql -uroot -p123456

# 执行清理脚本
source /path/to/clean-test-data.sql;
```

### 方式三：使用数据库管理工具

1. 使用 Navicat、DBeaver 等工具连接到数据库
2. 打开 `clean-test-data.sql` 文件
3. 执行 SQL 脚本

## 重要提示

⚠️ **执行前必读：**

1. **备份数据**：执行清理脚本前，请务必备份重要数据
2. **停止服务**：建议先停止所有微服务，避免数据冲突
3. **确认环境**：确保在正确的环境（开发/测试）中执行
4. **检查依赖**：如有外键依赖，可能需要调整执行顺序

⚠️ **默认保留：**

- 管理员账号：`admin`
- 默认密码：`password`
- 基础分类数据：一级和二级商品分类

## 清理后操作

执行清理脚本后，系统将处于"干净"状态：

1. **登录系统**：使用管理员账号登录

   - 用户名：`admin`
   - 密码：`password`

2. **添加数据**：通过以下方式添加正式数据

   - 通过管理后台添加商家、审核商家申请
   - 商家通过商家后台添加商品
   - 用户通过前端注册账号、下单购买

3. **修改密码**：建议首次登录后立即修改管理员密码

## 常见问题

### Q1: 清理后无法登录？

**A:** 确保 `users` 表中存在 admin 账号。可以手动执行：

```sql
INSERT INTO mall_user.users (id, username, nickname, email, phone, password, status)
VALUES (1, 'admin', '系统管理员', 'admin@mall.com', '13800138888', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 1)
ON DUPLICATE KEY UPDATE username=username;
```

### Q2: 清理后分类数据丢失？

**A:** 脚本保留了一级分类，如需恢复二级分类，请重新执行 `init.sql` 中的分类插入部分。

### Q3: 如何重置自增 ID？

**A:** 如需从 1 开始，取消注释 `clean-test-data.sql` 中的 `ALTER TABLE` 语句。

### Q4: 清理失败怎么办？

**A:** 可能是外键约束导致，建议：

1. 先删除子表数据
2. 再删除父表数据
3. 或临时禁用外键检查：`SET FOREIGN_KEY_CHECKS=0;`

## 脚本维护

如需添加新的清理逻辑：

1. 编辑 `clean-test-data.sql`
2. 在相应数据库部分添加 DELETE 语句
3. 更新此文档说明
4. 测试验证后提交

## 版本历史

- **v1.0** (2025-01-11): 初始版本，清理所有测试数据
- 更新 `init.sql`，移除测试数据插入

## 联系方式

如有问题或建议，请联系开发团队。
