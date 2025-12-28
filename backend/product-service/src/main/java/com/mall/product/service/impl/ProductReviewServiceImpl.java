package com.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.product.domain.entity.ProductReview;
import com.mall.product.domain.entity.ReviewLike;
import com.mall.product.mapper.ProductReviewMapper;
import com.mall.product.mapper.ReviewLikeMapper;
import com.mall.product.service.ProductReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品评价服务实现
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Service
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> 
        implements ProductReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductReviewServiceImpl.class);
    
    @Autowired
    private ReviewLikeMapper reviewLikeMapper;
    
    @Override
    public Map<String, Object> getReviewStatistics(Long productId) {
        logger.info("获取商品评价统计 - productId: {}", productId);
        
        Map<String, Object> stats = baseMapper.getReviewStatistics(productId);
        if (stats == null) {
            stats = new HashMap<>();
        }
        
        // 确保所有字段都有默认值，处理null情况
        stats.putIfAbsent("totalCount", 0L);
        stats.putIfAbsent("avgRating", 0.0);
        stats.putIfAbsent("avgDescriptionRating", 0.0);
        stats.putIfAbsent("avgServiceRating", 0.0);
        stats.putIfAbsent("avgLogisticsRating", 0.0);
        stats.putIfAbsent("goodCount", 0L);
        stats.putIfAbsent("mediumCount", 0L);
        stats.putIfAbsent("badCount", 0L);
        stats.putIfAbsent("withImageCount", 0L);
        
        // 处理可能为null的值
        Object totalCountObj = stats.get("totalCount");
        Object goodCountObj = stats.get("goodCount");
        
        Long totalCount = totalCountObj != null ? ((Number) totalCountObj).longValue() : 0L;
        Long goodCount = goodCountObj != null ? ((Number) goodCountObj).longValue() : 0L;
        
        // 计算好评率
        double goodRate = totalCount > 0 ? (goodCount * 100.0 / totalCount) : 100.0;
        stats.put("goodRate", Math.round(goodRate * 10) / 10.0);
        
        return stats;
    }
    
    @Override
    public IPage<ProductReview> getReviewPage(Long productId, int page, int size, String ratingType) {
        logger.info("分页查询商品评价 - productId: {}, page: {}, size: {}, ratingType: {}", 
                productId, page, size, ratingType);
        
        Page<ProductReview> pageParam = new Page<>(page, size);
        return baseMapper.selectReviewPage(pageParam, productId, ratingType);
    }
    
    @Override
    @Transactional
    public boolean addReview(ProductReview review) {
        logger.info("添加商品评价 - productId: {}, userId: {}, orderId: {}", 
                review.getProductId(), review.getUserId(), review.getOrderId());
        
        // 检查是否已评价
        if (hasReviewed(review.getUserId(), review.getOrderId(), review.getProductId())) {
            logger.warn("用户已评价该商品 - userId: {}, orderId: {}, productId: {}", 
                    review.getUserId(), review.getOrderId(), review.getProductId());
            return false;
        }
        
        // 设置默认值
        if (review.getRating() == null) {
            review.setRating(5);
        }
        if (review.getDescriptionRating() == null) {
            review.setDescriptionRating(review.getRating());
        }
        if (review.getServiceRating() == null) {
            review.setServiceRating(review.getRating());
        }
        if (review.getLogisticsRating() == null) {
            review.setLogisticsRating(review.getRating());
        }
        if (review.getAnonymous() == null) {
            review.setAnonymous(0);
        }
        if (review.getLikeCount() == null) {
            review.setLikeCount(0);
        }
        review.setStatus(1); // 直接发布
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        review.setDeleted(0);
        
        // 匿名处理
        if (review.getAnonymous() == 1) {
            review.setUserName("匿名用户");
            review.setUserAvatar(null);
        }
        
        return save(review);
    }
    
    @Override
    public boolean hasReviewed(Long userId, Long orderId, Long productId) {
        return baseMapper.checkUserReviewed(userId, orderId, productId) > 0;
    }
    
    @Override
    @Transactional
    public boolean merchantReply(Long reviewId, Long merchantId, String reply) {
        logger.info("商家回复评价 - reviewId: {}, merchantId: {}", reviewId, merchantId);
        
        LambdaUpdateWrapper<ProductReview> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductReview::getId, reviewId)
                .set(ProductReview::getMerchantReply, reply)
                .set(ProductReview::getMerchantReplyTime, LocalDateTime.now())
                .set(ProductReview::getUpdateTime, LocalDateTime.now());
        
        return update(updateWrapper);
    }
    
    @Override
    @Transactional
    public boolean likeReview(Long reviewId, Long userId) {
        logger.info("点赞评价 - reviewId: {}, userId: {}", reviewId, userId);
        
        // 检查用户是否已点赞（需要userId）
        if (userId != null) {
            int liked = reviewLikeMapper.checkUserLiked(reviewId, userId);
            if (liked > 0) {
                logger.warn("用户已点赞该评价 - reviewId: {}, userId: {}", reviewId, userId);
                throw new RuntimeException("您已点赞过该评价");
            }
            
            // 记录点赞
            ReviewLike reviewLike = new ReviewLike();
            reviewLike.setReviewId(reviewId);
            reviewLike.setUserId(userId);
            reviewLike.setCreateTime(LocalDateTime.now());
            reviewLikeMapper.insert(reviewLike);
        }
        
        // 增加点赞数
        LambdaUpdateWrapper<ProductReview> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ProductReview::getId, reviewId)
                .setSql("like_count = like_count + 1");
        
        return update(updateWrapper);
    }
}
