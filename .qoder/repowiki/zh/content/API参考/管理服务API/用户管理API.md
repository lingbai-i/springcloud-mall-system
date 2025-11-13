# 用户管理API

<cite>
**本文档引用文件**  
- [UserManagementController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/UserManagementController.java)
- [UserManagementService.java](file://backend/admin-service/src/main/java/com/mall/admin/service/UserManagementService.java)
- [UserManagementServiceImpl.java](file://backend/admin-service/src/main/java/com/mall/admin/service/impl/UserManagementServiceImpl.java)
- [UserServiceClient.java](file://backend/admin-service/src/main/java/com/mall/admin/client/UserServiceClient.java)
- [PageResult.java](file://backend/common-core/src/main/java/com/mall/common/core/domain/PageResult.java)
- [R.java](file://backend/common-core/src/main/java/com/mall/common/core/domain/R.java)
- [GlobalExceptionHandler.java](file://backend/admin-service/src/main/java/com/mall/admin/handler/GlobalExceptionHandler.java)
- [BusinessException.java](file://backend/admin-service/src/main/java/com/mall/admin/exception/BusinessException.java)
</cite>

## 目录
1. [用户列表查询接口](#用户列表查询接口)
2. [用户详情获取接口](#用户详情获取接口)
3. [用户禁用与启用接口](#用户禁用与启用接口)
4. [用户统计信息接口](#用户统计信息接口)
5. [统一响应结构](#统一响应结构)
6. [错误处理机制](#错误处理机制)

## 用户列表查询接口

该接口用于分页查询系统中的用户列表，支持关键词搜索和状态过滤。

### 接口信息
- **路径**: `GET /admin/users`
- **功能**: 查询用户列表并支持分页、搜索和状态筛选

### 请求参数
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `page` | Integer | 否 | 1 | 当前页码 |
| `size` | Integer | 否 | 10 | 每页大小 |
| `keyword` | String | 否 | - | 搜索关键词（支持用户名、手机号等模糊匹配） |
| `status` | Integer | 否 | - | 用户状态（0: 禁用, 1: 启用） |

### 响应结构
返回 `R<PageResult<Map<String, Object>>>` 结构，其中 `PageResult` 包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | List<Map<String, Object>> | 用户数据列表 |
| `total` | Long | 总记录数 |
| `current` | Long | 当前页码 |
| `size` | Long | 每页大小 |
| `pages` | Long | 总页数（自动计算） |

用户数据（Map中的字段）通常包含：
- `id`: 用户ID
- `username`: 用户名
- `phone`: 手机号
- `email`: 邮箱
- `status`: 状态（0: 禁用, 1: 启用）
- `createdAt`: 创建时间

### 请求示例
```http
GET /admin/users?page=1&size=10&keyword=张三&status=1
```

### 响应示例
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1001,
        "username": "zhangsan",
        "phone": "13800138000",
        "email": "zhangsan@example.com",
        "status": 1,
        "createdAt": "2025-01-10T10:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  },
  "timestamp": 1737000000000
}
```

**Section sources**
- [UserManagementController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/UserManagementController.java#L30-L39)
- [UserManagementService.java](file://backend/admin-service/src/main/java/com/mall/admin/service/UserManagementService.java#L24-L24)
- [PageResult.java](file://backend/common-core/src/main/java/com/mall/common/core/domain/PageResult.java#L18-L35)

## 用户详情获取接口

该接口用于获取指定用户的详细信息。

### 接口信息
- **路径**: `GET /admin/users/{id}`
- **功能**: 获取用户详情

### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | Long | 是 | 用户ID |

### 响应格式
返回 `R<Map<String, Object>>`，Map中包含用户所有详细信息，通常包括：
- `id`: 用户ID
- `username`: 用户名
- `nickname`: 昵称
- `phone`: 手机号
- `email`: 邮箱
- `avatar`: 头像URL
- `gender`: 性别
- `birthday`: 生日
- `status`: 状态
- `createdAt`: 创建时间
- `updatedAt`: 更新时间

### 请求示例
```http
GET /admin/users/1001
```

### 响应示例
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "username": "zhangsan",
    "nickname": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "avatar": "https://example.com/avatar.jpg",
    "gender": 1,
    "birthday": "1990-01-01",
    "status": 1,
    "createdAt": "2025-01-10T10:00:00",
    "updatedAt": "2025-01-10T10:00:00"
  },
  "timestamp": 1737000000000
}
```

**Section sources**
- [UserManagementController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/UserManagementController.java#L44-L48)
- [UserManagementService.java](file://backend/admin-service/src/main/java/com/mall/admin/service/UserManagementService.java#L32-L32)

## 用户禁用与启用接口

提供用户禁用和启用功能，操作需记录审计日志。

### 禁用用户接口
- **路径**: `PUT /admin/users/{id}/disable`
- **功能**: 禁用指定用户

### 启用用户接口
- **路径**: `PUT /admin/users/{id}/enable`
- **功能**: 启用指定用户

### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | Long | 是 | 用户ID |

### 权限要求
- 操作用户必须具有管理员权限
- 系统通过JWT验证身份，并从请求上下文中获取管理员ID

### 操作流程
1. 验证管理员身份和权限
2. 从 `HttpServletRequest` 中获取 `adminId`（通过 `request.getAttribute("adminId")`）
3. 调用用户服务执行禁用/启用操作
4. 记录审计日志（包含操作人、操作类型、资源ID等）
5. 返回操作结果

### 管理员ID获取
```java
Long adminId = (Long) request.getAttribute("adminId");
```
管理员ID由前置的认证过滤器（如JWT过滤器）在用户登录验证通过后注入到请求属性中。

### 请求示例
```http
PUT /admin/users/1001/disable
```

### 响应示例（成功）
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1737000000000
}
```

### 审计日志记录
操作成功后，系统会自动记录审计日志，包含：
- `adminId`: 操作管理员ID
- `operationType`: 操作类型（"禁用用户" 或 "启用用户"）
- `resourceType`: 资源类型（"user"）
- `resourceId`: 用户ID
- `operationDesc`: 操作描述
- `status`: 操作状态（1: 成功）
- `createdAt`: 操作时间

**Section sources**
- [UserManagementController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/UserManagementController.java#L53-L68)
- [UserManagementServiceImpl.java](file://backend/admin-service/src/main/java/com/mall/admin/service/impl/UserManagementServiceImpl.java#L50-L76)
- [UserManagementService.java](file://backend/admin-service/src/main/java/com/mall/admin/service/UserManagementService.java#L40-L48)

## 用户统计信息接口

该接口用于获取用户相关的统计数据。

### 接口信息
- **路径**: `GET /admin/users/stats`
- **功能**: 获取用户统计数据

### 响应数据指标
返回 `R<Map<String, Object>>`，包含以下统计指标：

| 指标 | 键名 | 类型 | 说明 |
|------|------|------|------|
| 总用户数 | `totalUsers` | Long | 系统中所有用户总数 |
| 启用用户数 | `enabledUsers` | Long | 状态为启用的用户数量 |
| 禁用用户数 | `disabledUsers` | Long | 状态为禁用的用户数量 |
| 今日新增用户 | `newUsersToday` | Long | 今天注册的用户数量 |
| 本周新增用户 | `newUsersThisWeek` | Long | 本周注册的用户数量 |
| 上周新增用户 | `newUsersLastWeek` | Long | 上周注册的用户数量 |
| 本月新增用户 | `newUsersThisMonth` | Long | 本月注册的用户数量 |

### 请求示例
```http
GET /admin/users/stats
```

### 响应示例
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalUsers": 1500,
    "enabledUsers": 1480,
    "disabledUsers": 20,
    "newUsersToday": 5,
    "newUsersThisWeek": 35,
    "newUsersLastWeek": 28,
    "newUsersThisMonth": 120
  },
  "timestamp": 1737000000000
}
```

**Section sources**
- [UserManagementController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/UserManagementController.java#L73-L77)
- [UserManagementServiceImpl.java](file://backend/admin-service/src/main/java/com/mall/admin/service/impl/UserManagementServiceImpl.java#L78-L85)
- [UserManagementService.java](file://backend/admin-service/src/main/java/com/mall/admin/service/UserManagementService.java#L55-L55)

## 统一响应结构

所有API接口均返回统一的响应结构 `R<T>`，确保前端处理的一致性。

### R类结构
| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 状态码（200: 成功, 500: 失败） |
| `message` | String | 返回消息 |
| `data` | T | 返回数据（泛型） |
| `timestamp` | long | 时间戳（毫秒） |

### 常用状态码
- `200`: 操作成功
- `400`: 参数校验失败
- `401`: 未授权或认证失败
- `403`: 权限不足
- `500`: 系统内部错误

### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { /* 具体数据 */ },
  "timestamp": 1737000000000
}
```

### 空数据成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1737000000000
}
```

**Section sources**
- [R.java](file://backend/common-core/src/main/java/com/mall/common/core/domain/R.java#L15-L137)

## 错误处理机制

系统提供完善的错误处理机制，确保API的健壮性和可维护性。

### 异常类型
| 异常类型 | HTTP状态码 | 说明 |
|---------|-----------|------|
| `BusinessException` | 500 | 业务逻辑异常 |
| `PermissionDeniedException` | 403 | 权限不足 |
| `UnauthorizedException` | 401 | 未授权访问 |
| `MethodArgumentNotValidException` | 400 | 参数校验失败 |
| `Exception` | 500 | 系统未知异常 |

### 全局异常处理
通过 `@RestControllerAdvice` 实现全局异常捕获，统一返回格式。

### 错误响应示例
```json
{
  "code": 500,
  "message": "查询用户列表失败: 数据库连接异常",
  "data": null,
  "timestamp": 1737000000000
}
```

```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": null,
  "timestamp": 1737000000000
}
```

```json
{
  "code": 403,
  "message": "权限不足，无法执行此操作",
  "data": null,
  "timestamp": 1737000000000
}
```

### 业务异常处理
当调用用户服务失败时，会抛出 `BusinessException`，包含具体的错误信息：

```java
R<PageResult<Map<String, Object>>> result = userServiceClient.getUserList(page, size, keyword, status);
if (!result.isSuccess()) {
    throw new BusinessException("查询用户列表失败: " + result.getMessage());
}
```

**Section sources**
- [GlobalExceptionHandler.java](file://backend/admin-service/src/main/java/com/mall/admin/handler/GlobalExceptionHandler.java#L17-L87)
- [BusinessException.java](file://backend/admin-service/src/main/java/com/mall/admin/exception/BusinessException.java#L6-L23)