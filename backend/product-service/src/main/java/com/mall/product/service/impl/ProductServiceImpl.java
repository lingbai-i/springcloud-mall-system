package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.product.domain.entity.Product;
import com.mall.product.domain.entity.ProductSku;
import com.mall.product.domain.entity.PriceHistory;
import com.mall.product.domain.entity.StockLog;
import com.mall.product.domain.dto.ProductDetailDto;
import com.mall.product.domain.dto.ProductQueryDto;
import com.mall.product.domain.dto.ProductStatistics;
import com.mall.product.mapper.ProductMapper;
import com.mall.product.mapper.StockLogMapper;
import com.mall.product.mapper.PriceHistoryMapper;
import com.mall.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * 使用 MyBatis-Plus 进行数据库操作，支持商家ID筛选
 * 
 * @author lingbai
 * @version 3.0
 * @since 2025-10-22
 * 修改日志：V3.0 2025-12-01：重构为数据库实现，移除模拟数据，添加商家筛选支持
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired(required = false)
    private ProductMapper productMapper;

    @Autowired(required = false)
    private StockLogMapper stockLogMapper;

    @Autowired(required = false)
    private PriceHistoryMapper priceHistoryMapper;

    // SKU缓存（暂时保留，后续可迁移到数据库）
    private static final Map<Long, List<ProductSku>> SKU_CACHE = new HashMap<>();

    // ==================== 商品基础查询 ====================

    /**
     * 根据分类ID分页查询商品
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param categoryId 分类ID
     * @return 商品分页数据
     */
    @Override
    public Object getProductsByCategoryId(Long current, Long size, Long categoryId) {
        // 调用支持商家筛选的重载方法，merchantId 为 null 表示不筛选商家
        return getProductsByCategoryId(current, size, categoryId, null);
    }

    /**
     * 根据分类ID分页查询商品（支持商家筛选）
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param categoryId 分类ID（可选）
     * @param merchantId 商家ID（可选）
     * @return 商品分页数据
     */
    @Override
    public Object getProductsByCategoryId(Long current, Long size, Long categoryId, Long merchantId) {
        logger.info("根据分类ID分页查询商品 - 页码: {}, 大小: {}, 分类ID: {}, 商家ID: {}", 
                current, size, categoryId, merchantId);

        try {
            if (productMapper == null) {
                return createEmptyPageData(current, size);
            }

            Page<Product> page = new Page<>(current, size);
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            
            // 分类筛选
            if (categoryId != null) {
                wrapper.eq(Product::getCategoryId, categoryId);
            }
            // 商家筛选
            if (merchantId != null) {
                wrapper.eq(Product::getMerchantId, merchantId);
            }
            wrapper.eq(Product::getStatus, 1); // 只查询上架商品
            wrapper.orderByDesc(Product::getCreateTime);

            IPage<Product> result = productMapper.selectPage(page, wrapper);
            logger.info("查询成功 - 总数: {}, 当前页数量: {}", result.getTotal(), result.getRecords().size());
            return createPageData(result.getRecords(), (int) result.getTotal(), current, size);
        } catch (Exception e) {
            logger.error("根据分类ID分页查询商品失败", e);
            return createEmptyPageData(current, size);
        }
    }

    /**
     * 搜索商品
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @return 商品分页数据
     */
    @Override
    public Object searchProducts(Long current, Long size, String keyword) {
        // 调用支持商家筛选的重载方法，merchantId 为 null 表示不筛选商家
        return searchProducts(current, size, keyword, null);
    }

    /**
     * 搜索商品（支持商家筛选）
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param merchantId 商家ID（可选）
     * @return 商品分页数据
     */
    @Override
    public Object searchProducts(Long current, Long size, String keyword, Long merchantId) {
        logger.info("搜索商品 - 页码: {}, 大小: {}, 关键词: {}, 商家ID: {}", current, size, keyword, merchantId);

        try {
            if (productMapper == null) {
                return createEmptyPageData(current, size);
            }

            Page<Product> page = new Page<>(current, size);
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            
            // 关键词筛选
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.and(w -> w
                    .like(Product::getName, keyword)
                    .or()
                    .like(Product::getDescription, keyword)
                );
            }
            // 商家筛选
            if (merchantId != null) {
                wrapper.eq(Product::getMerchantId, merchantId);
            }
            wrapper.eq(Product::getStatus, 1);
            wrapper.orderByDesc(Product::getSales);

            IPage<Product> result = productMapper.selectPage(page, wrapper);
            logger.info("搜索成功 - 总数: {}, 当前页数量: {}", result.getTotal(), result.getRecords().size());
            return createPageData(result.getRecords(), (int) result.getTotal(), current, size);
        } catch (Exception e) {
            logger.error("搜索商品失败 - 关键词: {}", keyword, e);
            return createEmptyPageData(current, size);
        }
    }

    /**
     * 根据商家ID分页查询商品
     * 
     * @param current 当前页码
     * @param size 每页大小
     * @param merchantId 商家ID
     * @return 商品分页数据
     */
    @Override
    public Object getProductsByMerchantId(Long current, Long size, Long merchantId) {
        logger.info("根据商家ID分页查询商品 - 页码: {}, 大小: {}, 商家ID: {}", current, size, merchantId);

        try {
            if (productMapper == null) {
                return createEmptyPageData(current, size);
            }

            if (merchantId == null) {
                logger.warn("商家ID不能为空");
                return createEmptyPageData(current, size);
            }

            Page<Product> page = new Page<>(current, size);
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getMerchantId, merchantId);
            wrapper.orderByDesc(Product::getCreateTime);

            IPage<Product> result = productMapper.selectPage(page, wrapper);
            logger.info("查询成功 - 商家ID: {}, 总数: {}, 当前页数量: {}", 
                    merchantId, result.getTotal(), result.getRecords().size());
            return createPageData(result.getRecords(), (int) result.getTotal(), current, size);
        } catch (Exception e) {
            logger.error("根据商家ID分页查询商品失败 - 商家ID: {}", merchantId, e);
            return createEmptyPageData(current, size);
        }
    }

    /**
     * 获取热销商品列表
     */
    @Override
    public List<Product> getHotProducts(Integer limit) {
        logger.info("获取热销商品列表 - 限制数量: {}", limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }

            if (productMapper != null) {
                return productMapper.selectHotProducts(null, limit);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("获取热销商品异常 - 限制数量: {}", limit, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取热销商品列表（支持商家筛选）
     */
    public List<Product> getHotProducts(Long merchantId, Integer limit) {
        logger.info("获取热销商品列表 - 商家ID: {}, 限制数量: {}", merchantId, limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }

            if (productMapper != null) {
                return productMapper.selectHotProducts(merchantId, limit);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("获取热销商品异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取推荐商品列表
     */
    @Override
    public List<Product> getRecommendProducts(Integer limit) {
        logger.info("获取推荐商品 - 限制数量: {}", limit);

        try {
            if (limit == null || limit <= 0) {
                limit = 10;
            }

            if (productMapper != null) {
                LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Product::getStatus, 1)
                       .eq(Product::getIsRecommend, true)
                       .orderByDesc(Product::getSales)
                       .last("LIMIT " + limit);
                return productMapper.selectList(wrapper);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("获取推荐商品失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据ID获取商品详情
     */
    @Override
    public Product getProductById(Long id) {
        logger.info("根据ID获取商品详情 - ID: {}", id);

        try {
            if (productMapper != null) {
                return productMapper.selectById(id);
            }
            return null;
        } catch (Exception e) {
            logger.error("根据ID获取商品详情失败 - ID: {}", id, e);
            return null;
        }
    }

    /**
     * 根据ID列表批量获取商品
     */
    @Override
    public List<Product> getProductsByIds(List<Long> ids) {
        logger.info("批量获取商品 - 数量: {}", ids != null ? ids.size() : 0);

        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            if (productMapper != null) {
                return productMapper.selectBatchIds(ids);
            }
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("批量获取商品失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取商品详细信息（包含SKU列表）
     */
    @Override
    public ProductDetailDto getProductDetail(Long id) {
        logger.info("获取商品详细信息 - ID: {}", id);

        try {
            Product product = getProductById(id);
            if (product == null) {
                return null;
            }

            ProductDetailDto detailDto = new ProductDetailDto();
            detailDto.setProduct(product);

            List<ProductSku> skuList = SKU_CACHE.getOrDefault(id, new ArrayList<>());
            detailDto.setSkuList(skuList);

            if (!skuList.isEmpty()) {
                OptionalDouble minPrice = skuList.stream().mapToDouble(ProductSku::getPrice).min();
                OptionalDouble maxPrice = skuList.stream().mapToDouble(ProductSku::getPrice).max();
                detailDto.setMinPrice(minPrice.orElse(product.getPrice()));
                detailDto.setMaxPrice(maxPrice.orElse(product.getPrice()));
                detailDto.setTotalStock(skuList.stream().mapToInt(ProductSku::getStock).sum());
            } else {
                detailDto.setMinPrice(product.getPrice());
                detailDto.setMaxPrice(product.getPrice());
                detailDto.setTotalStock(product.getStock());
            }

            detailDto.setCategoryName(product.getCategoryName());
            detailDto.setBrandName(product.getBrandName());

            return detailDto;
        } catch (Exception e) {
            logger.error("获取商品详细信息失败 - ID: {}", id, e);
            return null;
        }
    }

    /**
     * 高级商品搜索
     */
    @Override
    public Object advancedSearchProducts(ProductQueryDto queryDto, Long current, Long size) {
        logger.info("高级商品搜索 - 查询条件: {}", queryDto);

        try {
            if (productMapper == null) {
                return createEmptyPageData(current, size);
            }

            Page<Product> page = new Page<>(current, size);
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

            // 关键词筛选
            if (queryDto.getKeyword() != null && !queryDto.getKeyword().trim().isEmpty()) {
                wrapper.and(w -> w
                    .like(Product::getName, queryDto.getKeyword())
                    .or()
                    .like(Product::getDescription, queryDto.getKeyword())
                );
            }

            // 分类筛选
            if (queryDto.getCategoryId() != null) {
                wrapper.eq(Product::getCategoryId, queryDto.getCategoryId());
            }

            // 价格范围筛选
            if (queryDto.getMinPrice() != null) {
                wrapper.ge(Product::getPrice, queryDto.getMinPrice());
            }
            if (queryDto.getMaxPrice() != null) {
                wrapper.le(Product::getPrice, queryDto.getMaxPrice());
            }

            // 状态筛选
            if (queryDto.getStatus() != null) {
                wrapper.eq(Product::getStatus, queryDto.getStatus());
            }

            // 排序
            if ("price".equals(queryDto.getSortField())) {
                if ("desc".equals(queryDto.getSortOrder())) {
                    wrapper.orderByDesc(Product::getPrice);
                } else {
                    wrapper.orderByAsc(Product::getPrice);
                }
            } else if ("sales".equals(queryDto.getSortField())) {
                wrapper.orderByDesc(Product::getSales);
            } else {
                wrapper.orderByDesc(Product::getCreateTime);
            }

            IPage<Product> result = productMapper.selectPage(page, wrapper);
            return createPageData(result.getRecords(), (int) result.getTotal(), current, size);
        } catch (Exception e) {
            logger.error("高级商品搜索失败", e);
            return createEmptyPageData(current, size);
        }
    }

    // ==================== 商品管理 ====================

    /**
     * 新增商品
     */
    @Override
    @Transactional
    public boolean addProduct(Product product) {
        logger.info("新增商品 - 名称: {}", product != null ? product.getName() : null);

        try {
            if (product == null || product.getName() == null || product.getName().trim().isEmpty()) {
                logger.warn("商品信息无效");
                return false;
            }

            product.setCreateTime(LocalDateTime.now());
            product.setUpdateTime(LocalDateTime.now());
            if (product.getSales() == null) product.setSales(0);
            if (product.getStock() == null) product.setStock(0);
            if (product.getStatus() == null) product.setStatus(1);

            if (productMapper != null) {
                int rows = productMapper.insert(product);
                logger.info("商品新增成功 - ID: {}, 名称: {}", product.getId(), product.getName());
                return rows > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("新增商品失败 - 名称: {}", product != null ? product.getName() : null, e);
            return false;
        }
    }

    /**
     * 更新商品信息
     */
    @Override
    @Transactional
    public boolean updateProduct(Product product) {
        logger.info("更新商品 - ID: {}", product != null ? product.getId() : null);

        try {
            if (product == null || product.getId() == null) {
                logger.warn("商品信息无效");
                return false;
            }

            product.setUpdateTime(LocalDateTime.now());

            if (productMapper != null) {
                int rows = productMapper.updateById(product);
                logger.info("商品更新成功 - ID: {}", product.getId());
                return rows > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("更新商品失败 - ID: {}", product != null ? product.getId() : null, e);
            return false;
        }
    }

    /**
     * 删除商品
     */
    @Override
    @Transactional
    public boolean deleteProduct(Long id) {
        logger.info("删除商品 - ID: {}", id);

        try {
            if (id == null) {
                return false;
            }

            if (productMapper != null) {
                int rows = productMapper.deleteById(id);
                SKU_CACHE.remove(id);
                logger.info("商品删除成功 - ID: {}", id);
                return rows > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("删除商品失败 - ID: {}", id, e);
            return false;
        }
    }

    /**
     * 批量删除商品
     */
    @Override
    @Transactional
    public boolean batchDeleteProducts(List<Long> ids) {
        logger.info("批量删除商品 - 数量: {}", ids != null ? ids.size() : 0);

        try {
            if (ids == null || ids.isEmpty()) {
                return false;
            }

            if (productMapper != null) {
                int rows = productMapper.deleteBatchIds(ids);
                ids.forEach(SKU_CACHE::remove);
                logger.info("批量删除完成 - 删除数量: {}", rows);
                return rows > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("批量删除商品失败", e);
            return false;
        }
    }

    /**
     * 更新商品状态
     */
    @Override
    @Transactional
    public boolean updateProductStatus(Long id, Integer status) {
        logger.info("更新商品状态 - ID: {}, 状态: {}", id, status);

        try {
            if (productMapper != null) {
                Product product = new Product();
                product.setId(id);
                product.setStatus(status);
                product.setUpdateTime(LocalDateTime.now());
                int rows = productMapper.updateById(product);
                return rows > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("更新商品状态失败 - ID: {}", id, e);
            return false;
        }
    }


    // ==================== 商品归属验证 ====================

    /**
     * 验证商品归属
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 是否属于该商家
     */
    public boolean checkProductOwnership(Long productId, Long merchantId) {
        logger.info("验证商品归属 - 商品ID: {}, 商家ID: {}", productId, merchantId);

        try {
            if (productMapper != null) {
                Product product = productMapper.selectById(productId);
                return product != null && merchantId.equals(product.getMerchantId());
            }
            return false;
        } catch (Exception e) {
            logger.error("验证商品归属失败", e);
            return false;
        }
    }

    // ==================== 统计功能 ====================

    /**
     * 获取商品统计数据
     * 
     * @param merchantId 商家ID（可选，为空则统计全平台）
     * @return 统计数据
     */
    public ProductStatistics getProductStatistics(Long merchantId) {
        logger.info("获取商品统计数据 - 商家ID: {}", merchantId);

        try {
            ProductStatistics stats = new ProductStatistics();
            stats.setMerchantId(merchantId);

            if (productMapper != null) {
                // 商品总数
                stats.setTotalCount(productMapper.countByCondition(merchantId, null));
                // 上架数
                stats.setOnlineCount(productMapper.countByCondition(merchantId, 1));
                // 下架数
                stats.setOfflineCount(productMapper.countByCondition(merchantId, 0));
                // 库存预警数
                List<Product> warningProducts = productMapper.selectStockWarningProducts(merchantId);
                stats.setWarningCount((long) warningProducts.size());
                // 总销量
                stats.setTotalSales(productMapper.sumSales(merchantId));
                // 总销售额
                stats.setTotalRevenue(productMapper.sumRevenue(merchantId));
            }

            return stats;
        } catch (Exception e) {
            logger.error("获取商品统计数据失败", e);
            return new ProductStatistics();
        }
    }

    /**
     * 获取销售统计数据
     * 
     * @param merchantId 商家ID（可选，为空则统计全平台）
     * @return 销售统计数据（包含总销量和总销售额）
     */
    public Map<String, Object> getSalesStatistics(Long merchantId) {
        logger.info("获取销售统计数据 - 商家ID: {}", merchantId);

        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("merchantId", merchantId);

            if (productMapper != null) {
                // 总销量
                Long totalSales = productMapper.sumSales(merchantId);
                stats.put("totalSales", totalSales != null ? totalSales : 0L);
                // 总销售额
                Double totalRevenue = productMapper.sumRevenue(merchantId);
                stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
            } else {
                stats.put("totalSales", 0L);
                stats.put("totalRevenue", 0.0);
            }

            logger.info("销售统计数据 - 商家ID: {}, 总销量: {}, 总销售额: {}", 
                    merchantId, stats.get("totalSales"), stats.get("totalRevenue"));
            return stats;
        } catch (Exception e) {
            logger.error("获取销售统计数据失败", e);
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("merchantId", merchantId);
            emptyStats.put("totalSales", 0L);
            emptyStats.put("totalRevenue", 0.0);
            return emptyStats;
        }
    }

    /**
     * 获取库存预警商品
     */
    @Override
    public List<Product> getStockWarningProducts() {
        return getStockWarningProducts(null);
    }

    /**
     * 获取库存预警商品（支持商家筛选）
     */
    public List<Product> getStockWarningProducts(Long merchantId) {
        logger.info("获取库存预警商品 - 商家ID: {}", merchantId);

        try {
            if (productMapper != null) {
                return productMapper.selectStockWarningProducts(merchantId);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("获取库存预警商品失败", e);
            return new ArrayList<>();
        }
    }

    // ==================== 库存管理 ====================

    /**
     * 更新库存
     */
    @Override
    @Transactional
    public boolean updateStock(Long productId, Integer newStock) {
        logger.info("更新库存 - 商品ID: {}, 新库存: {}", productId, newStock);

        try {
            if (newStock < 0) {
                logger.warn("库存不能为负数");
                return false;
            }

            if (productMapper != null) {
                Product product = productMapper.selectById(productId);
                if (product == null) {
                    return false;
                }

                int oldStock = product.getStock() != null ? product.getStock() : 0;
                
                Product update = new Product();
                update.setId(productId);
                update.setStock(newStock);
                update.setUpdateTime(LocalDateTime.now());
                int rows = productMapper.updateById(update);

                // 记录库存日志（失败不影响主流程）
                if (rows > 0 && stockLogMapper != null) {
                    try {
                        StockLog log = new StockLog();
                        log.setProductId(productId);
                        log.setOldStock(oldStock);
                        log.setNewStock(newStock);
                        log.setChangeQuantity(newStock - oldStock);
                        log.setChangeType("UPDATE");
                        log.setCreateTime(LocalDateTime.now());
                        stockLogMapper.insert(log);
                    } catch (Exception e) {
                        logger.warn("记录库存日志失败，不影响主流程 - 商品ID: {}", productId, e);
                    }
                }

                return rows > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("更新库存失败 - 商品ID: {}", productId, e);
            return false;
        }
    }

    /**
     * 检查库存是否充足
     */
    public boolean checkProductStock(Long productId, Integer quantity) {
        logger.info("检查库存 - 商品ID: {}, 数量: {}", productId, quantity);

        try {
            if (productMapper != null) {
                Product product = productMapper.selectById(productId);
                return product != null && product.getStock() != null && product.getStock() >= quantity;
            }
            return false;
        } catch (Exception e) {
            logger.error("检查库存失败", e);
            return false;
        }
    }

    /**
     * 扣减库存
     */
    @Transactional
    public boolean deductProductStock(Long productId, Integer quantity, String orderNo) {
        logger.info("扣减库存 - 商品ID: {}, 数量: {}, 订单号: {}", productId, quantity, orderNo);

        try {
            if (productMapper != null) {
                Product product = productMapper.selectById(productId);
                if (product == null) return false;

                int oldStock = product.getStock() != null ? product.getStock() : 0;
                int rows = productMapper.deductStock(productId, quantity);

                if (rows > 0 && stockLogMapper != null) {
                    StockLog log = new StockLog();
                    log.setProductId(productId);
                    log.setOldStock(oldStock);
                    log.setNewStock(oldStock - quantity);
                    log.setChangeQuantity(-quantity);
                    log.setChangeType("DEDUCT");
                    log.setRelatedOrderNo(orderNo);
                    log.setCreateTime(LocalDateTime.now());
                    stockLogMapper.insert(log);
                }

                return rows > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("扣减库存失败", e);
            return false;
        }
    }

    /**
     * 恢复库存
     */
    @Transactional
    public boolean restoreProductStock(Long productId, Integer quantity, String orderNo) {
        logger.info("恢复库存 - 商品ID: {}, 数量: {}, 订单号: {}", productId, quantity, orderNo);

        try {
            if (productMapper != null) {
                Product product = productMapper.selectById(productId);
                if (product == null) return false;

                int oldStock = product.getStock() != null ? product.getStock() : 0;
                int rows = productMapper.restoreStock(productId, quantity);

                if (rows > 0 && stockLogMapper != null) {
                    StockLog log = new StockLog();
                    log.setProductId(productId);
                    log.setOldStock(oldStock);
                    log.setNewStock(oldStock + quantity);
                    log.setChangeQuantity(quantity);
                    log.setChangeType("RESTORE");
                    log.setRelatedOrderNo(orderNo);
                    log.setCreateTime(LocalDateTime.now());
                    stockLogMapper.insert(log);
                }

                return rows > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("恢复库存失败", e);
            return false;
        }
    }

    // ==================== 价格管理 ====================

    /**
     * 更新商品价格
     */
    @Override
    @Transactional
    public boolean updateProductPrice(Long productId, Double newPrice, String reason, Long operatorId) {
        logger.info("更新商品价格 - 商品ID: {}, 新价格: {}", productId, newPrice);

        try {
            if (newPrice == null || newPrice < 0) {
                logger.warn("价格无效");
                return false;
            }

            if (productMapper != null) {
                Product product = productMapper.selectById(productId);
                if (product == null) return false;

                Double oldPrice = product.getPrice();

                Product update = new Product();
                update.setId(productId);
                update.setPrice(newPrice);
                update.setUpdateTime(LocalDateTime.now());
                int rows = productMapper.updateById(update);

                // 记录价格历史
                if (rows > 0 && priceHistoryMapper != null) {
                    PriceHistory history = new PriceHistory();
                    history.setProductId(productId);
                    history.setOldPrice(oldPrice);
                    history.setNewPrice(newPrice);
                    history.setReason(reason);
                    history.setOperatorId(operatorId);
                    history.setCreateTime(LocalDateTime.now());
                    priceHistoryMapper.insert(history);
                }

                return rows > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("更新商品价格失败", e);
            return false;
        }
    }

    // ==================== SKU管理（暂时保留缓存实现） ====================

    @Override
    public List<ProductSku> getProductSkus(Long productId) {
        return SKU_CACHE.getOrDefault(productId, new ArrayList<>());
    }

    @Override
    public boolean addProductSku(ProductSku sku) {
        if (sku == null || sku.getProductId() == null) return false;
        sku.setId(System.currentTimeMillis());
        sku.setCreateTime(LocalDateTime.now());
        SKU_CACHE.computeIfAbsent(sku.getProductId(), k -> new ArrayList<>()).add(sku);
        return true;
    }

    @Override
    public boolean updateProductSku(ProductSku sku) {
        if (sku == null || sku.getId() == null) return false;
        List<ProductSku> skus = SKU_CACHE.get(sku.getProductId());
        if (skus != null) {
            for (int i = 0; i < skus.size(); i++) {
                if (sku.getId().equals(skus.get(i).getId())) {
                    sku.setUpdateTime(LocalDateTime.now());
                    skus.set(i, sku);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean deleteProductSku(Long skuId) {
        for (List<ProductSku> skus : SKU_CACHE.values()) {
            if (skus.removeIf(sku -> skuId.equals(sku.getId()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean batchSaveProductSkus(Long productId, List<ProductSku> skus) {
        if (productId == null || skus == null) return false;
        SKU_CACHE.put(productId, new ArrayList<>());
        for (ProductSku sku : skus) {
            sku.setProductId(productId);
            addProductSku(sku);
        }
        return true;
    }

    @Override
    public boolean updateSkuStock(Long skuId, Integer newStock) {
        for (List<ProductSku> skus : SKU_CACHE.values()) {
            for (ProductSku sku : skus) {
                if (skuId.equals(sku.getId())) {
                    sku.setStock(newStock);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean updateSkuPrice(Long skuId, Double newPrice, String reason, Long operatorId) {
        for (List<ProductSku> skus : SKU_CACHE.values()) {
            for (ProductSku sku : skus) {
                if (skuId.equals(sku.getId())) {
                    sku.setPrice(newPrice);
                    return true;
                }
            }
        }
        return false;
    }

    // ==================== 其他接口实现 ====================

    @Override
    public boolean batchUpdateStock(List<ProductService.StockUpdate> updates) {
        if (updates == null) return false;
        for (StockUpdate update : updates) {
            updateStock(update.getProductId(), update.getQuantity());
        }
        return true;
    }

    @Override
    public boolean batchUpdatePrices(List<ProductService.PriceUpdate> updates) {
        if (updates == null) return false;
        for (PriceUpdate update : updates) {
            updateProductPrice(update.getProductId(), update.getNewPrice(), update.getReason(), update.getOperatorId());
        }
        return true;
    }

    @Override
    public Object getStockLogs(Long productId, Long current, Long size) {
        return createEmptyPageData(current, size);
    }

    @Override
    public Object getPriceHistory(Long productId, Long current, Long size) {
        return createEmptyPageData(current, size);
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> createPageData(List<Product> records, int total, Long current, Long size) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("current", current);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);
        return result;
    }

    private Map<String, Object> createEmptyPageData(Long current, Long size) {
        return createPageData(new ArrayList<>(), 0, current, size);
    }
}
