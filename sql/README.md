# SQL 脚本说明

## 概述

本目录包含项目的数据库初始化和配置脚本。

## 文件说明

### 核心脚本（按执行顺序）

| 文件                     | 说明                       |
| ------------------------ | -------------------------- |
| `00-init-databases.sql`  | 初始化数据库结构（所有表） |
| `01-nacos-schema.sql`    | Nacos 配置中心表结构       |
| `02-admin-data.sql`      | 管理员账户初始数据         |
| `03-init-data.sql`       | 商品、分类等初始数据       |
| `04-user-favorites.sql`  | 用户收藏功能表             |
| `05-chart-test-data.sql` | 图表统计测试数据           |
| `06-fix-chart-data.sql`  | 图表数据修复               |

## 使用方法

### 初始化数据库

```bash
# 使用 Docker 执行
cat sql/00-init-databases.sql | docker exec -i mall-mysql-dev mysql -uroot -p123456

# 或连接 MySQL 后执行
source /path/to/00-init-databases.sql;
```

### 执行顺序

1. 先执行 `00-init-databases.sql` 创建表结构
2. 执行 `01-nacos-schema.sql` 初始化 Nacos
3. 执行 `02-admin-data.sql` 创建管理员
4. 根据需要执行其他数据脚本

## 默认账号

| 类型     | 用户名    | 密码  |
| -------- | --------- | ----- |
| 管理员   | admin     | nacos |
| 测试用户 | testlogin | nacos |

## 数据库连接

- **Host**: localhost
- **Port**: 3307 (Docker) / 3306 (本地)
- **User**: root
- **Password**: 123456

---

_更新时间：2026-01-01_
