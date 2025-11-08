package com.mall.admin.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 系统统计响应VO
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Data
public class SystemStatsResponse {
    
    /** 总用户数 */
    private Long totalUsers;
    
    /** 今日新增用户 */
    private Long todayNewUsers;
    
    /** 总商家数 */
    private Long totalMerchants;
    
    /** 待审核商家数 */
    private Long pendingMerchants;
    
    /** 总商品数 */
    private Long totalProducts;
    
    /** 在售商品数 */
    private Long onSaleProducts;
    
    /** 总订单数 */
    private Long totalOrders;
    
    /** 今日订单数 */
    private Long todayOrders;
    
    /** 总销售额 */
    private BigDecimal totalSales;
    
    /** 今日销售额 */
    private BigDecimal todaySales;
    
    /** 用户增长趋势（最近7天） */
    private List<Map<String, Object>> userGrowthTrend;
    
    /** 订单趋势（最近7天） */
    private List<Map<String, Object>> orderTrend;
    
    /** 销售趋势（最近7天） */
    private List<Map<String, Object>> salesTrend;
    
    /** 商品分类统计 */
    private List<Map<String, Object>> categoryStats;
    
    /** 热门商品TOP10 */
    private List<Map<String, Object>> topProducts;
    
    /** 活跃用户TOP10 */
    private List<Map<String, Object>> activeUsers;
}