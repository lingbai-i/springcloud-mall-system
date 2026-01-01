package com.mall.admin.client;

import com.mall.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单服务Feign Client
 * 
 * @author lingbai
 * @since 2025-12-25
 */
@FeignClient(name = "order-service", path = "/orders")
public interface OrderServiceClient {

    /**
     * 获取管理员订单统计
     * 
     * @return 订单统计数据
     */
    @GetMapping("/admin/stats")
    R<Map<String, Object>> getAdminOrderStats();

    /**
     * 获取所有订单列表（管理员）
     * 
     * @param status  订单状态
     * @param orderNo 订单号
     * @param page    页码
     * @param size    每页大小
     * @return 订单列表
     */
    @GetMapping("/admin")
    R<Map<String, Object>> getAllOrders(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size);

    /**
     * 获取销售趋势数据
     * 
     * @param days 天数
     * @return 销售趋势数据
     */
    @GetMapping("/admin/sales-trend")
    R<Map<String, Object>> getSalesTrend(@RequestParam(value = "days", defaultValue = "30") int days);

    /**
     * 获取最近订单
     * 
     * @param limit 数量限制
     * @return 最近订单列表
     */
    @GetMapping("/admin/recent")
    R<List<Map<String, Object>>> getRecentOrders(@RequestParam(value = "limit", defaultValue = "10") int limit);

    /**
     * 获取消费能力分布
     * 
     * @return 消费能力分布数据
     */
    @GetMapping("/admin/consume-distribution")
    R<List<Map<String, Object>>> getConsumeDistribution();
}