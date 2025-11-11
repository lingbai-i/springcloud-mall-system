# 基于 SpringCloud 的微服务在线商城系统

## 项目简介

本项目是一个基于 SpringCloud Alibaba 的微服务架构在线商城系统，采用前后端分离的设计模式，前端使用 Vue3+Element Plus，后端使用 Spring Boot + Spring Cloud Alibaba 技术栈，严格遵循国产中间件要求。

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

## 🎯 本地开发（推荐）

**适用场景**：日常开发、调试、快速迭代

### 一键启动

```bash
start-dev.bat
```

此脚本会：

1. 启动 Docker 基础设施（MySQL、Redis、Nacos）
2. 询问是否启动后端微服务
3. 询问是否启动前端

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
