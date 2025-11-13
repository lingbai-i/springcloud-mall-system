# 商家审核API

<cite>
**本文档引用的文件**
- [MerchantApplicationController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/MerchantApplicationController.java)
- [MerchantApprovalController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/MerchantApprovalController.java)
- [ApprovalRequest.java](file://backend/admin-service/src/main/java/com/mall/admin/domain/dto/ApprovalRequest.java)
- [MerchantApprovalServiceImpl.java](file://backend/admin-service/src/main/java/com/mall/admin/service/impl/MerchantApprovalServiceImpl.java)
- [JwtUtil.java](file://backend/admin-service/src/main/java/com/mall/admin/util/JwtUtil.java)
- [MerchantServiceClient.java](file://backend/admin-service/src/main/java/com/mall/admin/client/MerchantServiceClient.java)
</cite>

## 目录
1. [简介](#简介)
2. [商家申请列表接口](#商家申请列表接口)
3. [申请详情接口](#申请详情接口)
4. [审批接口](#审批接口)
5. [请求/响应示例](#请求响应示例)
6. [错误处理与日志记录](#错误处理与日志记录)

## 简介
本文档全面记录了商家入驻申请的审核流程，详细描述了商家审核相关的API接口。系统通过`admin-service`作为管理端服务，调用`merchant-service`完成商家申请的查询与审批操作。管理员可通过分页、状态过滤和关键字搜索功能查看申请列表，获取申请详情，并执行审批操作。审批过程中会进行商家账号创建、审批日志记录和短信通知等操作。

**Section sources**
- [MerchantApprovalController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/MerchantApprovalController.java#L15-L146)

## 商家申请列表接口
### 接口信息
- **路径**: `GET /admin/merchants/applications`
- **功能**: 获取商家入驻申请列表，支持分页、状态过滤和关键字搜索

### 请求参数
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| page | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 20 | 每页大小 |
| status | Integer | 否 | 无 | 审批状态过滤（0:待审核, 1:已通过, 2:已拒绝） |
| keyword | String | 否 | 无 | 关键字搜索（匹配店铺名称、联系人、手机号等） |

### 响应数据结构
响应体为标准的JSON格式，包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 响应状态码（200表示成功） |
| success | Boolean | 操作是否成功 |
| message | String | 响应消息 |
| data | Object | 分页数据对象 |

其中`data`对象包含以下分页信息：
- **total**: Long类型，表示总记录数
- **page**: Integer类型，表示当前页码
- **size**: Integer类型，表示每页大小
- **records**: Array类型，包含当前页的申请记录列表

### 功能说明
该接口支持灵活的查询功能：
- **分页功能**: 通过`page`和`size`参数实现分页查询，避免一次性加载过多数据
- **状态过滤**: 通过`status`参数筛选特定状态的申请（如只查看待审核申请）
- **关键字搜索**: 通过`keyword`参数在店铺名称、联系人姓名、手机号等字段进行模糊搜索

**Section sources**
- [MerchantApprovalController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/MerchantApprovalController.java#L33-L66)
- [MerchantApprovalServiceImpl.java](file://backend/admin-service/src/main/java/com/mall/admin/service/impl/MerchantApprovalServiceImpl.java#L40-L78)

## 申请详情接口
### 接口信息
- **路径**: `GET /admin/merchants/applications/{id}`
- **功能**: 获取指定商家入驻申请的详细信息

### 请求参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 申请ID |

### 响应数据
成功响应包含申请的完整信息，包括：
- 基本信息：店铺名称、用户名、密码（已加密）、联系人信息
- 企业信息：公司名称、统一社会信用代码、法人代表、营业执照
- 个人信息：身份证号、身份证正反面照片
- 申请状态：当前审批状态、审批时间、审批人等

响应格式与申请列表接口一致，包含`code`、`success`、`message`和`data`字段。

**Section sources**
- [MerchantApprovalController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/MerchantApprovalController.java#L71-L94)
- [MerchantApprovalServiceImpl.java](file://backend/admin-service/src/main/java/com/mall/admin/service/impl/MerchantApprovalServiceImpl.java#L159-L185)

## 审批接口
### 接口信息
- **路径**: `PUT /admin/merchants/applications/{id}/approve`
- **功能**: 审批商家入驻申请

### 请求参数
#### 路径参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 申请ID |

#### 请求体（JSON）
请求体为`ApprovalRequest`对象，包含以下字段：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approved | Boolean | 是 | 是否通过审批（true:通过, false:拒绝） |
| reason | String | 否 | 审批备注/拒绝原因 |

**ApprovalRequest对象说明**：
- `approved`字段必须提供，不能为空
- 当`approved`为`false`（拒绝）时，`reason`字段必须填写拒绝原因
- 当`approved`为`true`（通过）时，`reason`字段可选

### 管理员信息获取
审批操作需要记录执行审批的管理员信息，目前实现中存在TODO标记，需要从JWT token中提取：

```java
// TODO: 从token中获取真实管理员ID
Long adminId = 1L; 
// TODO: 从token中获取真实管理员用户名
String adminUsername = "admin";
```

实际应通过`JwtUtil`工具类从请求的JWT token中提取管理员信息：
- 从token的claims中获取`adminId`
- 从token的claims中获取`username`
- 同时记录操作的客户端IP地址

### 审批流程
审批操作包含以下步骤：
1. 验证申请是否已被审批（防止重复操作）
2. 验证拒绝原因（拒绝时必须填写原因）
3. 更新申请的审批状态
4. 如果审批通过，创建商家账号
5. 记录审批日志
6. 发送短信通知给申请人

**Section sources**
- [MerchantApprovalController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/MerchantApprovalController.java#L99-L146)
- [ApprovalRequest.java](file://backend/admin-service/src/main/java/com/mall/admin/domain/dto/ApprovalRequest.java#L12-L25)
- [MerchantApprovalServiceImpl.java](file://backend/admin-service/src/main/java/com/mall/admin/service/impl/MerchantApprovalServiceImpl.java#L83-L154)
- [JwtUtil.java](file://backend/admin-service/src/main/java/com/mall/admin/util/JwtUtil.java#L76-L87)

## 请求/响应示例
### 成功审批申请
**请求**:
```json
PUT /admin/merchants/applications/123/approve
Content-Type: application/json

{
  "approved": true,
  "reason": "资料齐全，符合入驻条件"
}
```

**响应**:
```json
{
  "code": 200,
  "success": true,
  "message": "审批通过成功",
  "data": {
    "success": true,
    "applicationId": 123,
    "approvalStatus": 1,
    "merchantId": 456,
    "smsSent": true
  }
}
```

### 拒绝申请
**请求**:
```json
PUT /admin/merchants/applications/123/approve
Content-Type: application/json

{
  "approved": false,
  "reason": "营业执照图片不清晰，请重新上传"
}
```

**响应**:
```json
{
  "code": 200,
  "success": true,
  "message": "已拒绝申请",
  "data": {
    "success": true,
    "applicationId": 123,
    "approvalStatus": 2,
    "smsSent": true
  }
}
```

### 错误响应示例
**请求**:
```json
PUT /admin/merchants/applications/123/approve
Content-Type: application/json

{
  "approved": false
}
```

**响应**:
```json
{
  "code": 500,
  "success": false,
  "message": "拒绝申请时必须填写原因"
}
```

**Section sources**
- [MerchantApprovalController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/MerchantApprovalController.java#L100-L131)
- [MerchantApprovalServiceImpl.java](file://backend/admin-service/src/main/java/com/mall/admin/service/impl/MerchantApprovalServiceImpl.java#L100-L103)

## 错误处理与日志记录
### 错误处理机制
系统实现了完善的错误处理机制：
- **参数验证**: 使用`@Valid`注解对请求体进行验证，确保`approved`字段不为空
- **业务逻辑验证**: 在服务层进行业务规则验证，如检查申请是否已被审批、拒绝时必须填写原因等
- **异常捕获**: 在控制器层捕获所有异常，返回统一的错误响应格式
- **HTTP状态码**: 成功响应返回200，错误响应返回500

### 日志记录策略
系统在关键操作点记录详细日志：
- **操作日志**: 记录管理员的查询和审批操作，包括操作时间、管理员ID、IP地址等
- **审批日志**: 记录每次审批的详细信息，包括申请ID、审批结果、审批原因等
- **错误日志**: 记录所有异常信息，便于问题排查
- **通知日志**: 记录短信发送结果

日志记录采用异步方式，避免影响主业务流程的性能。

### 审批后处理
审批成功后，系统会执行以下操作：
1. **创建商家账号**: 如果审批通过，自动创建商家账号
2. **记录审批日志**: 将审批操作记录到数据库
3. **发送短信通知**: 向申请人发送审批结果短信
   - 通过：发送包含登录信息的欢迎短信
   - 拒绝：发送包含拒绝原因的通知短信

这些操作均被妥善处理，即使某个步骤失败（如短信发送失败），也不会影响审批结果的保存。

**Section sources**
- [MerchantApprovalController.java](file://backend/admin-service/src/main/java/com/mall/admin/controller/MerchantApprovalController.java#L59-L65)
- [MerchantApprovalServiceImpl.java](file://backend/admin-service/src/main/java/com/mall/admin/service/impl/MerchantApprovalServiceImpl.java#L151-L153)
- [MerchantApprovalServiceImpl.java](file://backend/admin-service/src/main/java/com/mall/admin/service/impl/MerchantApprovalServiceImpl.java#L246-L323)