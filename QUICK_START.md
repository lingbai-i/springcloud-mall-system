# 🚀 快速上手指南（5分钟）

欢迎使用**基于Spring Cloud的微服务在线商城系统**！本指南将帮助您在5分钟内快速启动项目并开始开发。

## 📋 前置要求

确保已安装以下软件：

| 软件 | 版本要求 | 检查命令 |
|------|---------|---------|
| ✅ **JDK** | 17+ | `java -version` |
| ✅ **Maven** | 3.6+ | `mvn -version` |
| ✅ **Node.js** | 18+ | `node -v` |
| ✅ **Docker Desktop** | 20+ | `docker --version` |

---

## 🎯 快速启动（推荐）

### 方式一：智能后台启动（全自动 ⭐推荐）

**新版智能化启动脚本** - 自动检测所有服务并后台启动！

**Windows用户**：

```bash
# 双击运行或在命令行执行
start-dev-silent.bat
```

**特性**：
- ✨ **自动服务发现**: 无需手动配置，自动扫描所有微服务
- 🔄 **智能依赖管理**: 按照服务依赖关系有序启动
- 📊 **实时状态反馈**: 显示启动进度和服务统计
- 🎯 **后台静默运行**: 所有服务后台运行，无弹窗干扰
- 📝 **完整日志记录**: 所有日志自动保存到 `logs/` 目录

**启动时间**：
- 基础设施初始化：20秒
- 微服务自动启动：约60秒（按依赖顺序）
- 前端开发服务器：10秒
- **总计**: 约90秒完全启动

**启动后验证**：
```bash
# 检查所有服务状态
pwsh -File check-services-silent.ps1
```

---

### 方式二：交互式启动（可选）

**适合需要选择性启动服务的场景**：

```bash
# 双击运行或在命令行执行
start-dev.bat
```

**按提示选择**：
1. 是否启动后端微服务？→ `Y`
2. 是否启动前端？→ `Y`

**等待启动完成**：
- 基础设施：20秒
- 微服务：30秒
- 前端：10秒

---

### 方式二：手动启动（灵活）

适合需要调试特定服务的开发者。

#### 步骤1：启动基础设施

```bash
docker-compose -f docker-compose-dev.yml up -d
```

**验证启动**：
```bash
docker-compose -f docker-compose-dev.yml ps
```

预期输出：
```
NAME                STATUS          PORTS
mall-mysql-dev      Up (healthy)    0.0.0.0:3307->3306/tcp
mall-redis-dev      Up (healthy)    0.0.0.0:6379->6379/tcp
mall-nacos-dev      Up (healthy)    0.0.0.0:8848->8848/tcp
```

#### 步骤2：启动后端服务（在IDE中）

**推荐使用 IntelliJ IDEA 或 Eclipse**：

1. 打开 `backend` 目录作为Maven项目
2. 右键运行以下主类：
   - `GatewayServiceApplication` (端口 8080)
   - `UserServiceApplication` (端口 8082)
   - `ProductServiceApplication` (端口 8083)
   - `CartServiceApplication` (端口 8088)

**或使用命令行**：

```bash
# 在不同的终端窗口中运行
cd backend/gateway-service
mvn spring-boot:run

cd backend/user-service
mvn spring-boot:run

cd backend/product-service
mvn spring-boot:run

cd backend/cart-service
mvn spring-boot:run
```

#### 步骤3：启动前端

```bash
cd frontend
npm install    # 仅首次运行需要
npm run dev
```

---

## 🌐 访问地址

启动完成后，可以访问以下地址：

| 服务 | 地址 | 说明 |
|------|------|------|
| 🎨 **前端应用** | http://localhost:5173 | 用户界面 |
| 📡 **API网关** | http://localhost:8080 | 统一API入口 |
| 🎯 **Nacos控制台** | http://localhost:8848/nacos | 服务注册中心 (nacos/nacos) |
| 🗄️ **MySQL** | localhost:3307 | 数据库 (root/123456) |
| 📊 **Redis** | localhost:6379 | 缓存服务器 |

