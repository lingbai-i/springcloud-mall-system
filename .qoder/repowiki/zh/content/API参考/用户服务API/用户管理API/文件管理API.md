# 文件管理API

<cite>
**本文档引用的文件**   
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java)
- [MinioConfig.java](file://backend/user-service/src/main/java/com/mall/user/config/MinioConfig.java)
- [MinioService.java](file://backend/user-service/src/main/java/com/mall/user/service/MinioService.java)
- [UserService.java](file://backend/user-service/src/main/java/com/mall/user/service/UserService.java)
- [application.yml](file://backend/user-service/src/main/resources/application.yml)
</cite>

## 目录
1. [头像上传接口](#头像上传接口)
2. [通用文件上传接口](#通用文件上传接口)
3. [MinIO存储配置](#minio存储配置)
4. [访问URL生成规则](#访问url生成规则)
5. [异常处理机制](#异常处理机制)
6. [头像上传完整流程](#头像上传完整流程)

## 头像上传接口

头像上传接口通过`POST /users/upload-avatar`端点实现，用于上传用户头像图片。该接口要求使用`multipart/form-data`格式，包含名为`file`的文件字段。

### 接口详情
- **URL路径**: `/users/upload-avatar`
- **HTTP方法**: POST
- **请求头**: 需要包含有效的JWT认证令牌（Authorization: Bearer <token>）
- **请求体**: `multipart/form-data`格式，包含`file`字段
- **文件限制**: 
  - 文件大小不超过2MB
  - 仅支持图片格式（content-type以`image/`开头）

### 请求示例
```http
POST /users/upload-avatar HTTP/1.1
Host: localhost:8082
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="avatar.jpg"
Content-Type: image/jpeg

<二进制文件数据>
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

### 响应示例
```json
{
  "success": true,
  "message": "头像上传成功",
  "data": "http://localhost:9000/mall-avatars/avatar_1_1732672800000.jpg"
}
```

**Section sources**
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L626-L693)
- [MinioService.java](file://backend/user-service/src/main/java/com/mall/user/service/MinioService.java#L47-L69)

## 通用文件上传接口

通用文件上传接口通过`POST /users/upload`端点实现，用于商家入驻等场景的图片文件上传。该接口同样要求使用`multipart/form-data`格式。

### 接口详情
- **URL路径**: `/users/upload`
- **HTTP方法**: POST
- **请求体**: `multipart/form-data`格式，包含`file`字段
- **文件限制**: 
  - 文件大小不超过2MB
  - 仅支持图片格式（content-type以`image/`开头）

### 请求示例
```http
POST /users/upload HTTP/1.1
Host: localhost:8082
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="business_license.jpg"
Content-Type: image/jpeg

<二进制文件数据>
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

### 响应示例
```json
{
  "success": true,
  "message": "文件上传成功",
  "data": {
    "url": "http://localhost:9000/mall-avatars/file_1732672800000_1234.jpg",
    "filename": "file_1732672800000_1234.jpg"
  }
}
```

**Section sources**
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L726-L799)
- [MinioService.java](file://backend/user-service/src/main/java/com/mall/user/service/MinioService.java#L169-L184)

## MinIO存储配置

系统使用MinIO作为对象存储服务，相关配置在`application.yml`文件中定义。

### 配置详情
- **MinIO端点**: `http://localhost:9000`
- **访问密钥**: `minioadmin`
- **密钥**: `minioadmin`
- **存储桶名称**: `mall-files`
- **公共访问URL**: 通过`minio.public-url`配置项定义

### 存储桶创建
系统启动时会自动检查并创建指定的存储桶。如果存储桶不存在，系统会：
1. 创建新的存储桶
2. 设置存储桶为公开读权限，允许外部直接访问图片资源
3. 记录创建日志

```mermaid
flowchart TD
A[系统启动] --> B{存储桶是否存在?}
B --> |否| C[创建存储桶]
C --> D[设置公开读权限]
D --> E[记录创建日志]
B --> |是| F[记录存在日志]
E --> G[MinIO初始化完成]
F --> G
```

**Diagram sources **
- [MinioConfig.java](file://backend/user-service/src/main/java/com/mall/user/config/MinioConfig.java#L17-L91)
- [application.yml](file://backend/user-service/src/main/resources/application.yml#L114-L119)

## 访问URL生成规则

上传文件的访问URL遵循统一的生成规则，确保文件可以被正确访问。

### URL结构
```
{public-url}/{bucket-name}/{file-name}
```

### 组件说明
- **public-url**: MinIO服务的公共访问地址，配置在`minio.public-url`
- **bucket-name**: 存储桶名称，配置在`minio.bucket-name`
- **file-name**: 上传文件的实际文件名

### 头像文件命名规则
头像文件采用特定的命名格式，便于管理和识别：
```
avatar_{userId}_{timestamp}.{extension}
```
其中：
- `userId`: 用户ID
- `timestamp`: 当前时间戳
- `extension`: 原始文件的扩展名

**Section sources**
- [MinioService.java](file://backend/user-service/src/main/java/com/mall/user/service/MinioService.java#L65-L66)
- [MinioService.java](file://backend/user-service/src/main/java/com/mall/user/service/MinioService.java#L190-L194)

## 异常处理机制

系统实现了完善的异常处理机制，确保文件上传过程中的各种异常情况都能得到妥善处理。

### 异常类型及处理
- **认证异常**: 未提供或无效的JWT令牌，返回401状态码
- **文件为空**: 上传的文件为空，返回400状态码
- **文件类型错误**: 非图片格式文件，返回400状态码
- **文件过大**: 超过2MB限制，返回400状态码
- **上传失败**: MinIO存储异常，返回500状态码
- **用户不存在**: 根据用户名无法找到用户，返回404状态码

### 异常处理流程
```mermaid
flowchart TD
A[开始上传] --> B{认证有效?}
B --> |否| C[返回401错误]
B --> |是| D{文件有效?}
D --> |否| E[返回400错误]
D --> |是| F[上传到MinIO]
F --> G{上传成功?}
G --> |否| H[返回500错误]
G --> |是| I[更新数据库]
I --> J{更新成功?}
J --> |否| K[返回500错误]
J --> |是| L[删除旧头像]
L --> M[返回成功响应]
```

**Diagram sources **
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L639-L692)
- [MinioService.java](file://backend/user-service/src/main/java/com/mall/user/service/MinioService.java#L70-L73)

## 头像上传完整流程

头像上传是一个多步骤的完整流程，涉及用户信息获取、文件存储、数据库更新和旧文件清理。

### 流程步骤
1. 获取当前用户身份信息
2. 查询用户数据库记录
3. 验证上传文件（大小、类型）
4. 上传新头像到MinIO存储
5. 更新用户数据库中的头像URL
6. 删除旧头像文件（如果存在且存储在MinIO中）

### 完整流程图
```mermaid
sequenceDiagram
participant Client as 客户端
participant Controller as UserController
participant Service as UserService
participant MinIO as MinioService
participant DB as 数据库
Client->>Controller : POST /upload-avatar + 文件
Controller->>Controller : 验证JWT令牌
Controller->>Service : 根据用户名查询用户
Service-->>Controller : 返回用户信息
Controller->>MinIO : 验证文件(大小/类型)
Controller->>MinIO : 上传新头像到MinIO
MinIO-->>Controller : 返回新头像URL
Controller->>DB : 更新用户头像URL
DB-->>Controller : 更新成功
Controller->>MinIO : 删除旧头像(如存在)
MinIO-->>Controller : 删除结果
Controller-->>Client : 返回成功响应
```

**Diagram sources **
- [UserController.java](file://backend/user-service/src/main/java/com/mall/user/controller/UserController.java#L640-L678)
- [MinioService.java](file://backend/user-service/src/main/java/com/mall/user/service/MinioService.java#L47-L69)
- [UserService.java](file://backend/user-service/src/main/java/com/mall/user/service/UserService.java#L24-L37)