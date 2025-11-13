# 自动服务检测与启动系统

## 概述

本项目现已支持智能化的服务自动检测与启动功能。系统能够自动扫描 `backend` 目录下的所有微服务，并按照依赖关系有序启动。

## 核心特性

### 1. 自动服务发现
- **智能扫描**: 自动检测 `backend` 目录下所有包含 `pom.xml` 的 Maven 项目
- **排除过滤**: 自动排除非服务模块（common-bom、common-core、simple-test）
- **端口识别**: 从 `application.yml` 自动读取服务端口配置

### 2. 依赖关系管理
服务按照以下优先级启动：

| 优先级 | 服务名 | 端口 | 启动延迟 | 说明 |
|-------|--------|------|----------|------|
| 1 | gateway-service | 8080 | 5秒 | API网关，需要最先启动 |
| 2 | auth-service | 8081 | 3秒 | 认证服务 |
| 3 | user-service | 8082 | 3秒 | 用户服务 |
| 4 | product-service | 8083 | 3秒 | 商品服务 |
| 5 | order-service | 8084 | 3秒 | 订单服务 |
| 6 | payment-service | 8085 | 3秒 | 支付服务 |
| 7 | admin-service | 8086 | 3秒 | 管理服务 |
| 8 | merchant-service | 8087 | 3秒 | 商家服务 |
| 9 | cart-service | 8088 | 3秒 | 购物车服务 |
| 10 | sms-service | 8089 | 3秒 | 短信服务 |

### 3. 启动流程

```
┌─────────────────────────────────────┐
│  1. 环境检查                         │
│     - Docker 环境                   │
│     - Maven 环境                    │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  2. 启动基础设施                     │
│     - MySQL (端口 3307)             │
│     - Redis (端口 6379)             │
│     - Nacos (端口 8848)             │
│     等待 20 秒完成初始化             │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  3. 扫描可用服务                     │
│     - 遍历 backend 目录              │
│     - 检测 pom.xml 文件              │
│     - 过滤排除模块                   │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  4. 按序启动微服务                   │
│     - 按配置的优先级顺序启动         │
│     - 每个服务等待指定延迟           │
│     - 输出日志到 logs 目录           │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  5. 启动前端服务                     │
│     - 检查依赖安装                   │
│     - 启动开发服务器                 │
└─────────────────────────────────────┘
```

## 使用方法

### 启动所有服务

```bash
# Windows
start-dev-silent.bat

# 或使用 PowerShell
.\start-services.ps1
```

### 检查服务状态

```bash
# 自动检测并显示所有服务状态
pwsh -File check-services-silent.ps1
```

输出示例：
```
========================================
在线商城服务状态检查
========================================
检查时间: 2025-11-11 14:30:00

Docker 基础设施:
----------------------------------------
  MySQL [运行中] (健康) - 端口 3307
  Redis [运行中] (健康) - 端口 6379
  Nacos [运行中] (健康) - 端口 8848

后端微服务:
----------------------------------------
  Gateway Service [运行中] - 端口 8080 - 日志 2.5 MB
  Auth Service [运行中] - 端口 8081 - 日志 1.8 MB
  User Service [运行中] - 端口 8082 - 日志 1.2 MB
  ...

📊 统计信息:
  运行中: 13 / 13 服务 (100%)
```

### 查看实时日志

```bash
# 查看所有服务日志
pwsh -File tail-logs.ps1

# 查看特定服务日志
pwsh -File tail-logs.ps1 gateway-service
```

### 重启单个服务

```bash
pwsh -File restart-service.ps1 user-service
```

### 停止所有服务

```bash
stop-dev-silent.bat
```

## 配置说明

### 添加新服务

当您在 `backend` 目录下添加新的微服务时，系统会自动检测并启动，无需修改启动脚本。但为了控制启动顺序，建议在 `start-dev-silent.bat` 中添加配置：

```batch
REM 在 SERVICES_CONFIG 中添加新服务
set "SERVICES_CONFIG=!SERVICES_CONFIG!new-service:端口号:配置文件:延迟秒数;"
```

示例：
```batch
set "SERVICES_CONFIG=!SERVICES_CONFIG!notification-service:8090::3;"
```

### 服务配置格式

```
服务名:端口:配置文件:启动延迟
```

- **服务名**: backend 目录下的服务文件夹名称
- **端口**: 服务监听端口（从 application.yml 读取）
- **配置文件**: Spring Profile 名称（可选，如 `simple`）
- **启动延迟**: 启动后等待的秒数（默认 3 秒）

### 排除非服务模块

如果需要排除某些目录，在脚本中修改：

```batch
set "EXCLUDE_DIRS=common-bom common-core simple-test your-module"
```

## 日志管理

### 日志文件位置

所有服务日志保存在 `logs/` 目录：

```
logs/
├── gateway-service.log
├── auth-service.log
├── user-service.log
├── product-service.log
├── order-service.log
├── payment-service.log
├── admin-service.log
├── merchant-service.log
├── cart-service.log
├── sms-service.log
└── frontend.log
```

### 日志轮转

建议定期清理或归档日志文件：

```powershell
# 清理 7 天前的日志
Get-ChildItem logs -Filter *.log | 
  Where-Object {$_.LastWriteTime -lt (Get-Date).AddDays(-7)} | 
  Remove-Item
```

## 故障排查

### 服务启动失败

1. **检查日志文件**
   ```bash
   cat logs/服务名.log
   ```

2. **检查端口占用**
   ```bash
   netstat -ano | findstr "端口号"
   ```

3. **检查 Docker 容器状态**
   ```bash
   docker ps -a
   ```

### 基础设施连接失败

确保 Docker 容器正常运行：
```bash
docker-compose -f docker-compose-dev.yml ps
```

重启基础设施：
```bash
docker-compose -f docker-compose-dev.yml restart
```

### Maven 编译错误

清理并重新编译：
```bash
cd backend/服务名
mvn clean install -DskipTests
```

## 性能优化

### 并行启动（实验性）

可以修改脚本实现部分服务并行启动：

```batch
REM 同时启动多个无依赖关系的服务
start /B cmd /c "cd backend\user-service && mvn spring-boot:run > ..\..\logs\user.log 2>&1"
start /B cmd /c "cd backend\product-service && mvn spring-boot:run > ..\..\logs\product.log 2>&1"
start /B cmd /c "cd backend\sms-service && mvn spring-boot:run > ..\..\logs\sms.log 2>&1"
```

### 减少启动延迟

根据实际情况调整延迟时间：
```batch
REM 将默认 3 秒改为 2 秒
set "SERVICES_CONFIG=!SERVICES_CONFIG!service-name:port::2;"
```

## 扩展功能

### 健康检查

系统会自动检测服务端口是否监听，可以进一步集成健康检查端点：

```powershell
# 检查服务健康状态
Invoke-WebRequest http://localhost:8080/actuator/health
```

### 服务注册监控

通过 Nacos 控制台监控服务注册状态：
- URL: http://localhost:8848/nacos
- 用户名: nacos
- 密码: nacos

## 版本历史

### v2.0 (2025-11-11)
- ✨ 新增自动服务发现功能
- ✨ 支持动态服务数量调整
- ✨ 优化启动顺序和依赖管理
- 📝 增强状态反馈和日志输出
- 🔧 改进错误处理机制

### v1.0
- 基础的固定服务启动功能

## 技术支持

如有问题，请：
1. 查看服务日志文件
2. 运行状态检查脚本
3. 查阅本文档的故障排查章节

---

**作者**: lingbai  
**最后更新**: 2025-11-11
