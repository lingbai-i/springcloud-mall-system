# Auth Service - 认证授权服务

## 📋 服务概述

认证授权服务（Auth Service）是在线商城系统的核心安全组件，负责统一的身份认证、授权和令牌管理。

### 核心功能

- ✅ **JWT令牌管理**: 生成、验证和刷新JWT访问令牌
- ✅ **用户认证**: 支持密码登录和短信验证码登录
- ✅ **令牌刷新**: 通过刷新令牌获取新的访问令牌
- ✅ **会话管理**: 基于Redis的令牌存储和黑名单机制
- ✅ **安全登出**: 令牌失效和会话清理

### 技术栈

- **Spring Boot 3.x**: 应用框架
- **Spring Security**: 安全框架
- **JWT (JJWT 0.11.5)**: 令牌生成与验证
- **Redis**: 令牌存储和会话管理
- **Nacos**: 服务注册与发现
- **WebClient**: 调用其他微服务
- **Swagger/OpenAPI**: API文档

---

## 🚀 快速开始

### 前置要求

- Java 22+
- Maven 3.8+
- Redis 6.0+
- Nacos 2.0+

### 启动服务

```bash
# 开发环境启动
mvn spring-boot:run

# 或使用指定配置
mvn spring-boot:run -Dspring-boot.run.profiles=simple
```

### 访问地址

- **服务端口**: `http://localhost:8081`
- **API文档**: `http://localhost:8081/swagger-ui.html`
- **健康检查**: `http://localhost:8081/auth/health`

---

## 📡 API接口

### 1. 用户登录

**POST** `/auth/login`

**请求示例**:
```json
{
  "username": "admin",
  "password": "123456",
  "loginType": "password"
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "userId": 1,
    "username": "admin"
  }
}
```

### 2. 刷新令牌

**POST** `/auth/refresh`

**请求示例**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

### 3. 验证令牌

**POST** `/auth/validate`

**请求示例**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "valid": true
  }
}
```

### 4. 用户登出

**POST** `/auth/logout`

**请求头**:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

## ⚙️ 配置说明

### application.yml 核心配置

```yaml
# JWT配置
jwt:
  secret: your-secret-key  # 生产环境必须修改
  access-token-expiration: 900000  # 15分钟
  refresh-token-expiration: 604800000  # 7天
  issuer: mall-auth-service

# Redis配置
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 1

# 用户服务配置
user-service:
  url: http://localhost:8082
```

---

## 🔐 安全机制

### JWT令牌策略

1. **访问令牌（Access Token）**
   - 有效期：15分钟（默认）
   - 用于API请求认证
   - 使用HS512签名算法

2. **刷新令牌（Refresh Token）**
   - 有效期：7天（默认）
   - 用于获取新的访问令牌
   - 存储在Redis中，支持主动失效

### 令牌黑名单

- 用户登出时，令牌加入Redis黑名单
- 黑名单有效期 = 令牌剩余有效期
- 自动清理过期的黑名单记录

### 会话管理

- 无状态Session（Stateless）
- 所有会话信息存储在Redis
- 支持单点登出

---

## 🔄 服务集成

### 与用户服务集成

认证服务通过WebClient调用用户服务的验证接口：

```java
POST http://localhost:8082/api/users/validate
Body: {
  "username": "admin",
  "password": "123456"
}
```

### 网关集成

API网关可以调用认证服务验证令牌：

```java
POST http://localhost:8081/auth/validate
Body: {
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

---

## 📝 开发指南

### 添加新的登录方式

1. 在 `LoginRequest` 中添加新的 `loginType`
2. 在 `AuthService.validateUserCredentials()` 中实现验证逻辑
3. 更新API文档

### 自定义令牌有效期

修改 `application.yml`:

```yaml
jwt:
  access-token-expiration: 1800000  # 30分钟
  refresh-token-expiration: 1209600000  # 14天
```

### 密钥管理

**⚠️ 重要**: 生产环境必须使用强密钥

```bash
# 生成256位随机密钥（建议）
openssl rand -base64 64
```

---

## 🧪 测试

### 使用cURL测试

```bash
# 登录
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 验证令牌
curl -X POST http://localhost:8081/auth/validate \
  -H "Content-Type: application/json" \
  -d '{"token":"your-access-token"}'

# 登出
curl -X POST http://localhost:8081/auth/logout \
  -H "Authorization: Bearer your-access-token"
```

---

## 📊 监控与日志

### 日志级别

```yaml
logging:
  level:
    com.mall.auth: DEBUG  # 开发环境
    org.springframework.security: INFO
```

### 健康检查

```bash
curl http://localhost:8081/auth/health
```

---

## 🔧 故障排查

### 常见问题

1. **令牌验证失败**
   - 检查JWT密钥配置是否一致
   - 确认令牌未过期
   - 检查Redis连接

2. **Redis连接失败**
   - 验证Redis服务是否运行
   - 检查端口和密码配置

3. **用户服务不可用**
   - 确认用户服务已启动
   - 检查服务URL配置

---

## 📖 相关文档

- [JWT官方文档](https://jwt.io/)
- [Spring Security文档](https://spring.io/projects/spring-security)
- [Nacos文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)

---

## 👥 作者

**lingbai** - 2025-11-11

## 📄 许可证

MIT License
