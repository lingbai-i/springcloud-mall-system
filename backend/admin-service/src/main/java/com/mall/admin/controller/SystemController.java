package com.mall.admin.controller;

import com.mall.admin.domain.vo.SystemStatsResponse;
import com.mall.admin.service.SystemService;
import com.mall.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统管理控制器
 * 处理系统统计数据相关的HTTP请求
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-27
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@Tag(name = "系统管理", description = "系统统计数据相关接口")
public class SystemController {
    
    private final SystemService systemService;
    
    /**
     * 获取系统统计数据
     * 
     * @return 系统统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取系统统计数据", description = "获取系统整体统计数据，包括用户、商家、订单、销售等统计信息")
    public R<SystemStatsResponse> getSystemStats() {
        log.info("管理员获取系统统计数据");
        
        try {
            SystemStatsResponse stats = systemService.getSystemStats();
            
            log.info("系统统计数据获取成功");
            return R.ok(stats);
            
        } catch (Exception e) {
            log.error("获取系统统计数据异常", e);
            return R.fail("获取系统统计数据失败");
        }
    }
    
    /**
     * 获取订单统计数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单统计数据
     */
    @GetMapping("/stats/orders")
    @Operation(summary = "获取订单统计数据", description = "获取指定时间范围内的订单统计数据")
    public R<Map<String, Object>> getOrderStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        log.info("管理员获取订单统计数据，时间范围：{} - {}", startTime, endTime);
        
        try {
            // 构建查询参数
            Map<String, Object> params = new java.util.HashMap<>();
            if (startTime != null) {
                params.put("startTime", startTime);
            }
            if (endTime != null) {
                params.put("endTime", endTime);
            }
            
            Map<String, Object> stats = systemService.getOrderStats(params);
            
            log.info("订单统计数据获取成功");
            return R.ok(stats);
            
        } catch (Exception e) {
            log.error("获取订单统计数据异常", e);
            return R.fail("获取订单统计数据失败");
        }
    }
    
    /**
     * 获取销售统计数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 销售统计数据
     */
    @GetMapping("/stats/sales")
    @Operation(summary = "获取销售统计数据", description = "获取指定时间范围内的销售统计数据")
    public R<Map<String, Object>> getSalesStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        log.info("管理员获取销售统计数据，时间范围：{} - {}", startTime, endTime);
        
        try {
            // 构建查询参数
            Map<String, Object> params = new java.util.HashMap<>();
            if (startTime != null) {
                params.put("startTime", startTime);
            }
            if (endTime != null) {
                params.put("endTime", endTime);
            }
            
            Map<String, Object> stats = systemService.getSalesStats(params);
            
            log.info("销售统计数据获取成功");
            return R.ok(stats);
            
        } catch (Exception e) {
            log.error("获取销售统计数据异常", e);
            return R.fail("获取销售统计数据失败");
        }
    }
    
    /**
     * 获取商品统计数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 商品统计数据
     */
    @GetMapping("/stats/products")
    @Operation(summary = "获取商品统计数据", description = "获取指定时间范围内的商品统计数据")
    public R<Map<String, Object>> getProductStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        log.info("管理员获取商品统计数据，时间范围：{} - {}", startTime, endTime);
        
        try {
            // 构建查询参数
            Map<String, Object> params = new java.util.HashMap<>();
            if (startTime != null) {
                params.put("startTime", startTime);
            }
            if (endTime != null) {
                params.put("endTime", endTime);
            }
            
            Map<String, Object> stats = systemService.getProductStats(params);
            
            log.info("商品统计数据获取成功");
            return R.ok(stats);
            
        } catch (Exception e) {
            log.error("获取商品统计数据异常", e);
            return R.fail("获取商品统计数据失败");
        }
    }
    
    /**
     * 获取财务统计数据
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 财务统计数据
     */
    @GetMapping("/stats/finance")
    @Operation(summary = "获取财务统计数据", description = "获取指定时间范围内的财务统计数据")
    public R<Map<String, Object>> getFinanceStats(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        log.info("管理员获取财务统计数据，时间范围：{} - {}", startTime, endTime);
        
        try {
            // 构建查询参数
            Map<String, Object> params = new java.util.HashMap<>();
            if (startTime != null) {
                params.put("startTime", startTime);
            }
            if (endTime != null) {
                params.put("endTime", endTime);
            }
            
            Map<String, Object> stats = systemService.getFinanceStats(params);
            
            log.info("财务统计数据获取成功");
            return R.ok(stats);
            
        } catch (Exception e) {
            log.error("获取财务统计数据异常", e);
            return R.fail("获取财务统计数据失败");
        }
    }
}
