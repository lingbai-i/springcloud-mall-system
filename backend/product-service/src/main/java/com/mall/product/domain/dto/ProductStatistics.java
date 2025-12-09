package com.mall.product.domain.dto;

import lombok.Data;

/**
 * 商品统计数据 DTO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-12-01
 */
@Data
public class ProductStatistics {
    
    /** 商品总数 */
    private Long totalCount = 0L;
    
    /** 上架商品数 */
    private Long onlineCount = 0L;
    
    /** 下架商品数 */
    private Long offlineCount = 0L;
    
    /** 库存预警商品数 */
    private Long warningCount = 0L;
    
    /** 总销量 */
    private Long totalSales = 0L;
    
    /** 总销售额 */
    private Double totalRevenue = 0.0;
    
    /** 商家ID（可选，用于按商家筛选） */
    private Long merchantId;
}
