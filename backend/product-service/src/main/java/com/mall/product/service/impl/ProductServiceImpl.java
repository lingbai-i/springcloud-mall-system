package com.mall.product.service.impl;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.product.domain.entity.Product;
import com.mall.product.domain.entity.ProductSku;
import com.mall.product.domain.entity.PriceHistory;
import com.mall.product.domain.entity.StockLog;
import com.mall.product.domain.dto.ProductDetailDto;
import com.mall.product.domain.dto.ProductQueryDto;
import com.mall.product.service.ProductService;
import com.mall.product.feign.MerchantProductClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * 提供商品相关的业务逻辑处理，包括商品查询、库存管理、热销推荐等功能
 * 当前版本使用模拟数据，后续可集成真实的数据库和缓存
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-10-22
 *        修改日志：V2.0 2025-10-22：完善所有接口实现，添加多规格商品、库存管理、价格管理等功能
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired(required = false)
    private MerchantProductClient merchantProductClient;

    // 模拟数据存储
    private static final Map<Long, Product> PRODUCT_CACHE = new HashMap<>();
    private static final Map<Long, List<ProductSku>> SKU_CACHE = new HashMap<>();
    private static final List<PriceHistory> PRICE_HISTORY_CACHE = new ArrayList<>();
    private static final List<StockLog> STOCK_LOG_CACHE = new ArrayList<>();

    // 默认商家ID，用于获取商家商品数据
    private static final Long DEFAULT_MERCHANT_ID = 1L;

    static {
        // 初始化模拟数据
        initMockData();
    }

    // ==================== 辅助方法 ====================

    /**
     * 将merchant-service返回的商品数据转换为Product对象列表
     */
    private List<Product> convertMerchantProductsToProducts(List<Map<String, Object>> merchantProducts) {
        if (merchantProducts == null || merchantProducts.isEmpty()) {
            return new ArrayList<>();
        }

        return merchantProducts.stream()
                .map(this::convertMerchantProductToProduct)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 将单个merchant商品转换为Product对象
     */
    private Product convertMerchantProductToProduct(Map<String, Object> merchantProduct) {
        try {
            Product product = new Product();
                
            // 基本信息
            product.setId(getLongValue(merchantProduct, "id"));
            product.setName(getStringValue(merchantProduct, "productName"));
            product.setDescription(getStringValue(merchantProduct, "description"));
                
            // 价格信息
            product.setPrice(getDoubleValue(merchantProduct, "price"));
            product.setOriginalPrice(getDoubleValue(merchantProduct, "marketPrice"));
                
            // 库存信息
            product.setStock(getIntegerValue(merchantProduct, "stockQuantity"));
            product.setStockWarning(getIntegerValue(merchantProduct, "warningStock"));
                
            // 销售信息
            product.setSales(getIntegerValue(merchantProduct, "salesCount"));
                
            // 状态信息
            product.setStatus(getIntegerValue(merchantProduct, "status"));
                
            // 图片信息
            product.setMainImage(getStringValue(merchantProduct, "mainImage"));
            String images = getStringValue(merchantProduct, "images");
            if (images != null && !images.trim().isEmpty()) {
                product.setDetailImages(images);
            }
                
            // 分类和品牌
            product.setCategoryId(getLongValue(merchantProduct, "categoryId"));
            product.setBrandName(getStringValue(merchantProduct, "brand"));
                
            // 时间信息 - 安全处理
            Object createTimeObj = merchantProduct.get("createTime");
            if (createTimeObj != null) {
                if (createTimeObj instanceof LocalDateTime) {
                    product.setCreateTime((LocalDateTime) createTimeObj);
                } else if (createTimeObj instanceof String) {
                    try {
                        product.setCreateTime(LocalDateTime.parse((String) createTimeObj));
                    } catch (Exception e) {
                        product.setCreateTime(LocalDateTime.now());
                    }
                }
            }
                
            Object updateTimeObj = merchantProduct.get("updateTime");
            if (updateTimeObj != null) {
                if (updateTimeObj instanceof LocalDateTime) {
                    product.setUpdateTime((LocalDateTime) updateTimeObj);
                } else if (updateTimeObj instanceof String) {
                    try {
                        product.setUpdateTime(LocalDateTime.parse((String) updateTimeObj));
                    } catch (Exception e) {
                        product.setUpdateTime(LocalDateTime.now());
                    }
                }
            }
                
            // 设置默认评分
            product.setRating(4.5);
            product.setReviewCount(0);
                
            return product;
        } catch (Exception e) {
            logger.error("转换merchant商品数据失败", e);
            return null;
        }
    }

    /**
     * 安全获取Map中的String值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * 安全获取Map中的Long值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null)
            return null;
        if (value instanceof Number)
            return ((Number) value).longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 安全获取Map中的Integer值
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null)
            return 0;
        if (value instanceof Number)
            return ((Number) value).intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 安全获取Map中的Double值
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null)
            return 0.0;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        if (value instanceof BigDecimal)
            return ((BigDecimal) value).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // ==================== 商品基础查询 ====================

    /**
     * 根据分类ID分页查询商品
     * 支持按分类筛选和分页参数，当分类ID为空时查询所有商品
     * 
     * @param current    当前页码
     * @param size       每页大小
     * @param categoryId 分类ID，为空时查询所有商品
     * @return 商品分页数据，包含总数和商品列表
     */
    @Override
    public Object getProductsByCategoryId(Long current, Long size, Long categoryId) {
        logger.info("根据分类ID分页查询商品 - 页码: {}, 大小: {}, 分类ID: {}", current, size, categoryId);

        try {
            List<Product> allProducts = new ArrayList<>(PRODUCT_CACHE.values());

            // 按分类筛选
            if (categoryId != null) {
                allProducts = allProducts.stream()
                        .filter(p -> categoryId.equals(p.getCategoryId()))
                        .collect(Collectors.toList());
            }

            // 分页处理
            int start = (int) ((current - 1) * size);
            int end = Math.min(start + size.intValue(), allProducts.size());
            List<Product> pageProducts = allProducts.subList(start, end);

            return createPageData(pageProducts, allProducts.size(), current, size);
        } catch (Exception e) {
            logger.error("根据分类ID分页查询商品失败", e);
            return createEmptyPageData(current, size);
        }
    }

    /**
     * 搜索商品
     * 根据关键词搜索商品名称、描述等字段，支持分页
     * 
     * @param current 当前页码
     * @param size    每页大小
     * @param keyword 搜索关键词
     * @return 商品分页数据，包含搜索结果
     */
    @Override
    public Object searchProducts(Long current, Long size, String keyword) {
        logger.info("搜索商品 - 页码: {}, 大小: {}, 关键词: {}", current, size, keyword);

        try {
            List<Product> allProducts = new ArrayList<>(PRODUCT_CACHE.values());

            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.toLowerCase();
                allProducts = allProducts.stream()
                        .filter(p -> p.getName().toLowerCase().contains(lowerKeyword) ||
                                (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerKeyword))
                                ||
                                (p.getKeywords() != null && p.getKeywords().toLowerCase().contains(lowerKeyword)))
                        .collect(Collectors.toList());
            }

            // 分页处理
            int start = (int) ((current - 1) * size);
            int end = Math.min(start + size.intValue(), allProducts.size());
            List<Product> pageProducts = allProducts.subList(start, end);

            return createPageData(pageProducts, allProducts.size(), current, size);
        } catch (Exception e) {
            logger.error("搜索商品失败 - 关键词: {}", keyword, e);
            return createEmptyPageData(current, size);
        }
    }

    /**
     * 获取热销商品列表
     * 返回销量较高的商品，用于首页展示
     * 
     * @param limit 限制数量
     * @return 热销商品列表
     */
    @Override
    public List<Product> getHotProducts(Integer limit) {
        logger.info("获取热销商品列表 - 限制数量: {}", limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }

            // 尝试从 merchant-service 获取真实数据
            if (merchantProductClient != null) {
                try {
                    R<List<Map<String, Object>>> result = merchantProductClient
                            .getHotSellingProducts(DEFAULT_MERCHANT_ID, limit);
                    if (result != null && result.isSuccess() && result.getData() != null
                            && !result.getData().isEmpty()) {
                        return convertMerchantProductsToProducts(result.getData());
                    }
                } catch (Exception e) {
                    logger.warn("从 merchant-service 获取热销商品失败，使用模拟数据", e);
                }
            }

            // 如果 Feign 调用失败或没有数据，使用模拟数据
            return PRODUCT_CACHE.values().stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getSales(), p1.getSales()))
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取热销商品异常 - 限制数量: {}", limit, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取推荐商品列表
     * 返回系统推荐的商品，用于个性化展示
     * 
     * @param limit 限制数量
     * @return 推荐商品列表
     */
    @Override
    public List<Product> getRecommendProducts(Integer limit) {
        logger.info("获取推荐商品 - 限制数量: {}", limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }

            // 尝试从 merchant-service 获取真实数据
            if (merchantProductClient != null) {
                try {
                    R<PageResult<Map<String, Object>>> result = merchantProductClient.getRecommendedProducts(
                            DEFAULT_MERCHANT_ID, 1, limit);
                    if (result != null && result.isSuccess() && result.getData() != null &&
                            result.getData().getRecords() != null && !result.getData().getRecords().isEmpty()) {
                        return convertMerchantProductsToProducts(result.getData().getRecords());
                    }
                } catch (Exception e) {
                    logger.warn("从 merchant-service 获取推荐商品失败，使用模拟数据", e);
                }
            }

            // 如果 Feign 调用失败或没有数据，使用模拟数据
            return PRODUCT_CACHE.values().stream()
                    .filter(p -> p.getStatus() == 1) // 只推荐上架商品
                    .sorted((p1, p2) -> Double.compare(p2.getRating() != null ? p2.getRating() : 0.0,
                            p1.getRating() != null ? p1.getRating() : 0.0))
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取推荐商品失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据ID获取商品详情
     * 
     * @param id 商品ID
     * @return 商品详情
     */
    @Override
    public Product getProductById(Long id) {
        logger.info("根据ID获取商品详情 - ID: {}", id);

        try {
            return PRODUCT_CACHE.get(id);
        } catch (Exception e) {
            logger.error("根据ID获取商品详情失败 - ID: {}", id, e);
            return null;
        }
    }

    /**
     * 获取商品详细信息（包含SKU列表）
     * 
     * @param id 商品ID
     * @return 商品详细信息
     */
    @Override
    public ProductDetailDto getProductDetail(Long id) {
        logger.info("获取商品详细信息 - ID: {}", id);

        try {
            Product product = PRODUCT_CACHE.get(id);
            if (product == null) {
                return null;
            }

            ProductDetailDto detailDto = new ProductDetailDto();
            detailDto.setProduct(product);

            // 获取SKU列表
            List<ProductSku> skuList = SKU_CACHE.get(id);
            if (skuList != null) {
                detailDto.setSkuList(skuList);

                // 计算价格范围
                OptionalDouble minPrice = skuList.stream().mapToDouble(ProductSku::getPrice).min();
                OptionalDouble maxPrice = skuList.stream().mapToDouble(ProductSku::getPrice).max();
                detailDto.setMinPrice(minPrice.isPresent() ? minPrice.getAsDouble() : product.getPrice());
                detailDto.setMaxPrice(maxPrice.isPresent() ? maxPrice.getAsDouble() : product.getPrice());

                // 计算总库存
                int totalStock = skuList.stream().mapToInt(ProductSku::getStock).sum();
                detailDto.setTotalStock(totalStock);
            } else {
                detailDto.setSkuList(new ArrayList<>());
                detailDto.setMinPrice(product.getPrice());
                detailDto.setMaxPrice(product.getPrice());
                detailDto.setTotalStock(product.getStock());
            }

            // 设置分类和品牌名称
            detailDto.setCategoryName(product.getCategoryName());
            detailDto.setBrandName(product.getBrandName());

            // 设置评价统计
            ProductDetailDto.ProductRatingDto rating = new ProductDetailDto.ProductRatingDto();
            rating.setAverageRating(product.getRating());
            rating.setTotalReviews(product.getReviewCount());
            rating.setPositiveRate(product.getRating() != null && product.getRating() >= 4.0 ? 0.85 : 0.75);
            detailDto.setRating(rating);

            return detailDto;
        } catch (Exception e) {
            logger.error("获取商品详细信息失败 - ID: {}", id, e);
            return null;
        }
    }

    /**
     * 高级商品搜索
     * 
     * @param queryDto 查询条件
     * @param current  当前页码
     * @param size     每页大小
     * @return 商品分页数据
     */
    @Override
    public Object advancedSearchProducts(ProductQueryDto queryDto, Long current, Long size) {
        logger.info("高级商品搜索 - 查询条件: {}", queryDto);

        try {
            List<Product> allProducts = new ArrayList<>(PRODUCT_CACHE.values());

            // 应用筛选条件
            allProducts = allProducts.stream()
                    .filter(product -> {
                        // 关键词筛选
                        if (queryDto.getKeyword() != null && !queryDto.getKeyword().trim().isEmpty()) {
                            String keyword = queryDto.getKeyword().toLowerCase();
                            if (!product.getName().toLowerCase().contains(keyword) &&
                                    !product.getDescription().toLowerCase().contains(keyword)) {
                                return false;
                            }
                        }

                        // 分类筛选
                        if (queryDto.getCategoryId() != null
                                && !queryDto.getCategoryId().equals(product.getCategoryId())) {
                            return false;
                        }

                        // 品牌筛选
                        if (queryDto.getBrandId() != null && !queryDto.getBrandId().equals(product.getBrandId())) {
                            return false;
                        }

                        // 价格范围筛选
                        if (queryDto.getMinPrice() != null && product.getPrice() < queryDto.getMinPrice()) {
                            return false;
                        }
                        if (queryDto.getMaxPrice() != null && product.getPrice() > queryDto.getMaxPrice()) {
                            return false;
                        }

                        // 状态筛选
                        if (queryDto.getStatus() != null && !queryDto.getStatus().equals(product.getStatus())) {
                            return false;
                        }

                        // 库存筛选
                        if (queryDto.getHasStock() != null) {
                            boolean hasStock = product.getStock() > 0;
                            if (!queryDto.getHasStock().equals(hasStock)) {
                                return false;
                            }
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

            // 排序
            if (queryDto.getSortField() != null) {
                Comparator<Product> comparator = null;
                switch (queryDto.getSortField()) {
                    case "price":
                        comparator = Comparator.comparing(Product::getPrice);
                        break;
                    case "sales":
                        comparator = Comparator.comparing(Product::getSales);
                        break;
                    case "createTime":
                        comparator = Comparator.comparing(Product::getCreateTime);
                        break;
                    default:
                        comparator = Comparator.comparing(Product::getId);
                }

                if ("desc".equals(queryDto.getSortOrder())) {
                    comparator = comparator.reversed();
                }

                allProducts.sort(comparator);
            }

            // 分页处理
            int start = (int) ((current - 1) * size);
            int end = Math.min(start + size.intValue(), allProducts.size());
            List<Product> pageProducts = allProducts.subList(start, end);

            return createPageData(pageProducts, allProducts.size(), current, size);
        } catch (Exception e) {
            logger.error("高级商品搜索失败", e);
            return createEmptyPageData(current, size);
        }
    }

    // ==================== 商品管理 ====================

    /**
     * 新增商品
     * 
     * @param product 商品信息
     * @return 是否成功
     */
    @Override
    public boolean addProduct(Product product) {
        logger.info("新增商品 - 名称: {}", product.getName());

        try {
            if (product == null || product.getName() == null || product.getName().trim().isEmpty()) {
                logger.warn("商品信息无效");
                return false;
            }

            // 生成ID
            Long id = System.currentTimeMillis();
            product.setId(id);
            product.setCreateTime(LocalDateTime.now());
            product.setUpdateTime(LocalDateTime.now());

            PRODUCT_CACHE.put(id, product);
            logger.info("商品新增成功 - ID: {}, 名称: {}", id, product.getName());
            return true;
        } catch (Exception e) {
            logger.error("新增商品失败 - 名称: {}", product.getName(), e);
            return false;
        }
    }

    /**
     * 更新商品信息
     * 
     * @param product 商品信息
     * @return 是否成功
     */
    @Override
    public boolean updateProduct(Product product) {
        logger.info("更新商品 - ID: {}, 名称: {}", product.getId(), product.getName());

        try {
            if (product == null || product.getId() == null || !PRODUCT_CACHE.containsKey(product.getId())) {
                logger.warn("商品不存在或ID无效");
                return false;
            }

            product.setUpdateTime(LocalDateTime.now());
            PRODUCT_CACHE.put(product.getId(), product);
            logger.info("商品更新成功 - ID: {}, 名称: {}", product.getId(), product.getName());
            return true;
        } catch (Exception e) {
            logger.error("更新商品失败 - ID: {}, 名称: {}", product.getId(), product.getName(), e);
            return false;
        }
    }

    /**
     * 删除商品
     * 
     * @param id 商品ID
     * @return 是否成功
     */
    @Override
    public boolean deleteProduct(Long id) {
        logger.info("删除商品 - ID: {}", id);

        try {
            if (id == null || !PRODUCT_CACHE.containsKey(id)) {
                logger.warn("商品不存在 - ID: {}", id);
                return false;
            }

            PRODUCT_CACHE.remove(id);
            SKU_CACHE.remove(id); // 同时删除相关SKU
            logger.info("商品删除成功 - ID: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("删除商品失败 - ID: {}", id, e);
            return false;
        }
    }

    /**
     * 批量删除商品
     * 
     * @param ids 商品ID列表
     * @return 是否成功
     */
    @Override
    public boolean batchDeleteProducts(List<Long> ids) {
        logger.info("批量删除商品 - 数量: {}", ids != null ? ids.size() : 0);

        try {
            if (ids == null || ids.isEmpty()) {
                logger.warn("删除列表为空");
                return false;
            }

            int successCount = 0;
            for (Long id : ids) {
                if (deleteProduct(id)) {
                    successCount++;
                }
            }

            boolean allSuccess = successCount == ids.size();
            logger.info("批量删除完成 - 总数: {}, 成功: {}", ids.size(), successCount);
            return allSuccess;
        } catch (Exception e) {
            logger.error("批量删除商品失败", e);
            return false;
        }
    }

    /**
     * 更新商品状态
     * 
     * @param id     商品ID
     * @param status 状态
     * @return 是否成功
     */
    @Override
    public boolean updateProductStatus(Long id, Integer status) {
        logger.info("更新商品状态 - ID: {}, 状态: {}", id, status);

        try {
            Product product = PRODUCT_CACHE.get(id);
            if (product == null) {
                logger.warn("商品不存在 - ID: {}", id);
                return false;
            }

            product.setStatus(status);
            product.setUpdateTime(LocalDateTime.now());
            PRODUCT_CACHE.put(id, product);
            logger.info("商品状态更新成功 - ID: {}, 状态: {}", id, status);
            return true;
        } catch (Exception e) {
            logger.error("更新商品状态失败 - ID: {}, 状态: {}", id, status, e);
            return false;
        }
    }

    // ==================== 多规格商品管理 ====================

    /**
     * 获取商品的SKU列表
     * 
     * @param productId 商品ID
     * @return SKU列表
     */
    @Override
    public List<ProductSku> getProductSkus(Long productId) {
        logger.info("获取商品SKU列表 - 商品ID: {}", productId);

        try {
            return SKU_CACHE.getOrDefault(productId, new ArrayList<>());
        } catch (Exception e) {
            logger.error("获取商品SKU列表失败 - 商品ID: {}", productId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 新增商品SKU
     * 
     * @param sku SKU信息
     * @return 是否成功
     */
    @Override
    public boolean addProductSku(ProductSku sku) {
        logger.info("新增商品SKU - 商品ID: {}, SKU名称: {}", sku.getProductId(), sku.getSkuName());

        try {
            if (sku == null || sku.getProductId() == null || !PRODUCT_CACHE.containsKey(sku.getProductId())) {
                logger.warn("商品不存在或SKU信息无效");
                return false;
            }

            Long skuId = System.currentTimeMillis();
            sku.setId(skuId);
            sku.setCreateTime(LocalDateTime.now());
            sku.setUpdateTime(LocalDateTime.now());

            SKU_CACHE.computeIfAbsent(sku.getProductId(), k -> new ArrayList<>()).add(sku);
            logger.info("商品SKU新增成功 - SKU ID: {}", skuId);
            return true;
        } catch (Exception e) {
            logger.error("新增商品SKU失败", e);
            return false;
        }
    }

    /**
     * 更新商品SKU
     * 
     * @param sku SKU信息
     * @return 是否成功
     */
    @Override
    public boolean updateProductSku(ProductSku sku) {
        logger.info("更新商品SKU - SKU ID: {}", sku.getId());

        try {
            if (sku == null || sku.getId() == null || sku.getProductId() == null) {
                logger.warn("SKU信息无效");
                return false;
            }

            List<ProductSku> skus = SKU_CACHE.get(sku.getProductId());
            if (skus == null) {
                logger.warn("商品SKU列表不存在 - 商品ID: {}", sku.getProductId());
                return false;
            }

            for (int i = 0; i < skus.size(); i++) {
                if (sku.getId().equals(skus.get(i).getId())) {
                    sku.setUpdateTime(LocalDateTime.now());
                    skus.set(i, sku);
                    logger.info("商品SKU更新成功 - SKU ID: {}", sku.getId());
                    return true;
                }
            }

            logger.warn("SKU不存在 - SKU ID: {}", sku.getId());
            return false;
        } catch (Exception e) {
            logger.error("更新商品SKU失败 - SKU ID: {}", sku.getId(), e);
            return false;
        }
    }

    /**
     * 删除商品SKU
     * 
     * @param skuId SKU ID
     * @return 是否成功
     */
    @Override
    public boolean deleteProductSku(Long skuId) {
        logger.info("删除商品SKU - SKU ID: {}", skuId);

        try {
            for (List<ProductSku> skus : SKU_CACHE.values()) {
                if (skus.removeIf(sku -> skuId.equals(sku.getId()))) {
                    logger.info("商品SKU删除成功 - SKU ID: {}", skuId);
                    return true;
                }
            }

            logger.warn("SKU不存在 - SKU ID: {}", skuId);
            return false;
        } catch (Exception e) {
            logger.error("删除商品SKU失败 - SKU ID: {}", skuId, e);
            return false;
        }
    }

    /**
     * 批量保存商品SKU
     * 
     * @param productId 商品ID
     * @param skus      SKU列表
     * @return 是否成功
     */
    @Override
    public boolean batchSaveProductSkus(Long productId, List<ProductSku> skus) {
        logger.info("批量保存商品SKU - 商品ID: {}, SKU数量: {}", productId, skus != null ? skus.size() : 0);

        try {
            if (productId == null || !PRODUCT_CACHE.containsKey(productId)) {
                logger.warn("商品不存在 - 商品ID: {}", productId);
                return false;
            }

            if (skus == null || skus.isEmpty()) {
                logger.warn("SKU列表为空");
                return false;
            }

            // 清空原有SKU
            SKU_CACHE.put(productId, new ArrayList<>());

            // 批量添加新SKU
            int successCount = 0;
            for (ProductSku sku : skus) {
                sku.setProductId(productId);
                if (addProductSku(sku)) {
                    successCount++;
                }
            }

            boolean allSuccess = successCount == skus.size();
            logger.info("批量保存SKU完成 - 总数: {}, 成功: {}", skus.size(), successCount);
            return allSuccess;
        } catch (Exception e) {
            logger.error("批量保存商品SKU失败 - 商品ID: {}", productId, e);
            return false;
        }
    }

    // ==================== 库存管理 ====================

    /**
     * 更新商品库存
     * 单个商品库存调整，支持增加或减少
     * 
     * @param productId 商品ID
     * @param quantity  库存变化数量（正数为增加，负数为减少）
     * @return 更新是否成功
     */
    @Override
    public boolean updateStock(Long productId, Integer quantity) {
        logger.info("更新商品库存 - 商品ID: {}, 变化数量: {}", productId, quantity);

        try {
            Product product = PRODUCT_CACHE.get(productId);
            if (product == null) {
                logger.warn("商品不存在 - ID: {}", productId);
                return false;
            }

            if (quantity == null || quantity == 0) {
                logger.warn("库存变化数量无效: {}", quantity);
                return false;
            }

            int oldStock = product.getStock();
            int newStock = oldStock + quantity;

            if (newStock < 0) {
                logger.warn("库存不足 - 商品ID: {}, 当前库存: {}, 尝试减少: {}",
                        productId, oldStock, Math.abs(quantity));
                return false;
            }

            product.setStock(newStock);
            product.setUpdateTime(LocalDateTime.now());
            PRODUCT_CACHE.put(productId, product);

            // 记录库存变更日志
            recordStockLog(productId, null, oldStock, newStock, quantity, "手动调整", null, null, product.getName(), null);

            logger.info("库存更新成功 - 商品ID: {}, 原库存: {}, 新库存: {}", productId, oldStock, newStock);
            return true;
        } catch (Exception e) {
            logger.error("更新商品库存异常 - 商品ID: {}, 变化数量: {}", productId, quantity, e);
            return false;
        }
    }

    /**
     * 更新SKU库存
     * 
     * @param skuId    SKU ID
     * @param quantity 库存变化数量
     * @return 是否成功
     */
    @Override
    public boolean updateSkuStock(Long skuId, Integer quantity) {
        logger.info("更新SKU库存 - SKU ID: {}, 变化数量: {}", skuId, quantity);

        try {
            for (Map.Entry<Long, List<ProductSku>> entry : SKU_CACHE.entrySet()) {
                for (ProductSku sku : entry.getValue()) {
                    if (skuId.equals(sku.getId())) {
                        int oldStock = sku.getStock();
                        int newStock = oldStock + quantity;

                        if (newStock < 0) {
                            logger.warn("SKU库存不足 - SKU ID: {}, 当前库存: {}, 尝试减少: {}",
                                    skuId, oldStock, Math.abs(quantity));
                            return false;
                        }

                        sku.setStock(newStock);
                        sku.setUpdateTime(LocalDateTime.now());

                        // 记录库存变更日志
                        Product product = PRODUCT_CACHE.get(entry.getKey());
                        recordStockLog(entry.getKey(), skuId, oldStock, newStock, quantity, "手动调整", null, null,
                                product != null ? product.getName() : null, sku.getSkuName());

                        logger.info("SKU库存更新成功 - SKU ID: {}, 原库存: {}, 新库存: {}", skuId, oldStock, newStock);
                        return true;
                    }
                }
            }

            logger.warn("SKU不存在 - SKU ID: {}", skuId);
            return false;
        } catch (Exception e) {
            logger.error("更新SKU库存异常 - SKU ID: {}, 变化数量: {}", skuId, quantity, e);
            return false;
        }
    }

    /**
     * 批量更新商品库存
     * 支持一次性更新多个商品的库存
     * 
     * @param stockUpdates 库存更新列表
     * @return 批量更新是否成功
     */
    @Override
    public boolean batchUpdateStock(List<ProductService.StockUpdate> stockUpdates) {
        logger.info("批量更新商品库存 - 更新数量: {}", stockUpdates != null ? stockUpdates.size() : 0);

        try {
            if (stockUpdates == null || stockUpdates.isEmpty()) {
                logger.warn("批量更新列表为空");
                return false;
            }

            int successCount = 0;
            for (ProductService.StockUpdate update : stockUpdates) {
                if (update == null || update.getProductId() == null || update.getQuantity() == null) {
                    logger.warn("跳过无效的库存更新记录: {}", update);
                    continue;
                }

                boolean success = updateStock(update.getProductId(), update.getQuantity());
                if (success) {
                    successCount++;
                }
            }

            boolean allSuccess = successCount == stockUpdates.size();
            logger.info("批量库存更新完成 - 总数: {}, 成功: {}", stockUpdates.size(), successCount);
            return allSuccess;
        } catch (Exception e) {
            logger.error("批量更新商品库存异常", e);
            return false;
        }
    }

    /**
     * 获取库存预警商品列表
     * 
     * @return 库存预警商品列表
     */
    @Override
    public List<Product> getStockWarningProducts() {
        logger.info("获取库存预警商品列表");

        try {
            return PRODUCT_CACHE.values().stream()
                    .filter(p -> p.getStock() <= p.getStockWarning())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取库存预警商品列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取库存变更日志
     * 
     * @param productId 商品ID
     * @param current   当前页码
     * @param size      每页大小
     * @return 库存变更日志分页数据
     */
    @Override
    public Object getStockLogs(Long productId, Long current, Long size) {
        logger.info("获取库存变更日志 - 商品ID: {}, 页码: {}, 大小: {}", productId, current, size);

        try {
            List<StockLog> logs = STOCK_LOG_CACHE.stream()
                    .filter(log -> productId.equals(log.getProductId()))
                    .sorted((l1, l2) -> l2.getCreateTime().compareTo(l1.getCreateTime()))
                    .collect(Collectors.toList());

            // 分页处理
            int start = (int) ((current - 1) * size);
            int end = Math.min(start + size.intValue(), logs.size());
            List<StockLog> pageLogs = logs.subList(start, end);

            return createPageData(pageLogs, logs.size(), current, size);
        } catch (Exception e) {
            logger.error("获取库存变更日志失败 - 商品ID: {}", productId, e);
            return createEmptyPageData(current, size);
        }
    }

    // ==================== 价格管理 ====================

    /**
     * 更新商品价格
     * 
     * @param productId  商品ID
     * @param newPrice   新价格
     * @param reason     变更原因
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    @Override
    public boolean updateProductPrice(Long productId, Double newPrice, String reason, Long operatorId) {
        logger.info("更新商品价格 - 商品ID: {}, 新价格: {}, 原因: {}", productId, newPrice, reason);

        try {
            Product product = PRODUCT_CACHE.get(productId);
            if (product == null) {
                logger.warn("商品不存在 - ID: {}", productId);
                return false;
            }

            if (newPrice == null || newPrice < 0) {
                logger.warn("价格无效: {}", newPrice);
                return false;
            }

            Double oldPrice = product.getPrice();
            product.setPrice(newPrice);
            product.setUpdateTime(LocalDateTime.now());
            PRODUCT_CACHE.put(productId, product);

            // 记录价格变更历史
            recordPriceHistory(productId, null, oldPrice, newPrice, "PRODUCT", reason, operatorId, null,
                    product.getName(), null);

            logger.info("商品价格更新成功 - 商品ID: {}, 原价格: {}, 新价格: {}", productId, oldPrice, newPrice);
            return true;
        } catch (Exception e) {
            logger.error("更新商品价格失败 - 商品ID: {}, 新价格: {}", productId, newPrice, e);
            return false;
        }
    }

    /**
     * 更新SKU价格
     * 
     * @param skuId      SKU ID
     * @param newPrice   新价格
     * @param reason     变更原因
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    @Override
    public boolean updateSkuPrice(Long skuId, Double newPrice, String reason, Long operatorId) {
        logger.info("更新SKU价格 - SKU ID: {}, 新价格: {}, 原因: {}", skuId, newPrice, reason);

        try {
            for (Map.Entry<Long, List<ProductSku>> entry : SKU_CACHE.entrySet()) {
                for (ProductSku sku : entry.getValue()) {
                    if (skuId.equals(sku.getId())) {
                        if (newPrice == null || newPrice < 0) {
                            logger.warn("价格无效: {}", newPrice);
                            return false;
                        }

                        Double oldPrice = sku.getPrice();
                        sku.setPrice(newPrice);
                        sku.setUpdateTime(LocalDateTime.now());

                        // 记录价格变更历史
                        Product product = PRODUCT_CACHE.get(entry.getKey());
                        recordPriceHistory(entry.getKey(), skuId, oldPrice, newPrice, "SKU", reason, operatorId, null,
                                product != null ? product.getName() : null, sku.getSkuName());

                        logger.info("SKU价格更新成功 - SKU ID: {}, 原价格: {}, 新价格: {}", skuId, oldPrice, newPrice);
                        return true;
                    }
                }
            }

            logger.warn("SKU不存在 - SKU ID: {}", skuId);
            return false;
        } catch (Exception e) {
            logger.error("更新SKU价格失败 - SKU ID: {}, 新价格: {}", skuId, newPrice, e);
            return false;
        }
    }

    /**
     * 批量调价
     * 
     * @param priceUpdates 价格更新列表
     * @return 是否成功
     */
    @Override
    public boolean batchUpdatePrices(List<ProductService.PriceUpdate> priceUpdates) {
        logger.info("批量调价 - 更新数量: {}", priceUpdates != null ? priceUpdates.size() : 0);

        try {
            if (priceUpdates == null || priceUpdates.isEmpty()) {
                logger.warn("批量调价列表为空");
                return false;
            }

            int successCount = 0;
            for (ProductService.PriceUpdate update : priceUpdates) {
                if (update == null || update.getNewPrice() == null) {
                    logger.warn("跳过无效的价格更新记录: {}", update);
                    continue;
                }

                boolean success = false;
                if (update.getProductId() != null) {
                    success = updateProductPrice(update.getProductId(), update.getNewPrice(), update.getReason(),
                            update.getOperatorId());
                } else if (update.getSkuId() != null) {
                    success = updateSkuPrice(update.getSkuId(), update.getNewPrice(), update.getReason(),
                            update.getOperatorId());
                }

                if (success) {
                    successCount++;
                }
            }

            boolean allSuccess = successCount == priceUpdates.size();
            logger.info("批量调价完成 - 总数: {}, 成功: {}", priceUpdates.size(), successCount);
            return allSuccess;
        } catch (Exception e) {
            logger.error("批量调价失败", e);
            return false;
        }
    }

    /**
     * 获取价格变更历史
     * 
     * @param productId 商品ID
     * @param current   当前页码
     * @param size      每页大小
     * @return 价格变更历史分页数据
     */
    @Override
    public Object getPriceHistory(Long productId, Long current, Long size) {
        logger.info("获取价格变更历史 - 商品ID: {}, 页码: {}, 大小: {}", productId, current, size);

        try {
            List<PriceHistory> histories = PRICE_HISTORY_CACHE.stream()
                    .filter(history -> productId.equals(history.getProductId()))
                    .sorted((h1, h2) -> h2.getCreateTime().compareTo(h1.getCreateTime()))
                    .collect(Collectors.toList());

            // 分页处理
            int start = (int) ((current - 1) * size);
            int end = Math.min(start + size.intValue(), histories.size());
            List<PriceHistory> pageHistories = histories.subList(start, end);

            return createPageData(pageHistories, histories.size(), current, size);
        } catch (Exception e) {
            logger.error("获取价格变更历史失败 - 商品ID: {}", productId, e);
            return createEmptyPageData(current, size);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 初始化模拟数据
     * 注意：已禁用，不再初始化虚拟测试数据
     */
    private static void initMockData() {
        // 以下代码已注释，不再初始化虚拟测试数据
        /*
         * for (int i = 1; i <= 20; i++) {
         * Product product = new Product();
         * product.setId((long) i);
         * product.setName("测试商品 " + i);
         * product.setDescription("这是测试商品 " + i + " 的详细描述");
         * product.setPrice(99.99 + i);
         * product.setOriginalPrice(129.99 + i);
         * product.setCostPrice(59.99 + i);
         * product.setStock(100 + i * 10);
         * product.setStockWarning(20);
         * product.setSales(50 + i * 5);
         * product.setStatus(1);
         * product.setCategoryId((long) (i % 5 + 1));
         * product.setBrandId((long) (i % 3 + 1));
         * product.setUnit("件");
         * product.setBarcode("123456789" + String.format("%03d", i));
         * product.setModel("MODEL-" + i);
         * product.setHasSpecs(i % 3 == 0 ? 1 : 0);
         * product.setRating(4.0 + (i % 10) * 0.1);
         * product.setReviewCount(i * 10);
         * product.setKeywords("测试,商品,关键词" + i);
         * product.setCreateTime(LocalDateTime.now().minusDays(i));
         * product.setUpdateTime(LocalDateTime.now().minusDays(i));
         * 
         * PRODUCT_CACHE.put((long) i, product);
         * 
         * // 为多规格商品创建SKU
         * if (product.getHasSpecs() == 1) {
         * List<ProductSku> skus = new ArrayList<>();
         * for (int j = 1; j <= 3; j++) {
         * ProductSku sku = new ProductSku();
         * sku.setId((long) (i * 100 + j));
         * sku.setProductId((long) i);
         * sku.setSkuCode("SKU-" + i + "-" + j);
         * sku.setSkuName("规格" + j);
         * sku.setPrice(product.getPrice() + (j * 10));
         * sku.setStock(50 + j * 10);
         * String size = j == 1 ? "S" : (j == 2 ? "M" : "L");
         * sku.setSpecValues("颜色:红色,尺寸:" + size);
         * sku.setStatus(1);
         * sku.setCreateTime(LocalDateTime.now().minusDays(i));
         * sku.setUpdateTime(LocalDateTime.now().minusDays(i));
         * skus.add(sku);
         * }
         * SKU_CACHE.put((long) i, skus);
         * }
         * }
         */
    }

    /**
     * 记录库存变更日志
     */
    private void recordStockLog(Long productId, Long skuId, Integer oldStock, Integer newStock,
            Integer changeQuantity, String changeType, String reason,
            String relatedOrderNo, String productName, String skuName) {
        StockLog log = new StockLog();
        log.setId(System.currentTimeMillis());
        log.setProductId(productId);
        log.setSkuId(skuId);
        log.setOldStock(oldStock);
        log.setNewStock(newStock);
        log.setChangeQuantity(changeQuantity);
        log.setChangeType(changeType);
        log.setReason(reason);
        log.setRelatedOrderNo(relatedOrderNo);
        log.setProductName(productName);
        log.setSkuName(skuName);
        log.setCreateTime(LocalDateTime.now());

        STOCK_LOG_CACHE.add(log);
    }

    /**
     * 记录价格变更历史
     */
    private void recordPriceHistory(Long productId, Long skuId, Double oldPrice, Double newPrice,
            String priceType, String reason, Long operatorId, String operatorName,
            String productName, String skuName) {
        PriceHistory history = new PriceHistory();
        history.setId(System.currentTimeMillis());
        history.setProductId(productId);
        history.setSkuId(skuId);
        history.setOldPrice(oldPrice);
        history.setNewPrice(newPrice);
        history.setPriceType(priceType);
        history.setReason(reason);
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setProductName(productName);
        history.setSkuName(skuName);
        history.setCreateTime(LocalDateTime.now());

        PRICE_HISTORY_CACHE.add(history);
    }

    /**
     * 创建分页数据
     */
    private Map<String, Object> createPageData(List<?> records, int total, Long current, Long size) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("current", current);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);
        return result;
    }

    /**
     * 创建空分页数据
     */
    private Map<String, Object> createEmptyPageData(Long current, Long size) {
        return createPageData(new ArrayList<>(), 0, current, size);
    }

    // 注：辅助方法已在文件前面定义，此处不再重复
}