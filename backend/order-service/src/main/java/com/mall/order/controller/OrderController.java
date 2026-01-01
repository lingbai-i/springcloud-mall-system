package com.mall.order.controller;

import com.mall.common.core.domain.R;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.entity.Order;
import com.mall.order.enums.OrderStatus;
import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 * 提供订单相关的REST API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 获取订单列表
     * 支持按状态筛选和分页查询
     * 
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @param page   页码，从0开始
     * @param size   每页大小
     * @return 订单分页列表
     */
    @GetMapping
    public R<Map<String, Object>> getOrders(
            @RequestParam Long userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("获取用户订单列表，用户ID: {}, 状态: {}, 页码: {}, 大小: {}", userId, status, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;

        if (status != null) {
            orderPage = orderService.getUserOrdersByStatus(userId, status, pageable);
        } else {
            orderPage = orderService.getUserOrders(userId, pageable);
        }

        // 转换为普通 Map 避免 Redis 序列化问题
        Map<String, Object> result = new HashMap<>();
        result.put("content", orderPage.getContent());
        result.put("totalElements", orderPage.getTotalElements());
        result.put("totalPages", orderPage.getTotalPages());
        result.put("size", orderPage.getSize());
        result.put("number", orderPage.getNumber());
        result.put("first", orderPage.isFirst());
        result.put("last", orderPage.isLast());
        result.put("empty", orderPage.isEmpty());

        return R.ok(result);
    }

    /**
     * 根据ID获取订单详情
     * 支持用户通过userId查看自己的订单，商家通过merchantId查看自己店铺的订单
     * 
     * @param id         订单ID
     * @param userId     用户ID（可选）
     * @param merchantId 商家ID（可选）
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public R<Order> getOrderById(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long merchantId) {
        log.info("获取订单详情，订单ID: {}, 用户ID: {}, 商家ID: {}", id, userId, merchantId);

        Order order = orderService.getOrderByIdForUserOrMerchant(id, userId, merchantId);
        return R.ok(order);
    }

    /**
     * 创建订单
     * 
     * @param request 创建订单请求
     * @return 创建的订单
     */
    @PostMapping
    public R<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("创建订单，用户ID: {}", request.getUserId());

        Order order = orderService.createOrder(request);
        return R.ok(order);
    }

    /**
     * 取消订单
     * 
     * @param id     订单ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 操作结果
     */
    @PutMapping("/{id}/cancel")
    public R<Boolean> cancelOrder(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) String reason) {

        log.info("取消订单，订单ID: {}, 用户ID: {}, 原因: {}", id, userId, reason);

        Boolean result = orderService.cancelOrder(id, userId, reason);
        return R.ok(result);
    }

    /**
     * 确认收货
     * 
     * @param id     订单ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/confirm")
    public R<Boolean> confirmOrder(
            @PathVariable Long id,
            @RequestParam Long userId) {

        log.info("确认收货，订单ID: {}, 用户ID: {}", id, userId);

        Boolean result = orderService.confirmOrder(id, userId);
        return R.ok(result);
    }

    /**
     * 申请退款
     * 
     * @param id     订单ID
     * @param userId 用户ID
     * @param reason 退款原因
     * @return 操作结果
     */
    @PostMapping("/{id}/refund")
    public R<Boolean> applyRefund(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam String reason) {

        log.info("申请退款，订单ID: {}, 用户ID: {}, 原因: {}", id, userId, reason);

        Boolean result = orderService.applyRefund(id, userId, reason);
        return R.ok(result);
    }

    /**
     * 获取物流信息
     * 
     * @param id     订单ID
     * @param userId 用户ID
     * @return 物流信息
     */
    @GetMapping("/{id}/logistics")
    public R<Map<String, Object>> getOrderLogistics(
            @PathVariable Long id,
            @RequestParam Long userId) {

        log.info("获取订单物流信息，订单ID: {}, 用户ID: {}", id, userId);

        Map<String, Object> logistics = orderService.getOrderLogistics(id, userId);
        return R.ok(logistics);
    }

    /**
     * 订单支付
     * 
     * @param id            订单ID
     * @param userId        用户ID
     * @param paymentMethod 支付方式
     * @return 支付信息
     */
    @PostMapping("/{id}/pay")
    public R<Map<String, Object>> payOrder(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam String paymentMethod) {

        log.info("订单支付，订单ID: {}, 用户ID: {}, 支付方式: {}", id, userId, paymentMethod);

        Map<String, Object> paymentInfo = orderService.payOrder(id, userId, paymentMethod);
        return R.ok(paymentInfo);
    }

    /**
     * 获取订单统计
     * 
     * @param userId 用户ID
     * @return 订单统计信息
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> getOrderStats(@RequestParam Long userId) {
        log.info("获取用户订单统计，用户ID: {}", userId);

        Map<String, Object> stats = orderService.getOrderStats(userId);
        return R.ok(stats);
    }

    /**
     * 重新购买
     * 
     * @param id     订单ID
     * @param userId 用户ID
     * @return 新创建的订单
     */
    @PostMapping("/{id}/reorder")
    public R<Order> reorder(
            @PathVariable Long id,
            @RequestParam Long userId) {

        log.info("重新购买，订单ID: {}, 用户ID: {}", id, userId);

        Order newOrder = orderService.reorder(id, userId);
        return R.ok(newOrder);
    }

    /**
     * 支付成功回调（内部接口）
     * 
     * @param orderNo   订单号
     * @param paymentId 支付ID
     * @return 处理结果
     */
    @PostMapping("/payment-callback")
    public R<Boolean> handlePaymentSuccess(
            @RequestParam String orderNo,
            @RequestParam String paymentId) {

        log.info("处理支付成功回调，订单号: {}, 支付ID: {}", orderNo, paymentId);

        Boolean result = orderService.handlePaymentSuccess(orderNo, paymentId);
        return R.ok(result);
    }

    // ==================== 商家订单管理接口 ====================

    /**
     * 获取商家订单列表
     * 
     * @param merchantId 商家ID
     * @param status     订单状态（可选）
     * @param page       页码
     * @param size       每页大小
     * @return 订单分页列表
     */
    @GetMapping("/merchant")
    public R<Map<String, Object>> getMerchantOrders(
            @RequestParam Long merchantId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("获取商家订单列表，商家ID: {}, 状态: {}, 页码: {}, 大小: {}", merchantId, status, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;

        if (status != null) {
            orderPage = orderService.getMerchantOrdersByStatus(merchantId, status, pageable);
        } else {
            orderPage = orderService.getMerchantOrders(merchantId, pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", orderPage.getContent());
        result.put("totalElements", orderPage.getTotalElements());
        result.put("totalPages", orderPage.getTotalPages());
        result.put("size", orderPage.getSize());
        result.put("number", orderPage.getNumber());

        return R.ok(result);
    }

    /**
     * 商家发货
     * 
     * @param id               订单ID
     * @param merchantId       商家ID
     * @param logisticsCompany 物流公司
     * @param logisticsNo      物流单号
     * @return 操作结果
     */
    @PutMapping("/{id}/ship")
    public R<Boolean> shipOrder(
            @PathVariable Long id,
            @RequestParam Long merchantId,
            @RequestParam String logisticsCompany,
            @RequestParam String logisticsNo) {

        log.info("商家发货，订单ID: {}, 商家ID: {}, 物流公司: {}, 物流单号: {}",
                id, merchantId, logisticsCompany, logisticsNo);

        Boolean result = orderService.shipOrder(id, merchantId, logisticsCompany, logisticsNo);
        return R.ok(result);
    }

    /**
     * 获取商家订单统计
     * 
     * @param merchantId 商家ID
     * @return 订单统计信息
     */
    @GetMapping("/merchant/stats")
    public R<Map<String, Object>> getMerchantOrderStats(@RequestParam Long merchantId) {
        log.info("获取商家订单统计，商家ID: {}", merchantId);

        Map<String, Object> stats = orderService.getMerchantOrderStats(merchantId);
        return R.ok(stats);
    }

    /**
     * 获取商家最近订单
     * 
     * @param merchantId 商家ID
     * @param limit      数量限制
     * @return 最近订单列表
     */
    @GetMapping("/merchant/recent")
    public R<List<Order>> getMerchantRecentOrders(
            @RequestParam Long merchantId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        log.info("获取商家最近订单，商家ID: {}, 数量: {}", merchantId, limit);

        List<Order> orders = orderService.getMerchantRecentOrders(merchantId, limit);
        return R.ok(orders);
    }

    /**
     * 获取商家每日销售统计
     * 
     * @param merchantId 商家ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 每日销售统计列表
     */
    @GetMapping("/merchant/daily-sales")
    public R<List<Map<String, Object>>> getMerchantDailySales(
            @RequestParam Long merchantId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("获取商家每日销售统计，商家ID: {}, 开始日期: {}, 结束日期: {}", merchantId, startDate, endDate);

        List<Map<String, Object>> dailySales = orderService.getMerchantDailySales(merchantId, startDate, endDate);
        return R.ok(dailySales);
    }

    /**
     * 获取商家热销商品统计
     * 
     * @param merchantId 商家ID
     * @param limit      数量限制
     * @return 热销商品列表
     */
    @GetMapping("/merchant/hot-products")
    public R<List<Map<String, Object>>> getMerchantHotProducts(
            @RequestParam Long merchantId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        log.info("获取商家热销商品统计，商家ID: {}, 数量: {}", merchantId, limit);

        List<Map<String, Object>> hotProducts = orderService.getMerchantHotProducts(merchantId, limit);
        return R.ok(hotProducts);
    }

    // ==================== 管理员订单管理接口 ====================

    /**
     * 获取所有订单列表（管理员）
     * 
     * @param status  订单状态（可选）
     * @param orderNo 订单号（可选，用于搜索）
     * @param page    页码
     * @param size    每页大小
     * @return 订单分页列表
     */
    @GetMapping("/admin")
    public R<Map<String, Object>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("获取所有订单列表（管理员），状态: {}, 订单号: {}, 页码: {}, 大小: {}", status, orderNo, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage;

        if (orderNo != null && !orderNo.isEmpty()) {
            orderPage = orderService.searchOrdersByOrderNo(orderNo, pageable);
        } else if (status != null) {
            orderPage = orderService.getAllOrdersByStatus(status, pageable);
        } else {
            orderPage = orderService.getAllOrders(pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", orderPage.getContent());
        result.put("totalElements", orderPage.getTotalElements());
        result.put("totalPages", orderPage.getTotalPages());
        result.put("size", orderPage.getSize());
        result.put("number", orderPage.getNumber());

        return R.ok(result);
    }

    /**
     * 获取管理员订单统计
     * 
     * @return 订单统计信息
     */
    @GetMapping("/admin/stats")
    public R<Map<String, Object>> getAdminOrderStats() {
        log.info("获取管理员订单统计");

        Map<String, Object> stats = orderService.getAdminOrderStats();
        return R.ok(stats);
    }

    /**
     * 获取订单详情（管理员）
     * 
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/admin/{id}")
    public R<Order> getAdminOrderById(@PathVariable Long id) {
        log.info("获取订单详情（管理员），订单ID: {}", id);
        return R.ok(orderService.getOrderById(id));
    }

    /**
     * 管理员处理退款
     * 
     * @param id       订单ID
     * @param approved 是否同意退款
     * @param reason   处理原因
     * @return 操作结果
     */
    @PutMapping("/{id}/refund/handle")
    public R<Boolean> handleRefund(
            @PathVariable Long id,
            @RequestParam Boolean approved,
            @RequestParam(required = false) String reason) {

        log.info("管理员处理退款，订单ID: {}, 是否同意: {}, 原因: {}", id, approved, reason);

        Boolean result = orderService.handleRefund(id, approved, reason);
        return R.ok(result);
    }

    /**
     * 获取销售趋势数据（管理员）
     * 
     * @param days 天数
     * @return 销售趋势数据
     */
    @GetMapping("/admin/sales-trend")
    public R<Map<String, Object>> getAdminSalesTrend(
            @RequestParam(value = "days", defaultValue = "30") int days) {
        log.info("获取管理员销售趋势数据，天数: {}", days);

        Map<String, Object> result = orderService.getAdminSalesTrend(days);
        return R.ok(result);
    }

    /**
     * 获取最近订单（管理员）
     * 
     * @param limit 数量限制
     * @return 最近订单列表
     */
    @GetMapping("/admin/recent")
    public R<List<Map<String, Object>>> getAdminRecentOrders(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        log.info("获取管理员最近订单，数量: {}", limit);

        List<Map<String, Object>> orders = orderService.getAdminRecentOrders(limit);
        return R.ok(orders);
    }

    /**
     * 获取消费能力分布（管理员）
     * 根据用户总消费金额进行分级统计
     * 
     * @return 消费能力分布数据
     */
    @GetMapping("/admin/consume-distribution")
    public R<List<Map<String, Object>>> getConsumeDistribution() {
        log.info("获取消费能力分布数据");

        List<Map<String, Object>> distribution = orderService.getConsumeDistribution();
        return R.ok(distribution);
    }
}