---

## 🔐 测试账号

系统已预置测试账号供开发测试使用：

| 账号类型 | 用户名 | 密码 | 说明 |
|---------|--------|------|------|
| 管理员 | `admin` | `nacos` | 系统管理员账号 |
| 测试用户 | `testlogin` | `nacos` | 普通用户测试账号 |

### 登录方式

#### ✅ 账号密码登录（可用）

访问前端首页 http://localhost:5173/home，点击登录按钮，输入上述账号密码即可登录。

#### ✅ 短信验证码登录（可用）

访问前端首页 http://localhost:5173/home，点击登录按钮，选择"验证码登录"，输入手机号并获取验证码即可登录。

**功能说明**：
- 首次使用手机号登录会自动注册新账号
- 验证码有效期：3分钟
- 同一手机号发送间隔：60秒
- 自动生成默认用户名：`user_手机号`
- 自动生成默认邮箱：`手机号@mall.com`

**测试提示**：验证码会通过第三方推送服务发送，请查看SMS服务日志获取验证码

---

## 🔧 服务管理（新功能）

### 检查服务状态

```bash
# 智能检测并显示所有服务状态
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
  Product Service [运行中] - 端口 8083 - 日志 980 KB
  ... (自动检测所有服务)

📊 统计信息:
  运行中: 13 / 13 服务 (100%)
```

### 查看实时日志

```bash
# 查看所有服务日志
pwsh -File tail-logs.ps1

# 查看特定服务日志（例如网关服务）
pwsh -File tail-logs.ps1 gateway-service

# 查看用户服务日志
pwsh -File tail-logs.ps1 user-service
```

### 重启单个服务

```bash
# 重启指定服务
pwsh -File restart-service.ps1 user-service

# 重启网关服务
pwsh -File restart-service.ps1 gateway-service
```

### 服务列表

当前系统支持的所有微服务（自动检测）：

| 服务名 | 端口 | 说明 | 日志文件 |
|--------|------|------|----------|
| gateway-service | 8080 | API网关 | logs/gateway-service.log |
| auth-service | 8081 | 认证服务 | logs/auth-service.log |
| user-service | 8082 | 用户服务 | logs/user-service.log |
| product-service | 8083 | 商品服务 | logs/product-service.log |
| order-service | 8084 | 订单服务 | logs/order-service.log |
| payment-service | 8085 | 支付服务 | logs/payment-service.log |
| admin-service | 8086 | 管理服务 | logs/admin-service.log |
| merchant-service | 8087 | 商家服务 | logs/merchant-service.log |
| cart-service | 8088 | 购物车服务 | logs/cart-service.log |
| sms-service | 8089 | 短信服务 | logs/sms-service.log |

> 💡 **提示**: 新增的服务会被自动检测并启动，无需修改启动脚本！

### 添加新服务

要添加新的微服务：

1. 在 `backend/` 目录下创建新服务目录
2. 添加标准的 `pom.xml` 文件
3. 配置 `src/main/resources/application.yml` 中的端口
4. 运行 `start-dev-silent.bat` - 新服务会被自动检测并启动！

示例：
```yaml
# application.yml
server:
  port: 8090  # 配置服务端口

spring:
  application:
    name: notification-service  # 服务名称
```

---

## 🛑 停止服务

### 方式一：一键停止

```bash
stop-dev-silent.bat
```

### 方式二：手动停止

**停止后端服务**：
- 关闭运行微服务的终端窗口
- 或按 `Ctrl + C` 停止进程

**停止前端**：
- 关闭前端终端窗口
- 或按 `Ctrl + C` 停止进程

**停止基础设施**：
```bash
docker-compose -f docker-compose-dev.yml down
```

---

## 🔍 验证安装

### 1. 检查服务状态

```bash
# Windows PowerShell
.\check-services-silent.ps1
```

