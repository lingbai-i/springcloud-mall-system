# 分类基础操作API

<cite>
**Referenced Files in This Document**   
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java)
- [CategoryService.java](file://backend/product-service/src/main/java/com/mall/product/service/CategoryService.java)
- [Category.java](file://backend/product-service/src/main/java/com/mall/product/domain/entity/Category.java)
- [R.java](file://backend/common-core/src/main/java/com/mall/common/core/domain/R.java)
</cite>

## 目录
1. [简介](#简介)
2. [接口安全认证](#接口安全认证)
3. [创建分类](#创建分类)
4. [更新分类信息](#更新分类信息)
5. [删除分类](#删除分类)
6. [查询分类详情](#查询分类详情)
7. [批量操作](#批量操作)
8. [错误处理机制](#错误处理机制)

## 简介
分类基础操作API提供了对商品分类的增删改查（CRUD）功能，支持创建、更新、删除和查询分类信息。该API通过`CategoryController`中的`createCategory`、`updateCategory`、`deleteCategory`和`getCategoryById`等方法实现核心功能。所有接口均采用RESTful风格设计，返回统一的响应格式，并通过JWT进行安全认证。

**Section sources**
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L12-L433)

## 接口安全认证
所有分类管理接口均需通过JWT（JSON Web Token）进行身份验证。请求时必须在HTTP头中包含`Authorization`字段，其值为`Bearer <token>`格式的JWT令牌。系统通过`JwtAuthenticationFilter`和`JwtUtils`组件验证令牌的有效性，确保只有经过认证的用户才能访问分类管理功能。

**Section sources**
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L24)
- [JwtUtils.java](file://backend/user-service/src/main/java/com/mall/user/utils/JwtUtils.java)

## 创建分类
### 请求信息
- **HTTP方法**: POST
- **URL**: `/api/categories`
- **请求头**: `Authorization: Bearer <token>`
- **请求体**: JSON格式的分类对象

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| name | String | 是 | 分类名称，必须唯一 |
| parentId | Long | 否 | 父分类ID，顶级分类为null |
| level | Integer | 否 | 分类层级，系统自动计算 |
| sort | Integer | 否 | 排序权重，数值越大越靠前 |
| icon | String | 否 | 分类图标URL |
| status | Integer | 否 | 状态（1-启用，0-禁用） |
| description | String | 否 | 分类描述 |

### 响应格式
```json
{
  "code": 200,
  "message": "分类创建成功",
  "data": null,
  "timestamp": 1732214400000
}
```

### 业务规则
1. 分类名称必须唯一，创建时会进行重复性校验
2. 系统自动维护分类层级，最大层级限制为3级
3. 新创建的分类默认状态为启用（status=1）
4. sort字段默认值为100，可后续调整

### HTTP请求示例
```http
POST /api/categories HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "name": "智能手机",
  "parentId": 1,
  "description": "各类智能手机产品"
}
```

**Section sources**
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L130-L147)
- [CategoryService.java](file://backend/product-service/src/main/java/com/mall/product/service/CategoryService.java#L67-L68)

## 更新分类信息
### 请求信息
- **HTTP方法**: PUT
- **URL**: `/api/categories`
- **请求头**: `Authorization: Bearer <token>`
- **请求体**: JSON格式的分类对象（包含ID）

### 请求参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 分类ID |
| name | String | 是 | 分类名称，更新时需保证唯一性 |
| parentId | Long | 否 | 父分类ID |
| sort | Integer | 否 | 排序权重 |
| icon | String | 否 | 分类图标URL |
| status | Integer | 否 | 状态（1-启用，0-禁用） |
| description | String | 否 | 分类描述 |

### 响应格式
```json
{
  "code": 200,
  "message": "分类更新成功",
  "data": null,
  "timestamp": 1732214400000
}
```

### 业务规则
1. 更新时会检查分类名称的唯一性，避免与其他分类重名
2. 修改父分类时，系统会自动更新层级信息
3. 不允许将分类移动到其子分类下，防止循环引用
4. 可以单独更新分类状态，无需传递完整对象

### HTTP请求示例
```http
PUT /api/categories HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "id": 5,
  "name": "高端智能手机",
  "sort": 200,
  "description": "旗舰级智能手机产品"
}
```

**Section sources**
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L152-L169)
- [CategoryService.java](file://backend/product-service/src/main/java/com/mall/product/service/CategoryService.java#L75-L76)

## 删除分类
### 请求信息
- **HTTP方法**: DELETE
- **URL**: `/api/categories/{id}`
- **请求头**: `Authorization: Bearer <token>`

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 要删除的分类ID |

### 响应格式
```json
{
  "code": 200,
  "message": "分类删除成功",
  "data": null,
  "timestamp": 1732214400000
}
```

### 业务规则
1. 删除前会检查是否存在子分类，如果有子分类则不允许删除
2. 删除分类时，该分类下的商品会自动归类到父分类
3. 系统会记录删除操作日志，便于审计追踪
4. 删除操作是软删除，数据不会从数据库中物理移除

### HTTP请求示例
```http
DELETE /api/categories/5 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Section sources**
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L174-L190)
- [CategoryService.java](file://backend/product-service/src/main/java/com/mall/product/service/CategoryService.java#L83-L84)

## 查询分类详情
### 请求信息
- **HTTP方法**: GET
- **URL**: `/api/categories/{id}`
- **请求头**: `Authorization: Bearer <token>`

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 分类ID |

### 响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 5,
    "name": "智能手机",
    "parentId": 1,
    "level": 2,
    "sort": 100,
    "icon": "https://example.com/icon/phone.png",
    "status": 1,
    "description": "各类智能手机产品",
    "createTime": "2025-10-22T10:00:00",
    "updateTime": "2025-10-22T10:00:00"
  },
  "timestamp": 1732214400000
}
```

### 业务规则
1. 根据ID精确查询单个分类的完整信息
2. 返回结果包含分类的基本属性和元数据
3. 如果分类不存在，返回404错误
4. 支持查询已删除的分类（软删除）记录

### HTTP请求示例
```http
GET /api/categories/5 HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Section sources**
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L72-L88)
- [CategoryService.java](file://backend/product-service/src/main/java/com/mall/product/service/CategoryService.java#L42-L43)

## 批量操作
### 批量删除分类
- **HTTP方法**: DELETE
- **URL**: `/api/categories/batch`
- **请求体**: 分类ID列表 `[1, 2, 3]`

### 批量更新分类状态
- **HTTP方法**: PUT
- **URL**: `/api/categories/batch/status`
- **请求参数**: `ids=[1,2,3]&status=0`

### 业务规则
1. 批量删除时，系统会逐个检查每个分类的删除条件
2. 批量操作支持事务，确保数据一致性
3. 返回结果包含成功和失败的统计信息
4. 单次批量操作最多支持100个分类

**Section sources**
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L196-L212)
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L353-L369)

## 错误处理机制
### 统一响应格式
所有API接口均使用`R<T>`类作为统一的响应封装，包含以下字段：
- `code`: 状态码（200-成功，500-失败）
- `message`: 响应消息
- `data`: 返回数据
- `timestamp`: 时间戳

### 常见错误码
| 状态码 | 错误信息 | 说明 |
|-------|---------|------|
| 401 | 未授权 | JWT令牌无效或过期 |
| 403 | 权限不足 | 用户无权执行该操作 |
| 404 | 分类不存在 | 请求的分类ID不存在 |
| 500 | 操作失败 | 服务器内部错误 |

### 异常处理
系统通过全局异常处理器捕获并处理各类异常，包括：
- `BusinessException`: 业务逻辑异常
- `AuthenticationException`: 认证异常
- `PermissionDeniedException`: 权限异常
- `IllegalArgumentException`: 参数异常

**Section sources**
- [R.java](file://backend/common-core/src/main/java/com/mall/common/core/domain/R.java)
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L46-L48)
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L86-L88)