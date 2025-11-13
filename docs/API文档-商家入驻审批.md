# 商家入驻审批系统 API 文档

> **版本**: 1.0  
> **更新时间**: 2025-11-11  
> **Base URL**: http://localhost:8080

---

## 📋 API 概览

### 商家端接口

| 接口名称 | 方法 | 路径 | 说明 |
|---------|------|------|------|
| 提交入驻申请 | POST | /api/merchants/apply | 商家提交入驻申请 |
| 查询申请详情 | GET | /api/merchants/applications/{id} | 查询申请状态 |
| 申请统计 | GET | /api/merchants/applications/stats | 获取统计数据 |

### 管理员接口

| 接口名称 | 方法 | 路径 | 说明 |
|---------|------|------|------|
| 申请列表 | GET | /api/admin/merchants/applications | 查询所有申请 |
| 申请详情 | GET | /api/admin/merchants/applications/{id} | 查询详情 |
| 审批申请 | PUT | /api/admin/merchants/applications/{id}/approve | 审批操作 |

---

## 🔌 详细接口说明

### 1. 提交商家入驻申请

**接口地址**: `POST /api/merchants/apply`

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "entityType": "enterprise",
  "shopType": "flagship",
  "shopName": "测试旗舰店",
  "contactName": "张三",
  "contactPhone": "13800138000",
  "email": "test@example.com",
  "companyName": "测试科技有限公司",
  "creditCode": "91110000MA001234AB",
  "legalPerson": "张三",
  "businessLicense": "http://localhost:9000/mall-avatars/file_123.jpg",
  "username": "merchant001",
  "password": "password123",
  "remark": "备注信息"
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| entityType | string | 是 | enterprise/individual/personal |
| shopType | string | 企业时必填 | flagship/specialty/franchise/ordinary |
| shopName | string | 是 | 店铺名称 |
| contactName | string | 是 | 联系人姓名 |
| contactPhone | string | 是 | 手机号 |
| email | string | 否 | 邮箱 |
| companyName | string | 企业/个体时必填 | 公司名称 |
| creditCode | string | 企业/个体时必填 | 统一社会信用代码 |
| legalPerson | string | 否 | 法定代表人 |
| businessLicense | string | 企业/个体时必填 | 营业执照URL |
| idCard | string | 个人时必填 | 身份证号 |
| idCardFront | string | 个人时必填 | 身份证正面URL |
| idCardBack | string | 个人时必填 | 身份证反面URL |
| username | string | 是 | 登录账号 |
| password | string | 是 | 登录密码 |

**响应示例**:
```json
{
  "code": 200,
  "success": true,
  "message": "申请提交成功！我们将在1个工作日内完成审核",
  "data": {
    "applicationId": 1,
    "shopName": "测试旗舰店",
    "estimatedReviewTime": "1个工作日"
  }
}
```

---

### 2. 获取申请列表（管理员）

**接口地址**: `GET /api/admin/merchants/applications`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | integer | 否 | 页码，默认1 |
| size | integer | 否 | 每页大小，默认20 |
| status | integer | 否 | 0-待审核,1-已通过,2-已拒绝 |
| keyword | string | 否 | 搜索关键词 |

**请求头**:
```
Authorization: Bearer {adminToken}
```

**响应示例**:
```json
{
  "code": 200,
  "success": true,
  "data": {
    "total": 50,
    "page": 1,
    "size": 20,
    "records": [
      {
        "id": 1,
        "shopName": "测试旗舰店",
        "contactName": "张三",
        "contactPhone": "138****8000",
        "contactPhoneMasked": "138****8000",
        "entityType": "enterprise",
        "entityTypeText": "企业",
        "shopType": "flagship",
        "shopTypeText": "旗舰店",
        "approvalStatus": 0,
        "approvalStatusText": "待审核",
        "createdTime": "2025-11-11 18:00:00"
      }
    ]
  }
}
```

---

### 3. 审批商家申请

**接口地址**: `PUT /api/admin/merchants/applications/{id}/approve`

**路径参数**:
- `id`: 申请ID

**请求头**:
```
Content-Type: application/json
Authorization: Bearer {adminToken}
```

**请求体**:
```json
{
  "approved": true,
  "reason": "符合入驻条件，审核通过"
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| approved | boolean | 是 | true-通过, false-拒绝 |
| reason | string | 拒绝时必填 | 审批意见/拒绝原因 |

**响应示例（通过）**:
```json
{
  "code": 200,
  "success": true,
  "message": "审批通过成功",
  "data": {
    "applicationId": 1,
    "approvalStatus": 1,
    "merchantId": 100,
    "smsSent": true
  }
}
```

**响应示例（拒绝）**:
```json
{
  "code": 200,
  "success": true,
  "message": "已拒绝申请",
  "data": {
    "applicationId": 1,
    "approvalStatus": 2,
    "smsSent": true
  }
}
```

---

## 🔐 认证说明

### 管理员接口认证

所有 `/api/admin/*` 接口需要管理员登录认证：

1. 先调用管理员登录接口获取token
2. 在请求头中添加：`Authorization: Bearer {token}`
3. Token有效期：7天

---

## 📊 状态码说明

| 状态码 | 说明 |
|-------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 404 | 资源不存在 |
| 409 | 冲突（如账号已存在） |
| 500 | 服务器错误 |

---

## 🎯 审批状态枚举

| 值 | 说明 |
|----|------|
| 0 | 待审核 |
| 1 | 已通过 |
| 2 | 已拒绝 |

---

## 📱 短信通知

### 审批通过短信
```
【在线商城】恭喜！您的商家入驻申请已审核通过！
店铺名称：{shopName}
登录账号：{username}
请访问商家后台开启电商之旅！
```

### 审批拒绝短信
```
【在线商城】很遗憾，您的商家入驻申请未通过审核。
店铺名称：{shopName}
拒绝原因：{reason}
您可以重新提交申请。
```

---

*最后更新: 2025-11-11*







