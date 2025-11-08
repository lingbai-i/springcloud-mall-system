package com.mall.product.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * 商品价格历史记录实体类
 * 用于记录商品价格变更历史
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceHistory extends BaseEntity {
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * SKU ID（如果是SKU价格变更）
     */
    private Long skuId;
    
    /**
     * 变更前价格
     */
    private Double oldPrice;
    
    /**
     * 变更后价格
     */
    private Double newPrice;
    
    /**
     * 价格变更类型：1-商品价格，2-SKU价格
     */
    private String priceType;
    
    /**
     * 变更原因
     */
    private String reason;
    
    /**
     * 变更原因（别名）
     */
    private String changeReason;
    
    /**
     * 审核状态
     */
    private String auditStatus;
    
    /**
     * 审核原因
     */
    private String auditReason;
    
    /**
     * 审核时间
     */
    private java.time.LocalDateTime auditTime;
    
    /**
     * 审核员ID
     */
    private Long auditorId;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    /**
     * 操作人姓名
     */
    private String operatorName;
    
    /**
     * 商品名称（冗余字段）
     */
    private String productName;
    
    /**
     * SKU名称（冗余字段）
     */
    private String skuName;
    
    /**
     * 价格版本号
     */
    private Long priceVersion;
}