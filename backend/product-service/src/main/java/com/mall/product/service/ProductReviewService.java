package com.mall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.product.domain.entity.ProductReview;

import java.util.Map;

/**
 * 商品评价服务接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
public interface ProductReviewService {
    
    /**
     * 获取商品评价统计
     * @param productId 商品ID
     * @return 统计信息
     */
    Map<String, Object> getReviewStatistics(Long productId);
    
    /**
     * 分页查询商品评价
     * @param productId 商品ID
     * @param page 页码
     * @param size 每页数量
     * @param ratingType 评价类型
     * @return 评价分页列表
     */
    IPage<ProductReview> getReviewPage(Long productId, int page, int size, String ratingType);
    
    /**
     * 添加商品评价
     * @param review 评价信息
     * @return 是否成功
     */
    boolean addReview(ProductReview review);
    
    /**
     * 检查用户是否已评价
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param productId 商品ID
     * @return 是否已评价
     */
    boolean hasReviewed(Long userId, Long orderId, Long productId);
    
    /**
     * 商家回复评价
     * @param reviewId 评价ID
     * @param merchantId 商家ID
     * @param reply 回复内容
     * @return 是否成功
     */
    boolean merchantReply(Long reviewId, Long merchantId, String reply);
    
    /**
     * 点赞评价
     * @param reviewId 评价ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean likeReview(Long reviewId, Long userId);
}
