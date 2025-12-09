package com.mall.product.controller;

import com.mall.common.core.domain.R;
import com.mall.product.domain.entity.Product;
import com.mall.product.domain.entity.ProductSku;
import com.mall.product.domain.dto.ProductDetailDto;
import com.mall.product.domain.dto.ProductQueryDto;
import com.mall.product.domain.dto.ProductStatistics;
import com.mall.product.service.ProductService;
import com.mall.product.service.impl.ProductServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品控制器
 * 提供商品相关的REST API接口，包括商品查询、搜索、库存管理、价格管理等功能
 * 支持分页查询、关键词搜索、热销推荐、多规格商品管理等业务场景
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-10-22
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductServiceImpl productServiceImpl;

    // ==================== 商品基础查询 ====================

    /**
     * 根据分类ID分页查询商品
     * 支持按分类筛选、商家筛选和分页参数
     * 
     * @param current 当前页码，默认为1
     * @param size 每页大小，默认为10
     * @param categoryId 分类ID，可选参数
     * @param merchantId 商家ID，可选参数（用于筛选特定商家的商品）
     * @return 统一响应结果，包含商品分页数据
     */
    @GetMapping("/products")
    public R<Object> getProducts(@RequestParam(defaultValue = "1") Long current,
                                 @RequestParam(defaultValue = "10") Long size,
                                 @RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) Long merchantId) {
        logger.info("接收到分页查询商品的请求 - 页码: {}, 大小: {}, 分类ID: {}, 商家ID: {}", 
                current, size, categoryId, merchantId);
        
        try {
            Object pageData = productService.getProductsByCategoryId(current, size, categoryId, merchantId);
            logger.info("成功获取商品分页数据");
            return R.ok(pageData);
        } catch (Exception e) {
            logger.error("分页查询商品失败", e);
            return R.fail("查询商品失败");
        }
    }

    /**
     * 根据商家ID分页查询商品
     * 专门用于查询特定商家的所有商品
     * 
     * @param merchantId 商家ID
     * @param current 当前页码，默认为1
     * @param size 每页大小，默认为10
     * @return 统一响应结果，包含商品分页数据
     */
    @GetMapping("/products/merchant/{merchantId}")
    public R<Object> getProductsByMerchantId(@PathVariable Long merchantId,
                                              @RequestParam(defaultValue = "1") Long current,
                                              @RequestParam(defaultValue = "10") Long size) {
        logger.info("接收到根据商家ID查询商品的请求 - 商家ID: {}, 页码: {}, 大小: {}", merchantId, current, size);
        
        try {
            Object pageData = productService.getProductsByMerchantId(current, size, merchantId);
            logger.info("成功获取商家商品列表 - 商家ID: {}", merchantId);
            return R.ok(pageData);
        } catch (Exception e) {
            logger.error("根据商家ID查询商品失败 - 商家ID: {}", merchantId, e);
            return R.fail("查询商品失败");
        }
    }

    /**
     * 搜索商品
     * 根据关键词搜索商品，支持分页和商家筛选
     * 
     * @param current 当前页码，默认为1
     * @param size 每页大小，默认为10
     * @param keyword 搜索关键词
     * @param merchantId 商家ID，可选参数（用于筛选特定商家的商品）
     * @return 统一响应结果，包含搜索结果分页数据
     */
    @GetMapping("/search")
    public R<Object> searchProducts(@RequestParam(defaultValue = "1") Long current,
                                    @RequestParam(defaultValue = "10") Long size,
                                    @RequestParam String keyword,
                                    @RequestParam(required = false) Long merchantId) {
        logger.info("接收到搜索商品的请求 - 页码: {}, 大小: {}, 关键词: {}, 商家ID: {}", 
                current, size, keyword, merchantId);
        
        try {
            Object pageData = productService.searchProducts(current, size, keyword, merchantId);
            logger.info("成功完成商品搜索 - 关键词: {}", keyword);
            return R.ok(pageData);
        } catch (Exception e) {
            logger.error("搜索商品失败 - 关键词: {}", keyword, e);
            return R.fail("搜索商品失败");
        }
    }

    /**
     * 获取热销商品列表
     * 返回销量较高的商品，用于首页展示
     * 
     * @param limit 限制数量，默认为10
     * @return 统一响应结果，包含热销商品列表
     */
    @GetMapping("/hot")
    public R<List<Product>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        logger.info("接收到获取热销商品的请求 - 限制数量: {}", limit);
        
        try {
            List<Product> hotProducts = productService.getHotProducts(limit);
            logger.info("成功获取热销商品 - 数量: {}", hotProducts.size());
            return R.ok(hotProducts);
        } catch (Exception e) {
            logger.error("获取热销商品失败", e);
            return R.fail("获取热销商品失败");
        }
    }

    /**
     * 获取推荐商品列表
     * 返回系统推荐的商品，用于个性化展示
     * 
     * @param limit 限制数量，默认为10
     * @return 统一响应结果，包含推荐商品列表
     */
    @GetMapping("/recommend")
    public R<List<Product>> getRecommendProducts(@RequestParam(defaultValue = "10") Integer limit) {
        logger.info("接收到获取推荐商品的请求 - 限制数量: {}", limit);
        
        try {
            List<Product> recommendProducts = productService.getRecommendProducts(limit);
            logger.info("成功获取推荐商品 - 数量: {}", recommendProducts.size());
            return R.ok(recommendProducts);
        } catch (Exception e) {
            logger.error("获取推荐商品失败", e);
            return R.fail("获取推荐商品失败");
        }
    }

    /**
     * 批量获取商品信息
     * 根据商品ID列表批量获取商品信息，用于订单服务等内部调用
     * 直接返回List便于Feign客户端解析
     * 
     * @param productIds 商品ID列表
     * @return 商品信息列表（Map格式，包含id、name、price、image、stock字段）
     */
    @PostMapping("/products/batch")
    public List<java.util.Map<String, Object>> getProductsBatch(@RequestBody List<Long> productIds) {
        logger.info("接收到批量获取商品信息的请求 - 数量: {}", productIds != null ? productIds.size() : 0);
        
        try {
            if (productIds == null || productIds.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            List<Product> products = productService.getProductsByIds(productIds);
            logger.info("成功批量获取商品信息 - 请求数量: {}, 返回数量: {}", productIds.size(), products.size());
            
            // 转换为Map格式，与订单服务期望的格式一致（包含merchantId用于订单创建）
            return products.stream().map(product -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", product.getId());
                map.put("name", product.getName());
                map.put("price", product.getPrice());
                map.put("image", product.getMainImage());
                map.put("stock", product.getStock());
                map.put("merchantId", product.getMerchantId());
                return map;
            }).toList();
        } catch (Exception e) {
            logger.error("批量获取商品信息失败", e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 根据ID获取商品详情
     * 返回指定商品的详细信息
     * 
     * @param id 商品ID
     * @return 统一响应结果，包含商品详情
     */
    @GetMapping("/{id}")
    public R<Product> getProductById(@PathVariable Long id) {
        logger.info("接收到根据ID获取商品详情的请求 - ID: {}", id);
        
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                logger.info("成功获取商品详情 - ID: {}, 名称: {}", product.getId(), product.getName());
                return R.ok(product);
            } else {
                logger.warn("未找到指定商品 - ID: {}", id);
                return R.fail("商品不存在");
            }
        } catch (Exception e) {
            logger.error("根据ID获取商品详情失败 - ID: {}", id, e);
            return R.fail("获取商品详情失败");
        }
    }

    /**
     * 获取商品详细信息（包含SKU列表）
     * 
     * @param id 商品ID
     * @return 统一响应结果，包含商品详细信息
     */
    @GetMapping("/{id}/detail")
    public R<ProductDetailDto> getProductDetail(@PathVariable Long id) {
        logger.info("接收到获取商品详细信息的请求 - ID: {}", id);
        
        try {
            ProductDetailDto productDetail = productService.getProductDetail(id);
            if (productDetail != null) {
                logger.info("成功获取商品详细信息 - ID: {}", id);
                return R.ok(productDetail);
            } else {
                logger.warn("商品不存在 - ID: {}", id);
                return R.fail("商品不存在");
            }
        } catch (Exception e) {
            logger.error("获取商品详细信息失败 - ID: {}", id, e);
            return R.fail("获取商品详细信息失败");
        }
    }

    /**
     * 高级商品搜索
     * 支持多种筛选条件和排序方式
     * 
     * @param queryDto 查询条件
     * @param current 当前页码，默认为1
     * @param size 每页大小，默认为10
     * @return 统一响应结果，包含搜索结果分页数据
     */
    @PostMapping("/search/advanced")
    public R<Object> advancedSearchProducts(@RequestBody ProductQueryDto queryDto,
                                           @RequestParam(defaultValue = "1") Long current,
                                           @RequestParam(defaultValue = "10") Long size) {
        logger.info("接收到高级商品搜索的请求 - 查询条件: {}, 页码: {}, 大小: {}", queryDto, current, size);
        
        try {
            Object pageData = productService.advancedSearchProducts(queryDto, current, size);
            logger.info("成功完成高级商品搜索");
            return R.ok(pageData);
        } catch (Exception e) {
            logger.error("高级商品搜索失败", e);
            return R.fail("搜索商品失败");
        }
    }

    // ==================== 商品管理 ====================

    /**
     * 新增商品
     * 
     * @param product 商品信息
     * @return 统一响应结果
     */
    @PostMapping("/products")
    public R<String> addProduct(@RequestBody Product product) {
        logger.info("接收到新增商品的请求 - 商品名称: {}", product.getName());
        
        try {
            boolean success = productService.addProduct(product);
            if (success) {
                logger.info("商品新增成功 - 名称: {}", product.getName());
                return R.ok("商品新增成功");
            } else {
                logger.warn("商品新增失败 - 名称: {}", product.getName());
                return R.fail("商品新增失败");
            }
        } catch (Exception e) {
            logger.error("新增商品时发生异常 - 名称: {}", product.getName(), e);
            return R.fail("商品新增失败");
        }
    }

    /**
     * 更新商品信息
     * 
     * @param product 商品信息
     * @return 统一响应结果
     */
    @PutMapping("/products")
    public R<String> updateProduct(@RequestBody Product product) {
        logger.info("接收到更新商品的请求 - 商品ID: {}, 名称: {}", product.getId(), product.getName());
        
        try {
            boolean success = productService.updateProduct(product);
            if (success) {
                logger.info("商品更新成功 - ID: {}, 名称: {}", product.getId(), product.getName());
                return R.ok("商品更新成功");
            } else {
                logger.warn("商品更新失败 - ID: {}, 名称: {}", product.getId(), product.getName());
                return R.fail("商品更新失败");
            }
        } catch (Exception e) {
            logger.error("更新商品时发生异常 - ID: {}, 名称: {}", product.getId(), product.getName(), e);
            return R.fail("商品更新失败");
        }
    }

    /**
     * 删除商品
     * 
     * @param id 商品ID
     * @return 统一响应结果
     */
    @DeleteMapping("/{id}")
    public R<String> deleteProduct(@PathVariable Long id) {
        logger.info("接收到删除商品的请求 - 商品ID: {}", id);
        
        try {
            boolean success = productService.deleteProduct(id);
            if (success) {
                logger.info("商品删除成功 - ID: {}", id);
                return R.ok("商品删除成功");
            } else {
                logger.warn("商品删除失败 - ID: {}", id);
                return R.fail("商品删除失败");
            }
        } catch (Exception e) {
            logger.error("删除商品时发生异常 - ID: {}", id, e);
            return R.fail("商品删除失败");
        }
    }

    /**
     * 批量删除商品
     * 
     * @param ids 商品ID列表
     * @return 统一响应结果
     */
    @DeleteMapping("/batch")
    public R<String> batchDeleteProducts(@RequestBody List<Long> ids) {
        logger.info("接收到批量删除商品的请求 - 数量: {}", ids != null ? ids.size() : 0);
        
        try {
            boolean success = productService.batchDeleteProducts(ids);
            if (success) {
                logger.info("批量删除商品成功 - 数量: {}", ids.size());
                return R.ok("批量删除成功");
            } else {
                logger.warn("批量删除商品失败");
                return R.fail("批量删除失败");
            }
        } catch (Exception e) {
            logger.error("批量删除商品时发生异常", e);
            return R.fail("批量删除失败");
        }
    }

    /**
     * 更新商品状态
     * 
     * @param id 商品ID
     * @param status 状态（0-下架，1-上架）
     * @return 统一响应结果
     */
    @PutMapping("/{id}/status")
    public R<String> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        logger.info("接收到更新商品状态的请求 - 商品ID: {}, 状态: {}", id, status);
        
        try {
            boolean success = productService.updateProductStatus(id, status);
            if (success) {
                logger.info("商品状态更新成功 - ID: {}, 状态: {}", id, status);
                return R.ok("状态更新成功");
            } else {
                logger.warn("商品状态更新失败 - ID: {}, 状态: {}", id, status);
                return R.fail("状态更新失败");
            }
        } catch (Exception e) {
            logger.error("更新商品状态时发生异常 - ID: {}, 状态: {}", id, status, e);
            return R.fail("状态更新失败");
        }
    }

    // ==================== 多规格商品管理 ====================

    /**
     * 获取商品的SKU列表
     * 
     * @param productId 商品ID
     * @return 统一响应结果，包含SKU列表
     */
    @GetMapping("/{productId}/skus")
    public R<List<ProductSku>> getProductSkus(@PathVariable Long productId) {
        logger.info("接收到获取商品SKU列表的请求 - 商品ID: {}", productId);
        
        try {
            List<ProductSku> skus = productService.getProductSkus(productId);
            logger.info("成功获取商品SKU列表 - 商品ID: {}, SKU数量: {}", productId, skus.size());
            return R.ok(skus);
        } catch (Exception e) {
            logger.error("获取商品SKU列表失败 - 商品ID: {}", productId, e);
            return R.fail("获取SKU列表失败");
        }
    }

    /**
     * 新增商品SKU
     * 
     * @param sku SKU信息
     * @return 统一响应结果
     */
    @PostMapping("/skus")
    public R<String> addProductSku(@RequestBody ProductSku sku) {
        logger.info("接收到新增商品SKU的请求 - 商品ID: {}, SKU名称: {}", sku.getProductId(), sku.getSkuName());
        
        try {
            boolean success = productService.addProductSku(sku);
            if (success) {
                logger.info("商品SKU新增成功 - 商品ID: {}, SKU名称: {}", sku.getProductId(), sku.getSkuName());
                return R.ok("SKU新增成功");
            } else {
                logger.warn("商品SKU新增失败 - 商品ID: {}, SKU名称: {}", sku.getProductId(), sku.getSkuName());
                return R.fail("SKU新增失败");
            }
        } catch (Exception e) {
            logger.error("新增商品SKU时发生异常 - 商品ID: {}, SKU名称: {}", sku.getProductId(), sku.getSkuName(), e);
            return R.fail("SKU新增失败");
        }
    }

    /**
     * 更新商品SKU
     * 
     * @param sku SKU信息
     * @return 统一响应结果
     */
    @PutMapping("/skus")
    public R<String> updateProductSku(@RequestBody ProductSku sku) {
        logger.info("接收到更新商品SKU的请求 - SKU ID: {}, 名称: {}", sku.getId(), sku.getSkuName());
        
        try {
            boolean success = productService.updateProductSku(sku);
            if (success) {
                logger.info("商品SKU更新成功 - SKU ID: {}, 名称: {}", sku.getId(), sku.getSkuName());
                return R.ok("SKU更新成功");
            } else {
                logger.warn("商品SKU更新失败 - SKU ID: {}, 名称: {}", sku.getId(), sku.getSkuName());
                return R.fail("SKU更新失败");
            }
        } catch (Exception e) {
            logger.error("更新商品SKU时发生异常 - SKU ID: {}, 名称: {}", sku.getId(), sku.getSkuName(), e);
            return R.fail("SKU更新失败");
        }
    }

    /**
     * 删除商品SKU
     * 
     * @param skuId SKU ID
     * @return 统一响应结果
     */
    @DeleteMapping("/skus/{skuId}")
    public R<String> deleteProductSku(@PathVariable Long skuId) {
        logger.info("接收到删除商品SKU的请求 - SKU ID: {}", skuId);
        
        try {
            boolean success = productService.deleteProductSku(skuId);
            if (success) {
                logger.info("商品SKU删除成功 - SKU ID: {}", skuId);
                return R.ok("SKU删除成功");
            } else {
                logger.warn("商品SKU删除失败 - SKU ID: {}", skuId);
                return R.fail("SKU删除失败");
            }
        } catch (Exception e) {
            logger.error("删除商品SKU时发生异常 - SKU ID: {}", skuId, e);
            return R.fail("SKU删除失败");
        }
    }

    /**
     * 批量保存商品SKU
     * 
     * @param productId 商品ID
     * @param skus SKU列表
     * @return 统一响应结果
     */
    @PostMapping("/{productId}/skus/batch")
    public R<String> batchSaveProductSkus(@PathVariable Long productId, @RequestBody List<ProductSku> skus) {
        logger.info("接收到批量保存商品SKU的请求 - 商品ID: {}, SKU数量: {}", productId, skus != null ? skus.size() : 0);
        
        try {
            boolean success = productService.batchSaveProductSkus(productId, skus);
            if (success) {
                logger.info("批量保存商品SKU成功 - 商品ID: {}, SKU数量: {}", productId, skus.size());
                return R.ok("批量保存SKU成功");
            } else {
                logger.warn("批量保存商品SKU失败 - 商品ID: {}", productId);
                return R.fail("批量保存SKU失败");
            }
        } catch (Exception e) {
            logger.error("批量保存商品SKU时发生异常 - 商品ID: {}", productId, e);
            return R.fail("批量保存SKU失败");
        }
    }

    // ==================== 商品归属验证 ====================

    /**
     * 验证商品归属
     * 检查商品是否属于指定商家
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 是否属于该商家
     */
    @GetMapping("/products/{productId}/ownership")
    public R<Boolean> checkProductOwnership(@PathVariable Long productId,
                                            @RequestParam Long merchantId) {
        logger.info("验证商品归属 - 商品ID: {}, 商家ID: {}", productId, merchantId);
        
        try {
            boolean isOwner = productServiceImpl.checkProductOwnership(productId, merchantId);
            logger.info("商品归属验证结果 - 商品ID: {}, 商家ID: {}, 归属: {}", productId, merchantId, isOwner);
            return R.ok(isOwner);
        } catch (Exception e) {
            logger.error("验证商品归属失败 - 商品ID: {}, 商家ID: {}", productId, merchantId, e);
            return R.fail("验证商品归属失败");
        }
    }

    // ==================== 统计功能 ====================

    /**
     * 获取商品统计数据
     * 支持全平台统计和按商家筛选
     * 
     * @param merchantId 商家ID（可选，为空则统计全平台）
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public R<ProductStatistics> getProductStatistics(@RequestParam(required = false) Long merchantId) {
        logger.info("获取商品统计数据 - 商家ID: {}", merchantId);
        
        try {
            ProductStatistics stats = productServiceImpl.getProductStatistics(merchantId);
            logger.info("成功获取商品统计数据 - 商家ID: {}, 总数: {}, 上架: {}, 下架: {}", 
                    merchantId, stats.getTotalCount(), stats.getOnlineCount(), stats.getOfflineCount());
            return R.ok(stats);
        } catch (Exception e) {
            logger.error("获取商品统计数据失败 - 商家ID: {}", merchantId, e);
            return R.fail("获取统计数据失败");
        }
    }

    /**
     * 获取库存预警商品列表（支持商家筛选）
     * 
     * @param merchantId 商家ID（可选，为空则查询全平台）
     * @return 库存预警商品列表
     */
    @GetMapping("/stock/warning/merchant")
    public R<List<Product>> getStockWarningProductsByMerchant(
            @RequestParam(required = false) Long merchantId) {
        logger.info("获取库存预警商品列表 - 商家ID: {}", merchantId);
        
        try {
            List<Product> warningProducts = productServiceImpl.getStockWarningProducts(merchantId);
            logger.info("成功获取库存预警商品列表 - 商家ID: {}, 数量: {}", merchantId, warningProducts.size());
            return R.ok(warningProducts);
        } catch (Exception e) {
            logger.error("获取库存预警商品列表失败 - 商家ID: {}", merchantId, e);
            return R.fail("获取库存预警商品列表失败");
        }
    }

    /**
     * 获取热销商品排行（支持商家筛选）
     * 
     * @param merchantId 商家ID（可选，为空则查询全平台）
     * @param limit 限制数量，默认为10
     * @return 热销商品列表
     */
    @GetMapping("/hot/merchant")
    public R<List<Product>> getHotProductsByMerchant(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(defaultValue = "10") Integer limit) {
        logger.info("获取热销商品排行 - 商家ID: {}, 限制数量: {}", merchantId, limit);
        
        try {
            List<Product> hotProducts = productServiceImpl.getHotProducts(merchantId, limit);
            logger.info("成功获取热销商品排行 - 商家ID: {}, 数量: {}", merchantId, hotProducts.size());
            return R.ok(hotProducts);
        } catch (Exception e) {
            logger.error("获取热销商品排行失败 - 商家ID: {}", merchantId, e);
            return R.fail("获取热销商品排行失败");
        }
    }

    /**
     * 获取销售统计数据
     * 计算总销量和总销售额，支持按商家筛选
     * 
     * @param merchantId 商家ID（可选，为空则统计全平台）
     * @return 销售统计数据
     */
    @GetMapping("/statistics/sales")
    public R<java.util.Map<String, Object>> getSalesStatistics(
            @RequestParam(required = false) Long merchantId) {
        logger.info("获取销售统计数据 - 商家ID: {}", merchantId);
        
        try {
            java.util.Map<String, Object> stats = productServiceImpl.getSalesStatistics(merchantId);
            logger.info("成功获取销售统计数据 - 商家ID: {}, 总销量: {}, 总销售额: {}", 
                    merchantId, stats.get("totalSales"), stats.get("totalRevenue"));
            return R.ok(stats);
        } catch (Exception e) {
            logger.error("获取销售统计数据失败 - 商家ID: {}", merchantId, e);
            return R.fail("获取销售统计数据失败");
        }
    }

    // ==================== 库存管理 ====================

    /**
     * 更新商品库存
     * 单个商品库存调整，支持增加或减少
     * 
     * @param productId 商品ID
     * @param quantity 库存变化数量（正数为增加，负数为减少）
     * @return 统一响应结果，包含更新结果
     */
    @PutMapping("/{productId}/stock")
    public R<String> updateStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        logger.info("接收到更新商品库存的请求 - 商品ID: {}, 变化数量: {}", productId, quantity);
        
        try {
            boolean success = productService.updateStock(productId, quantity);
            if (success) {
                logger.info("商品库存更新成功 - 商品ID: {}, 变化数量: {}", productId, quantity);
                return R.ok("库存更新成功");
            } else {
                logger.warn("商品库存更新失败 - 商品ID: {}, 变化数量: {}", productId, quantity);
                return R.fail("库存更新失败");
            }
        } catch (Exception e) {
            logger.error("更新商品库存时发生异常 - 商品ID: {}, 变化数量: {}", productId, quantity, e);
            return R.fail("库存更新失败");
        }
    }

    /**
     * 更新SKU库存
     * 
     * @param skuId SKU ID
     * @param quantity 库存变化数量
     * @return 统一响应结果
     */
    @PutMapping("/skus/{skuId}/stock")
    public R<String> updateSkuStock(@PathVariable Long skuId, @RequestParam Integer quantity) {
        logger.info("接收到更新SKU库存的请求 - SKU ID: {}, 变化数量: {}", skuId, quantity);
        
        try {
            boolean success = productService.updateSkuStock(skuId, quantity);
            if (success) {
                logger.info("SKU库存更新成功 - SKU ID: {}, 变化数量: {}", skuId, quantity);
                return R.ok("SKU库存更新成功");
            } else {
                logger.warn("SKU库存更新失败 - SKU ID: {}, 变化数量: {}", skuId, quantity);
                return R.fail("SKU库存更新失败");
            }
        } catch (Exception e) {
            logger.error("更新SKU库存时发生异常 - SKU ID: {}, 变化数量: {}", skuId, quantity, e);
            return R.fail("SKU库存更新失败");
        }
    }

    /**
     * 批量更新商品库存
     * 支持一次性更新多个商品的库存
     * 
     * @param stockUpdates 库存更新列表
     * @return 统一响应结果，包含批量更新结果
     */
    @PutMapping("/stock/batch")
    public R<String> batchUpdateStock(@RequestBody List<ProductService.StockUpdate> stockUpdates) {
        logger.info("接收到批量更新商品库存的请求 - 更新数量: {}", stockUpdates != null ? stockUpdates.size() : 0);
        
        try {
            boolean success = productService.batchUpdateStock(stockUpdates);
            if (success) {
                logger.info("批量库存更新成功 - 更新了 {} 个商品", stockUpdates.size());
                return R.ok("批量库存更新成功");
            } else {
                logger.warn("批量库存更新失败");
                return R.fail("批量库存更新失败");
            }
        } catch (Exception e) {
            logger.error("批量更新商品库存时发生异常", e);
            return R.fail("批量库存更新失败");
        }
    }

    /**
     * 获取库存预警商品列表
     * 
     * @return 统一响应结果，包含库存预警商品列表
     */
    @GetMapping("/stock/warning")
    public R<List<Product>> getStockWarningProducts() {
        logger.info("接收到获取库存预警商品列表的请求");
        
        try {
            List<Product> warningProducts = productService.getStockWarningProducts();
            logger.info("成功获取库存预警商品列表 - 数量: {}", warningProducts.size());
            return R.ok(warningProducts);
        } catch (Exception e) {
            logger.error("获取库存预警商品列表失败", e);
            return R.fail("获取库存预警商品列表失败");
        }
    }

    /**
     * 获取库存变更日志
     * 
     * @param productId 商品ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 统一响应结果，包含库存变更日志分页数据
     */
    @GetMapping("/{productId}/stock/logs")
    public R<Object> getStockLogs(@PathVariable Long productId,
                                  @RequestParam(defaultValue = "1") Long current,
                                  @RequestParam(defaultValue = "10") Long size) {
        logger.info("接收到获取库存变更日志的请求 - 商品ID: {}, 页码: {}, 大小: {}", productId, current, size);
        
        try {
            Object pageData = productService.getStockLogs(productId, current, size);
            logger.info("成功获取库存变更日志 - 商品ID: {}", productId);
            return R.ok(pageData);
        } catch (Exception e) {
            logger.error("获取库存变更日志失败 - 商品ID: {}", productId, e);
            return R.fail("获取库存变更日志失败");
        }
    }

    // ==================== 价格管理 ====================

    /**
     * 更新商品价格
     * 
     * @param productId 商品ID
     * @param newPrice 新价格
     * @param reason 变更原因
     * @param operatorId 操作人ID
     * @return 统一响应结果
     */
    @PutMapping("/{productId}/price")
    public R<String> updateProductPrice(@PathVariable Long productId,
                                        @RequestParam Double newPrice,
                                        @RequestParam(required = false) String reason,
                                        @RequestParam(required = false) Long operatorId) {
        logger.info("接收到更新商品价格的请求 - 商品ID: {}, 新价格: {}, 原因: {}", productId, newPrice, reason);
        
        try {
            boolean success = productService.updateProductPrice(productId, newPrice, reason, operatorId);
            if (success) {
                logger.info("商品价格更新成功 - 商品ID: {}, 新价格: {}", productId, newPrice);
                return R.ok("价格更新成功");
            } else {
                logger.warn("商品价格更新失败 - 商品ID: {}, 新价格: {}", productId, newPrice);
                return R.fail("价格更新失败");
            }
        } catch (Exception e) {
            logger.error("更新商品价格时发生异常 - 商品ID: {}, 新价格: {}", productId, newPrice, e);
            return R.fail("价格更新失败");
        }
    }

    /**
     * 更新SKU价格
     * 
     * @param skuId SKU ID
     * @param newPrice 新价格
     * @param reason 变更原因
     * @param operatorId 操作人ID
     * @return 统一响应结果
     */
    @PutMapping("/skus/{skuId}/price")
    public R<String> updateSkuPrice(@PathVariable Long skuId,
                                    @RequestParam Double newPrice,
                                    @RequestParam(required = false) String reason,
                                    @RequestParam(required = false) Long operatorId) {
        logger.info("接收到更新SKU价格的请求 - SKU ID: {}, 新价格: {}, 原因: {}", skuId, newPrice, reason);
        
        try {
            boolean success = productService.updateSkuPrice(skuId, newPrice, reason, operatorId);
            if (success) {
                logger.info("SKU价格更新成功 - SKU ID: {}, 新价格: {}", skuId, newPrice);
                return R.ok("SKU价格更新成功");
            } else {
                logger.warn("SKU价格更新失败 - SKU ID: {}, 新价格: {}", skuId, newPrice);
                return R.fail("SKU价格更新失败");
            }
        } catch (Exception e) {
            logger.error("更新SKU价格时发生异常 - SKU ID: {}, 新价格: {}", skuId, newPrice, e);
            return R.fail("SKU价格更新失败");
        }
    }

    /**
     * 批量调价
     * 
     * @param priceUpdates 价格更新列表
     * @return 统一响应结果
     */
    @PutMapping("/price/batch")
    public R<String> batchUpdatePrices(@RequestBody List<ProductService.PriceUpdate> priceUpdates) {
        logger.info("接收到批量调价的请求 - 更新数量: {}", priceUpdates != null ? priceUpdates.size() : 0);
        
        try {
            boolean success = productService.batchUpdatePrices(priceUpdates);
            if (success) {
                logger.info("批量调价成功 - 更新了 {} 个商品/SKU", priceUpdates.size());
                return R.ok("批量调价成功");
            } else {
                logger.warn("批量调价失败");
                return R.fail("批量调价失败");
            }
        } catch (Exception e) {
            logger.error("批量调价时发生异常", e);
            return R.fail("批量调价失败");
        }
    }

    /**
     * 获取价格变更历史
     * 
     * @param productId 商品ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 统一响应结果，包含价格变更历史分页数据
     */
    @GetMapping("/{productId}/price/history")
    public R<Object> getPriceHistory(@PathVariable Long productId,
                                     @RequestParam(defaultValue = "1") Long current,
                                     @RequestParam(defaultValue = "10") Long size) {
        logger.info("接收到获取价格变更历史的请求 - 商品ID: {}, 页码: {}, 大小: {}", productId, current, size);
        
        try {
            Object pageData = productService.getPriceHistory(productId, current, size);
            logger.info("成功获取价格变更历史 - 商品ID: {}", productId);
            return R.ok(pageData);
        } catch (Exception e) {
            logger.error("获取价格变更历史失败 - 商品ID: {}", productId, e);
            return R.fail("获取价格变更历史失败");
        }
    }

    // ==================== 订单服务调用接口 ====================
    // 以下接口供订单服务(order-service)通过Feign调用
    // 修改日志：V2.1 2025-12-01：新增订单服务所需的库存检查、扣减、恢复接口

    /**
     * 检查商品库存是否充足
     * 供订单服务创建订单前检查库存
     * 
     * @param productId 商品ID
     * @param quantity 需要的数量
     * @return 库存是否充足
     */
    @GetMapping("/products/{productId}/stock/check")
    public Boolean checkProductStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        logger.info("检查商品库存 - 商品ID: {}, 需要数量: {}", productId, quantity);
        
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                logger.warn("商品不存在 - ID: {}", productId);
                return false;
            }
            
            boolean sufficient = product.getStock() != null && product.getStock() >= quantity;
            logger.info("库存检查结果 - 商品ID: {}, 当前库存: {}, 需要: {}, 充足: {}", 
                    productId, product.getStock(), quantity, sufficient);
            return sufficient;
        } catch (Exception e) {
            logger.error("检查库存失败 - 商品ID: {}", productId, e);
            return false;
        }
    }

    /**
     * 扣减商品库存
     * 供订单服务创建订单时扣减库存
     * 
     * @param request 库存扣减请求，包含productId、quantity、orderNo
     * @return 扣减是否成功
     */
    @PostMapping("/products/stock/deduct")
    public Boolean deductProductStock(@RequestBody java.util.Map<String, Object> request) {
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        String orderNo = request.get("orderNo") != null ? request.get("orderNo").toString() : "";
        
        logger.info("扣减商品库存 - 商品ID: {}, 数量: {}, 订单号: {}", productId, quantity, orderNo);
        
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                logger.warn("商品不存在，无法扣减库存 - 商品ID: {}", productId);
                return false;
            }
            
            // 检查库存是否充足
            int currentStock = product.getStock() != null ? product.getStock() : 0;
            if (currentStock < quantity) {
                logger.warn("库存不足 - 商品ID: {}, 当前库存: {}, 需要: {}", 
                        productId, currentStock, quantity);
                return false;
            }
            
            // 扣减库存（计算新库存值）
            int newStock = currentStock - quantity;
            boolean success = productService.updateStock(productId, newStock);
            if (success) {
                logger.info("库存扣减成功 - 商品ID: {}, 扣减数量: {}, 订单号: {}", productId, quantity, orderNo);
            } else {
                logger.error("库存扣减失败 - 商品ID: {}", productId);
            }
            return success;
        } catch (Exception e) {
            logger.error("扣减库存异常 - 商品ID: {}", productId, e);
            return false;
        }
    }

    /**
     * 恢复商品库存
     * 供订单取消时恢复库存
     * 
     * @param request 库存恢复请求，包含productId、quantity、orderNo
     * @return 恢复是否成功
     */
    @PostMapping("/products/stock/restore")
    public Boolean restoreProductStock(@RequestBody java.util.Map<String, Object> request) {
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        String orderNo = request.get("orderNo") != null ? request.get("orderNo").toString() : "";
        
        logger.info("恢复商品库存 - 商品ID: {}, 数量: {}, 订单号: {}", productId, quantity, orderNo);
        
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                logger.warn("商品不存在，无法恢复库存 - 商品ID: {}", productId);
                return false;
            }
            
            // 恢复库存（计算新库存值）
            int currentStock = product.getStock() != null ? product.getStock() : 0;
            int newStock = currentStock + quantity;
            boolean success = productService.updateStock(productId, newStock);
            if (success) {
                logger.info("库存恢复成功 - 商品ID: {}, 恢复数量: {}, 订单号: {}", productId, quantity, orderNo);
            } else {
                logger.error("库存恢复失败 - 商品ID: {}", productId);
            }
            return success;
        } catch (Exception e) {
            logger.error("恢复库存异常 - 商品ID: {}", productId, e);
            return false;
        }
    }
}