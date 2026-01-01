package com.mall.user.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户收藏VO
 * 
 * @author lingbai
 * @version 1.0
 * P25-09-20
 */
@Data
public class UserFavoriteVO {
    
    /** 收藏ID */
    private Long id;
    
    /** 商品ID */
    private Long productId;
    
    /** 商品名称 */
    private String productName;
    
    /** 商品图片 */
    private String productImage;
    
    /** 商品价格 */
    private BigDecimal productPrice;
    
    /** 商品原价 */
    private BigDecimal originalPrice;
    
    /** 商品描述 */
    private String productDesc;
    
    /** 收藏时间 */
    private LocalDateTime createTime;
}
