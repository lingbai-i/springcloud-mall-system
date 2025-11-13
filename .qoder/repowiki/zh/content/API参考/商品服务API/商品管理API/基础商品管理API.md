# 基础商品管理API

<cite>
**Referenced Files in This Document**   
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java)
- [Product.java](file://backend/product-service/src/main/java/com/mall/product/domain/entity/Product.java)
- [product.js](file://frontend/src/api/product.js)
- [MerchantProductController.java](file://backend/merchant-service/src/main/java/com/mall/merchant/controller/MerchantProductController.java)
</cite>

## 目录
1. [简介](#简介)
2. [商品实体类](#商品实体类)
3. [商品增删改查操作](#商品增删改查操作)
4. [商品状态管理](#商品状态管理)
5. [分页与搜索查询](#分页与搜索查询)
6. [性能优化建议](#性能优化建议)
7. [使用示例](#使用示例)

## 简介
本API文档详细描述了基础商品管理系统的各项功能，涵盖商品的增删改查、状态管理、上下架操作、分页查询及搜索功能。系统基于`ProductController`中的端点实现，支持商品全生命周期管理，包括创建、更新、删除、状态变更等核心操作。文档同时说明了`Product`实体类的字段含义与约束条件，并提供使用示例，帮助开发者快速理解并集成商品管理功能。

**Section sources**
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L16-L692)
- [Product.java](file://backend/product-service/src/main/java/com/mall/product/domain/entity/Product.java#L11-L183)

## 商品实体类
`Product`实体类定义了商品的核心属性，包含名称、描述、价格、库存、分类等关键字段。所有字段均通过注解进行数据校验与映射，确保数据一致性与完整性。

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| name | String | 商品名称 | 必填，唯一 |
| description | String | 商品描述 | 可选 |
| price | Double | 商品价格 | 必填，大于0 |
| stock | Integer | 商品库存 | 必填，非负 |
| categoryId | Long | 商品分类ID | 必填，关联分类表 |
| brandId | Long | 商品品牌ID | 可选 |
| mainImage | String | 商品主图URL | 可选 |
| detailImages | String | 详情图片URL列表（JSON格式） | 可选 |
| status | Integer | 商品状态：0-下架，1-上架 | 默认为1 |
| sales | Integer | 商品销量 | 默认为0 |
| weight | Integer | 商品重量（克） | 可选 |
| specifications | String | 规格参数（JSON格式） | 可选 |
| keywords | String | SEO关键词 | 可选 |
| originalPrice | Double | 原价（用于折扣显示） | 可选 |
| costPrice | Double | 成本价 | 可选 |
| stockWarning | Integer | 库存预警值 | 可选 |
| unit | String | 商品单位（件、个、盒等） | 可选 |
| barcode | String | 条形码 | 可选 |
| model | String | 型号 | 可选 |
| hasSpecs | Integer | 是否支持多规格：0-单规格，1-多规格 | 默认为0 |
| skuList | String | SKU信息列表（JSON格式） | 多规格时必填 |
| attributes | String | 商品属性（颜色、尺寸等，JSON格式） | 可选 |
| rating | Double | 商品评分（1-5星） | 可选 |
| reviewCount | Integer | 评价数量 | 默认为0 |
| tags | String | 商品标签（热销、新品、推荐等） | 可选 |
| detailContent | String | 详情描述（富文本） | 可选 |
| videoUrl | String | 商品视频URL | 可选 |
| isVirtual | Integer | 是否虚拟商品：0-实物，1-虚拟 | 默认为0 |
| freightTemplateId | Long | 运费模板ID | 可选 |
| categoryName | String | 分类名称（冗余字段） | 用于展示 |
| brandName | String | 品牌名称（冗余字段） | 用于展示 |

**Section sources**
- [Product.java](file://backend/product-service/src/main/java/com/mall/product/domain/entity/Product.java#L11-L183)

## 商品增删改查操作
商品管理API提供标准的CRUD接口，支持对商品进行创建、查询、更新和删除操作。

### 新增商品
通过POST请求创建新商品。

**请求**
```
POST /api/products
Content-Type: application/json
```

**请求体**
```json
{
  "name": "商品名称",
  "description": "商品描述",
  "price": 99.9,
  "stock": 100,
  "categoryId": 1
}
```

**响应**
- 成功：`{ "code": 200, "msg": "商品新增成功" }`
- 失败：`{ "code": 500, "msg": "商品新增失败" }`

### 查询商品详情
根据商品ID获取商品详细信息。

**请求**
```
GET /api/{id}
```

**响应**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "商品名称",
    "price": 99.9,
    "stock": 100,
    "status": 1
  }
}
```

### 更新商品信息
通过PUT请求更新商品信息。

**请求**
```
PUT /api/products
Content-Type: application/json
```

**请求体**
```json
{
  "id": 1,
  "name": "更新后的名称",
  "price": 89.9
}
```

**响应**
- 成功：`{ "code": 200, "msg": "商品更新成功" }`
- 失败：`{ "code": 500, "msg": "商品更新失败" }`

### 删除商品
根据商品ID删除商品。

**请求**
```
DELETE /api/{id}
```

**响应**
- 成功：`{ "code": 200, "msg": "商品删除成功" }`
- 失败：`{ "code": 500, "msg": "商品删除失败" }`

**Section sources**
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L214-L281)

## 商品状态管理
商品状态管理支持上下架操作和批量状态更新，便于商家灵活控制商品展示。

### 更新商品状态
修改商品上下架状态。

**请求**
```
PUT /api/{id}/status?status=1
```

**参数**
- `status`: 0-下架，1-上架

**响应**
- 成功：`{ "code": 200, "msg": "状态更新成功" }`
- 失败：`{ "code": 500, "msg": "状态更新失败" }`

### 批量删除商品
批量删除多个商品。

**请求**
```
DELETE /api/batch
Content-Type: application/json
```

**请求体**
```json
[1, 2, 3]
```

**响应**
- 成功：`{ "code": 200, "msg": "批量删除成功" }`
- 失败：`{ "code": 500, "msg": "批量删除失败" }`

### 批量更新状态
支持批量更新商品状态（如批量上架/下架）。

**请求**
```
PUT /api/status/batch
Content-Type: application/json
```

**请求体**
```json
[
  { "id": 1, "status": 1 },
  { "id": 2, "status": 0 }
]
```

**响应**
- 成功：`{ "code": 200, "msg": "批量状态更新成功" }`
- 失败：`{ "code": 500, "msg": "批量状态更新失败" }`

**Section sources**
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L315-L306)

## 分页与搜索查询
系统提供分页查询和关键词搜索功能，支持按分类、关键词、价格范围等条件筛选商品。

### 分页查询商品
根据分类ID分页查询商品。

**请求**
```
GET /api/products?current=1&size=10&categoryId=1
```

**参数**
- `current`: 当前页码，默认1
- `size`: 每页大小，默认10
- `categoryId`: 分类ID（可选）

**响应**
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

### 搜索商品
根据关键词搜索商品。

**请求**
```
GET /api/search?current=1&size=10&keyword=手机
```

**参数**
- `keyword`: 搜索关键词

**响应**
```json
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 5,
    "size": 10,
    "current": 1
  }
}
```

### 获取热销商品
获取销量最高的商品列表。

**请求**
```
GET /api/hot?limit=10
```

**响应**
```json
{
  "code": 200,
  "data": [
    { "id": 1, "name": "热销商品1", "sales": 1000 },
    { "id": 2, "name": "热销商品2", "sales": 950 }
  ]
}
```

### 获取推荐商品
获取系统推荐的商品列表。

**请求**
```
GET /api/recommend?limit=10
```

**响应**
```json
{
  "code": 200,
  "data": [
    { "id": 3, "name": "推荐商品1", "rating": 4.8 },
    { "id": 4, "name": "推荐商品2", "rating": 4.7 }
  ]
}
```

**Section sources**
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L47-L128)

## 性能优化建议
为确保商品管理API在高并发场景下的稳定性和响应速度，建议采取以下优化措施：

1. **分页查询最佳实践**
   - 使用合理的分页大小（建议10-50条/页）
   - 避免深度分页（如第1000页），可通过游标分页替代
   - 在`categoryId`、`status`等常用查询字段上建立数据库索引

2. **缓存策略**
   - 对商品详情、分类列表等读多写少的数据使用Redis缓存
   - 设置合理的缓存过期时间（如10-30分钟）
   - 商品更新时及时清除或更新缓存

3. **数据库优化**
   - 为`name`、`price`、`stock`等查询字段建立复合索引
   - 定期分析慢查询日志，优化SQL执行计划
   - 考虑对商品表进行垂直或水平分表

4. **接口优化**
   - 提供字段选择参数，避免返回不必要的字段
   - 对搜索接口实现Elasticsearch全文检索
   - 使用CDN加速商品图片等静态资源访问

5. **批量操作优化**
   - 批量删除和更新使用数据库批量操作，减少网络往返
   - 限制单次批量操作的数量（建议不超过1000条）
   - 异步处理耗时的批量任务

**Section sources**
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L47-L692)

## 使用示例
以下为商品管理API的典型使用场景示例。

### 创建商品
```javascript
// 前端调用示例
import request from '@/utils/request';

const createProduct = (data) => {
  return request({
    url: '/api/products',
    method: 'post',
    data
  });
};

// 使用
createProduct({
  name: 'iPhone 15',
  description: '最新款苹果手机',
  price: 5999.0,
  stock: 100,
  categoryId: 1
}).then(res => {
  console.log('商品创建成功');
});
```

### 商品上下架
```javascript
// 上架商品
const putOnShelf = (id) => {
  return request({
    url: `/api/${id}/status`,
    method: 'put',
    params: { status: 1 }
  });
};

// 下架商品
const takeOffShelf = (id) => {
  return request({
    url: `/api/${id}/status`,
    method: 'put',
    params: { status: 0 }
  });
};

// 使用
putOnShelf(1).then(() => {
  console.log('商品已上架');
});
```

### 批量删除商品
```javascript
const batchDelete = (ids) => {
  return request({
    url: '/api/batch',
    method: 'delete',
    data: ids
  });
};

// 使用
batchDelete([1, 2, 3]).then(() => {
  console.log('批量删除成功');
});
```

### 分页查询商品
```javascript
const getProducts = (current, size, categoryId) => {
  return request({
    url: '/api/products',
    method: 'get',
    params: { current, size, categoryId }
  });
};

// 使用
getProducts(1, 10, 1).then(res => {
  console.log('商品列表:', res.data.records);
});
```

**Section sources**
- [product.js](file://frontend/src/api/product.js#L1-L171)
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L47-L692)