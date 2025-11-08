package com.mall.admin.service;

import com.mall.admin.domain.vo.SystemStatsResponse;

import java.util.Map;

/**
 * 系统服务接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
public interface SystemService {
    
    /**
     * 获取系统统计数据
     * 
     * @return 系统统计数据
     */
    SystemStatsResponse getSystemStats();
    
    /**
     * 获取订单统计数据
     * 
     * @param params 查询参数
     * @return 订单统计数据
     */
    Map<String, Object> getOrderStats(Map<String, Object> params);
    
    /**
     * 获取销售统计数据
     * 
     * @param params 查询参数
     * @return 销售统计数据
     */
    Map<String, Object> getSalesStats(Map<String, Object> params);
    
    /**
     * 获取商品统计数据
     * 
     * @param params 查询参数
     * @return 商品统计数据
     */
    Map<String, Object> getProductStats(Map<String, Object> params);
    
    /**
     * 获取财务统计数据
     * 
     * @param params 查询参数
     * @return 财务统计数据
     */
    Map<String, Object> getFinanceStats(Map<String, Object> params);
}