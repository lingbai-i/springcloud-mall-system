package com.mall.product.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品评价实体
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-28
 */
@Data
@TableName("product_review")
public class ProductReview {
    
    /**
     * 评价ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 综合评分（1-5星）
     */
    private Integer rating;
    
    /**
     * 描述相符评分（1-5星）
     */
    private Integer descriptionRating;
    
    /**
     * 卖家服务评分（1-5星）
     */
    private Integer serviceRating;
    
    /**
     * 物流服务评分（1-5星）
     */
    private Integer logisticsRating;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 评价图片（多张图片用逗号分隔）
     */
    private String images;
    
    /**
     * 是否匿名（0-否，1-是）
     */
    private Integer anonymous;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 状态（0-待审核，1-已发布，2-已隐藏）
     */
    private Integer status;
    
    /**
     * 商家回复
     */
    private String merchantReply;
    
    /**
     * 商家回复时间
     */
    private LocalDateTime merchantReplyTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 是否删除（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}
