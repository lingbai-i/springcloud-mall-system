package com.mall.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 * 
 * @author lingbai
 * @version 1.0
 * P25-09-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_favorites")
public class UserFavorite extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    /** 收藏ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /** 用户ID */
    @TableField("user_id")
    private Long userId;
    
    /** 商品ID */
    @TableField("product_id")
    private Long productId;
    
    /** 商品名称（冗余） */
    @TableField("product_name")
    private String productName;
    
    /** 商品图片（冗余） */
    @TableField("product_image")
    private String productImage;
    
    /** 商品价格（冗余） */
    @TableField("product_price")
    private BigDecimal productPrice;
    
    /** 商品描述（冗余） */
    @TableField("product_desc")
    private String productDesc;
    
    /** 商品原价（冗余） */
    @TableField("original_price")
    private BigDecimal originalPrice;
    
    /** 创建时间 */
    @TableField("created_time")
    private LocalDateTime createTime;
    
    /** 更新时间 */
    @TableField("updated_time")
    private LocalDateTime updateTime;
}
