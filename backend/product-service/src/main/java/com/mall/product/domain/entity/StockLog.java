package com.mall.product.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 库存变更日志实体类
 * 用于记录商品库存变更历史
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-01-21
 * 修改日志：V1.1 2025-12-01：添加 MyBatis-Plus 注解
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName("stock_log")
public class StockLog extends BaseEntity {
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * SKU ID（如果是SKU库存变更）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Long skuId;
    
    /**
     * 变更前库存
     */
    @com.baomidou.mybatisplus.annotation.TableField("before_stock")
    private Integer oldStock;
    
    /**
     * 变更后库存
     */
    @com.baomidou.mybatisplus.annotation.TableField("after_stock")
    private Integer newStock;
    
    /**
     * 变更数量（正数为入库，负数为出库）
     */
    @com.baomidou.mybatisplus.annotation.TableField("quantity")
    private Integer changeQuantity;
    
    /**
     * 变更类型：1-入库，2-出库，3-调整，4-盘点
     */
    @com.baomidou.mybatisplus.annotation.TableField("operation_type")
    private String changeType;
    
    /**
     * 变更原因
     */
    private String reason;
    
    /**
     * 关联单据号（订单号、入库单号等）
     */
    @com.baomidou.mybatisplus.annotation.TableField("order_no")
    private String relatedOrderNo;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    /**
     * 操作人姓名
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String operatorName;
    
    /**
     * 商品名称（冗余字段）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String productName;
    
    /**
     * SKU名称（冗余字段）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String skuName;
}