预期输出示例：
```
✅ 网关服务 (8080): 运行中
✅ 用户服务 (8082): 运行中
✅ 商品服务 (8083): 运行中
✅ 购物车服务 (8088): 运行中
✅ 前端服务 (5173): 运行中
```

### 2. 测试前端访问

访问 http://localhost:5173，应该看到商城首页。

### 3. 测试API

```bash
# 测试网关健康检查
curl http://localhost:8080/actuator/health

# 测试用户服务
curl http://localhost:8080/api/users/test
```

---

## ❓ 常见问题

### 1. 端口被占用

**问题**：启动时提示端口已被占用

**解决方案**：

```bash
# 查看端口占用
netstat -ano | findstr :8080

# 停止占用端口的进程
taskkill /PID <进程ID> /F
```

### 2. Nacos启动失败

**问题**：Nacos容器无法启动或健康检查失败

**解决方案**：

```bash
# 查看Nacos日志
docker logs mall-nacos-dev

# 等待MySQL完全启动后，重启Nacos
docker-compose -f docker-compose-dev.yml restart nacos

# 检查Nacos健康状态
curl http://localhost:8848/nacos/actuator/health
```

### 3. 前端无法连接后端

**问题**：前端显示网络错误或无法加载数据

**确认以下几点**：

1. **网关服务是否启动**：
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **服务是否注册到Nacos**：
   - 访问 http://localhost:8848/nacos
   - 登录后查看"服务管理" → "服务列表"
   - 确认 user-service、product-service 等已注册

3. **浏览器控制台是否有错误**：
   - 打开开发者工具 (F12)
   - 查看 Console 和 Network 标签页

### 4. MySQL连接失败

**问题**：服务启动时报数据库连接错误

**解决方案**：

```bash
# 检查MySQL容器状态
docker ps | grep mall-mysql-dev

# 查看MySQL日志
docker logs mall-mysql-dev

# 测试连接（需要MySQL客户端）
mysql -h localhost -P 3307 -u root -p123456

# 或重启MySQL容器
docker-compose -f docker-compose-dev.yml restart mysql
```

### 5. 首次启动很慢

**原因**：Maven需要下载依赖，前端需要安装node_modules

**解决方案**：
- 耐心等待，首次下载后会缓存
- 使用国内镜像加速（阿里云Maven镜像、淘宝NPM镜像）

---

## 📚 下一步

项目启动成功后，您可以：

1. 📖 阅读 [README.md](README.md) 了解项目架构
2. 📊 查看 [API文档](http://localhost:8080/doc.html)（如果配置了Knife4j）
3. 🔧 阅读 [需求说明](在线商城需求说明.md) 了解业务需求
4. 💻 开始开发您的功能模块

---

## 📞 技术支持

遇到问题？

1. **查看日志文件**：
   - 后端：控制台输出或 `logs/` 目录
   - 前端：浏览器开发者工具
   - Docker：`docker logs <容器名>`

2. **检查服务状态**：
   ```bash
   .\check-services-silent.ps1
   ```

3. **查看Nacos服务列表**：
   - http://localhost:8848/nacos
   - 确认所有服务已注册

4. **查看项目文档**：
   - [README.md](README.md) - 项目说明
   - [技术栈设计.md](技术栈设计.md) - 技术选型
   - [开发进度报告.md](开发进度报告.md) - 开发状态

---

## 🎉 开始开发

恭喜！您已经成功启动了项目。现在可以开始愉快地开发了！

**推荐的开发工作流**：

1. 修改代码（后端/前端）
2. 保存文件（热重载自动生效）
3. 在浏览器中测试
4. 查看日志排查问题
5. 重复以上步骤

**提示**：
- 后端代码修改后需要重启对应的微服务
- 前端代码修改后会自动热重载
- 配置文件修改后需要重启服务

---

*最后更新：2025年11月10日*  
*版本：1.0*

