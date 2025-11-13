# 用户管理API

<cite>
**本文档引用的文件**
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java)
- [UserInfoResponse.java](file://backend/user-service/src/main/java/com/mall/user/domain/vo/UserInfoResponse.java)
- [UpdateUserRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/UpdateUserRequest.java)
- [ChangePasswordRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/ChangePasswordRequest.java)
- [MinioService.java](file://backend/user-service/src/main/java/com/mall/user/service/MinioService.java)
</cite>

## 目录
1. [用户信息获取](#用户信息获取)
2. [用户信息更新](#用户信息更新)
3. [密码修改](#密码修改)
4. [头像上传](#头像上传)
5. [用户信息校验](#用户信息校验)
6. [通用文件上传](#通用文件上传)
7. [认证机制说明](#认证机制说明)

## 用户信息获取

该接口用于获取当前登录用户的详细信息。

**接口详情**
- **HTTP方法**: `GET`
- **URL路径**: `/api/users/profile`
- **认证要求**: 需要在请求头中携带有效的JWT令牌

**请求头**
```
Authorization: Bearer <JWT令牌>
```

**响应格式**
响应体为标准JSON格式，包含以下字段：
- `success`: 布尔值，表示请求是否成功
- `message`: 字符串，描述请求结果
- `data`: 对象，包含用户详细信息（UserInfoResponse）

**UserInfoResponse字段说明**
- `userId`: 用户ID
- `username`: 用户名
- `nickname`: 昵称
- `email`: 邮箱
- `phone`: 手机号
- `avatar`: 头像URL
- `gender`: 性别（0-未知，1-男，2-女）
- `birthday`: 生日
- `bio`: 个人简介
- `status`: 状态（0-禁用，1-正常）
- `createTime`: 创建时间
- `lastLoginTime`: 最后登录时间
- `hasSetPassword`: 是否已设置密码

**成功响应示例**
```json
{
  "success": true,
  "message": "获取用户信息成功",
  "data": {
    "userId": 123,
    "username": "john_doe",
    "nickname": "John",
    "email": "john@example.com",
    "phone": "13800138000",
    "avatar": "http://localhost:9000/mall-avatars/avatar_123.jpg",
    "gender": 1,
    "birthday": "1990-01-01",
    "bio": "这是个人简介",
    "status": 1,
    "createTime": "2025-01-27 10:00:00",
    "lastLoginTime": "2025-11-12 14:30:00",
    "hasSetPassword": true
  }
}
```

**错误响应状态码**
- `401`: 未提供有效的认证令牌或令牌无效
- `404`: 用户信息不存在
- `500`: 服务器内部错误

**Section sources**
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L189-L267)

## 用户信息更新

该接口用于更新当前登录用户的信息。

**接口详情**
- **HTTP方法**: `PUT`
- **URL路径**: `/api/users/profile`
- **认证要求**: 需要在请求头中携带有效的JWT令牌
- **请求内容类型**: `application/json`

**请求头**
```
Authorization: Bearer <JWT令牌>
Content-Type: application/json
```

**请求体 (UpdateUserRequest)**
```json
{
  "nickname": "新昵称",
  "email": "newemail@example.com",
  "phone": "13900139000",
  "gender": 1,
  "birthday": "1995-05-05",
  "avatar": "http://example.com/newavatar.jpg",
  "bio": "新的个人简介"
}
```

**字段验证规则**
- `nickname`: 长度2-20个字符
- `email`: 必须是有效的邮箱格式
- `phone`: 必须是有效的手机号格式（1开头，11位数字）
- `gender`: 0（未知）、1（男）、2（女）
- `bio`: 最多200个字符

**成功响应**
```json
{
  "success": true,
  "message": "用户信息更新成功"
}
```

**错误响应**
- `400`: 请求参数验证失败
- `401`: 未提供有效的认证令牌
- `400`: 用户信息更新失败

**请求示例**
```bash
curl -X PUT "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "张三",
    "email": "zhangsan@example.com",
    "phone": "13812345678",
    "gender": 1,
    "birthday": "1990-01-01",
    "bio": "热爱编程的技术爱好者"
  }'
```

**Section sources**
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L277-L333)
- [UpdateUserRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/UpdateUserRequest.java#L7-L64)

## 密码修改

该接口用于修改当前登录用户的密码。

**接口详情**
- **HTTP方法**: `PUT`
- **URL路径**: `/api/users/change-password`
- **认证要求**: 需要在请求头中携带有效的JWT令牌
- **请求内容类型**: `application/json`

**请求头**
```
Authorization: Bearer <JWT令牌>
Content-Type: application/json
```

**请求体 (ChangePasswordRequest)**
```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码",
  "confirmNewPassword": "确认新密码"
}
```

**字段说明**
- `oldPassword`: 当前密码，不能为空
- `newPassword`: 新密码，长度6-20位，不能为空
- `confirmNewPassword`: 确认新密码，必须与新密码一致

**成功响应**
```json
{
  "success": true,
  "message": "密码修改成功"
}
```

**错误响应**
- `400`: 新密码和确认密码不一致
- `401`: 未提供有效的认证令牌或令牌无效
- `400`: 密码修改失败

**请求示例**
```bash
curl -X PUT "http://localhost:8080/api/users/change-password" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "oldpassword123",
    "newPassword": "newpassword456",
    "confirmNewPassword": "newpassword456"
  }'
```

**Section sources**
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L342-L391)
- [ChangePasswordRequest.java](file://backend/user-service/src/main/java/com/mall/user/dto/ChangePasswordRequest.java#L7-L62)

## 头像上传

该接口用于上传用户头像到MinIO存储。

**接口详情**
- **HTTP方法**: `POST`
- **URL路径**: `/api/users/upload-avatar`
- **认证要求**: 需要在请求头中携带有效的JWT令牌
- **请求内容类型**: `multipart/form-data`

**请求头**
```
Authorization: Bearer <JWT令牌>
```

**请求参数**
- `file`: 头像文件，作为multipart表单字段上传

**文件限制**
- 文件类型：仅支持图片文件（image/*）
- 文件大小：不超过2MB

**成功响应**
```json
{
  "success": true,
  "message": "头像上传成功",
  "data": "http://localhost:9000/mall-avatars/avatar_123.jpg"
}
```

**处理流程**
1. 获取当前登录用户信息
2. 验证上传的文件
3. 将文件上传到MinIO存储
4. 更新用户数据库中的头像URL字段
5. 删除旧的头像文件（如果存在）

**请求示例**
```bash
curl -X POST "http://localhost:8080/api/users/upload-avatar" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -F "file=@/path/to/avatar.jpg"
```

**Section sources**
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L626-L693)
- [MinioService.java](file://backend/user-service/src/main/java/com/mall/user/service/MinioService.java)

## 用户信息校验

提供多个接口用于检查用户信息的可用性。

### 检查用户名

**接口详情**
- **HTTP方法**: `GET`
- **URL路径**: `/api/users/check-username`
- **参数**: `username`（要检查的用户名）

**响应示例**
```json
{
  "success": true,
  "available": true,
  "message": "用户名可用"
}
```

### 检查邮箱

**接口详情**
- **HTTP方法**: `GET`
- **URL路径**: `/api/users/check-email`
- **参数**: `email`（要检查的邮箱）

**响应示例**
```json
{
  "success": true,
  "available": false,
  "message": "邮箱已被使用"
}
```

### 检查手机号

**接口详情**
- **HTTP方法**: `GET`
- **URL路径**: `/api/users/check-phone`
- **参数**: `phone`（要检查的手机号）

**响应示例**
```json
{
  "success": true,
  "available": true,
  "message": "手机号可用"
}
```

**通用响应字段**
- `success`: 布尔值，表示请求是否成功
- `available`: 布尔值，表示该信息是否可用
- `message`: 描述信息（"可用"或"已被使用"）

**Section sources**
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L399-L484)

## 通用文件上传

该接口用于商家入驻等场景的通用文件上传。

**接口详情**
- **HTTP方法**: `POST`
- **URL路径**: `/api/users/upload`
- **请求内容类型**: `multipart/form-data`

**请求参数**
- `file`: 要上传的文件

**文件限制**
- 文件类型：仅支持图片文件
- 文件大小：不超过2MB

**成功响应**
```json
{
  "success": true,
  "message": "文件上传成功",
  "data": {
    "url": "http://localhost:9000/mall-avatars/file_1234567890_1234.jpg",
    "filename": "file_1234567890_1234.jpg"
  }
}
```

**处理流程**
1. 验证上传的文件
2. 生成唯一的文件名
3. 将文件上传到MinIO存储
4. 返回文件的访问URL

**请求示例**
```bash
curl -X POST "http://localhost:8080/api/users/upload" \
  -F "file=@/path/to/document.jpg"
```

**Section sources**
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L726-L799)

## 认证机制说明

### JWT令牌传递

所有需要认证的API接口都需要在HTTP请求头中携带JWT令牌：

```
Authorization: Bearer <JWT令牌>
```

其中`<JWT令牌>`是通过登录接口获取的访问令牌。

### 权限验证流程

1. **请求拦截**: 每个需要认证的请求都会被拦截
2. **令牌提取**: 从`Authorization`头中提取JWT令牌
3. **令牌验证**: 验证令牌的有效性和签名
4. **用户信息提取**: 从令牌中解析用户名
5. **权限检查**: 确认用户是否有权限执行该操作

### 敏感操作保护

- **密码修改**: 需要提供原密码进行验证
- **信息更新**: 只能更新当前登录用户的信息
- **头像上传**: 验证文件类型和大小，防止恶意文件上传
- **用户信息获取**: 只能获取当前登录用户的信息

### 开发模式

系统支持开发模式，在开发模式下可以使用默认测试用户，无需提供有效的JWT令牌。

**Section sources**
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L53-L54)
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L197-L235)