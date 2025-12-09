package com.mall.merchant.service;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.MerchantProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商家商品服务接口
 * 提供商家商品相关的业务逻辑处理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
public interface MerchantProductService {
    
    /**
     * 添加商品
     * 
     * @param merchantId 商家ID
     * @param product 商品信息
     * @return 添加结果
     */
    R<Void> addProduct(Long merchantId, MerchantProduct product);
    
    /**
     * 更新商品信息
     * 
     * @param merchantId 商家ID
     * @param product 商品信息
     * @return 更新结果
     */
    R<Void> updateProduct(Long merchantId, MerchantProduct product);
    
    /**
     * 删除商品
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @return 删除结果
     */
    R<Void> deleteProduct(Long merchantId, Long productId);
    
    /**
     * 批量删除商品
     * 
     * @param merchantId 商家ID
     * @param productIds 商品ID列表
     * @return 删除结果
     */
    R<Void> batchDeleteProducts(Long merchantId, List<Long> productIds);
    
    /**
     * 上架商品
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @return 上架结果
     */
    R<Void> onlineProduct(Long merchantId, Long productId);
    
    /**
     * 下架商品
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @return 下架结果
     */
    R<Void> offlineProduct(Long merchantId, Long productId);
    
    /**
     * 批量上架商品
     * 
     * @param merchantId 商家ID
     * @param productIds 商品ID列表
     * @return 上架结果
     */
    R<Void> batchOnlineProducts(Long merchantId, List<Long> productIds);
    
    /**
     * 批量下架商品
     * 
     * @param merchantId 商家ID
     * @param productIds 商品ID列表
     * @return 下架结果
     */
    R<Void> batchOfflineProducts(Long merchantId, List<Long> productIds);
    
    /**
     * 根据ID获取商品详情
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @return 商品详情
     */
    R<MerchantProduct> getProductById(Long merchantId, Long productId);
    
