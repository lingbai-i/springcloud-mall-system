package com.mall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.domain.entity.ReviewLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评价点赞记录Mapper
 * 
 * @author lingbai
 * @since 2025-12-28
 */
@Mapper
public interface ReviewLikeMapper extends BaseMapper<ReviewLike> {
    
    /**
     * 检查用户是否已点赞
     */
    @Select("SELECT COUNT(*) FROM review_like WHERE review_id = #{reviewId} AND user_id = #{userId}")
    int checkUserLiked(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
}
