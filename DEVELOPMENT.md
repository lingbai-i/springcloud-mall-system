# 🛠️ 开发指南

本指南将帮助您高效地进行本地开发和调试。

## 📋 目录

- [开发环境设置](#开发环境设置)
- [启动方式](#启动方式)
- [常见开发场景](#常见开发场景)
- [调试技巧](#调试技巧)
- [日志管理](#日志管理)
- [常见问题](#常见问题)

---

## 🚀 开发环境设置

### 前置要求

确保已安装以下软件：

- **JDK 17+**
- **Maven 3.6+**
- **Node.js 18+**
- **Docker Desktop**
- **IDE**: IntelliJ IDEA 或 Eclipse（推荐 IDEA）

### 首次设置

1. **克隆项目**

   ```bash
   git clone <repository-url>
   cd 在线商城系统
   ```

2. **导入 IDE**

   - IDEA: File → Open → 选择项目根目录
   - 等待 Maven 依赖下载完成

3. **初始化数据库**

   ```bash
   # 启动Docker基础设施
   docker-compose -f docker-compose-dev.yml up -d

   # 等待MySQL启动完成
   docker logs mall-mysql-dev
   ```

---

## 🎯 启动方式

项目提供**两种启动方式**，根据您的需求选择：

### 方式 1：后台启动（推荐）

**适用场景**：日常开发，不需要频繁调试

**优点**：

- ✅ 无多个 CMD 窗口
- ✅ 日志集中管理
- ✅ 资源占用少

**使用方法**：

```bash
# 启动所有服务（后台运行）
start-dev-silent.bat

# 查看实时日志
.\tail-logs.ps1 [服务名]

# 停止所有服务
stop-dev-silent.bat
```

### 方式 2：窗口启动（调试时）

**适用场景**：需要频繁查看日志或调试

**使用方法**：

```bash
# 启动所有服务（独立窗口）
start-dev.bat
```

### 方式 3：IDE 启动（单服务调试）

**适用场景**：调试特定服务

**步骤**：

1. 启动基础设施和其他服务（后台）
2. 在 IDE 中运行要调试的服务
3. 设置断点并调试

---

## 💼 常见开发场景

### 场景 1：全新启动开发环境

```bash
# 1. 启动所有服务到后台
start-dev-silent.bat

# 2. 等待1-2分钟服务启动

# 3. 检查服务状态
.\check-services-silent.ps1

# 4. 访问前端
# http://localhost:5173
```

### 场景 2：调试用户服务

```bash
# 1. 后台启动所有服务
start-dev-silent.bat

# 2. 停止后台的user-service
# (它会自动检测端口冲突并停止)

# 3. 在IDE中运行 UserServiceApplication
#    - 右键 → Run 'UserServiceApplication'
#    - 或 Debug 模式运行

# 4. 设置断点并测试
```

### 场景 3：修改代码后快速重启

```bash
# 方法1：重启单个服务
.\restart-service.ps1 user

# 方法2：在IDE中重启
# 停止服务 → 修改代码 → 重新运行
```

### 场景 4：查看服务日志

```bash
# 查看单个服务实时日志
.\tail-logs.ps1 user

# 查看所有服务日志摘要
.\tail-logs.ps1 all

# 查看最后50行
.\tail-logs.ps1 gateway -Lines 50
```

### 场景 5：测试前端功能

```bash
# 1. 确保后端服务运行
.\check-services-silent.ps1

# 2. 访问前端
# http://localhost:5173

# 3. 打开浏览器开发者工具（F12）
#    - Network: 查看API请求
#    - Console: 查看前端日志
```

---

## 🐛 调试技巧

### 后端调试

#### 1. IDEA 断点调试

```java
// 在需要调试的代码处设置断点
@GetMapping("/users/{id}")
public Result getUser(@PathVariable Long id) {
    // 点击行号设置断点
    User user = userService.getById(id);  // ← 断点
    return Result.success(user);
}
```

**步骤**：

1. 在 IDE 中以 Debug 模式运行服务
2. 访问对应的 API 接口
3. 程序会在断点处暂停
4. 使用调试工具栏查看变量、单步执行等

#### 2. 日志调试

```java
// 添加日志输出
@Service
public class UserServiceImpl {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public User getById(Long id) {
        log.debug("查询用户ID: {}", id);  // 调试日志
        User user = userMapper.selectById(id);
        log.info("查询到用户: {}", user);  // 信息日志
        return user;
    }
}
```

**查看日志**：

```bash
# 实时查看
.\tail-logs.ps1 user

# 或直接查看日志文件
notepad logs\user.log
```

#### 3. 远程调试 Docker 容器

如果服务在 Docker 中运行，可以配置远程调试：

```yaml
# docker-compose.yml
services:
  user-service:
    environment:
      JAVA_OPTS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
    ports:
      - "5005:5005" # 调试端口
```

### 前端调试

#### 1. 浏览器开发者工具

```javascript
// 在代码中添加断点
function login(username, password) {
  debugger; // JavaScript断点
  return api.post("/users/login", { username, password });
}
```

#### 2. Vue DevTools

安装 Chrome 扩展：Vue.js devtools

可以：

- 查看组件树
- 检查组件状态
- 查看 Vuex/Pinia 状态
- 跟踪事件

#### 3. 网络请求调试

打开 Network 标签：

- 查看 API 请求和响应
- 检查请求头和响应头
- 查看请求耗时

---

## 📝 日志管理

### 日志位置

所有服务日志保存在 `logs/` 目录：

```
logs/
├── gateway.log     # 网关服务
├── user.log        # 用户服务
├── product.log     # 商品服务
├── cart.log        # 购物车服务
├── order.log       # 订单服务
├── payment.log     # 支付服务
├── admin.log       # 管理服务
├── merchant.log    # 商家服务
├── sms.log         # 短信服务
└── frontend.log    # 前端服务
```

### 查看日志

**实时查看**：

```bash
# 单个服务
.\tail-logs.ps1 user

# 所有服务摘要
.\tail-logs.ps1 all
```

**文本编辑器查看**：

```bash
# 用记事本打开
notepad logs\user.log

# 用VS Code打开
code logs\user.log
```

### 日志级别

```properties
# application.yml
logging:
  level:
    root: INFO          # 根日志级别
    com.mall: DEBUG     # 项目日志级别
    org.springframework: INFO
```

**日志级别说明**：

- **ERROR**: 错误信息（红色显示）
- **WARN**: 警告信息（黄色显示）
- **INFO**: 一般信息（绿色显示）
- **DEBUG**: 调试信息（灰色显示）

---

## 🔧 服务管理

### 检查服务状态

```bash
.\check-services-silent.ps1
```

输出示例：

```
✅ Gateway Service (8080) - 运行中
✅ User Service (8082) - 运行中
❌ Order Service (8084) - 未运行
```

### 重启单个服务

```bash
# 语法
.\restart-service.ps1 <服务名>

# 示例
.\restart-service.ps1 user
.\restart-service.ps1 gateway
```

### 停止所有服务

```bash
stop-dev-silent.bat
```

---

## 🔍 常见问题

### 1. 端口被占用

**问题**：启动服务时提示端口已被占用

**解决方案**：

```bash
# 查看端口占用
netstat -ano | findstr :8080

# 停止占用进程
taskkill /PID <进程ID> /F

# 或使用重启脚本（自动处理）
.\restart-service.ps1 gateway
```

### 2. 服务启动失败

**问题**：服务无法启动或启动后立即停止

**排查步骤**：

1. **查看日志**

   ```bash
   .\tail-logs.ps1 <服务名>
   ```

2. **检查配置**

   - 数据库连接配置
   - Redis 连接配置
   - Nacos 连接配置

3. **检查依赖服务**

   ```bash
   # 确保基础设施运行正常
   docker ps
   ```

4. **清理并重新编译**
   ```bash
   cd backend/<服务目录>
   mvn clean compile
   ```

### 3. 前端无法连接后端

**问题**：前端显示网络错误

**排查步骤**：

1. **检查网关是否运行**

   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **检查服务注册**

   - 访问 http://localhost:8848/nacos
   - 查看服务列表

3. **查看浏览器控制台**
   - F12 → Network
   - 查看请求状态和错误信息

### 4. 数据库连接失败

**问题**：服务日志显示数据库连接错误

**解决方案**：

1. **检查 MySQL 容器**

   ```bash
   docker logs mall-mysql-dev
   ```

2. **测试连接**

   ```bash
   mysql -h localhost -P 3307 -u root -p123456
   ```

3. **检查配置**
   ```yaml
   # application.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3307/mall_user
       username: root
       password: 123456
   ```

### 5. 日志文件过大

**问题**：日志文件占用大量磁盘空间

**解决方案**：

```bash
# 停止服务并清理日志
stop-dev-silent.bat
# 选择 y 清理日志文件

# 或手动删除
del /Q logs\*.log
```

---

## 💡 最佳实践

### 1. 开发前

- ✅ 拉取最新代码
- ✅ 检查服务状态
- ✅ 确保基础设施运行

### 2. 开发中

- ✅ 只调试需要的服务（在 IDE 中运行）
- ✅ 其他服务后台运行
- ✅ 经常查看日志
- ✅ 及时提交代码

### 3. 开发后

- ✅ 测试修改的功能
- ✅ 停止不需要的服务
- ✅ 清理日志文件（可选）

---

## 📚 相关文档

- [README.md](README.md) - 项目说明
- [QUICK_START.md](QUICK_START.md) - 快速上手
- [在线商城需求说明.md](在线商城需求说明.md) - 需求文档
- [技术栈设计.md](技术栈设计.md) - 技术选型

---

## 🆘 获取帮助

如果遇到问题：

1. 查看本文档的[常见问题](#常见问题)部分
2. 查看服务日志找到错误信息
3. 检查 Nacos 服务注册状态
4. 查看项目 Issue 或联系项目维护者

---

_最后更新：2026 年 01 月 01 日_  
_版本：1.1_
