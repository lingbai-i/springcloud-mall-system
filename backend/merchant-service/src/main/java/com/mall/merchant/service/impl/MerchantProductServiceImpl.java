package com.mall.merchant.service.impl;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.client.ProductClient;
import com.mall.merchant.domain.entity.MerchantProduct;
import com.mall.merchant.repository.MerchantProductRepository;
import com.mall.merchant.service.MerchantProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 商家商品服务实现类
 * 重构版本：通过 ProductClient 调用 product-service 进行商品管理
 * 商品数据统一存储在 product-service，merchant-service 仅负责商家相关的业务逻辑
 * 
 * @author lingbai
 * @version 3.0
 * @since 2025-01-27
 * 修改日志：
 * V3.0 2025-12-01：完全重构为调用 product-service，移除本地数据库操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantProductServiceImpl implements MerchantProductService {

    /**
     * 商品服务 Feign 客户端
     * 用于调用 product-service 获取商品数据
     */
    @Autowired
    private ProductClient productClient;
    
    /**
     * 商家商品仓库（保留用于兼容性，后续可移除）
     */
    @Autowired(required = false)
    private MerchantProductRepository productRepository;

    /**
     * 添加商品
     * 调用 product-service 创建商品
     * 
     * @param merchantId 商家ID
     * @param product 商品信息
     * @return 添加结果
     */
    @Override
    @Transactional
    public R<Void> addProduct(Long merchantId, MerchantProduct product) {
        log.info("添加商品，商家ID：{}，商品名称：{}", merchantId, product.getProductName());

        try {
            // 构建商品数据Map，调用 product-service
            Map<String, Object> productData = convertToProductMap(merchantId, product);
            
            R<String> result = productClient.createProduct(productData);
            
            if (result != null && result.isSuccess()) {
                log.info("添加商品成功，商家ID：{}，商品名称：{}", merchantId, product.getProductName());
                return R.ok();
            } else {
                String errorMsg = result != null ? result.getMessage() : "调用商品服务失败";
                log.error("添加商品失败，商家ID：{}，错误信息：{}", merchantId, errorMsg);
                return R.fail(errorMsg);
            }
        } catch (Exception e) {
            log.error("添加商品失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("添加商品失败，请稍后重试");
        }
    }

    /**
     * 更新商品信息
     * 先验证商品归属，再调用 product-service 更新
     * 
     * @param merchantId 商家ID
     * @param product 商品信息
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> updateProduct(Long merchantId, MerchantProduct product) {
        log.info("更新商品信息，商品ID：{}，商家ID：{}", product.getId(), merchantId);

        try {
            // 验证商品归属
            R<Boolean> ownershipResult = checkProductOwnership(product.getId(), merchantId);
            if (!ownershipResult.isSuccess() || !Boolean.TRUE.equals(ownershipResult.getData())) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", product.getId(), merchantId);
                return R.fail("无权限操作该商品");
            }

            // 构建更新数据
            Map<String, Object> productData = convertToProductMap(merchantId, product);
            productData.put("id", product.getId());
            
            R<String> result = productClient.updateProduct(productData);
            
            if (result != null && result.isSuccess()) {
                log.info("更新商品信息成功，商品ID：{}", product.getId());
                return R.ok();
            } else {
                String errorMsg = result != null ? result.getMessage() : "调用商品服务失败";
                log.error("更新商品信息失败，商品ID：{}，错误信息：{}", product.getId(), errorMsg);
                return R.fail(errorMsg);
            }
        } catch (Exception e) {
            log.error("更新商品信息失败，商品ID：{}，错误信息：{}", product.getId(), e.getMessage(), e);
            return R.fail("更新商品失败，请稍后重试");
        }
    }

    /**
     * 删除商品
     * 先验证商品归属，再调用 product-service 删除
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public R<Void> deleteProduct(Long merchantId, Long productId) {
        log.info("删除商品，商品ID：{}，商家ID：{}", productId, merchantId);

        try {
            // 验证商品归属
            R<Boolean> ownershipResult = checkProductOwnership(productId, merchantId);
            if (!ownershipResult.isSuccess() || !Boolean.TRUE.equals(ownershipResult.getData())) {
                log.warn("商品不存在或不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("商品不存在或无权限操作");
            }

            R<String> result = productClient.deleteProduct(productId);
            
            if (result != null && result.isSuccess()) {
                log.info("删除商品成功，商品ID：{}", productId);
                return R.ok();
            } else {
                String errorMsg = result != null ? result.getMessage() : "调用商品服务失败";
                log.error("删除商品失败，商品ID：{}，错误信息：{}", productId, errorMsg);
                return R.fail(errorMsg);
            }
        } catch (Exception e) {
            log.error("删除商品失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("删除商品失败，请稍后重试");
        }
    }


    /**
     * 根据ID获取商品详情
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @return 商品详情
     */
    @Override
    public R<MerchantProduct> getProductById(Long merchantId, Long productId) {
        log.debug("获取商品详情，商品ID：{}，商家ID：{}", productId, merchantId);

        try {
            R<Map<String, Object>> result = productClient.getProductById(productId);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                Map<String, Object> productData = result.getData();
                
                // 验证商品归属
                Long productMerchantId = getLongValue(productData, "merchantId");
                if (merchantId != null && !merchantId.equals(productMerchantId)) {
                    log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                    return R.fail("无权限访问该商品");
                }
                
                MerchantProduct product = convertToMerchantProduct(productData);
                return R.ok(product);
            } else {
                log.warn("商品不存在，ID：{}", productId);
                return R.fail("商品不存在");
            }
        } catch (Exception e) {
            log.error("获取商品详情失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("获取商品详情失败");
        }
    }

    /**
     * 分页查询商品列表
     * 调用 product-service 按商家ID筛选
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @param productName 商品名称（可选）
     * @param categoryId 分类ID（可选）
     * @param brand 品牌（可选）
     * @param status 商品状态（可选）
     * @param minPrice 最低价格（可选）
     * @param maxPrice 最高价格（可选）
     * @return 商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getProductList(Long merchantId, Integer page, Integer size,
            String productName, Long categoryId, String brand, Integer status,
            BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("分页查询商品列表，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);

        try {
            R<Object> result = productClient.getProductsByMerchantId(merchantId, (long) page, (long) size);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                return convertToPageResult(result.getData());
            } else {
                log.error("查询商品列表失败，商家ID：{}", merchantId);
                return R.fail("查询商品列表失败");
            }
        } catch (Exception e) {
            log.error("分页查询商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("查询商品列表失败");
        }
    }

    /**
     * 批量获取商品信息
     * 
     * @param productIds 商品ID列表
     * @return 商品列表
     */
    @Override
    public R<List<MerchantProduct>> getProductsBatch(List<Long> productIds) {
        log.debug("批量获取商品信息，商品数量：{}", productIds.size());
        
        try {
            List<MerchantProduct> products = new ArrayList<>();
            // 逐个获取商品信息（后续可优化为批量接口）
            for (Long productId : productIds) {
                R<Map<String, Object>> result = productClient.getProductById(productId);
                if (result != null && result.isSuccess() && result.getData() != null) {
                    products.add(convertToMerchantProduct(result.getData()));
                }
            }
            return R.ok(products);
        } catch (Exception e) {
            log.error("批量获取商品信息失败，错误信息：{}", e.getMessage(), e);
            return R.fail("获取商品信息失败");
        }
    }

    /**
     * 上架商品
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @return 上架结果
     */
    @Override
    @Transactional
    public R<Void> onlineProduct(Long merchantId, Long productId) {
        log.info("上架商品，商品ID：{}，商家ID：{}", productId, merchantId);

        try {
            // 验证商品归属
            R<Boolean> ownershipResult = checkProductOwnership(productId, merchantId);
            if (!ownershipResult.isSuccess() || !Boolean.TRUE.equals(ownershipResult.getData())) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            R<String> result = productClient.updateProductStatus(productId, 1);
            
            if (result != null && result.isSuccess()) {
                log.info("上架商品成功，商品ID：{}", productId);
                return R.ok();
            } else {
                String errorMsg = result != null ? result.getMessage() : "调用商品服务失败";
                log.error("上架商品失败，商品ID：{}，错误信息：{}", productId, errorMsg);
                return R.fail(errorMsg);
            }
        } catch (Exception e) {
            log.error("上架商品失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("上架商品失败，请稍后重试");
        }
    }

    /**
     * 下架商品
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @return 下架结果
     */
    @Override
    @Transactional
    public R<Void> offlineProduct(Long merchantId, Long productId) {
        log.info("下架商品，商品ID：{}，商家ID：{}", productId, merchantId);

        try {
            // 验证商品归属
            R<Boolean> ownershipResult = checkProductOwnership(productId, merchantId);
            if (!ownershipResult.isSuccess() || !Boolean.TRUE.equals(ownershipResult.getData())) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            R<String> result = productClient.updateProductStatus(productId, 0);
            
            if (result != null && result.isSuccess()) {
                log.info("下架商品成功，商品ID：{}", productId);
                return R.ok();
            } else {
                String errorMsg = result != null ? result.getMessage() : "调用商品服务失败";
                log.error("下架商品失败，商品ID：{}，错误信息：{}", productId, errorMsg);
                return R.fail(errorMsg);
            }
        } catch (Exception e) {
            log.error("下架商品失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("下架商品失败，请稍后重试");
        }
    }

    /**
     * 批量上架商品
     * 
     * @param merchantId 商家ID
     * @param productIds 商品ID列表
     * @return 批量上架结果
     */
    @Override
    @Transactional
    public R<Void> batchOnlineProducts(Long merchantId, List<Long> productIds) {
        log.info("批量上架商品，商家ID：{}，商品数量：{}", merchantId, productIds.size());

        try {
            int successCount = 0;
            for (Long productId : productIds) {
                R<Void> result = onlineProduct(merchantId, productId);
                if (result.isSuccess()) {
                    successCount++;
                }
            }

            if (successCount == productIds.size()) {
                log.info("批量上架商品成功，商家ID：{}，数量：{}", merchantId, successCount);
                return R.ok();
            } else {
                log.warn("部分商品上架失败，预期：{}，实际：{}", productIds.size(), successCount);
                return R.fail("部分商品上架失败");
            }
        } catch (Exception e) {
            log.error("批量上架商品失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("批量上架失败，请稍后重试");
        }
    }

    /**
     * 批量下架商品
     * 
     * @param merchantId 商家ID
     * @param productIds 商品ID列表
     * @return 批量下架结果
     */
    @Override
    @Transactional
    public R<Void> batchOfflineProducts(Long merchantId, List<Long> productIds) {
        log.info("批量下架商品，商家ID：{}，商品数量：{}", merchantId, productIds.size());

        try {
            int successCount = 0;
            for (Long productId : productIds) {
                R<Void> result = offlineProduct(merchantId, productId);
                if (result.isSuccess()) {
                    successCount++;
                }
            }

            log.info("批量下架商品成功，商家ID：{}，数量：{}", merchantId, successCount);
            return R.ok();
        } catch (Exception e) {
            log.error("批量下架商品失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("批量下架失败，请稍后重试");
        }
    }

    /**
     * 批量删除商品
     * 
     * @param merchantId 商家ID
     * @param productIds 商品ID列表
     * @return 批量删除结果
     */
    @Override
    @Transactional
    public R<Void> batchDeleteProducts(Long merchantId, List<Long> productIds) {
        log.info("批量删除商品，商家ID：{}，商品数量：{}", merchantId, productIds.size());

        try {
            int successCount = 0;
            for (Long productId : productIds) {
                R<Void> result = deleteProduct(merchantId, productId);
                if (result.isSuccess()) {
                    successCount++;
                }
            }

            if (successCount == productIds.size()) {
                log.info("批量删除商品成功，商家ID：{}，数量：{}", merchantId, successCount);
                return R.ok();
            } else {
                log.warn("部分商品删除失败，预期：{}，实际：{}", productIds.size(), successCount);
                return R.fail("部分商品删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除商品失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("批量删除失败，请稍后重试");
        }
    }


    /**
     * 更新商品库存
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param quantity 库存数量
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> updateStock(Long merchantId, Long productId, Integer quantity) {
        log.info("更新商品库存，商品ID：{}，商家ID：{}，库存：{}", productId, merchantId, quantity);

        try {
            // 验证商品归属
            R<Boolean> ownershipResult = checkProductOwnership(productId, merchantId);
            if (!ownershipResult.isSuccess() || !Boolean.TRUE.equals(ownershipResult.getData())) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            R<String> result = productClient.updateStock(productId, quantity);
            
            if (result != null && result.isSuccess()) {
                log.info("更新商品库存成功，商品ID：{}", productId);
                return R.ok();
            } else {
                String errorMsg = result != null ? result.getMessage() : "调用商品服务失败";
                log.error("更新商品库存失败，商品ID：{}，错误信息：{}", productId, errorMsg);
                return R.fail(errorMsg);
            }
        } catch (Exception e) {
            log.error("更新商品库存失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("更新库存失败，请稍后重试");
        }
    }

    /**
     * 更新商品价格
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param price 价格
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> updatePrice(Long merchantId, Long productId, BigDecimal price) {
        log.info("更新商品价格，商品ID：{}，商家ID：{}，价格：{}", productId, merchantId, price);

        try {
            // 验证商品归属
            R<Boolean> ownershipResult = checkProductOwnership(productId, merchantId);
            if (!ownershipResult.isSuccess() || !Boolean.TRUE.equals(ownershipResult.getData())) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            R<String> result = productClient.updateProductPrice(productId, price.doubleValue(), "商家更新价格", merchantId);
            
            if (result != null && result.isSuccess()) {
                log.info("更新商品价格成功，商品ID：{}", productId);
                return R.ok();
            } else {
                String errorMsg = result != null ? result.getMessage() : "调用商品服务失败";
                log.error("更新商品价格失败，商品ID：{}，错误信息：{}", productId, errorMsg);
                return R.fail(errorMsg);
            }
        } catch (Exception e) {
            log.error("更新商品价格失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("更新价格失败，请稍后重试");
        }
    }

    /**
     * 设置商品推荐状态
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param isRecommend 是否推荐
     * @return 设置结果
     */
    @Override
    @Transactional
    public R<Void> setRecommendStatus(Long merchantId, Long productId, Boolean isRecommend) {
        log.info("设置商品推荐状态，商品ID：{}，商家ID：{}，推荐：{}", productId, merchantId, isRecommend);

        try {
            // 验证商品归属
            R<Boolean> ownershipResult = checkProductOwnership(productId, merchantId);
            if (!ownershipResult.isSuccess() || !Boolean.TRUE.equals(ownershipResult.getData())) {
                return R.fail("无权限操作该商品");
            }

            // 获取商品信息并更新
            R<Map<String, Object>> productResult = productClient.getProductById(productId);
            if (productResult == null || !productResult.isSuccess()) {
                return R.fail("商品不存在");
            }

            Map<String, Object> productData = productResult.getData();
            productData.put("isRecommend", isRecommend ? 1 : 0);
            
            R<String> result = productClient.updateProduct(productData);
            
            if (result != null && result.isSuccess()) {
                log.info("设置商品推荐状态成功，商品ID：{}", productId);
                return R.ok();
            } else {
                return R.fail("设置推荐状态失败");
            }
        } catch (Exception e) {
            log.error("设置商品推荐状态失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("设置推荐状态失败，请稍后重试");
        }
    }

    /**
     * 设置商品新品状态
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param isNew 是否新品
     * @return 设置结果
     */
    @Override
    @Transactional
    public R<Void> setNewStatus(Long merchantId, Long productId, Boolean isNew) {
        log.info("设置商品新品状态，商品ID：{}，商家ID：{}，新品：{}", productId, merchantId, isNew);

        try {
            // 验证商品归属
            R<Boolean> ownershipResult = checkProductOwnership(productId, merchantId);
            if (!ownershipResult.isSuccess() || !Boolean.TRUE.equals(ownershipResult.getData())) {
                return R.fail("无权限操作该商品");
            }

            R<Map<String, Object>> productResult = productClient.getProductById(productId);
            if (productResult == null || !productResult.isSuccess()) {
                return R.fail("商品不存在");
            }

            Map<String, Object> productData = productResult.getData();
            productData.put("isNew", isNew ? 1 : 0);
            
            R<String> result = productClient.updateProduct(productData);
            
            if (result != null && result.isSuccess()) {
                log.info("设置商品新品状态成功，商品ID：{}", productId);
                return R.ok();
            } else {
                return R.fail("设置新品状态失败");
            }
        } catch (Exception e) {
            log.error("设置商品新品状态失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("设置新品状态失败，请稍后重试");
        }
    }

    /**
     * 设置商品热销状态
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param isHot 是否热销
     * @return 设置结果
     */
    @Override
    @Transactional
    public R<Void> setHotStatus(Long merchantId, Long productId, Boolean isHot) {
        log.info("设置商品热销状态，商品ID：{}，商家ID：{}，热销：{}", productId, merchantId, isHot);

        try {
            // 验证商品归属
            R<Boolean> ownershipResult = checkProductOwnership(productId, merchantId);
            if (!ownershipResult.isSuccess() || !Boolean.TRUE.equals(ownershipResult.getData())) {
                return R.fail("无权限操作该商品");
            }

            R<Map<String, Object>> productResult = productClient.getProductById(productId);
            if (productResult == null || !productResult.isSuccess()) {
                return R.fail("商品不存在");
            }

            Map<String, Object> productData = productResult.getData();
            productData.put("isHot", isHot ? 1 : 0);
            
            R<String> result = productClient.updateProduct(productData);
            
            if (result != null && result.isSuccess()) {
                log.info("设置商品热销状态成功，商品ID：{}", productId);
                return R.ok();
            } else {
                return R.fail("设置热销状态失败");
            }
        } catch (Exception e) {
            log.error("设置商品热销状态失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("设置热销状态失败，请稍后重试");
        }
    }

    /**
     * 增加商品浏览量
     * 
     * @param productId 商品ID
     * @return 增加结果
     */
    @Override
    @Transactional
    public R<Void> increaseViewCount(Long productId) {
        log.debug("增加商品浏览量，商品ID：{}", productId);
        // 浏览量统计可以在本地处理或调用 product-service
        // 暂时返回成功
        return R.ok();
    }

    /**
     * 增加商品收藏数
     * 
     * @param productId 商品ID
     * @return 增加结果
     */
    @Override
    @Transactional
    public R<Void> increaseFavoriteCount(Long productId) {
        log.debug("增加商品收藏数，商品ID：{}", productId);
        // 收藏数统计可以在本地处理或调用 product-service
        return R.ok();
    }

    /**
     * 减少商品收藏数
     * 
     * @param productId 商品ID
     * @return 减少结果
     */
    @Override
    @Transactional
    public R<Void> decreaseFavoriteCount(Long productId) {
        log.debug("减少商品收藏数，商品ID：{}", productId);
        return R.ok();
    }

    /**
     * 增加商品销售数量
     * 
     * @param merchantId 商家ID
     * @param productId 商品ID
     * @param quantity 销售数量
     * @return 增加结果
     */
    @Override
    @Transactional
    public R<Void> increaseSalesCount(Long merchantId, Long productId, Integer quantity) {
        log.debug("增加商品销售数量，商家ID：{}，商品ID：{}，数量：{}", merchantId, productId, quantity);
        // 销量统计由 product-service 在订单完成时更新
        return R.ok();
    }


    /**
     * 获取商品统计数据
     * 调用 product-service 获取统计数据
     * 
     * @param merchantId 商家ID
     * @return 统计数据
     */
    @Override
    public R<Map<String, Object>> getProductStatistics(Long merchantId) {
        log.debug("获取商品统计数据，商家ID：{}", merchantId);

        try {
            R<Map<String, Object>> result = productClient.getStatistics(merchantId);
            
            if (result != null && result.isSuccess()) {
                return R.ok(result.getData());
            } else {
                log.error("获取商品统计数据失败，商家ID：{}", merchantId);
                return R.fail("获取统计数据失败");
            }
        } catch (Exception e) {
            log.error("获取商品统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取统计数据失败");
        }
    }

    /**
     * 导出商品数据
     * 
     * @param merchantId 商家ID
     * @param productName 商品名称（可选）
     * @param categoryId 分类ID（可选）
     * @param brand 品牌（可选）
     * @param status 商品状态（可选）
     * @return 导出数据
     */
    @Override
    public R<byte[]> exportProductData(Long merchantId, String productName, Long categoryId, String brand,
            Integer status) {
        log.info("导出商品数据，商家ID：{}", merchantId);
        // 导出功能暂时返回空数据
        return R.ok(new byte[0]);
    }

    /**
     * 检查商品是否属于指定商家
     * 调用 product-service 验证商品归属
     * 
     * @param productId 商品ID
     * @param merchantId 商家ID
     * @return 是否属于该商家
     */
    @Override
    public R<Boolean> checkProductOwnership(Long productId, Long merchantId) {
        log.debug("检查商品归属，商品ID：{}，商家ID：{}", productId, merchantId);

        try {
            R<Boolean> result = productClient.checkProductOwnership(productId, merchantId);
            
            if (result != null && result.isSuccess()) {
                return R.ok(result.getData());
            } else {
                // 如果调用失败，尝试通过获取商品信息来验证
                R<Map<String, Object>> productResult = productClient.getProductById(productId);
                if (productResult != null && productResult.isSuccess() && productResult.getData() != null) {
                    Long productMerchantId = getLongValue(productResult.getData(), "merchantId");
                    return R.ok(merchantId.equals(productMerchantId));
                }
                return R.ok(false);
            }
        } catch (Exception e) {
            log.error("检查商品归属失败，商品ID：{}，商家ID：{}，错误信息：{}", productId, merchantId, e.getMessage(), e);
            return R.fail("检查商品归属失败");
        }
    }

    /**
     * 复制商品
     * 
     * @param merchantId 商家ID
     * @param productId 原商品ID
     * @return 复制结果
     */
    @Override
    @Transactional
    public R<Void> copyProduct(Long merchantId, Long productId) {
        log.info("复制商品，商家ID：{}，原商品ID：{}", merchantId, productId);

        try {
            // 验证商品归属
            R<Boolean> ownershipResult = checkProductOwnership(productId, merchantId);
            if (!ownershipResult.isSuccess() || !Boolean.TRUE.equals(ownershipResult.getData())) {
                return R.fail("无权限操作该商品");
            }

            // 获取原商品信息
            R<Map<String, Object>> productResult = productClient.getProductById(productId);
            if (productResult == null || !productResult.isSuccess() || productResult.getData() == null) {
                return R.fail("原商品不存在");
            }

            // 创建新商品
            Map<String, Object> newProductData = new HashMap<>(productResult.getData());
            newProductData.remove("id");
            newProductData.put("name", newProductData.get("name") + " - 副本");
            newProductData.put("status", 0); // 默认下架
            newProductData.put("sales", 0);
            
            R<String> result = productClient.createProduct(newProductData);
            
            if (result != null && result.isSuccess()) {
                log.info("复制商品成功，商家ID：{}，原商品ID：{}", merchantId, productId);
                return R.ok();
            } else {
                return R.fail("复制商品失败");
            }
        } catch (Exception e) {
            log.error("复制商品失败，商家ID：{}，原商品ID：{}，错误信息：{}", merchantId, productId, e.getMessage(), e);
            return R.fail("复制商品失败，请稍后重试");
        }
    }

    /**
     * 批量更新商品库存
     * 
     * @param merchantId 商家ID
     * @param stockUpdates 库存更新列表
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> batchUpdateStock(Long merchantId, Map<Long, Integer> stockUpdates) {
        log.info("批量更新商品库存，商家ID：{}，更新数量：{}", merchantId, stockUpdates.size());

        try {
            for (Map.Entry<Long, Integer> entry : stockUpdates.entrySet()) {
                R<Void> result = updateStock(merchantId, entry.getKey(), entry.getValue());
                if (!result.isSuccess()) {
                    return R.fail("批量更新库存失败：" + result.getMessage());
                }
            }
            return R.ok();
        } catch (Exception e) {
            log.error("批量更新商品库存失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("批量更新库存失败，请稍后重试");
        }
    }

    /**
     * 批量更新商品价格
     * 
     * @param merchantId 商家ID
     * @param priceUpdates 价格更新列表
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> batchUpdatePrice(Long merchantId, Map<Long, BigDecimal> priceUpdates) {
        log.info("批量更新商品价格，商家ID：{}，更新数量：{}", merchantId, priceUpdates.size());

        try {
            for (Map.Entry<Long, BigDecimal> entry : priceUpdates.entrySet()) {
                R<Void> result = updatePrice(merchantId, entry.getKey(), entry.getValue());
                if (!result.isSuccess()) {
                    return R.fail("批量更新价格失败：" + result.getMessage());
                }
            }
            return R.ok();
        } catch (Exception e) {
            log.error("批量更新商品价格失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("批量更新价格失败，请稍后重试");
        }
    }

    /**
     * 获取上架商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 上架商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getOnlineProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取上架商品列表，商家ID：{}，页码：{}，每页大小：{}", merchantId, page, size);
        // 调用商品列表接口，筛选状态为上架的商品
        return getProductList(merchantId, page, size, null, null, null, 1, null, null);
    }

    /**
     * 获取下架商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 下架商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getOfflineProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取下架商品列表，商家ID：{}，页码：{}，每页大小：{}", merchantId, page, size);
        return getProductList(merchantId, page, size, null, null, null, 0, null, null);
    }

    /**
     * 获取库存不足商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @param lowStockThreshold 库存不足阈值
     * @return 库存不足商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getLowStockProducts(Long merchantId, Integer page, Integer size,
            Integer lowStockThreshold) {
        log.debug("获取库存不足商品列表，商家ID：{}，页码：{}，每页大小：{}，阈值：{}", merchantId, page, size, lowStockThreshold);

        try {
            R<List<Map<String, Object>>> result = productClient.getStockWarningProductsByMerchant(merchantId);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                List<MerchantProduct> products = new ArrayList<>();
                for (Map<String, Object> data : result.getData()) {
                    products.add(convertToMerchantProduct(data));
                }
                
                PageResult<MerchantProduct> pageResult = new PageResult<>();
                pageResult.setRecords(products);
                pageResult.setTotal((long) products.size());
                pageResult.setCurrent((long) page);
                pageResult.setSize((long) size);
                pageResult.setPages(1L);
                
                return R.ok(pageResult);
            } else {
                return R.fail("获取库存不足商品列表失败");
            }
        } catch (Exception e) {
            log.error("获取库存不足商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取库存不足商品列表失败");
        }
    }

    /**
     * 获取热销商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 热销商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getHotProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取热销商品列表，商家ID：{}，页码：{}，每页大小：{}", merchantId, page, size);

        try {
            R<List<Map<String, Object>>> result = productClient.getHotProductsByMerchant(merchantId, size);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                List<MerchantProduct> products = new ArrayList<>();
                for (Map<String, Object> data : result.getData()) {
                    products.add(convertToMerchantProduct(data));
                }
                
                PageResult<MerchantProduct> pageResult = new PageResult<>();
                pageResult.setRecords(products);
                pageResult.setTotal((long) products.size());
                pageResult.setCurrent((long) page);
                pageResult.setSize((long) size);
                pageResult.setPages(1L);
                
                return R.ok(pageResult);
            } else {
                return R.fail("获取热销商品列表失败");
            }
        } catch (Exception e) {
            log.error("获取热销商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取热销商品列表失败");
        }
    }

    /**
     * 获取推荐商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 推荐商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getRecommendProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取推荐商品列表，商家ID：{}，页码：{}，每页大小：{}", merchantId, page, size);
        return getProductList(merchantId, page, size, null, null, null, null, null, null);
    }

    /**
     * 获取新品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 新品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getNewProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取新品列表，商家ID：{}，页码：{}，每页大小：{}", merchantId, page, size);
        return getProductList(merchantId, page, size, null, null, null, null, null, null);
    }

    /**
     * 获取热销商品列表
     * 
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 热销商品列表
     */
    @Override
    public R<List<MerchantProduct>> getHotSellingProducts(Long merchantId, Integer limit) {
        log.debug("获取热销商品列表，商家ID：{}，限制：{}", merchantId, limit);

        try {
            R<List<Map<String, Object>>> result = productClient.getHotProductsByMerchant(merchantId, limit);
            
            if (result != null && result.isSuccess() && result.getData() != null) {
                List<MerchantProduct> products = new ArrayList<>();
                for (Map<String, Object> data : result.getData()) {
                    products.add(convertToMerchantProduct(data));
                }
                return R.ok(products);
            } else {
                return R.fail("获取热销商品列表失败");
            }
        } catch (Exception e) {
            log.error("获取热销商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取热销商品列表失败");
        }
    }

    /**
     * 获取推荐商品列表
     * 
     * @param merchantId 商家ID
     * @param page 页码
     * @param size 每页大小
     * @return 推荐商品列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getRecommendedProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取推荐商品列表，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);
        return getRecommendProducts(merchantId, page, size);
    }


    // ==================== 私有辅助方法 ====================

    /**
     * 将 MerchantProduct 转换为 Map 用于调用 product-service
     * 
     * @param merchantId 商家ID
     * @param product 商品实体
     * @return 商品数据Map
     */
    private Map<String, Object> convertToProductMap(Long merchantId, MerchantProduct product) {
        Map<String, Object> map = new HashMap<>();
        map.put("merchantId", merchantId);
        map.put("name", product.getProductName());
        map.put("description", product.getDescription());
        map.put("price", product.getPrice());
        map.put("originalPrice", product.getMarketPrice());
        map.put("costPrice", product.getCostPrice());
        map.put("stock", product.getStockQuantity());
        map.put("stockWarning", product.getWarningStock());
        map.put("sales", product.getSalesCount() != null ? product.getSalesCount() : 0);
        map.put("status", product.getStatus() != null ? product.getStatus() : 0);
        map.put("isRecommend", product.getIsRecommended() != null ? product.getIsRecommended() : 0);
        map.put("isNew", product.getIsNew() != null ? product.getIsNew() : 0);
        map.put("isHot", product.getIsHot() != null ? product.getIsHot() : 0);
        map.put("categoryId", product.getCategoryId());
        map.put("brandName", product.getBrand());
        map.put("mainImage", product.getMainImage());
        map.put("detailImages", product.getImages());
        map.put("sortOrder", product.getSortOrder() != null ? product.getSortOrder() : 0);
        return map;
    }

    /**
     * 将 product-service 返回的 Map 转换为 MerchantProduct
     * 
     * @param data 商品数据Map
     * @return MerchantProduct 实体
     */
    private MerchantProduct convertToMerchantProduct(Map<String, Object> data) {
        MerchantProduct product = new MerchantProduct();
        product.setId(getLongValue(data, "id"));
        product.setMerchantId(getLongValue(data, "merchantId"));
        product.setProductName(getStringValue(data, "name"));
        product.setDescription(getStringValue(data, "description"));
        product.setPrice(getBigDecimalValue(data, "price"));
        product.setMarketPrice(getBigDecimalValue(data, "originalPrice"));
        product.setCostPrice(getBigDecimalValue(data, "costPrice"));
        product.setStockQuantity(getIntValue(data, "stock"));
        product.setWarningStock(getIntValue(data, "stockWarning"));
        product.setSalesCount(getIntValue(data, "sales"));
        product.setStatus(getIntValue(data, "status"));
        product.setIsRecommended(getIntValue(data, "isRecommend"));
        product.setIsNew(getIntValue(data, "isNew"));
        product.setIsHot(getIntValue(data, "isHot"));
        product.setCategoryId(getLongValue(data, "categoryId"));
        product.setBrand(getStringValue(data, "brandName"));
        product.setMainImage(getStringValue(data, "mainImage"));
        product.setImages(getStringValue(data, "detailImages"));
        product.setSortOrder(getIntValue(data, "sortOrder"));
        return product;
    }

    /**
     * 将 product-service 返回的分页数据转换为 PageResult
     * 
     * @param data 分页数据
     * @return PageResult
     */
    @SuppressWarnings("unchecked")
    private R<PageResult<MerchantProduct>> convertToPageResult(Object data) {
        try {
            if (data instanceof Map) {
                Map<String, Object> pageData = (Map<String, Object>) data;
                List<Map<String, Object>> records = (List<Map<String, Object>>) pageData.get("records");
                
                List<MerchantProduct> products = new ArrayList<>();
                if (records != null) {
                    for (Map<String, Object> record : records) {
                        products.add(convertToMerchantProduct(record));
                    }
                }
                
                PageResult<MerchantProduct> pageResult = new PageResult<>();
                pageResult.setRecords(products);
                pageResult.setTotal(getLongValue(pageData, "total"));
                pageResult.setCurrent(getLongValue(pageData, "current"));
                pageResult.setSize(getLongValue(pageData, "size"));
                pageResult.setPages(getLongValue(pageData, "pages"));
                
                return R.ok(pageResult);
            }
            return R.fail("数据格式错误");
        } catch (Exception e) {
            log.error("转换分页数据失败：{}", e.getMessage(), e);
            return R.fail("数据转换失败");
        }
    }

    /**
     * 从 Map 中获取 Long 值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从 Map 中获取 Integer 值
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从 Map 中获取 String 值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 从 Map 中获取 BigDecimal 值
     */
    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value);
        }
        if (value instanceof Integer) {
            return BigDecimal.valueOf((Integer) value);
        }
        if (value instanceof Long) {
            return BigDecimal.valueOf((Long) value);
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
