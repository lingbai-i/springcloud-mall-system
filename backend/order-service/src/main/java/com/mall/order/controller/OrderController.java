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
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 订单分页列表
     */
    @GetMapping
    public R<Page<Order>> getOrders(
            @RequestParam Long userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("获取用户订单列表，用户ID: {}, 状态: {}, 页码: {}, 大小: {}", userId, status, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders;
        
        if (status != null) {
            orders = orderService.getUserOrdersByStatus(userId, status, pageable);
        } else {
            orders = orderService.getUserOrders(userId, pageable);
        }
        
        return R.ok(orders);
    }
    
    /**
     * 根据ID获取订单详情
     * 
     * @param id 订单ID
     * @param userId 用户ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public R<Order> getOrderById(@PathVariable Long id, @RequestParam Long userId) {
        log.info("获取订单详情，订单ID: {}, 用户ID: {}", id, userId);
        
        Order order = orderService.getOrderById(id, userId);
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
     * @param id 订单ID
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
     * @param id 订单ID
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
     * @param id 订单ID
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
     * @param id 订单ID
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
     * @param id 订单ID
     * @param userId 用户ID
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
     * @param id 订单ID
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
     * @param orderNo 订单号
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
}