package com.mall.admin.controller;

import com.mall.admin.domain.vo.ServiceHealthVO;
import com.mall.admin.domain.vo.SystemStatisticsVO;
import com.mall.admin.service.SystemMonitorService;
import com.mall.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统监控控制器
 * 
 * @author lingbai
 * @since 2025-01-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class SystemMonitorController {
    
    private final SystemMonitorService systemMonitorService;
    
    /**
     * 获取系统概览统计
     */
    @GetMapping("/overview")
    public R<SystemStatisticsVO> getSystemOverview() {
        SystemStatisticsVO statistics = systemMonitorService.getSystemStatistics();
        return R.ok(statistics);
    }
    
    /**
     * 获取服务健康状态
     */
    @GetMapping("/health")
    public R<ServiceHealthVO> getServiceHealth() {
        ServiceHealthVO health = systemMonitorService.getServiceHealth();
        return R.ok(health);
    }
}
