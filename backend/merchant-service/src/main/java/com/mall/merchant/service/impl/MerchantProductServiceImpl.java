package com.mall.merchant.service.impl;

import com.mall.common.core.domain.PageResult;
import com.mall.common.core.domain.R;
import com.mall.merchant.domain.entity.MerchantProduct;
import com.mall.merchant.repository.MerchantProductRepository;
import com.mall.merchant.service.MerchantProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 商家商品服务实现类
 * 实现商家商品相关的业务逻辑处理
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantProductServiceImpl implements MerchantProductService {

    private static final Logger log = LoggerFactory.getLogger(MerchantProductServiceImpl.class);

    private final MerchantProductRepository productRepository;

    /**
     * 添加商品
     * 验证商品信息并保存到数据库
     * 
     * @param product 商品信息
     * @return 添加结果
     */
    @Override
    @Transactional
    public R<Void> addProduct(Long merchantId, MerchantProduct product) {
        log.info("添加商品，商家ID：{}，商品名称：{}", merchantId, product.getProductName());

        // 设置商家ID
        product.setMerchantId(merchantId);

        try {
            // 设置默认值
            product.setStatus(product.getStatus() != null ? product.getStatus() : 0); // 默认下架状态
            product.setSalesCount(0);
            product.setViewCount(0);
            product.setFavoriteCount(0);
            product.setReviewCount(0); // 评价数量
            product.setRating(BigDecimal.ZERO);
            product.setIsRecommended(product.getIsRecommended() != null ? product.getIsRecommended() : 0);
            product.setIsNew(product.getIsNew() != null ? product.getIsNew() : 1);
            product.setIsHot(0);
            product.setSortOrder(0);

            // 确保库存预警值不为null
            if (product.getWarningStock() == null) {
                product.setWarningStock(10);
            }

            // 保存商品
            MerchantProduct savedProduct = productRepository.save(product);

            log.info("添加商品成功，商家ID：{}，商品ID：{}", merchantId, savedProduct.getId());
            return R.ok();

        } catch (Exception e) {
            log.error("添加商品失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("添加商品失败，请稍后重试");
        }
    }

    /**
     * 更新商品信息
     * 
     * @param product 商品信息
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> updateProduct(Long merchantId, MerchantProduct product) {
        log.info("更新商品信息，商品ID：{}", product.getId());

        try {
            Optional<MerchantProduct> existingProductOpt = productRepository.findById(product.getId());
            if (!existingProductOpt.isPresent()) {
                log.warn("商品不存在，ID：{}", product.getId());
                return R.fail("商品不存在");
            }

            MerchantProduct existingProduct = existingProductOpt.get();

            // 验证商品是否属于该商家
            if (!existingProduct.getMerchantId().equals(merchantId)) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", product.getId(), merchantId);
                return R.fail("无权限操作该商品");
            }

            // 更新允许修改的字段
            if (StringUtils.hasText(product.getProductName())) {
                existingProduct.setProductName(product.getProductName());
            }
            if (product.getCategoryId() != null) {
                existingProduct.setCategoryId(product.getCategoryId());
            }
            if (StringUtils.hasText(product.getBrand())) {
                existingProduct.setBrand(product.getBrand());
            }
            if (product.getPrice() != null) {
                existingProduct.setPrice(product.getPrice());
            }
            if (product.getMarketPrice() != null) {
                existingProduct.setMarketPrice(product.getMarketPrice());
            }
            if (product.getStockQuantity() != null) {
                existingProduct.setStockQuantity(product.getStockQuantity());
            }
            if (StringUtils.hasText(product.getMainImage())) {
                existingProduct.setMainImage(product.getMainImage());
            }
            if (StringUtils.hasText(product.getImages())) {
                existingProduct.setImages(product.getImages());
            }
            if (StringUtils.hasText(product.getDescription())) {
                existingProduct.setDescription(product.getDescription());
            }
            if (StringUtils.hasText(product.getSpecifications())) {
                existingProduct.setSpecifications(product.getSpecifications());
            }
            if (StringUtils.hasText(product.getAttributes())) {
                existingProduct.setAttributes(product.getAttributes());
            }
            if (product.getWeight() != null) {
                existingProduct.setWeight(product.getWeight());
            }
            if (StringUtils.hasText(product.getDimensions())) {
                existingProduct.setDimensions(product.getDimensions());
            }
            if (StringUtils.hasText(product.getSeoKeywords())) {
                existingProduct.setSeoKeywords(product.getSeoKeywords());
            }
            if (StringUtils.hasText(product.getSeoDescription())) {
                existingProduct.setSeoDescription(product.getSeoDescription());
            }

            productRepository.save(existingProduct);

            log.info("更新商品信息成功，商品ID：{}", product.getId());
            return R.ok();

        } catch (Exception e) {
            log.error("更新商品信息失败，商品ID：{}，错误信息：{}", product.getId(), e.getMessage(), e);
            return R.fail("更新商品失败，请稍后重试");
        }
    }

    /**
     * 删除商品
     * 
     * @param productId  商品ID
     * @param merchantId 商家ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public R<Void> deleteProduct(Long merchantId, Long productId) {
        log.info("删除商品，商品ID：{}，商家ID：{}", productId, merchantId);

        try {
            // 检查商品是否属于该商家
            if (!productRepository.existsByIdAndMerchantId(productId, merchantId)) {
                log.warn("商品不存在或不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("商品不存在或无权限操作");
            }

            productRepository.deleteById(productId);

            log.info("删除商品成功，商品ID：{}", productId);
            return R.ok();

        } catch (Exception e) {
            log.error("删除商品失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("删除商品失败，请稍后重试");
        }
    }

    /**
     * 根据ID获取商品详情
     * 
     * @param productId  商品ID
     * @param merchantId 商家ID（可选，用于权限验证）
     * @return 商品详情
     */
    @Override
    public R<MerchantProduct> getProductById(Long merchantId, Long productId) {
        log.debug("获取商品详情，商品ID：{}，商家ID：{}", productId, merchantId);

        try {
            Optional<MerchantProduct> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                log.warn("商品不存在，ID：{}", productId);
                return R.fail("商品不存在");
            }

            MerchantProduct product = productOpt.get();

            // 如果指定了商家ID，验证商品是否属于该商家
            if (merchantId != null && !product.getMerchantId().equals(merchantId)) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限访问该商品");
            }

            return R.ok(product);

        } catch (Exception e) {
            log.error("获取商品详情失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("获取商品详情失败");
        }
    }

    /**
     * 分页查询商品列表
     * 
     * @param merchantId  商家ID
     * @param page        页码
     * @param size        每页大小
     * @param productName 商品名称（可选）
     * @param categoryId  分类ID（可选）
     * @param status      商品状态（可选）
     * @return 商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getProductList(Long merchantId, Integer page, Integer size,
            String productName, Long categoryId, String brand, Integer status,
            BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("分页查询商品列表，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);

        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
            // 品牌字段使用字符串 brand，与实体保持一致
            Page<MerchantProduct> productPage = productRepository.findByConditions(
                    merchantId, productName, categoryId, brand, status, pageable);

            PageResult<MerchantProduct> result = PageResult.of(productPage.getContent(), productPage.getTotalElements(),
                    (long) page, (long) size);
            return R.ok(result);

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
            List<MerchantProduct> products = productRepository.findAllById(productIds);
            return R.ok(products);
        } catch (Exception e) {
            log.error("批量获取商品信息失败，错误信息：{}", e.getMessage(), e);
            return R.fail("获取商品信息失败");
        }
    }

    /**
     * 上架商品
     * 
     * @param productId  商品ID
     * @param merchantId 商家ID
     * @return 上架结果
     */
    @Override
    @Transactional
    public R<Void> onlineProduct(Long merchantId, Long productId) {
        log.info("上架商品，商品ID：{}，商家ID：{}", productId, merchantId);

        try {
            Optional<MerchantProduct> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                log.warn("商品不存在，ID：{}", productId);
                return R.fail("商品不存在");
            }

            MerchantProduct product = productOpt.get();

            // 验证商品是否属于该商家
            if (!product.getMerchantId().equals(merchantId)) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            // 检查商品信息是否完整
            if (!StringUtils.hasText(product.getProductName()) ||
                    product.getPrice() == null ||
                    product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
                return R.fail("商品信息不完整，无法上架");
            }

            product.setStatus(1); // 上架状态
            productRepository.save(product);

            log.info("上架商品成功，商品ID：{}", productId);
            return R.ok();

        } catch (Exception e) {
            log.error("上架商品失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("上架商品失败，请稍后重试");
        }
    }

    /**
     * 下架商品
     * 
     * @param productId  商品ID
     * @param merchantId 商家ID
     * @return 下架结果
     */
    @Override
    @Transactional
    public R<Void> offlineProduct(Long merchantId, Long productId) {
        log.info("下架商品，商品ID：{}，商家ID：{}", productId, merchantId);

        try {
            Optional<MerchantProduct> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                log.warn("商品不存在，ID：{}", productId);
                return R.fail("商品不存在");
            }

            MerchantProduct product = productOpt.get();

            // 验证商品是否属于该商家
            if (!product.getMerchantId().equals(merchantId)) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            product.setStatus(0); // 默认下架状态
            product.setSalesCount(0);
            product.setViewCount(0);
            product.setFavoriteCount(0);
            // product.setCommentCount(0); // 该字段可能不存在
            product.setRating(BigDecimal.ZERO);
            product.setIsRecommended(0);
            product.setIsNew(1);
            product.setIsHot(0);
            product.setSortOrder(0);

            productRepository.save(product);

            log.info("下架商品成功，商品ID：{}", productId);
            return R.ok();

        } catch (Exception e) {
            log.error("下架商品失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("下架商品失败，请稍后重试");
        }
    }

    /**
     * 批量上架商品
     * 
     * @param productIds 商品ID列表
     * @param merchantId 商家ID
     * @return 批量上架结果
     */
    @Override
    @Transactional
    public R<Void> batchOnlineProducts(Long merchantId, List<Long> productIds) {
        log.info("批量上架商品，商家ID：{}，商品数量：{}", merchantId, productIds.size());

        try {
            int updatedCount = productRepository.batchUpdateStatus(productIds, 1, merchantId);

            if (updatedCount != productIds.size()) {
                log.warn("部分商品上架失败，预期：{}，实际：{}", productIds.size(), updatedCount);
                return R.fail("部分商品上架失败，请检查商品信息");
            }

            log.info("批量上架商品成功，商家ID：{}，数量：{}", merchantId, updatedCount);
            return R.ok();

        } catch (Exception e) {
            log.error("批量上架商品失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("批量上架失败，请稍后重试");
        }
    }

    /**
     * 批量下架商品
     * 
     * @param productIds 商品ID列表
     * @param merchantId 商家ID
     * @return 批量下架结果
     */
    @Override
    @Transactional
    public R<Void> batchOfflineProducts(Long merchantId, List<Long> productIds) {
        log.info("批量下架商品，商家ID：{}，商品数量：{}", merchantId, productIds.size());

        try {
            int updatedCount = productRepository.batchUpdateStatus(productIds, 0, merchantId);

            log.info("批量下架商品成功，商家ID：{}，数量：{}", merchantId, updatedCount);
            return R.ok();

        } catch (Exception e) {
            log.error("批量下架商品失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("批量下架失败，请稍后重试");
        }
    }

    /**
     * 批量删除商品
     * 
     * @param productIds 商品ID列表
     * @param merchantId 商家ID
     * @return 批量删除结果
     */
    @Override
    @Transactional
    public R<Void> batchDeleteProducts(Long merchantId, List<Long> productIds) {
        log.info("批量删除商品，商家ID：{}，商品数量：{}", merchantId, productIds.size());

        try {
            // 验证所有商品都属于该商家
            for (Long productId : productIds) {
                if (!productRepository.existsByIdAndMerchantId(productId, merchantId)) {
                    log.warn("商品不存在或不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                    return R.fail("部分商品不存在或无权限操作");
                }
            }

            productRepository.deleteAllById(productIds);

            log.info("批量删除商品成功，商家ID：{}，数量：{}", merchantId, productIds.size());
            return R.ok();

        } catch (Exception e) {
            log.error("批量删除商品失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("批量删除失败，请稍后重试");
        }
    }

    /**
     * 更新商品库存
     * 
     * @param productId  商品ID
     * @param merchantId 商家ID
     * @param stock      库存数量
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> updateStock(Long merchantId, Long productId, Integer quantity) {
        log.info("更新商品库存，商品ID：{}，商家ID：{}，库存：{}", productId, merchantId, quantity);

        try {
            // 获取商品并更新库存
            Optional<MerchantProduct> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                log.warn("商品不存在，ID：{}", productId);
                return R.fail("商品不存在");
            }

            MerchantProduct product = productOpt.get();

            // 验证商品是否属于该商家
            if (!product.getMerchantId().equals(merchantId)) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            product.setStockQuantity(quantity);
            productRepository.save(product);

            log.info("更新商品库存成功，商品ID：{}", productId);
            return R.ok();

        } catch (Exception e) {
            log.error("更新商品库存失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("更新库存失败，请稍后重试");
        }
    }

    /**
     * 更新商品价格
     * 
     * @param productId     商品ID
     * @param merchantId    商家ID
     * @param price         价格
     * @param originalPrice 原价（可选）
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> updatePrice(Long merchantId, Long productId, BigDecimal price) {
        log.info("更新商品价格，商品ID：{}，商家ID：{}，价格：{}", productId, merchantId, price);

        try {
            Optional<MerchantProduct> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                log.warn("商品不存在，ID：{}", productId);
                return R.fail("商品不存在");
            }

            MerchantProduct product = productOpt.get();

            // 验证商品是否属于该商家
            if (!product.getMerchantId().equals(merchantId)) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            product.setPrice(price);

            productRepository.save(product);

            log.info("更新商品价格成功，商品ID：{}", productId);
            return R.ok();

        } catch (Exception e) {
            log.error("更新商品价格失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("更新价格失败，请稍后重试");
        }
    }

    /**
     * 设置商品推荐状态
     * 
     * @param productId     商品ID
     * @param merchantId    商家ID
     * @param isRecommended 是否推荐
     * @return 设置结果
     */
    @Override
    @Transactional
    public R<Void> setRecommendStatus(Long merchantId, Long productId, Boolean isRecommend) {
        log.info("设置商品推荐状态，商品ID：{}，商家ID：{}，推荐：{}", productId, merchantId, isRecommend);

        try {
            Optional<MerchantProduct> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                log.warn("商品不存在，ID：{}", productId);
                return R.fail("商品不存在");
            }

            MerchantProduct product = productOpt.get();

            // 验证商品是否属于该商家
            if (!product.getMerchantId().equals(merchantId)) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            product.setIsRecommended(isRecommend ? 1 : 0);
            productRepository.save(product);

            log.info("设置商品推荐状态成功，商品ID：{}", productId);
            return R.ok();

        } catch (Exception e) {
            log.error("设置商品推荐状态失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("设置推荐状态失败，请稍后重试");
        }
    }

    /**
     * 设置商品新品状态
     * 
     * @param productId  商品ID
     * @param merchantId 商家ID
     * @param isNew      是否新品
     * @return 设置结果
     */
    @Override
    @Transactional
    public R<Void> setNewStatus(Long merchantId, Long productId, Boolean isNew) {
        log.info("设置商品新品状态，商品ID：{}，商家ID：{}，新品：{}", productId, merchantId, isNew);

        try {
            Optional<MerchantProduct> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                log.warn("商品不存在，ID：{}", productId);
                return R.fail("商品不存在");
            }

            MerchantProduct product = productOpt.get();

            // 验证商品是否属于该商家
            if (!product.getMerchantId().equals(merchantId)) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            product.setIsNew(isNew ? 1 : 0);
            productRepository.save(product);

            log.info("设置商品新品状态成功，商品ID：{}", productId);
            return R.ok();

        } catch (Exception e) {
            log.error("设置商品新品状态失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("设置新品状态失败，请稍后重试");
        }
    }

    /**
     * 设置商品热销状态
     * 
     * @param productId  商品ID
     * @param merchantId 商家ID
     * @param isHot      是否热销
     * @return 设置结果
     */
    @Override
    @Transactional
    public R<Void> setHotStatus(Long merchantId, Long productId, Boolean isHot) {
        log.info("设置商品热销状态，商品ID：{}，商家ID：{}，热销：{}", productId, merchantId, isHot);

        try {
            Optional<MerchantProduct> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                log.warn("商品不存在，ID：{}", productId);
                return R.fail("商品不存在");
            }

            MerchantProduct product = productOpt.get();

            // 验证商品是否属于该商家
            if (!product.getMerchantId().equals(merchantId)) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            product.setIsHot(isHot ? 1 : 0);
            productRepository.save(product);

            log.info("设置商品热销状态成功，商品ID：{}", productId);
            return R.ok();

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

        try {
            productRepository.increaseViewCount(productId, 1);
            return R.ok();

        } catch (Exception e) {
            log.error("增加商品浏览量失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("增加浏览量失败");
        }
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

        try {
            productRepository.increaseFavoriteCount(productId);
            return R.ok();

        } catch (Exception e) {
            log.error("增加商品收藏数失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("增加收藏数失败");
        }
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

        try {
            productRepository.decreaseFavoriteCount(productId);
            return R.ok();

        } catch (Exception e) {
            log.error("减少商品收藏数失败，商品ID：{}，错误信息：{}", productId, e.getMessage(), e);
            return R.fail("减少收藏数失败");
        }
    }

    /**
     * 增加商品销售数量
     * 
     * @param merchantId 商家ID
     * @param productId  商品ID
     * @param quantity   销售数量
     * @return 增加结果
     */
    @Override
    @Transactional
    public R<Void> increaseSalesCount(Long merchantId, Long productId, Integer quantity) {
        log.debug("增加商品销售数量，商家ID：{}，商品ID：{}，数量：{}", merchantId, productId, quantity);

        try {
            // 验证商品是否属于该商家
            if (!productRepository.existsByIdAndMerchantId(productId, merchantId)) {
                return R.fail("商品不存在或不属于该商家");
            }

            productRepository.increaseSalesCount(productId, quantity, merchantId);
            return R.ok();

        } catch (Exception e) {
            log.error("增加商品销售数量失败，商家ID：{}，商品ID：{}，错误信息：{}", merchantId, productId, e.getMessage(), e);
            return R.fail("增加销售数量失败");
        }
    }

    /**
     * 获取商品统计数据
     * 
     * @param merchantId 商家ID
     * @return 统计数据
     */
    @Override
    public R<Map<String, Object>> getProductStatistics(Long merchantId) {
        log.debug("获取商品统计数据，商家ID：{}", merchantId);

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总商品数
            Long totalProducts = productRepository.countByMerchantId(merchantId);
            statistics.put("totalProducts", totalProducts);

            // 上架商品数
            Long publishedProducts = productRepository.countByMerchantIdAndStatus(merchantId, 1);
            statistics.put("publishedProducts", publishedProducts);

            // 下架商品数
            Long unpublishedProducts = productRepository.countByMerchantIdAndStatus(merchantId, 0);
            statistics.put("unpublishedProducts", unpublishedProducts);

            // 库存不足商品数（库存小于10）
            Long lowStockProducts = productRepository.countLowStockProducts(merchantId, 10);
            statistics.put("lowStockProducts", lowStockProducts);

            // 今日新增商品数
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime todayEnd = todayStart.plusDays(1);
            Long todayNewProducts = productRepository.countNewProducts(merchantId, todayStart, todayEnd);
            statistics.put("todayNewProducts", todayNewProducts);

            // 推荐商品数 - 使用现有的查询方法
            Long recommendedProducts = productRepository.countByMerchantIdAndIsRecommendedAndStatus(merchantId, 1, 1);
            statistics.put("recommendedProducts", recommendedProducts);

            // 新品数 - 使用现有的查询方法
            Long newProducts = productRepository.countByMerchantIdAndIsNewAndStatus(merchantId, 1, 1);
            statistics.put("newProducts", newProducts);

            // 热销商品数 - 使用现有的查询方法
            Long hotProducts = productRepository.countByMerchantIdAndIsHotAndStatus(merchantId, 1, 1);
            statistics.put("hotProducts", hotProducts);

            return R.ok(statistics);

        } catch (Exception e) {
            log.error("获取商品统计数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取统计数据失败");
        }
    }

    /**
     * 导出商品数据
     * 
     * @param merchantId  商家ID
     * @param productName 商品名称（可选）
     * @param categoryId  分类ID（可选）
     * @param status      商品状态（可选）
     * @return 导出数据
     */
    @Override
    public R<byte[]> exportProductData(Long merchantId, String productName, Long categoryId, String brand,
            Integer status) {
        // 品牌参数改为字符串，避免与实体不一致
        log.info("导出商品数据，商家ID：{}，商品名称：{}，分类ID：{}，品牌：{}，状态：{}",
                merchantId, productName, categoryId, brand, status);

        try {
            // 这里应该实现Excel导出逻辑
            // 暂时返回空数据
            return R.ok(new byte[0]);

        } catch (Exception e) {
            log.error("导出商品数据失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("导出失败，请稍后重试");
        }
    }

    /**
     * 检查商品是否属于指定商家
     * 
     * @param productId  商品ID
     * @param merchantId 商家ID
     * @return 是否属于
     */
    @Override
    public R<Boolean> checkProductOwnership(Long productId, Long merchantId) {
        log.debug("检查商品归属，商品ID：{}，商家ID：{}", productId, merchantId);

        try {
            boolean exists = productRepository.existsByIdAndMerchantId(productId, merchantId);
            return R.ok(exists);

        } catch (Exception e) {
            log.error("检查商品归属失败，商品ID：{}，商家ID：{}，错误信息：{}", productId, merchantId, e.getMessage(), e);
            return R.fail("检查商品归属失败");
        }
    }

    /**
     * 复制商品
     * 
     * @param merchantId 商家ID
     * @param productId  原商品ID
     * @return 复制结果
     */
    @Override
    @Transactional
    public R<Void> copyProduct(Long merchantId, Long productId) {
        log.info("复制商品，商家ID：{}，原商品ID：{}", merchantId, productId);

        try {
            Optional<MerchantProduct> originalProductOpt = productRepository.findById(productId);
            if (!originalProductOpt.isPresent()) {
                log.warn("原商品不存在，ID：{}", productId);
                return R.fail("原商品不存在");
            }

            MerchantProduct originalProduct = originalProductOpt.get();

            // 验证商品是否属于该商家
            if (!originalProduct.getMerchantId().equals(merchantId)) {
                log.warn("商品不属于该商家，商品ID：{}，商家ID：{}", productId, merchantId);
                return R.fail("无权限操作该商品");
            }

            // 创建新商品
            MerchantProduct newProduct = new MerchantProduct();
            newProduct.setMerchantId(merchantId);
            newProduct.setProductName(originalProduct.getProductName() + " - 副本");
            newProduct.setCategoryId(originalProduct.getCategoryId());
            newProduct.setBrand(originalProduct.getBrand());
            newProduct.setPrice(originalProduct.getPrice());
            newProduct.setMarketPrice(originalProduct.getMarketPrice());
            newProduct.setStockQuantity(originalProduct.getStockQuantity());
            newProduct.setMainImage(originalProduct.getMainImage());
            newProduct.setImages(originalProduct.getImages());
            newProduct.setDescription(originalProduct.getDescription());
            newProduct.setSpecifications(originalProduct.getSpecifications());
            newProduct.setAttributes(originalProduct.getAttributes());
            newProduct.setWeight(originalProduct.getWeight());
            newProduct.setDimensions(originalProduct.getDimensions());
            newProduct.setSeoKeywords(originalProduct.getSeoKeywords());
            newProduct.setSeoDescription(originalProduct.getSeoDescription());

            // 设置默认值
            newProduct.setStatus(0); // 默认下架状态
            newProduct.setSalesCount(0);
            newProduct.setViewCount(0);
            newProduct.setFavoriteCount(0);
            newProduct.setRating(BigDecimal.ZERO);
            newProduct.setIsRecommended(0);
            newProduct.setIsNew(1);
            newProduct.setIsHot(0);
            newProduct.setSortOrder(0);

            // 保存新商品
            MerchantProduct savedProduct = productRepository.save(newProduct);

            log.info("复制商品成功，商家ID：{}，新商品ID：{}", merchantId, savedProduct.getId());
            return R.ok();

        } catch (Exception e) {
            log.error("复制商品失败，商家ID：{}，原商品ID：{}，错误信息：{}", merchantId, productId, e.getMessage(), e);
            return R.fail("复制商品失败，请稍后重试");
        }
    }

    /**
     * 批量更新商品库存
     * 
     * @param merchantId   商家ID
     * @param stockUpdates 库存更新列表
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> batchUpdateStock(Long merchantId, Map<Long, Integer> stockUpdates) {
        log.info("批量更新商品库存，商家ID：{}，更新数量：{}", merchantId, stockUpdates.size());

        try {
            for (Map.Entry<Long, Integer> entry : stockUpdates.entrySet()) {
                Long productId = entry.getKey();
                Integer quantity = entry.getValue();

                // 调用单个更新方法
                R<Void> result = updateStock(merchantId, productId, quantity);
                if (!result.isSuccess()) {
                    log.warn("批量更新库存失败，商品ID：{}，错误信息：{}", productId, result.getMessage());
                    return R.fail("批量更新库存失败：" + result.getMessage());
                }
            }

            log.info("批量更新商品库存成功，商家ID：{}", merchantId);
            return R.ok();

        } catch (Exception e) {
            log.error("批量更新商品库存失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("批量更新库存失败，请稍后重试");
        }
    }

    /**
     * 批量更新商品价格
     * 
     * @param merchantId   商家ID
     * @param priceUpdates 价格更新列表
     * @return 更新结果
     */
    @Override
    @Transactional
    public R<Void> batchUpdatePrice(Long merchantId, Map<Long, BigDecimal> priceUpdates) {
        log.info("批量更新商品价格，商家ID：{}，更新数量：{}", merchantId, priceUpdates.size());

        try {
            for (Map.Entry<Long, BigDecimal> entry : priceUpdates.entrySet()) {
                Long productId = entry.getKey();
                BigDecimal price = entry.getValue();

                // 调用单个更新方法
                R<Void> result = updatePrice(merchantId, productId, price);
                if (!result.isSuccess()) {
                    log.warn("批量更新价格失败，商品ID：{}，错误信息：{}", productId, result.getMessage());
                    return R.fail("批量更新价格失败：" + result.getMessage());
                }
            }

            log.info("批量更新商品价格成功，商家ID：{}", merchantId);
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
     * @param page       页码
     * @param size       每页大小
     * @return 上架商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getOnlineProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取上架商品列表，商家ID：{}，页码：{}，每页大小：{}", merchantId, page, size);

        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updateTime"));
            Page<MerchantProduct> productPage = productRepository.findByMerchantIdAndStatus(merchantId, 1, pageable);

            PageResult<MerchantProduct> pageResult = PageResult.of(
                    productPage.getContent(),
                    productPage.getTotalElements(),
                    (long) page,
                    (long) size);

            return R.ok(pageResult);

        } catch (Exception e) {
            log.error("获取上架商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取上架商品列表失败");
        }
    }

    /**
     * 获取下架商品列表
     * 
     * @param merchantId 商家ID
     * @param page       页码
     * @param size       每页大小
     * @return 下架商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getOfflineProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取下架商品列表，商家ID：{}，页码：{}，每页大小：{}", merchantId, page, size);

        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updateTime"));
            Page<MerchantProduct> productPage = productRepository.findByMerchantIdAndStatus(merchantId, 0, pageable);

            PageResult<MerchantProduct> pageResult = PageResult.of(
                    productPage.getContent(),
                    productPage.getTotalElements(),
                    (long) page,
                    (long) size);

            return R.ok(pageResult);

        } catch (Exception e) {
            log.error("获取下架商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取下架商品列表失败");
        }
    }

    /**
     * 获取库存不足商品列表
     * 
     * @param merchantId        商家ID
     * @param page              页码
     * @param size              每页大小
     * @param lowStockThreshold 库存不足阈值
     * @return 库存不足商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getLowStockProducts(Long merchantId, Integer page, Integer size,
            Integer lowStockThreshold) {
        log.debug("获取库存不足商品列表，商家ID：{}，页码：{}，每页大小：{}，阈值：{}", merchantId, page, size, lowStockThreshold);

        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "stockQuantity"));
            Page<MerchantProduct> productPage = productRepository.findByMerchantIdAndStockQuantityLessThan(merchantId,
                    lowStockThreshold, pageable);

            PageResult<MerchantProduct> pageResult = PageResult.of(
                    productPage.getContent(),
                    productPage.getTotalElements(),
                    (long) page,
                    (long) size);

            return R.ok(pageResult);

        } catch (Exception e) {
            log.error("获取库存不足商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取库存不足商品列表失败");
        }
    }

    /**
     * 获取热销商品列表
     * 
     * @param merchantId 商家ID
     * @param page       页码
     * @param size       每页大小
     * @return 热销商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getHotProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取热销商品列表，商家ID：{}，页码：{}，每页大小：{}", merchantId, page, size);

        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "salesCount"));
            Page<MerchantProduct> productPage = productRepository.findByMerchantIdAndIsHot(merchantId, 1, pageable);

            PageResult<MerchantProduct> pageResult = PageResult.of(
                    productPage.getContent(),
                    productPage.getTotalElements(),
                    (long) page,
                    (long) size);

            return R.ok(pageResult);

        } catch (Exception e) {
            log.error("获取热销商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取热销商品列表失败");
        }
    }

    /**
     * 获取推荐商品列表
     * 
     * @param merchantId 商家ID
     * @param page       页码
     * @param size       每页大小
     * @return 推荐商品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getRecommendProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取推荐商品列表，商家ID：{}，页码：{}，每页大小：{}", merchantId, page, size);

        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "updateTime"));
            Page<MerchantProduct> productPage = productRepository.findByMerchantIdAndIsRecommended(merchantId, 1,
                    pageable);

            PageResult<MerchantProduct> pageResult = PageResult.of(
                    productPage.getContent(),
                    productPage.getTotalElements(),
                    (long) page,
                    (long) size);

            return R.ok(pageResult);

        } catch (Exception e) {
            log.error("获取推荐商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取推荐商品列表失败");
        }
    }

    /**
     * 获取新品列表
     * 
     * @param merchantId 商家ID
     * @param page       页码
     * @param size       每页大小
     * @return 新品分页列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getNewProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取新品列表，商家ID：{}，页码：{}，每页大小：{}", merchantId, page, size);

        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<MerchantProduct> productPage = productRepository.findByMerchantIdAndIsNew(merchantId, 1, pageable);

            PageResult<MerchantProduct> pageResult = PageResult.of(
                    productPage.getContent(),
                    productPage.getTotalElements(),
                    (long) page,
                    (long) size);

            return R.ok(pageResult);

        } catch (Exception e) {
            log.error("获取新品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取新品列表失败");
        }
    }

    /**
     * 获取热销商品列表
     * 
     * @param merchantId 商家ID（可为null，表示获取所有商家的热销商品）
     * @param limit      数量限制
     * @return 热销商品列表
     */
    @Override
    public R<List<MerchantProduct>> getHotSellingProducts(Long merchantId, Integer limit) {
        log.debug("获取热销商品列表，商家ID：{}，限制：{}", merchantId, limit);

        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "salesCount"));
            Page<MerchantProduct> page;

            if (merchantId == null) {
                // 获取所有商家的热销商品（仅上架状态）
                page = productRepository.findByStatus(1, pageable);
            } else {
                // 获取指定商家的热销商品
                page = productRepository.findByMerchantIdAndStatus(merchantId, 1, pageable);
            }

            return R.ok(page.getContent());

        } catch (Exception e) {
            log.error("获取热销商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取热销商品列表失败");
        }
    }

    /**
     * 获取推荐商品列表
     * 
     * @param merchantId 商家ID（可为null，表示获取所有商家的推荐商品）
     * @param page       页码
     * @param size       每页大小
     * @return 推荐商品列表
     */
    @Override
    public R<PageResult<MerchantProduct>> getRecommendedProducts(Long merchantId, Integer page, Integer size) {
        log.debug("获取推荐商品列表，商家ID：{}，页码：{}，大小：{}", merchantId, page, size);

        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<MerchantProduct> productPage;

            if (merchantId == null) {
                // 获取所有商家的推荐商品（仅上架状态）
                productPage = productRepository.findByStatusAndIsRecommended(1, 1, pageable);
            } else {
                // 获取指定商家的推荐商品
                productPage = productRepository.findByMerchantIdAndIsRecommended(merchantId, 1, pageable);
            }

            PageResult<MerchantProduct> result = new PageResult<>();
            result.setRecords(productPage.getContent());
            result.setTotal(productPage.getTotalElements());
            result.setSize((long) size);
            result.setCurrent((long) page);
            result.setPages((long) productPage.getTotalPages());

            return R.ok(result);

        } catch (Exception e) {
            log.error("获取推荐商品列表失败，商家ID：{}，错误信息：{}", merchantId, e.getMessage(), e);
            return R.fail("获取推荐商品列表失败");
        }
    }
}