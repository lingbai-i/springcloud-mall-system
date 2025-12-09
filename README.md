# 基于 SpringCloud 的微服务在线商城系统

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-22-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-green.svg)](https://vuejs.org/)

## 项目简介

本项目是一个基于 SpringCloud Alibaba 的**智能化微服务架构**在线商城系统，采用前后端分离的设计模式。项目集成了**自动服务发现与启动**功能，支持动态服务管理，大幅简化开发和部署流程。

### 🚀 核心特性

- ✅ **自动服务发现**: 智能扫描并启动所有微服务，无需手动配置
- ✅ **依赖关系管理**: 按照服务依赖顺序智能启动
- ✅ **实时状态监控**: 一键检查所有服务运行状态
- ✅ **灵活扩展**: 新增服务自动识别，零配置集成
- ✅ **完整日志系统**: 统一日志管理和实时查看

## 技术架构

### 后端技术栈

- **核心框架**: Java 22 + Spring Boot 3.x + Spring Cloud Alibaba
- **注册中心**: Nacos
- **配置中心**: Nacos Config
- **服务网关**: Spring Cloud Gateway
- **服务调用**: OpenFeign
- **熔断限流**: Sentinel
- **分布式事务**: Seata
- **消息队列**: RocketMQ
- **数据库**: MySQL 8.0
- **缓存**: Redis Cluster
- **搜索引擎**: Elasticsearch 8
- **对象存储**: MinIO
- **监控**: Prometheus + Grafana
- **文档**: Knife4j (Swagger3)

### 前端技术栈

- **框架**: Vue 3 + Vite
- **UI 组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios
- **工具库**: VueUse + Hutool

## 📁 项目目录结构

```
springcloud-mall/
├── backend/              # 后端微服务
│   ├── gateway-service/     # 网关服务
│   ├── auth-service/        # 认证服务
│   ├── user-service/        # 用户服务
│   ├── product-service/     # 商品服务
│   ├── order-service/       # 订单服务
│   ├── merchant-service/    # 商家服务
│   ├── admin-service/       # 管理服务
│   └── ...                  # 其他服务
├── frontend/             # 前端Vue项目
├── docs/                 # 项目文档
│   ├── 商家审核系统-API文档.md
│   ├── 数据库设计文档-完整版.md
│   └── ...
├── sql/                  # 数据库脚本
├── scripts/              # 启动和管理脚本
├── assets/               # 项目资源文件
│   ├── icon/                # 图标资源
│   └── vector-icons/        # 矢量图标
├── config/               # 配置文件
├── docker-compose.yml    # Docker编排文件
└── README.md            # 项目说明
```

## 系统架构

### 微服务划分

```
├── gateway-service          # 网关服务
├── auth-service            # 认证服务
├── user-service            # 用户服务
├── product-service         # 商品服务
├── search-service          # 搜索服务
├── cart-service            # 购物车服务
├── order-service           # 订单服务
├── inventory-service       # 库存服务
├── payment-service         # 支付服务
├── refund-service          # 退款服务
├── merchant-service        # 商家服务
├── settlement-service      # 结算服务
├── withdrawal-service      # 提现服务
├── cms-service            # 内容管理服务
├── coupon-service         # 优惠券服务
├── admin-service          # 管理服务
└── notify-service         # 通知服务
```

### 数据库设计

- `mall_user`: 用户相关数据
- `mall_product`: 商品相关数据
- `mall_order`: 订单相关数据
- `mall_merchant`: 商家相关数据

## 功能特性

### 用户端功能

- ✅ 用户注册登录
- ✅ 商品浏览搜索
- ✅ 购物车管理
- ✅ 订单管理
- ✅ 支付功能
- ✅ 个人中心
- ✅ 收货地址管理

### 商家端功能

- ✅ 商家入驻审核
- ✅ 店铺管理
- ✅ 商品发布管理
- ✅ 订单处理
- ✅ 库存管理
- ✅ 结算提现
- ✅ 数据统计

### 管理端功能

- ✅ 系统监控
- ✅ 用户管理
- ✅ 商家审核
- ✅ 内容管理
- ✅ 权限管理
- ✅ 数据分析

## 🚀 快速开始

### 环境要求

- **JDK**: 17+ (推荐 JDK 17)
- **Node.js**: 18+
- **Maven**: 3.6+
- **Docker**: 20+ & Docker Compose
- **MySQL**: 8.0+
- **Redis**: 6.0+

### ⚠️ 遇到启动问题？

如果启动脚本闪退或遇到错误，请查看:
- 📖 **[故障排查指南](TROUBLESHOOTING.md)** - 详细的问题诊断和解决方案
- 📋 查看 [常见问题 FAQ](#常见问题-faq)

## 🎯 快速启动

### 一键启动所有服务

```bash
# Windows
scripts\start-all.bat

# Linux/Mac
./scripts/start-all-services.ps1
```

此脚本会自动启动：
1. Docker 基础设施（MySQL、Redis、Nacos）
2. 所有后端微服务
3. 前端Vue应用

### 服务管理

```bash
# 服务管理工具（Windows）
scripts\service-manager.bat

# 服务管理工具（PowerShell）
.\scripts\service-manager.ps1

# 停止所有服务
scripts\stop-dev-silent.bat
```

### 手动启动

```bash
# 1. 仅启动基础设施
docker-compose -f docker-compose-dev.yml up -d

# 2. 在IDE中启动需要的微服务
#    - GatewayServiceApplication (8080)
#    - UserServiceApplication (8082)
#    - ProductServiceApplication (8083)
#    - CartServiceApplication (8088)

# 3. 启动前端
cd frontend && npm run dev
```

### 停止服务

```bash
stop-dev.bat
```

> 💡 **提示**：本地开发模式启动更快，占用资源更少，适合日常开发。详见 [快速上手指南](QUICK_START.md)

---

## 📦 完整部署（用于测试/生产）

**适用场景**：功能测试、演示、生产部署

### ⚡ 一键启动

**Windows 用户:**

```bash
# 1. 启动所有服务
start-all-services.bat

# 2. 检查服务状态
check-services.bat

# 3. 停止所有服务
stop-all-services.bat
```

**Linux/Mac 用户:**

```bash
# 1. 给脚本执行权限
chmod +x *.sh

# 2. 启动所有服务
./start-all-services.sh

# 3. 检查服务状态
./check-services.sh
```

### 📋 手动启动步骤

#### 1. 克隆项目

```bash
git clone <repository-url>
cd 在线商城系统
```

#### 2. 启动基础设施

```bash
# 启动MySQL、Redis、Nacos等中间件
docker-compose up -d

# 等待服务启动完成 (约30秒)
docker-compose ps
```

#### 3. 初始化数据库

```bash
# 连接MySQL并执行初始化脚本
mysql -u root -p123456 < sql/init-databases.sql

# 或者使用Docker执行
docker exec -i mysql mysql -uroot -p123456 < sql/init-databases.sql
```

#### 4. 启动后端微服务

```bash
cd backend

# 编译所有微服务
mvn clean compile -DskipTests

# 按顺序启动微服务
cd gateway-service && mvn spring-boot:run &     # 网关服务 (8080)
cd auth-service && mvn spring-boot:run &        # 认证服务 (8081)
cd user-service && mvn spring-boot:run &        # 用户服务 (8082)
cd product-service && mvn spring-boot:run &     # 商品服务 (8083)
cd cart-service && mvn spring-boot:run &        # 购物车服务 (8088)
cd order-service && mvn spring-boot:run &       # 订单服务 (8084)
cd payment-service && mvn spring-boot:run &     # 支付服务 (8085)
cd merchant-service && mvn spring-boot:run &    # 商家服务 (8087)
cd admin-service && mvn spring-boot:run &       # 管理服务 (8086)
```

#### 5. 启动前端项目

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 🌐 访问地址

| 服务                | 地址                        | 账号密码    |
| ------------------- | --------------------------- | ----------- |
| 🎨 **前端应用**     | http://localhost:3003       | test/123456 |
| 📡 **API 网关**     | http://localhost:8080       | -           |
| 🎯 **Nacos 控制台** | http://localhost:8848/nacos | nacos/nacos |
| 🗄️ **MySQL 数据库** | localhost:3306              | root/123456 |
| 📊 **Redis 缓存**   | localhost:6379              | -           |

### 📊 微服务端口分配

| 服务名称         | 端口 | 状态检查                              |
| ---------------- | ---- | ------------------------------------- |
| Gateway Service  | 8080 | http://localhost:8080/actuator/health |
| Auth Service     | 8081 | http://localhost:8081/actuator/health |
| User Service     | 8082 | http://localhost:8082/actuator/health |
| Product Service  | 8083 | http://localhost:8083/actuator/health |
| Order Service    | 8084 | http://localhost:8084/actuator/health |
| Payment Service  | 8085 | http://localhost:8085/actuator/health |
| Admin Service    | 8086 | http://localhost:8086/actuator/health |
| Merchant Service | 8087 | http://localhost:8087/actuator/health |
| Cart Service     | 8088 | http://localhost:8088/actuator/health |

### 🔍 启动验证

#### 检查基础设施

```bash
# 检查Docker容器状态
docker-compose ps

# 检查Nacos服务注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=gateway-service
```

#### 检查微服务状态

```bash
# 检查网关健康状态
curl http://localhost:8080/actuator/health

# 检查服务注册情况
curl http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=10
```

#### 测试 API 接口

```bash
# 测试用户注册
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","email":"test@example.com"}'

# 测试商品查询
curl http://localhost:8080/api/product/list?page=1&size=10
```

## 项目结构

```
spring-cloud-mall/
├── common-bom/                 # 依赖管理
├── common-core/                # 公共核心模块
├── common-security/            # 安全模块
├── common-redis/               # Redis模块
├── common-web/                 # Web模块
├── gateway-service/            # 网关服务
├── auth-service/              # 认证服务
├── user-service/              # 用户服务
├── product-service/           # 商品服务
├── order-service/             # 订单服务
├── merchant-service/          # 商家服务
├── admin-service/             # 管理服务
├── frontend/                  # 前端项目
├── sql/                       # 数据库脚本
├── config/                    # 配置文件
├── docker-compose.yml         # Docker编排文件
└── README.md                  # 项目说明
```

## 开发规范

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用统一的代码格式化配置
- 必须编写单元测试
- 接口必须有完整的文档注释

### 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

### 分支管理

- `main`: 主分支，用于生产环境
- `develop`: 开发分支
- `feature/*`: 功能分支
- `hotfix/*`: 热修复分支

## 部署说明

### Docker 部署

```bash
# 构建镜像
docker-compose build

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### Kubernetes 部署

```bash
# 部署到K8s集群
kubectl apply -f k8s/

# 查看部署状态
kubectl get pods -n mall
```

## 监控告警

### 应用监控

- 使用 Prometheus 收集指标
- Grafana 展示监控面板
- 支持自定义告警规则

### 日志监控

- 使用 ELK Stack 收集日志
- 支持日志检索和分析
- 异常日志自动告警

### 链路追踪

- 使用 SkyWalking 进行链路追踪
- 支持性能分析和问题定位

## 性能优化

### 缓存策略

- Redis 多级缓存
- 本地缓存+分布式缓存
- 缓存预热和更新策略

### 数据库优化

- 读写分离
- 分库分表
- 索引优化

### 接口优化

- 接口限流
- 数据压缩
- CDN 加速

## 安全防护

### 认证授权

- JWT Token 认证
- RBAC 权限控制
- OAuth2.0 集成

### 数据安全

- 敏感数据加密
- SQL 注入防护
- XSS 攻击防护

### 接口安全

- 接口签名验证
- 防重放攻击
- 限流熔断

## 测试策略

### 单元测试

- JUnit5 + Mockito
- 测试覆盖率要求 80%+

### 集成测试

- TestContainers
- 端到端测试

### 性能测试

- JMeter 压力测试
- 性能基准测试

## 常见问题

### Q: 如何解决 Nacos 连接失败？

A: 检查 Nacos 服务是否启动，确认网络连接正常。

### Q: 如何配置多环境？

A: 在 Nacos 中创建不同的命名空间，对应 dev/test/prod 环境。

### Q: 如何扩展新的微服务？

A: 参考现有服务结构，创建新的服务模块，注册到 Nacos。

## 🔧 常见问题 FAQ

### Q1: 启动脚本闪退怎么办？

**A**: 这是最常见的问题，通常由以下原因引起:

1. **Docker Desktop 未运行** (90%的情况)
   ```bash
   # 检查 Docker 是否运行
   docker ps
   ```
   解决: 启动 Docker Desktop，等待完全启动后重试

2. **使用调试模式定位问题**
   ```bash
   # 运行调试版本，查看详细错误信息
   start-dev-debug.bat
   ```

3. **查看完整排查指南**
   - 📖 [故障排查指南](TROUBLESHOOTING.md)

---

### Q2: 端口被占用怎么办？

**A**: 检查并释放被占用的端口

```powershell
# 查看端口占用
netstat -ano | findstr :8080

# 结束占用进程 (PID 从上一命令获取)
taskkill /PID <进程ID> /F
```

常用端口: `3307, 6379, 8848, 8080-8089, 5173`

---

### Q3: 服务启动失败怎么办?

**A**: 按以下步骤排查:

1. **查看服务日志**
   ```bash
   # 查看特定服务日志
   pwsh -File tail-logs.ps1 gateway-service
   
   # 或直接打开日志文件
   notepad logs\gateway-service.log
   ```

2. **检查服务状态**
   ```bash
   pwsh -File check-services-silent.ps1
   ```

3. **重启单个服务**
   ```bash
   pwsh -File restart-service.ps1 gateway-service
   ```

---

### Q4: Maven 下载依赖很慢?

**A**: 配置国内镜像源

编辑 `~/.m2/settings.xml`:
```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```

---

### Q5: 如何只启动部分服务?

**A**: 编辑 `start-dev-silent.bat`，注释掉不需要的服务:

```batch
REM set "SERVICES_CONFIG=!SERVICES_CONFIG!payment-service:8085::3;"
REM set "SERVICES_CONFIG=!SERVICES_CONFIG!sms-service:8089::3;"
```

---

### Q6: 新增服务后如何启动?

**A**: 无需修改脚本！

1. 在 `backend/` 下创建新服务目录
2. 添加 `pom.xml` 和源代码
3. 配置 `application.yml` 中的端口
4. 运行 `start-dev-silent.bat` - 新服务会自动被检测并启动

---

### Q7: 如何查看所有服务的API文档?

**A**: 各服务的 Swagger 文档地址:

- 网关: http://localhost:8080/doc.html
- 用户服务: http://localhost:8082/doc.html
- 商品服务: http://localhost:8083/doc.html
- ... (其他服务类似)

---

### Q8: Docker 容器无法启动?

**A**: 尝试以下方法:

```bash
# 1. 停止并删除所有容器
docker-compose -f docker-compose-dev.yml down -v

# 2. 清理 Docker 缓存
docker system prune -a

# 3. 重新启动
start-dev-silent.bat
```

---

### 获取更多帮助

- 📖 [快速启动指南](QUICK_START.md)
- 📖 [故障排查指南](TROUBLESHOOTING.md)
- 📖 [服务管理指南](QUICK_REFERENCE.md)
- 📖 [自动服务检测说明](docs/AUTO_SERVICE_DETECTION.md)

---

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目地址: https://github.com/your-repo/spring-cloud-mall
- 问题反馈: https://github.com/your-repo/spring-cloud-mall/issues
- 邮箱: mall@example.com

## 致谢

感谢所有为本项目做出贡献的开发者！

---

**注意**: 本项目仅用于学习和研究目的，请勿用于商业用途。
