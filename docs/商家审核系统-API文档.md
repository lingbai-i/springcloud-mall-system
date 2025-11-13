# 商家审核系统 - API 文档

## 概述

商家审核系统提供完整的商家入驻申请、审核、管理功能。本文档详细说明所有相关 API 接口。

**版本**: v1.0  
**更新时间**: 2025-11-12  
**基础URL**: `http://localhost:8080/api`

---

## 目录

1. [商家申请接口](#商家申请接口)
2. [管理员审核接口](#管理员审核接口)
3. [数据模型](#数据模型)
4. [错误码说明](#错误码说明)

---

## 商家申请接口

### 1. 提交商家入驻申请

**接口**: `POST /merchant/apply`

**描述**: 商家提交入驻申请，系统将保存申请信息并等待管理员审核

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "entityType": "enterprise",          // 必填，主体类型：enterprise-企业, individual-个体, personal-个人
  "shopType": "flagship",              // 选填，店铺类型：flagship-旗舰店, specialty-专卖店, franchise-专营店, ordinary-普通企业店, small-小店
  "shopName": "测试旗舰店",            // 必填，店铺名称，2-50字符
  "contactName": "张三",               // 必填，联系人姓名
  "contactPhone": "13800138000",       // 必填，联系电话，11位手机号
  "email": "test@example.com",         // 必填，邮箱地址
  "companyName": "测试科技有限公司",    // 企业/个体必填，公司名称
  "creditCode": "91110000000000000X",  // 企业/个体必填，统一社会信用代码
  "legalPerson": "张三",               // 企业/个体必填，法人代表
  "businessLicense": "http://...",     // 企业/个体必填，营业执照图片URL
  "idCard": "110101199001011234",      // 个人必填，身份证号
  "idCardFront": "http://...",         // 个人必填，身份证正面照URL
  "idCardBack": "http://...",          // 个人必填，身份证反面照URL
  "username": "testshop",              // 必填，登录账号，唯一
  "password": "123456"                 // 必填，登录密码，6-20字符
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "success": true,
  "message": "申请提交成功，请等待审核",
  "data": {
    "applicationId": 1,
    "status": "pending",
    "submittedAt": "2025-11-12T07:30:00",
    "shopName": "测试旗舰店"
  }
}
```

**错误响应** (500):
```json
{
  "code": 500,
  "success": false,
  "message": "提交失败: 用户名已存在"
}
```

**错误情况**:
- 用户名已存在
- 手机号已被使用
- 店铺名称已存在
- 必填字段缺失
- 数据格式不正确

---

### 2. 查询申请详情

**接口**: `GET /merchant/applications/{id}`

**描述**: 查询指定申请的详细信息

**路径参数**:
- `id`: 申请ID

**成功响应** (200):
```json
{
  "code": 200,
  "success": true,
  "data": {
    "id": 1,
    "shopName": "测试旗舰店",
    "entityType": "enterprise",
    "contactName": "张三",
    "contactPhone": "13800138000",
    "email": "test@example.com",
    "companyName": "测试科技有限公司",
    "creditCode": "91110000000000000X",
    "legalPerson": "张三",
    "businessLicense": "http://...",
    "username": "testshop",
    "approvalStatus": 0,
    "createdTime": "2025-11-12T07:30:00",
    "updatedTime": "2025-11-12T07:30:00"
  }
}
```

---

## 管理员审核接口

### 1. 获取申请列表

**接口**: `GET /admin/merchants/applications`

**描述**: 管理员查询商家入驻申请列表，支持分页、筛选、搜索

**请求头**:
```
Authorization: Bearer {token}
```

**查询参数**:
- `page`: 页码，默认 1
- `size`: 每页大小，默认 20
- `status`: 审批状态，0-待审核，1-已通过，2-已拒绝，不传则查询全部
- `keyword`: 搜索关键词，支持店铺名称、联系人、手机号模糊搜索

**示例请求**:
```
GET /admin/merchants/applications?page=1&size=20&status=0&keyword=测试
```

**成功响应** (200):
```json
{
  "code": 200,
  "success": true,
  "data": {
    "records": [
      {
        "id": 1,
        "shopName": "测试旗舰店",
        "entityType": "enterprise",
        "entityTypeText": "企业",
        "shopType": "flagship",
        "shopTypeText": "旗舰店",
        "contactName": "张三",
        "contactPhone": "13800138000",
        "contactPhoneMasked": "138****8000",
        "email": "test@example.com",
        "username": "testshop",
        "approvalStatus": 0,
        "createdTime": "2025-11-12T07:30:00",
        "updatedTime": "2025-11-12T07:30:00"
      }
    ],
    "total": 1,
    "size": 20,
    "current": 1,
    "pages": 1
  }
}
```

---

### 2. 获取申请统计

**接口**: `GET /admin/merchants/applications/stats`

**描述**: 获取各状态申请的统计数据

**请求头**:
```
Authorization: Bearer {token}
```

**成功响应** (200):
```json
{
  "code": 200,
  "success": true,
  "data": {
    "pending": 5,    // 待审核数量
    "approved": 12,  // 已通过数量
    "rejected": 3,   // 已拒绝数量
    "total": 20      // 总申请数
  }
}
```

---

### 3. 审批申请

**接口**: `PUT /admin/merchants/applications/{id}/approve`

**描述**: 管理员审批商家入驻申请，通过后自动创建商家账号

**路径参数**:
- `id`: 申请ID

**请求头**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "approved": true,                    // 必填，是否通过，true-通过，false-拒绝
  "reason": "资料齐全，符合入驻条件"  // 拒绝时必填，通过时选填
}
```

**成功响应** (200):
```json
{
  "code": 200,
  "success": true,
  "message": "审核通过"
}
```

**业务逻辑**:
1. 验证申请是否存在
2. 验证申请是否已被审核
3. 更新审批状态和审批信息
4. 如果审批通过：
   - 自动创建商家账号（`merchant` 表）
   - 设置商家状态为正常
   - 关联商家ID到申请记录
5. 如果审批拒绝：
   - 仅更新审批状态和原因
   - 不创建商家账号

---

### 4. 获取申请详情

**接口**: `GET /admin/merchants/applications/{id}`

**描述**: 管理员查询指定申请的详细信息

**路径参数**:
- `id`: 申请ID

**请求头**:
```
Authorization: Bearer {token}
```

**成功响应** (200):
```json
{
  "code": 200,
  "success": true,
  "data": {
    "id": 1,
    "shopName": "测试旗舰店",
    "entityType": "enterprise",
    "entityTypeText": "企业",
    "shopType": "flagship",
    "shopTypeText": "旗舰店",
    "contactName": "张三",
    "contactPhone": "13800138000",
    "email": "test@example.com",
    "companyName": "测试科技有限公司",
    "creditCode": "91110000000000000X",
    "legalPerson": "张三",
    "businessLicense": "http://...",
    "username": "testshop",
    "approvalStatus": 1,
    "approvalReason": "资料齐全，符合入驻条件",
    "approvalTime": "2025-11-12T08:00:00",
    "approvalBy": 1,
    "approvalByName": "管理员",
    "merchantId": 10,
    "createdTime": "2025-11-12T07:30:00",
    "updatedTime": "2025-11-12T08:00:00"
  }
}
```

---

## 数据模型

### MerchantApplicationDTO (申请提交)

| 字段 | 类型 | 必填 | 说明 | 验证规则 |
|------|------|------|------|----------|
| entityType | String | 是 | 主体类型 | enterprise/individual/personal |
| shopType | String | 否 | 店铺类型 | flagship/specialty/franchise/ordinary/small |
| shopName | String | 是 | 店铺名称 | 2-50字符，唯一 |
| contactName | String | 是 | 联系人姓名 | - |
| contactPhone | String | 是 | 联系电话 | 11位手机号，唯一 |
| email | String | 是 | 邮箱地址 | 有效的邮箱格式 |
| companyName | String | 条件 | 公司名称 | 企业/个体必填 |
| creditCode | String | 条件 | 统一社会信用代码 | 企业/个体必填，18位 |
| legalPerson | String | 条件 | 法人代表 | 企业/个体必填 |
| businessLicense | String | 条件 | 营业执照URL | 企业/个体必填 |
| idCard | String | 条件 | 身份证号 | 个人必填，18位 |
| idCardFront | String | 条件 | 身份证正面URL | 个人必填 |
| idCardBack | String | 条件 | 身份证反面URL | 个人必填 |
| username | String | 是 | 登录账号 | 唯一，字母数字 |
| password | String | 是 | 登录密码 | 6-20字符 |

### MerchantApplicationVO (申请展示)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 申请ID |
| shopName | String | 店铺名称 |
| entityType | String | 主体类型代码 |
| entityTypeText | String | 主体类型文本 |
| shopType | String | 店铺类型代码 |
| shopTypeText | String | 店铺类型文本 |
| contactName | String | 联系人姓名 |
| contactPhone | String | 联系电话（完整） |
| contactPhoneMasked | String | 联系电话（脱敏） |
| email | String | 邮箱地址 |
| companyName | String | 公司名称 |
| creditCode | String | 统一社会信用代码 |
| legalPerson | String | 法人代表 |
| businessLicense | String | 营业执照URL |
| idCard | String | 身份证号 |
| idCardFront | String | 身份证正面URL |
| idCardBack | String | 身份证反面URL |
| username | String | 登录账号 |
| approvalStatus | Integer | 审批状态：0-待审核，1-已通过，2-已拒绝 |
| approvalReason | String | 审批意见 |
| approvalTime | DateTime | 审批时间 |
| approvalBy | Long | 审批人ID |
| approvalByName | String | 审批人姓名 |
| merchantId | Long | 关联的商家ID（通过后） |
| createdTime | DateTime | 申请时间 |
| updatedTime | DateTime | 更新时间 |

### 审批状态说明

| 状态码 | 状态名称 | 说明 |
|--------|----------|------|
| 0 | 待审核 | 申请已提交，等待管理员审核 |
| 1 | 已通过 | 管理员审核通过，商家账号已创建 |
| 2 | 已拒绝 | 管理员审核拒绝，不予入驻 |

---

## 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式和必填项 |
| 401 | 未授权 | 需要登录或token已过期 |
| 403 | 无权限 | 需要管理员权限 |
| 404 | 资源不存在 | 检查申请ID是否正确 |
| 500 | 服务器错误 | 联系技术支持 |

---

## 接口调用示例

### cURL 示例

#### 1. 提交申请
```bash
curl -X POST http://localhost:8080/api/merchant/apply \
  -H "Content-Type: application/json" \
  -d '{
    "entityType": "enterprise",
    "shopName": "测试旗舰店",
    "contactName": "张三",
    "contactPhone": "13800138000",
    "email": "test@example.com",
    "companyName": "测试科技有限公司",
    "creditCode": "91110000000000000X",
    "legalPerson": "张三",
    "businessLicense": "http://example.com/license.jpg",
    "username": "testshop",
    "password": "123456"
  }'
```

#### 2. 查询申请列表（管理员）
```bash
curl -X GET "http://localhost:8080/api/admin/merchants/applications?page=1&size=20&status=0" \
  -H "Authorization: Bearer {token}"
```

#### 3. 审批申请（管理员）
```bash
curl -X PUT http://localhost:8080/api/admin/merchants/applications/1/approve \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "approved": true,
    "reason": "资料齐全，符合入驻条件"
  }'
```

### JavaScript 示例

```javascript
// 提交申请
const submitApplication = async (data) => {
  const response = await fetch('http://localhost:8080/api/merchant/apply', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  })
  return await response.json()
}

// 查询申请列表（管理员）
const getApplicationList = async (params) => {
  const query = new URLSearchParams(params).toString()
  const response = await fetch(`http://localhost:8080/api/admin/merchants/applications?${query}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  return await response.json()
}

// 审批申请（管理员）
const approveApplication = async (id, data) => {
  const response = await fetch(`http://localhost:8080/api/admin/merchants/applications/${id}/approve`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  })
  return await response.json()
}
```

---

## 业务流程

### 商家入驻流程

```
1. 商家填写入驻申请表单
   ↓
2. 提交申请（POST /merchant/apply）
   ↓
3. 系统验证数据唯一性和格式
   ↓
4. 保存到 merchant_applications 表
   ↓
5. 返回申请ID和状态
   ↓
6. 管理员在后台查看待审核列表
   ↓
7. 管理员审核申请（PUT /admin/merchants/applications/{id}/approve）
   ↓
8. 系统更新审批状态
   ↓
9. 如果通过：自动创建商家账号
   ↓
10. 商家收到审核结果通知
```

### 数据流转

```
前端表单
  ↓ POST /api/merchant/apply
Gateway (8080)
  ↓ lb://merchant-service
Merchant Service (8087)
  ↓ MerchantApplicationController
  ↓ MerchantApplicationService
  ↓ MerchantApplicationRepository
MySQL (mall_merchant.merchant_applications)
```

---

## 安全说明

### 数据验证

1. **唯一性验证**:
   - 用户名必须唯一
   - 手机号必须唯一
   - 店铺名称必须唯一

2. **格式验证**:
   - 手机号：11位数字，1开头
   - 邮箱：标准邮箱格式
   - 身份证号：18位
   - 信用代码：18位

3. **必填验证**:
   - 根据主体类型动态验证必填字段
   - 企业/个体：需要营业执照相关信息
   - 个人：需要身份证相关信息

### 权限控制

1. **商家申请接口**: 无需登录，公开访问
2. **管理员审核接口**: 需要管理员权限
3. **数据脱敏**: 列表展示时手机号自动脱敏

### 密码安全

- 使用 BCrypt 加密存储
- 传输时使用 HTTPS（生产环境）
- 不在日志中记录密码

---

## 测试用例

### 测试场景 1：企业商家入驻

```json
{
  "entityType": "enterprise",
  "shopType": "flagship",
  "shopName": "华为官方旗舰店",
  "contactName": "王经理",
  "contactPhone": "13900139000",
  "email": "huawei@example.com",
  "companyName": "华为技术有限公司",
  "creditCode": "91440300708461136T",
  "legalPerson": "任正非",
  "businessLicense": "http://localhost:9000/mall-files/license1.jpg",
  "username": "huawei_official",
  "password": "Huawei@2025"
}
```

### 测试场景 2：个人商家入驻

```json
{
  "entityType": "personal",
  "shopName": "小张的杂货铺",
  "contactName": "张小明",
  "contactPhone": "13800138001",
  "email": "zhangxm@example.com",
  "idCard": "110101199001011234",
  "idCardFront": "http://localhost:9000/mall-files/id_front.jpg",
  "idCardBack": "http://localhost:9000/mall-files/id_back.jpg",
  "username": "zhangxm_shop",
  "password": "Zhang@123"
}
```

### 测试场景 3：审批通过

```json
{
  "approved": true,
  "reason": "资料齐全，审核通过"
}
```

### 测试场景 4：审批拒绝

```json
{
  "approved": false,
  "reason": "营业执照已过期，请更新后重新申请"
}
```

---

## 常见问题

### Q1: 提交申请后多久能审核？
A: 管理员会在 1-3 个工作日内完成审核，请耐心等待。

### Q2: 审核被拒绝后能重新申请吗？
A: 可以，修改相关信息后使用新的用户名重新提交申请。

### Q3: 忘记申请时填写的用户名怎么办？
A: 请联系客服，提供手机号和店铺名称进行查询。

### Q4: 审核通过后如何登录？
A: 使用申请时填写的用户名和密码登录商家后台。

---

## 技术支持

- **文档版本**: v1.0
- **最后更新**: 2025-11-12
- **技术支持**: support@mall.com
- **问题反馈**: https://github.com/your-repo/issues











