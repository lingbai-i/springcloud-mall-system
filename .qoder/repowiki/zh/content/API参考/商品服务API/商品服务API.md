# 商品服务API

<cite>
**本文档引用文件**   
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java)
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java)
- [StockController.java](file://backend/product-service/src/main/java/com/mall/product/controller/StockController.java)
- [ProductQueryDto.java](file://backend/product-service/src/main/java/com/mall/product/domain/dto/ProductQueryDto.java)
- [ProductDetailDto.java](file://backend/product-service/src/main/java/com/mall/product/domain/dto/ProductDetailDto.java)
</cite>

## 目录
1. [商品查询API](#商品查询api)
2. [分类管理API](#分类管理api)
3. [库存操作API](#库存操作api)
4. [数据传输对象说明](#数据传输对象说明)
5. [分页与筛选参数使用示例](#分页与筛选参数使用示例)
6. [性能优化建议](#性能优化建议)

## 商品查询API

商品查询API提供了多种方式获取商品信息，包括基础查询、搜索功能和商品详情获取。所有商品相关接口均以`/api`为前缀。

### 商品列表获取

通过`GET /api/products`接口可获取商品列表，支持按分类筛选和分页查询。

**请求参数**：
- `current`：当前页码，默认为1
- `size`：每页大小，默认为10
- `categoryId`：分类ID，可选参数，用于按分类筛选

**响应格式**：
返回统一响应结果，包含商品分页数据。响应体遵循`R<Object>`结构，其中data字段包含分页数据。

**Section sources**
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L47-L61)

### 商品详情查询

通过`GET /api/{id}`接口可获取指定商品的详细信息。

**请求参数**：
- `id`：商品ID，路径参数

**响应格式**：
返回`R<Product>`结构，包含商品基本信息。若商品不存在，返回失败响应。

**Section sources**
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L137-L154)

### 高级商品搜索

通过`POST /api/search/advanced`接口可进行高级商品搜索，支持多种筛选条件。

**请求参数**：
- 请求体包含`ProductQueryDto`对象
- `current`：当前页码
- `size`：每页大小

**响应格式**：
返回统一响应结果，包含搜索结果分页数据。

**Section sources**
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L190-L204)

## 分类管理API

分类管理API提供了完整的分类树操作功能，支持分类的增删改查和树形结构管理。

### 分类树获取

通过`GET /api/categories/tree`接口可获取完整的分类树结构。

**请求参数**：
无

**响应格式**：
返回`R<List<Category>>`结构，包含根分类列表，每个分类对象包含其子分类，形成树形结构。

**Section sources**
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L111-L123)

### 分类基础操作

分类控制器提供了完整的CRUD操作：

- `GET /api/categories/all`：获取所有分类列表
- `GET /api/categories/{id}`：根据ID获取分类详情
- `POST /api/categories`：创建新分类
- `PUT /api/categories`：更新分类信息
- `DELETE /api/categories/{id}`：删除分类

**Section sources**
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L37-L191)

### 分类高级功能

分类管理还支持以下高级功能：

- `GET /api/categories/children`：根据父级ID获取子分类列表
- `GET /api/categories/search`：搜索分类
- `GET /api/categories/{id}/path`：获取分类路径
- `GET /api/categories/level/{level}`：根据级别获取分类

**Section sources**
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L94-L288)

## 库存操作API

库存操作API提供了全面的库存管理功能，包括库存查询、变更和预警。

### 库存查询

通过`GET /stock/logs`接口可获取库存变更日志。

**请求参数**：
- `productId`：商品ID，可选
- `skuId`：SKU ID，可选
- `current`：当前页码，默认为1
- `size`：每页大小，默认为10

**响应格式**：
返回库存变更日志分页数据。

**Section sources**
- [StockController.java](file://backend/product-service/src/main/java/com/mall/product/controller/StockController.java#L192-L206)

### 库存变更操作

库存控制器提供了以下变更操作：

- `POST /stock/deduct`：库存扣减
- `POST /stock/rollback`：库存回滚
- `POST /stock/taking`：库存盘点
- `POST /stock/batch/deduct`：批量库存扣减
- `POST /stock/batch/rollback`：批量库存回滚

**请求参数**：
- `productId`：商品ID
- `skuId`：SKU ID（可选）
- `quantity`：数量
- `orderNo`：订单号
- `operatorId`：操作员ID

**响应格式**：
返回操作结果，包含成功状态和消息。

**Section sources**
- [StockController.java](file://backend/product-service/src/main/java/com/mall/product/controller/StockController.java#L81-L181)

### 库存预警

通过`GET /stock/warning`接口可获取库存预警商品列表。

**请求参数**：
- `warningLevel`：预警级别，1-低库存，2-缺货

**响应格式**：
返回预警商品列表。

**Section sources**
- [StockController.java](file://backend/product-service/src/main/java/com/mall/product/controller/StockController.java#L58-L69)

## 数据传输对象说明

### ProductQueryDto

`ProductQueryDto`用于高级商品搜索的查询条件。

**字段说明**：
- `keyword`：商品名称关键词
- `categoryId`：分类ID
- `brandId`：品牌ID
- `minPrice`：最低价格
- `maxPrice`：最高价格
- `status`：商品状态（0-下架，1-上架）
- `hasStock`：是否有库存（true-有库存，false-无库存）
- `sortField`：排序字段（price-价格，sales-销量，createTime-创建时间）
- `sortOrder`：排序方向（asc-升序，desc-降序）
- `tags`：商品标签列表
- `attributes`：商品属性筛选列表

**AttributeFilter字段**：
- `name`：属性名称
- `value`：属性值

**Section sources**
- [ProductQueryDto.java](file://backend/product-service/src/main/java/com/mall/product/domain/dto/ProductQueryDto.java#L16-L84)

### ProductDetailDto

`ProductDetailDto`用于返回商品详细信息。

**字段说明**：
- `product`：商品基本信息
- `skuList`：商品SKU列表
- `totalStock`：商品总库存
- `minPrice`：最低价格
- `maxPrice`：最高价格
- `categoryName`：商品分类名称
- `brandName`：商品品牌名称
- `rating`：商品评价统计

**ProductRatingDto字段**：
- `averageRating`：平均评分
- `totalReviews`：评价总数
- `positiveRate`：好评率

**Section sources**
- [ProductDetailDto.java](file://backend/product-service/src/main/java/com/mall/product/domain/dto/ProductDetailDto.java#L18-L77)

## 分页与筛选参数使用示例

### 分页参数使用

所有分页接口均支持`current`和`size`参数：

```http
GET /api/products?current=1&size=20 HTTP/1.1
```

获取第一页，每页20条商品记录。

### 筛选条件使用

使用`ProductQueryDto`进行高级筛选：

```json
{
  "keyword": "手机",
  "categoryId": 1001,
  "minPrice": 1000,
  "maxPrice": 5000,
  "status": 1,
  "hasStock": true,
  "sortField": "sales",
  "sortOrder": "desc",
  "tags": ["热销", "新品"],
  "attributes": [
    {
      "name": "颜色",
      "value": "黑色"
    },
    {
      "name": "内存",
      "value": "128GB"
    }
  ]
}
```

### 分类树查询示例

```http
GET /api/categories/tree HTTP/1.1
```

返回完整的分类树结构，便于前端构建分类导航。

**Section sources**
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L47-L61)
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L111-L123)
- [ProductQueryDto.java](file://backend/product-service/src/main/java/com/mall/product/domain/dto/ProductQueryDto.java#L16-L84)

## 性能优化建议

### 缓存策略

1. **分类数据缓存**：分类树结构相对稳定，建议在客户端或API网关层进行缓存，减少数据库查询。
2. **商品列表缓存**：热销商品、推荐商品等固定列表可设置较长时间的缓存。
3. **查询结果缓存**：对常用的筛选条件组合结果进行缓存，提高响应速度。

### 分页优化

1. **合理设置分页大小**：避免一次性获取过多数据，建议每页大小控制在10-50条之间。
2. **使用游标分页**：对于大数据量的场景，考虑使用基于游标的分页而非基于页码的分页，提高性能。
3. **限制最大页码**：防止恶意请求获取过深分页数据。

### 查询优化

1. **索引优化**：确保在常用查询字段（如分类ID、价格区间、状态等）上建立适当索引。
2. **避免N+1查询**：在获取商品详情时，使用JOIN或批量查询方式获取关联数据，避免多次数据库访问。
3. **字段选择**：根据实际需要选择返回字段，避免返回不必要的数据。

### 批量操作

1. **批量接口使用**：在需要处理多个商品或分类时，优先使用批量操作接口，减少网络开销。
2. **异步处理**：对于耗时较长的批量操作，考虑使用异步处理模式，立即返回任务ID，通过轮询获取结果。

**Section sources**
- [ProductController.java](file://backend/product-service/src/main/java/com/mall/product/controller/ProductController.java#L47-L61)
- [CategoryController.java](file://backend/product-service/src/main/java/com/mall/product/controller/CategoryController.java#L312-L328)
- [StockController.java](file://backend/product-service/src/main/java/com/mall/product/controller/StockController.java#L39-L50)