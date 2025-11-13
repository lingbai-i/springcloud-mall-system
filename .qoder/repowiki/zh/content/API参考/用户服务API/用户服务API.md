# 用户服务API

<cite>
**本文档引用的文件**   
- [AuthController.java](file://backend/user-service/src/main/java/com/mall/user/controller/AuthController.java)
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java)
- [LoginRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/LoginRequest.java)
- [RegisterRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/RegisterRequest.java)
- [UpdateUserRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/UpdateUserRequest.java)
- [ChangePasswordRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/ChangePasswordRequest.java)
- [LoginResponse.java](file://backend/user-service/src/main/java/com/mall/user/domain/vo/LoginResponse.java)
- [UserInfoResponse.java](file://backend/user-service/src/main/java/com/mall/user/domain/vo/UserInfoResponse.java)
- [JwtUtils.java](file://backend/user-service/src/main/java/com/mall/user/utils/JwtUtils.java)
- [JwtAuthenticationFilter.java](file://backend/user-service/src/main/java/com/mall/user/security/JwtAuthenticationFilter.java)
- [AuthService.java](file://backend/user-service/src/main/java/com/mall/user/service/AuthService.java)
- [AuthServiceImpl.java](file://backend/user-service/src/main/java/com/mall/user/service/impl/AuthServiceImpl.java)
</cite>

## 目录
1. [简介](#简介)
2. [认证机制](#认证机制)
3. [API接口说明](#api接口说明)
4. [JWT令牌管理](#jwt令牌管理)
5. [请求示例](#请求示例)
6. [错误码处理](#错误码处理)
7. [安全说明](#安全说明)

## 简介
本API文档详细描述了用户服务中的用户注册、登录、个人信息管理等核心功能接口。文档基于`UserController`和`AuthController`中的REST端点，为前端开发者提供完整的认证功能集成指南。系统采用JWT（JSON Web Token）进行身份验证，支持用户名密码登录、手机号验证码登录、自动注册等多种认证方式。

**本文档引用的文件**  
- [AuthController.java](file://backend/user-service/src/main/java/com/mall/user/controller/AuthController.java#L1-L208)
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L1-L839)

## 认证机制
系统采用基于JWT的无状态认证机制，通过`JwtAuthenticationFilter`实现请求的自动认证。认证流程如下：
1. 用户登录或注册成功后，服务器生成JWT访问令牌和刷新令牌
2. 前端在后续请求的`Authorization`头中携带`Bearer {token}`格式的令牌
3. 服务器通过`JwtAuthenticationFilter`自动验证令牌有效性
4. 验证通过的请求将设置Spring Security上下文，包含当前用户信息

不需要认证的公开端点包括：
- `/auth/**`：认证相关接口
- `/api/users/register`：用户注册
- `/api/users/login`：用户登录
- `/api/users/check-*`：用户名/邮箱/手机号可用性检查
- `/api/users/test`：测试接口

**本文档引用的文件**  
- [JwtAuthenticationFilter.java](file://backend/user-service/src/main/java/com/mall/user/security/JwtAuthenticationFilter.java#L1-L136)
- [AuthController.java](file://backend/user-service/src/main/java/com/mall/user/controller/AuthController.java#L1-L208)

## API接口说明

### 用户注册
新用户注册接口，支持邮箱、手机号等多种方式注册。

**HTTP方法**：`POST`  
**URL路径**：`/api/users/register` 或 `/users/register`  
**认证要求**：无需认证

#### 请求参数
| 参数名 | 类型 | 必填 | 描述 | 示例 |
|-------|------|------|------|------|
| username | string | 是 | 用户名，3-20字符，仅字母、数字、下划线 | john_doe |
| password | string | 是 | 密码，6-20字符 | 123456 |
| confirmPassword | string | 是 | 确认密码 | 123456 |
| email | string | 是 | 邮箱地址 | john@example.com |
| phone | string | 否 | 手机号 | 13800138000 |
| nickname | string | 否 | 昵称 | John |

#### 响应格式
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600,
    "userInfo": {
      "id": 1,
      "username": "john_doe",
      "nickname": "John",
      "email": "john@example.com",
      "phone": "13800138000",
      "avatar": null,
      "gender": 0,
      "birthday": null,
      "bio": null,
      "hasSetPassword": true
    }
  }
}
```

**本文档引用的文件**  
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L75-L113)
- [RegisterRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/RegisterRequest.java#L1-L101)

### 用户登录
用户通过用户名和密码登录。

**HTTP方法**：`POST`  
**URL路径**：`/auth/login`  
**认证要求**：无需认证

#### 请求参数
| 参数名 | 类型 | 必填 | 描述 | 示例 |
|-------|------|------|------|------|
| username | string | 是 | 用户名或手机号 | john_doe |
| password | string | 是 | 密码 | 123456 |

#### 响应格式
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userInfo": {
      "id": 1,
      "username": "john_doe",
      "nickname": "John",
      "email": "john@example.com",
      "phone": "13800138000",
      "avatar": null,
      "gender": 0,
      "birthday": null,
      "bio": null,
      "hasSetPassword": true
    }
  }
}
```

**本文档引用的文件**  
- [AuthController.java](file://backend/user-service/src/main/java/com/mall/user/controller/AuthController.java#L64-L73)
- [LoginRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/LoginRequest.java#L1-L39)
- [LoginResponse.java](file://backend/user-service/src/main/java/com/mall/user/domain/vo/LoginResponse.java#L1-L219)

### 手机号验证码登录
支持手机号验证码登录，未注册用户将自动注册。

**HTTP方法**：`POST`  
**URL路径**：`/auth/sms-login`  
**认证要求**：无需认证

#### 请求参数
| 参数名 | 类型 | 必填 | 描述 | 示例 |
|-------|------|------|------|------|
| phone | string | 是 | 手机号 | 13800138000 |
| smsCode | string | 是 | 验证码 | 123456 |

#### 响应格式
与用户登录接口相同，返回`LoginResponse`对象。

**本文档引用的文件**  
- [AuthController.java](file://backend/user-service/src/main/java/com/mall/user/controller/AuthController.java#L49-L58)
- [AuthServiceImpl.java](file://backend/user-service/src/main/java/com/mall/user/service/impl/AuthServiceImpl.java#L194-L270)

### 获取用户信息
获取当前登录用户的详细信息。

**HTTP方法**：`GET`  
**URL路径**：`/api/users/profile` 或 `/users/profile`  
**认证要求**：需要在`Authorization`头中携带有效的JWT令牌

#### 响应格式
```json
{
  "success": true,
  "message": "获取用户信息成功",
  "data": {
    "id": 1,
    "username": "john_doe",
    "nickname": "John",
    "email": "john@example.com",
    "phone": "13800138000",
    "avatar": "http://localhost:9000/mall-avatars/avatar_1.jpg",
    "gender": 1,
    "birthday": "1990-01-01",
    "bio": "这是一个个人简介",
    "status": 1,
    "createTime": "2025-01-27 10:00:00",
    "lastLoginTime": "2025-01-27 10:30:00",
    "hasSetPassword": true
  }
}
```

**本文档引用的文件**  
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L189-L267)
- [UserInfoResponse.java](file://backend/user-service/src/main/java/com/mall/user/domain/vo/UserInfoResponse.java#L1-L233)

### 更新用户信息
更新当前登录用户的信息。

**HTTP方法**：`PUT`  
**URL路径**：`/api/users/profile` 或 `/users/profile`  
**认证要求**：需要在`Authorization`头中携带有效的JWT令牌

#### 请求参数
| 参数名 | 类型 | 必填 | 描述 | 示例 |
|-------|------|------|------|------|
| nickname | string | 否 | 昵称 | John |
| email | string | 否 | 邮箱 | john@example.com |
| phone | string | 否 | 手机号 | 13800138000 |
| avatar | string | 否 | 头像URL | http://example.com/avatar.jpg |

#### 响应格式
```json
{
  "success": true,
  "message": "用户信息更新成功"
}
```

**本文档引用的文件**  
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L277-L333)
- [UpdateUserRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/UpdateUserRequest.java#L1-L64)

### 修改密码
修改当前登录用户的密码。

**HTTP方法**：`PUT`  
**URL路径**：`/api/users/change-password` 或 `/users/change-password`  
**认证要求**：需要在`Authorization`头中携带有效的JWT令牌

#### 请求参数
| 参数名 | 类型 | 必填 | 描述 | 示例 |
|-------|------|------|------|------|
| oldPassword | string | 是 | 当前密码 | oldpassword |
| newPassword | string | 是 | 新密码 | newpassword |
| confirmNewPassword | string | 是 | 确认新密码 | newpassword |

#### 响应格式
```json
{
  "success": true,
  "message": "密码修改成功"
}
```

**本文档引用的文件**  
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L342-L391)
- [ChangePasswordRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/ChangePasswordRequest.java#L1-L62)

### 检查用户名/邮箱/手机号可用性
检查用户名、邮箱或手机号是否已被使用。

**HTTP方法**：`GET`  
**URL路径**：
- `/api/users/check-username?username={username}`
- `/api/users/check-email?email={email}`
- `/api/users/check-phone?phone={phone}`

**认证要求**：无需认证

#### 响应格式
```json
{
  "success": true,
  "available": true,
  "message": "用户名可用"
}
```

**本文档引用的文件**  
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L399-L484)

## JWT令牌管理

### JWT令牌生成
系统使用`JwtUtils`工具类生成JWT令牌，配置参数如下：

| 配置项 | 默认值 | 描述 |
|-------|-------|------|
| `jwt.secret` | mallSecretKeyForJWTTokenGenerationAndValidation2025... | JWT密钥，256位 |
| `jwt.expiration` | 86400000 (24小时) | 访问令牌过期时间 |
| `jwt.refresh-expiration` | 604800000 (7天) | 刷新令牌过期时间 |

```mermaid
sequenceDiagram
participant 前端 as 前端应用
participant AuthController as AuthController
participant AuthService as AuthService
participant JwtUtils as JwtUtils
前端->>AuthController : POST /auth/login
AuthController->>AuthService : login(loginRequest)
AuthService->>JwtUtils : generateToken(username)
JwtUtils-->>AuthService : JWT令牌
AuthService-->>AuthController : LoginResponse
AuthController-->>前端 : 返回包含令牌的响应
```

**图表来源**  
- [AuthController.java](file://backend/user-service/src/main/java/com/mall/user/controller/AuthController.java#L64-L73)
- [AuthServiceImpl.java](file://backend/user-service/src/main/java/com/mall/user/service/impl/AuthServiceImpl.java#L126-L181)
- [JwtUtils.java](file://backend/user-service/src/main/java/com/mall/user/utils/JwtUtils.java#L43-L53)

### 令牌验证流程
```mermaid
flowchart TD
A[收到请求] --> B{请求路径是否在<br/>免认证列表中?}
B --> |是| C[跳过JWT验证]
B --> |否| D[检查Authorization头]
D --> E{是否存在Bearer令牌?}
E --> |否| F[设置匿名上下文]
E --> |是| G[提取JWT令牌]
G --> H[调用JwtUtils验证令牌]
H --> I{令牌是否有效?}
I --> |否| J[清除安全上下文]
I --> |是| K[从令牌获取用户名]
K --> L[创建认证对象]
L --> M[设置安全上下文]
C --> N[继续处理请求]
F --> N
J --> N
M --> N
```

**图表来源**  
- [JwtAuthenticationFilter.java](file://backend/user-service/src/main/java/com/mall/user/security/JwtAuthenticationFilter.java#L61-L123)
- [JwtUtils.java](file://backend/user-service/src/main/java/com/mall/user/utils/JwtUtils.java#L91-L98)

### 令牌刷新机制
当访问令牌即将过期时，前端可以使用刷新令牌获取新的访问令牌。

**HTTP方法**：`POST`  
**URL路径**：`/auth/refresh`  
**认证要求**：需要在`Authorization`头中携带有效的JWT令牌

刷新流程：
1. 用户请求`/auth/refresh`接口
2. 系统验证当前令牌有效性
3. 生成新的访问令牌
4. 将旧令牌加入黑名单（临时禁用）
5. 返回新的访问令牌

```mermaid
sequenceDiagram
participant 前端 as 前端应用
participant AuthController as AuthController
participant AuthService as AuthService
participant JwtUtils as JwtUtils
前端->>AuthController : POST /auth/refresh
AuthController->>AuthService : refreshToken(token)
AuthService->>JwtUtils : validateToken(token)
JwtUtils-->>AuthService : 验证结果
AuthService->>JwtUtils : refreshToken(token)
JwtUtils-->>AuthService : 新令牌
AuthService->>AuthService : logout(oldToken)
AuthService-->>AuthController : 新令牌
AuthController-->>前端 : 返回新令牌
```

**图表来源**  
- [AuthController.java](file://backend/user-service/src/main/java/com/mall/user/controller/AuthController.java#L115-L130)
- [AuthServiceImpl.java](file://backend/user-service/src/main/java/com/mall/user/service/impl/AuthServiceImpl.java#L401-L425)
- [JwtUtils.java](file://backend/user-service/src/main/java/com/mall/user/utils/JwtUtils.java#L165-L173)

## 请求示例

### 用户注册请求示例
```json
POST /api/users/register
Content-Type: application/json

{
  "username": "john_doe",
  "password": "123456",
  "confirmPassword": "123456",
  "email": "john@example.com",
  "phone": "13800138000",
  "nickname": "John"
}
```

### 更新用户信息请求示例
```json
PUT /api/users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
  "nickname": "John Smith",
  "email": "john.smith@example.com",
  "phone": "13800138001"
}
```

### 修改密码请求示例
```json
PUT /api/users/change-password
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json

{
  "oldPassword": "123456",
  "newPassword": "newpassword123",
  "confirmNewPassword": "newpassword123"
}
```

**本文档引用的文件**  
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L75-L391)
- [UpdateUserRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/UpdateUserRequest.java#L1-L64)
- [ChangePasswordRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/ChangePasswordRequest.java#L1-L62)

## 错误码处理

### 常见错误码
| 错误码 | 错误信息 | 原因 | 解决方案 |
|-------|--------|------|--------|
| 400 | 用户名已存在 | 注册时用户名已被使用 | 更换用户名重新注册 |
| 400 | 邮箱已被使用 | 注册时邮箱已被使用 | 更换邮箱或使用找回密码功能 |
| 400 | 手机号已被使用 | 注册时手机号已被使用 | 更换手机号或使用手机登录 |
| 401 | 用户名或密码错误 | 登录凭证不正确 | 检查用户名密码，或使用忘记密码功能 |
| 401 | Token无效或已过期 | JWT令牌无效或已过期 | 使用刷新令牌获取新令牌，或重新登录 |
| 401 | 未提供有效的认证令牌 | 请求缺少Authorization头 | 在请求头中添加有效的JWT令牌 |
| 400 | 两次输入的密码不一致 | 密码和确认密码不匹配 | 确保两次输入的密码完全一致 |
| 400 | 验证码错误或已过期 | 手机验证码不正确或已过期 | 重新获取验证码并输入 |

### 错误响应格式
```json
{
  "success": false,
  "message": "用户名已存在"
}
```

或

```json
{
  "code": 400,
  "msg": "用户名或密码错误"
}
```

**本文档引用的文件**  
- [AuthController.java](file://backend/user-service/src/main/java/com/mall/user/controller/AuthController.java#L54-L57)
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L107-L112)

## 安全说明
1. **密码安全**：所有密码在存储前都使用BCrypt算法进行哈希加密
2. **JWT安全**：使用HS512算法签名，密钥长度符合安全要求
3. **令牌管理**：登出时将令牌加入黑名单（当前临时禁用Redis功能）
4. **输入验证**：所有请求参数都经过JSR-303验证，防止无效数据
5. **CORS配置**：已配置适当的跨域资源共享策略
6. **敏感信息保护**：用户密码等敏感信息不会在响应中返回

**本文档引用的文件**  
- [JwtUtils.java](file://backend/user-service/src/main/java/com/mall/user/utils/JwtUtils.java#L26-L35)
- [AuthServiceImpl.java](file://backend/user-service/src/main/java/com/mall/user/service/impl/AuthServiceImpl.java#L52-L53)
- [JwtAuthenticationFilter.java](file://backend/user-service/src/main/java/com/mall/user/security/JwtAuthenticationFilter.java#L38-L58)