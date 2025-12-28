package com.mall.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.common.core.domain.R;
import com.mall.product.domain.entity.ProductReview;
import com.mall.product.service.ProductReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品评价控制器
 * 
 * @author lingbai
 * @version 2.0
 * @since 2025-12-28
 */
@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ProductReviewService productReviewService;

    /**
     * 获取商品评价列表（含统计信息）
     * 
     * @param id 商品ID
     * @param page 页码
     * @param size 每页数量
     * @param ratingType 评价类型：all-全部, good-好评, medium-中评, bad-差评, withImage-有图
     * @return 评价列表和统计信息
     */
    @GetMapping("/{id}/reviews")
    public R<Map<String, Object>> getProductReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String ratingType) {
        logger.info("获取商品评价 - 商品ID: {}, page: {}, size: {}, ratingType: {}", id, page, size, ratingType);
        
        // 获取统计信息
        Map<String, Object> statistics = productReviewService.getReviewStatistics(id);
        
        // 获取评价列表
        IPage<ProductReview> reviewPage = productReviewService.getReviewPage(id, page, size, ratingType);
        
        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("statistics", statistics);
        result.put("list", reviewPage.getRecords());
        result.put("total", reviewPage.getTotal());
        result.put("page", reviewPage.getCurrent());
        result.put("size", reviewPage.getSize());
        result.put("pages", reviewPage.getPages());
        
        return R.ok(result);
    }

    /**
     * 添加商品评价
     * 
     * @param id 商品ID
     * @param review 评价信息
     * @return 操作结果
     */
    @PostMapping("/{id}/reviews")
    public R<Boolean> addReview(
            @PathVariable Long id,
            @RequestBody ProductReview review,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        logger.info("添加商品评价 - 商品ID: {}, userId: {}", id, userId);
        
        // 设置商品ID和用户信息
        review.setProductId(id);
        if (userId != null) {
            review.setUserId(userId);
        }
        if (userName != null && review.getUserName() == null) {
            review.setUserName(userName);
        }
        
        boolean success = productReviewService.addReview(review);
        if (success) {
            return R.ok(true);
        } else {
            return R.fail("评价失败，可能已经评价过该商品");
        }
    }

    /**
     * 商家回复评价
     * 
     * @param reviewId 评价ID
     * @param request 回复内容
     * @return 操作结果
     */
    @PostMapping("/reviews/{reviewId}/reply")
    public R<Boolean> merchantReply(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId) {
        logger.info("商家回复评价 - reviewId: {}, merchantId: {}", reviewId, merchantId);
        
        String reply = request.get("reply");
        if (reply == null || reply.trim().isEmpty()) {
            return R.fail("回复内容不能为空");
        }
        
        boolean success = productReviewService.merchantReply(reviewId, merchantId, reply);
        return success ? R.ok(true) : R.fail("回复失败");
    }

    /**
     * 点赞评价
     * 
     * @param reviewId 评价ID
     * @param userIdParam 用户ID（请求参数）
     * @param userIdHeader 用户ID（请求头）
     * @return 操作结果
     */
    @PostMapping("/reviews/{reviewId}/like")
    public R<Boolean> likeReview(
            @PathVariable Long reviewId,
            @RequestParam(value = "userId", required = false) Long userIdParam,
            @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
        Long userId = userIdParam != null ? userIdParam : userIdHeader;
        logger.info("点赞评价 - reviewId: {}, userId: {}", reviewId, userId);
        
        try {
            boolean success = productReviewService.likeReview(reviewId, userId);
            return success ? R.ok(true) : R.fail("点赞失败");
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    /**
     * 检查用户是否已评价
     * 
     * @param id 商品ID
     * @param orderId 订单ID
     * @param userIdParam 用户ID（请求参数）
     * @param userIdHeader 用户ID（请求头）
     * @return 是否已评价
     */
    @GetMapping("/{id}/reviews/check")
    public R<Boolean> checkReviewed(
            @PathVariable Long id,
            @RequestParam Long orderId,
            @RequestParam(value = "userId", required = false) Long userIdParam,
            @RequestHeader(value = "X-User-Id", required = false) Long userIdHeader) {
        // 优先使用请求参数中的userId，其次使用请求头中的userId
        Long userId = userIdParam != null ? userIdParam : userIdHeader;
        logger.info("检查是否已评价 - productId: {}, orderId: {}, userId: {}", id, orderId, userId);
        
        if (userId == null) {
            // 如果没有userId，默认返回未评价，让前端可以继续操作
            logger.warn("未提供用户ID，默认返回未评价状态");
            return R.ok(false);
        }
        
        boolean hasReviewed = productReviewService.hasReviewed(userId, orderId, id);
        return R.ok(hasReviewed);
    }
}
