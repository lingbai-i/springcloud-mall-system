package com.mall.admin.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 系统统计数据VO
 */
@Data
public class SystemStatisticsVO {
    
    /**
     * 总用户数
     */
    private Long totalUsers;
    
    /**
     * 今日新增用户
     */
    private Long todayNewUsers;
    
    /**
     * 活跃用户数
     */
    private Long activeUsers;
    
    /**
     * 总商家数
     */
    private Long totalMerchants;
    
    /**
     * 待审核商家
     */
    private Long pendingMerchants;
    
    /**
     * 总订单数
     */
    private Long totalOrders;
    
    /**
     * 今日订单数
     */
    private Long todayOrders;
    
    /**
     * 总交易额
     */
    private BigDecimal totalRevenue;
    
    /**
     * 今日交易额
     */
    private BigDecimal todayRevenue;
}
