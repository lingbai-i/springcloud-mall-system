package com.mall.product.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价点赞记录实体
 * 
 * @author lingbai
 * @since 2025-12-28
 */
@Data
@TableName("review_like")
public class ReviewLike {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reviewId;
    
    private Long userId;
    
    private LocalDateTime createTime;
}
