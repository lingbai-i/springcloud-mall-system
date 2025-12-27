package com.mall.merchant.client;

import com.mall.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 订单服务 Feign 客户端
 * 用于调用 order-service 获取订单数据
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-12-26
 */
@FeignClient(name = "order-service", path = "/orders")
public interface OrderServiceClient {

    /**
     * 获取商家订单列表
     * 
     * @param merchantId 商家ID
     * @param status 订单状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 订单分页列表
     */
    @GetMapping("/merchant")
    R<Map<String, Object>> getMerchantOrders(
            @RequestParam("merchantId") Long merchantId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size);

    /**
     * 获取商家订单统计
     * 
     * @param merchantId 商家ID
     * @return 订单统计信息
     */
    @GetMapping("/merchant/stats")
    R<Map<String, Object>> getMerchantOrderStats(@RequestParam("merchantId") Long merchantId);

    /**
     * 获取商家最近订单
     * 注意：order-service 返回 List<Order>，但 Feign 会自动将其反序列化为 Map
     * 
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 最近订单列表（Order 对象会被反序列化为 Map）
     */
    @GetMapping("/merchant/recent")
    R<List<Map<String, Object>>> getMerchantRecentOrders(
            @RequestParam("merchantId") Long merchantId,
            @RequestParam(value = "limit", defaultValue = "10") int limit);

    /**
     * 获取商家每日销售统计
     * 
     * @param merchantId 商家ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日销售统计列表
     */
    @GetMapping("/merchant/daily-sales")
    R<List<Map<String, Object>>> getMerchantDailySales(
            @RequestParam("merchantId") Long merchantId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);

    /**
     * 获取商家热销商品统计
     * 
     * @param merchantId 商家ID
     * @param limit 数量限制
     * @return 热销商品列表
     */
    @GetMapping("/merchant/hot-products")
    R<List<Map<String, Object>>> getMerchantHotProducts(
            @RequestParam("merchantId") Long merchantId,
            @RequestParam(value = "limit", defaultValue = "10") int limit);
}
