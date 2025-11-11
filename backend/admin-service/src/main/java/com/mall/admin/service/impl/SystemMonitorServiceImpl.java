package com.mall.admin.service.impl;

import com.mall.admin.client.MerchantServiceClient;
import com.mall.admin.client.UserServiceClient;
import com.mall.admin.domain.vo.ServiceHealthVO;
import com.mall.admin.domain.vo.SystemStatisticsVO;
import com.mall.admin.service.SystemMonitorService;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控服务实现
 * 
 * @author system
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMonitorServiceImpl implements SystemMonitorService {
    
    private final UserServiceClient userServiceClient;
    private final MerchantServiceClient merchantServiceClient;
    
    @Override
    public SystemStatisticsVO getSystemStatistics() {
        SystemStatisticsVO statistics = new SystemStatisticsVO();
        
        try {
            // 获取用户统计
            R<Map<String, Object>> userStats = userServiceClient.getUserStatistics();
            if (userStats.isSuccess()) {
                Map<String, Object> data = userStats.getData();
                statistics.setTotalUsers(getLongValue(data, "totalUsers"));
                statistics.setActiveUsers(getLongValue(data, "activeUsers"));
            }
        } catch (Exception e) {
            log.error("获取用户统计失败", e);
        }
        
        try {
            // 获取商家统计
            R<Map<String, Object>> merchantStats = merchantServiceClient.getMerchantStatistics();
            if (merchantStats.isSuccess()) {
                Map<String, Object> data = merchantStats.getData();
                statistics.setTotalMerchants(getLongValue(data, "totalMerchants"));
                statistics.setPendingMerchants(getLongValue(data, "pendingMerchants"));
            }
        } catch (Exception e) {
            log.error("获取商家统计失败", e);
        }
        
        return statistics;
    }
    
    @Override
    public ServiceHealthVO getServiceHealth() {
        ServiceHealthVO health = new ServiceHealthVO();
        Map<String, String> services = new HashMap<>();
        
        // 检查用户服务
        try {
            userServiceClient.getUserStatistics();
            services.put("user-service", "UP");
        } catch (Exception e) {
            services.put("user-service", "DOWN");
            log.error("用户服务健康检查失败", e);
        }
        
        // 检查商家服务
        try {
            merchantServiceClient.getMerchantStatistics();
            services.put("merchant-service", "UP");
        } catch (Exception e) {
            services.put("merchant-service", "DOWN");
            log.error("商家服务健康检查失败", e);
        }
        
        health.setServices(services);
        return health;
    }
    
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}
