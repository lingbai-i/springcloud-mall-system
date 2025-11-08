package com.mall.order.service;

import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.entity.Order;
import com.mall.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 * 定义订单相关的业务操作方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
public interface OrderService {
    
    /**
     * 创建订单
     * 从购物车创建订单，包含库存验证、价格计算等逻辑
     * 
     * @param request 创建订单请求
     * @return 创建的订单信息
     */
    Order createOrder(CreateOrderRequest request);
    
    /**
     * 根据订单ID获取订单详情
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 订单详情
     */
    Order getOrderById(Long orderId, Long userId);
    
    /**
     * 根据订单号获取订单详情
     * 
     * @param orderNo 订单号
     * @return 订单详情
     */
    Order getOrderByOrderNo(String orderNo);
    
    /**
     * 分页查询用户订单列表
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 订单分页数据
     */
    Page<Order> getUserOrders(Long userId, Pageable pageable);
    
    /**
     * 根据状态分页查询用户订单
     * 
     * @param userId 用户ID
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页数据
     */
    Page<Order> getUserOrdersByStatus(Long userId, OrderStatus status, Pageable pageable);
    
    /**
     * 取消订单
     * 只有待付款状态的订单可以取消
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 取消结果
     */
    Boolean cancelOrder(Long orderId, Long userId, String reason);
    
    /**
     * 确认收货
     * 只有已发货状态的订单可以确认收货
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 确认结果
     */
    Boolean confirmOrder(Long orderId, Long userId);
    
    /**
     * 申请退款
     * 已付款、已发货、已完成状态的订单可以申请退款
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param reason 退款原因
     * @return 退款申请结果
     */
    Boolean applyRefund(Long orderId, Long userId, String reason);
    
    /**
     * 订单支付
     * 调用支付服务完成订单支付
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @param paymentMethod 支付方式
     * @return 支付结果
     */
    Map<String, Object> payOrder(Long orderId, Long userId, String paymentMethod);
    
    /**
     * 处理支付成功回调
     * 更新订单状态为已付款
     * 
     * @param orderNo 订单号
     * @param paymentId 支付ID
     * @return 处理结果
     */
    Boolean handlePaymentSuccess(String orderNo, String paymentId);
    
    /**
     * 获取订单物流信息
     * 
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 物流信息
     */
    Map<String, Object> getOrderLogistics(Long orderId, Long userId);
    
    /**
     * 重新购买
     * 根据历史订单创建新订单
     * 
     * @param orderId 原订单ID
     * @param userId 用户ID
     * @return 新订单信息
     */
    Order reorder(Long orderId, Long userId);
    
    /**
     * 获取用户订单统计信息
     * 
     * @param userId 用户ID
     * @return 订单统计数据
     */
    Map<String, Object> getOrderStats(Long userId);
    
    /**
     * 处理超时订单
     * 定时任务调用，取消超时未付款的订单
     * 
     * @return 处理的订单数量
     */
    Integer handleTimeoutOrders();
    
    /**
     * 自动确认收货
     * 定时任务调用，自动确认超时未确认的订单
     * 
     * @return 处理的订单数量
     */
    Integer autoConfirmOrders();
    
    /**
     * 更新订单状态
     * 内部方法，用于状态流转
     * 
     * @param orderId 订单ID
     * @param status 新状态
     * @return 更新结果
     */
    Boolean updateOrderStatus(Long orderId, OrderStatus status);
}