    /**
     * 分页查询商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @param productName 商品名称（可选）
     * @param categoryId 分类ID（可选）
     * @param brandId 品牌ID（可选）
     * @param status 商品状态（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @return 商品列表
     */
    /**
     * 分页查询商品列表
     * 修复：实体使用品牌字符串字段（brand），不存在 brandId；调整签名避免上下文加载失败。
     *
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @param productName 商品名称（可选）
     * @param categoryId 分类ID（可选）
     * @param brand 品牌名称（可选）
     * @param status 商品状态（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @return 商品列表
     */
    R<PageResult<MerchantProduct>> getProductList(Long merchantId, Integer page, Integer size, 
                                                 String productName, Long categoryId, String brand, Integer status,
                                                 BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * 获取上架商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 上架商品分页列表
     */
    R<PageResult<MerchantProduct>> getOnlineProducts(Long merchantId, Integer page, Integer size);
    
    /**
     * 获取下架商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 下架商品分页列表
     */
    R<PageResult<MerchantProduct>> getOfflineProducts(Long merchantId, Integer page, Integer size);
    
    /**
     * 获取库存不足商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @param lowStockThreshold 库存不足阈值
     * @return 库存不足商品分页列表
     */
    R<PageResult<MerchantProduct>> getLowStockProducts(Long merchantId, Integer page, Integer size, Integer lowStockThreshold);
    
    /**
     * 获取热销商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 热销商品分页列表
     */
    R<PageResult<MerchantProduct>> getHotProducts(Long merchantId, Integer page, Integer size);
    
    /**
     * 获取推荐商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 推荐商品分页列表
     */
    R<PageResult<MerchantProduct>> getRecommendProducts(Long merchantId, Integer page, Integer size);
    
    /**
     * 获取新品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 新品分页列表
     */
    R<PageResult<MerchantProduct>> getNewProducts(Long merchantId, Integer page, Integer size);
    
    /**
     * 更新商品库存
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param quantity 库存变化量（正数增加，负数减少）
     * @return 更新结果
     */
    R<Void> updateStock(Long merchantId, Long productId, Integer quantity);
    
    /**
     * 批量更新商品库存
     * 
     * @param merchantId 商家ID
     * @param stockUpdates 库存更新列表，Map的key为商品ID，value为库存变化量
     * @return 更新结果
     */
    R<Void> batchUpdateStock(Long merchantId, Map<Long, Integer> stockUpdates);
    
    /**
     * 更新商品价格
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param price 新价格
     * @return 更新结果
     */
    R<Void> updatePrice(Long merchantId, Long productId, BigDecimal price);
    
    /**
     * 批量更新商品价格
     * 
     * @param merchantId 商家ID
     * @param priceUpdates 价格更新列表，Map的key为商品ID，value为新价格
     * @return 更新结果
     */
    R<Void> batchUpdatePrice(Long merchantId, Map<Long, BigDecimal> priceUpdates);
    
    /**
     * 设置商品推荐状态
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param isRecommend 是否推荐
     * @return 设置结果
     */
    R<Void> setRecommendStatus(Long merchantId, Long productId, Boolean isRecommend);
    
    /**
     * 设置商品新品状态
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param isNew 是否新品
     * @return 设置结果
     */
    R<Void> setNewStatus(Long merchantId, Long productId, Boolean isNew);
    
    /**
     * 设置商品热销状态
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param isHot 是否热销
     * @return 设置结果
     */
    R<Void> setHotStatus(Long merchantId, Long productId, Boolean isHot);
    
    /**
     * 增加商品浏览量
     * 
     * @param productId 商品ID
     * @return 增加结果
     */
    R<Void> increaseViewCount(Long productId);
    
    /**
     * 增加商品收藏数
     * 
     * @param productId 商品ID
     * @return 增加结果
     */
    R<Void> increaseFavoriteCount(Long productId);
    
    /**
     * 减少商品收藏数
     * 
     * @param productId 商品ID
     * @return 减少结果
     */
    R<Void> decreaseFavoriteCount(Long productId);
    
    /**
     * 增加商品销售数量
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param quantity 销售数量
     * @return 增加结果
     */
    R<Void> increaseSalesCount(Long merchantId, Long productId, Integer quantity);
    
    /**
     * 获取商品统计数据
     * 
     * @param merchantId 商家ID
     * @return 统计数据
     */
    R<Map<String, Object>> getProductStatistics(Long merchantId);
    
    /**
     * 获取热销商品列表
     * 
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 热销商品列表
     */
    R<List<MerchantProduct>> getHotSellingProducts(Long merchantId, Integer limit);
    
    /**
     * 获取推荐商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 推荐商品列表
     */
    R<PageResult<MerchantProduct>> getRecommendedProducts(Long merchantId, Integer page, Integer size);
    
    /**
     * 导出商品数据
     * 
     * @param merchantId 商家ID
     * @param productName 商品名称（可选）
     * @param categoryId 分类ID（可选）
     * @param brandId 品牌ID（可选）
     * @param status 商品状态（可选）
     * @return 导出数据
     */
    /**
     * 导出商品数据
     * 修复：品牌参数改为 String brand 与实体保持一致。
     */
    R<byte[]> exportProductData(Long merchantId, String productName, Long categoryId, String brand, Integer status);
    
    /**
     * 复制商品
     * 
     * @param merchantId 商家ID
     * @param productId 原商品ID
     * @return 复制结果
     */
    R<Void> copyProduct(Long merchantId, Long productId);
    
    /**
     * 检查商品是否属于指定商家
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 是否属于该商家
     */
    R<Boolean> checkProductOwnership(Long productId, Long merchantId);
    
    /**
     * 批量获取商品信息
     * 
     * @param productIds 商品ID列表
     * @return 商品列表
     */
    R<List<MerchantProduct>> getProductsBatch(List<Long> productIds);
}