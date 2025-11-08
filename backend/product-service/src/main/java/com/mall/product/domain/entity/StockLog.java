package com.mall.product.domain.entity;

import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 库存变更日志实体类
 * 用于记录商品库存变更历史
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockLog extends BaseEntity {
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * SKU ID（如果是SKU库存变更）
     */
    private Long skuId;
    
    /**
     * 变更前库存
     */
    private Integer oldStock;
    
    /**
     * 变更后库存
     */
    private Integer newStock;
    
    /**
     * 变更数量（正数为入库，负数为出库）
     */
    private Integer changeQuantity;
    
    /**
     * 变更类型：1-入库，2-出库，3-调整，4-盘点
     */
    private String changeType;
    
    /**
     * 变更原因
     */
    private String reason;
    
    /**
     * 关联单据号（订单号、入库单号等）
     */
    private String relatedOrderNo;
    
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
}