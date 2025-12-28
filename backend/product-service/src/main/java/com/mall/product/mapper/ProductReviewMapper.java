package com.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.product.domain.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 商品评价Mapper
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {
    
    /**
     * 获取商品评价统计
     * @param productId 商品ID
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "COALESCE(AVG(rating), 0) as avgRating, " +
            "COALESCE(AVG(description_rating), 0) as avgDescriptionRating, " +
            "COALESCE(AVG(service_rating), 0) as avgServiceRating, " +
            "COALESCE(AVG(logistics_rating), 0) as avgLogisticsRating, " +
            "SUM(CASE WHEN rating >= 4 THEN 1 ELSE 0 END) as goodCount, " +
            "SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END) as mediumCount, " +
            "SUM(CASE WHEN rating <= 2 THEN 1 ELSE 0 END) as badCount, " +
            "SUM(CASE WHEN images IS NOT NULL AND images != '' THEN 1 ELSE 0 END) as withImageCount " +
            "FROM product_review WHERE product_id = #{productId} AND status = 1 AND deleted = 0")
    Map<String, Object> getReviewStatistics(@Param("productId") Long productId);
    
    /**
     * 分页查询商品评价
     * @param page 分页参数
     * @param productId 商品ID
     * @param ratingType 评价类型：all-全部, good-好评, medium-中评, bad-差评, withImage-有图
     * @return 评价列表
     */
    @Select("<script>" +
            "SELECT * FROM product_review " +
            "WHERE product_id = #{productId} AND status = 1 AND deleted = 0 " +
            "<if test=\"ratingType == 'good'\">AND rating >= 4</if>" +
            "<if test=\"ratingType == 'medium'\">AND rating = 3</if>" +
            "<if test=\"ratingType == 'bad'\">AND rating &lt;= 2</if>" +
            "<if test=\"ratingType == 'withImage'\">AND images IS NOT NULL AND images != ''</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    IPage<ProductReview> selectReviewPage(Page<ProductReview> page, 
                                          @Param("productId") Long productId,
                                          @Param("ratingType") String ratingType);
    
    /**
     * 检查用户是否已评价该订单商品
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param productId 商品ID
     * @return 评价数量
     */
    @Select("SELECT COUNT(*) FROM product_review " +
            "WHERE user_id = #{userId} AND order_id = #{orderId} AND product_id = #{productId} AND deleted = 0")
    int checkUserReviewed(@Param("userId") Long userId, 
                          @Param("orderId") Long orderId, 
                          @Param("productId") Long productId);
}
