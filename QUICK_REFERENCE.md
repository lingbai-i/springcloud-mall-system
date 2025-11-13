# 🚀 快速参考 - 在线商城系统

> **一页纸速查手册** - 最常用的命令和操作

---

## ⚡ 遇到问题？先诊断！

```bash
# 🔧 一键诊断所有问题（推荐首选）
diagnose.bat

# 🐛 调试模式启动（逐步诊断）
start-dev-debug.bat

# 📖 查看故障排查指南
notepad TROUBLESHOOTING.md
```

---

## 📋 服务启动（三种方式）

### 🌟 方式1: 智能自动启动（推荐）
```bash
start-dev-silent.bat
```
- ✅ 自动检测所有服务
- ✅ 后台静默运行
- ✅ 完整日志记录
- ⏱️ 约90秒完全启动

### 💬 方式2: 交互式启动
```bash
start-dev.bat
```
- 询问是否启动后端
- 询问是否启动前端
- 可选择性启动

### 🎮 方式3: 服务管理工具
```bash
service-manager.bat
```
- 交互式菜单
- 单服务管理
- 日志查看

---

## 🔍 服务状态检查

```bash
# 自动检测所有服务状态
pwsh -File check-services-silent.ps1
```

**输出信息**:
- 基础设施状态（MySQL、Redis、Nacos）
- 所有微服务状态和端口
- 日志文件大小
- 总体运行统计

---

## 📝 日志管理

```bash
# 查看所有服务日志
pwsh -File tail-logs.ps1

# 查看特定服务日志
pwsh -File tail-logs.ps1 gateway-service
pwsh -File tail-logs.ps1 user-service
```

**日志位置**: `logs/服务名.log`

---

## 🔄 服务重启

```bash
# 重启指定服务
pwsh -File restart-service.ps1 user-service

# 使用服务管理工具
service-manager.bat
# 选择 4. 重启服务
```

---

## 🛑 停止服务

```bash
# 停止所有服务
stop-dev-silent.bat

# 或使用服务管理工具
service-manager.bat
# 选择 7. 停止所有服务
```

---

## 🌐 访问地址

| 服务 | 地址 | 凭证 |
|------|------|------|
| 🎨 前端 | http://localhost:5173 | - |
| 📡 网关 | http://localhost:8080 | - |
| 🎯 Nacos | http://localhost:8848/nacos | nacos/nacos |
| 🗄️ MySQL | localhost:3307 | root/123456 |
| 📊 Redis | localhost:6379 | - |

---

## 🔐 测试账号

| 类型 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | nacos |
| 测试用户 | testlogin | nacos |

---

## 📊 服务列表

| 服务名 | 端口 | 说明 |
|--------|------|------|
| gateway-service | 8080 | API网关 |
| auth-service | 8081 | 认证服务 |
| user-service | 8082 | 用户服务 |
| product-service | 8083 | 商品服务 |
| order-service | 8084 | 订单服务 |
| payment-service | 8085 | 支付服务 |
| admin-service | 8086 | 管理服务 |
| merchant-service | 8087 | 商家服务 |
| cart-service | 8088 | 购物车服务 |
| sms-service | 8089 | 短信服务 |

---

## 🐛 常见问题

### 端口被占用
```bash
# 查看端口占用
netstat -ano | findstr "8080"

# 终止进程（替换PID）
taskkill /F /PID <进程ID>
```

### 服务启动失败
```bash
# 1. 查看日志
cat logs/服务名.log

# 2. 检查基础设施
docker-compose -f docker-compose-dev.yml ps

# 3. 重启基础设施
docker-compose -f docker-compose-dev.yml restart
```

### Maven编译错误
```bash
# 清理并重新编译
cd backend/服务名
mvn clean install -DskipTests
```

### Docker未启动
```bash
# 检查Docker Desktop是否运行
docker --version

# 启动Docker Desktop（Windows）
# 从开始菜单启动Docker Desktop
```

---

## ⚡ 快捷操作

### 一键启动开发环境
```bash
start-dev-silent.bat
```

### 快速检查状态
```bash
pwsh -File check-services-silent.ps1
```

### 查看网关日志
```bash
pwsh -File tail-logs.ps1 gateway-service
```

### 重启用户服务
```bash
pwsh -File restart-service.ps1 user-service
```

### 完全停止
```bash
stop-dev-silent.bat
```

---

## 💡 开发技巧

### 调试单个服务
1. 启动基础设施: `docker-compose -f docker-compose-dev.yml up -d`
2. 启动网关: `pwsh -File service-manager.ps1 start gateway-service`
3. 在IDE中调试目标服务

### 性能监控
- Nacos控制台: http://localhost:8848/nacos
- 查看服务注册情况
- 监控服务健康状态

### 数据库管理
```bash
# 连接MySQL
mysql -h 127.0.0.1 -P 3307 -u root -p123456

# 或使用Navicat等图形化工具
```

---

## 📚 详细文档

- [快速上手指南](QUICK_START.md) - 5分钟入门
- [自动服务检测](docs/AUTO_SERVICE_DETECTION.md) - 系统架构
- [开发指南](DEVELOPMENT.md) - 开发规范
- [更新日志](CHANGELOG_SERVICE_AUTO_DETECTION.md) - 版本历史

---

## 🔗 快速链接

```bash
# 项目结构
├── backend/              # 后端微服务
├── frontend/            # 前端Vue项目
├── logs/                # 服务日志
├── sql/                 # 数据库脚本
├── docs/                # 文档
└── start-dev-silent.bat # 启动脚本
```

---

**提示**: 将此文件添加到浏览器书签，随时查阅！ 📌

---

**版本**: 1.0  
**更新**: 2025-11-11  
**作者**: lingbai
