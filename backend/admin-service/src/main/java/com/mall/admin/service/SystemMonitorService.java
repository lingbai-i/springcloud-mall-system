package com.mall.admin.service;

import com.mall.admin.domain.vo.ServiceHealthVO;
import com.mall.admin.domain.vo.SystemStatisticsVO;

/**
 * 系统监控服务接口
 * 
 * @author system
 * @since 2025-01-09
 */
public interface SystemMonitorService {
    
    /**
     * 获取系统统计数据
     * 
     * @return 统计数据
     */
    SystemStatisticsVO getSystemStatistics();
    
    /**
     * 获取服务健康状态
     * 
     * @return 健康状态
     */
    ServiceHealthVO getServiceHealth();
}
