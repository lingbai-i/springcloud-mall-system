package com.mall.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.mall.order.client.CartClient;
import com.mall.order.client.ProductClient;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.enums.OrderStatus;
import com.mall.order.repository.OrderItemRepository;
import com.mall.order.repository.OrderRepository;
import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类（简化版）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductClient productClient;
    private final CartClient cartClient;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(CreateOrderRequest request) {
        log.info("开始创建订单，用户ID: {}", request.getUserId());
        
        // 1. 验证订单项
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("订单项不能为空");
        }
        
        // 2. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        order.setRemark(request.getRemark());
        
        // 3. 计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setProductName("商品-" + itemRequest.getProductId());
            orderItem.setProductPrice(itemRequest.getProductPrice() != null ? itemRequest.getProductPrice() : BigDecimal.TEN);
            orderItem.setQuantity(itemRequest.getQuantity());
            
            BigDecimal subtotal = orderItem.getProductPrice().multiply(new BigDecimal(itemRequest.getQuantity()));
            orderItem.setSubtotal(subtotal);
            totalAmount = totalAmount.add(subtotal);
            
            orderItems.add(orderItem);
        }
        
        order.setProductAmount(totalAmount);
        order.setTotalAmount(totalAmount.add(order.getShippingFee()).subtract(order.getDiscountAmount()));
        order.setPayableAmount(order.getTotalAmount());
        order.setOrderItems(orderItems);
        
        // 4. 保存订单
        Order savedOrder = orderRepository.save(order);
        log.info("订单创建成功: orderNo={}", savedOrder.getOrderNo());
        
        return savedOrder;
    }
    
    @Override
    @Cacheable(value = "orders", key = "#orderId")
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    }
    
    @Override
    public Order getOrderByNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    }
    
    @Override
    public Page<Order> getUserOrders(Long userId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByUserIdAndStatus(userId, status, pageable);
        }
        return orderRepository.findByUserId(userId, pageable);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "orders", key = "#orderId")
    public Order cancelOrder(Long orderId, Long userId, String reason) {
        Order order = getOrderById(orderId);
        
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("当前订单状态不允许取消");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        
        return orderRepository.save(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "orders", key = "#orderId")
    public Order payOrder(Long orderId) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("订单状态不允许支付");
        }
        
        order.setStatus(OrderStatus.PAID);
        order.setPayTime(LocalDateTime.now());
        
        return orderRepository.save(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "orders", key = "#orderId")
    public Order shipOrder(Long orderId, String expressNo, String expressCompany) {
        Order order = getOrderById(orderId);
        
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("订单状态不允许发货");
        }
        
        order.setStatus(OrderStatus.SHIPPED);
        order.setShipTime(LocalDateTime.now());
        order.setExpressNo(expressNo);
        order.setExpressCompany(expressCompany);
        
        return orderRepository.save(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "orders", key = "#orderId")
    public Order confirmOrder(Long orderId, Long userId) {
        Order order = getOrderById(orderId);
        
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }
        
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("订单状态不允许确认收货");
        }
        
        order.setStatus(OrderStatus.COMPLETED);
        order.setConfirmTime(LocalDateTime.now());
        
        return orderRepository.save(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "orders", key = "#orderId")
    public Order deleteOrder(Long orderId, Long userId) {
        Order order = getOrderById(orderId);
        
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此订单");
        }
        
        if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.COMPLETED) {
            throw new IllegalStateException("订单状态不允许删除");
        }
        
        order.setDeleted(true);
        return orderRepository.save(order);
    }
    
    @Override
    public Long countUserOrders(Long userId, OrderStatus status) {
        if (status != null) {
            return orderRepository.countByUserIdAndStatus(userId, status);
        }
        return orderRepository.countByUserId(userId);
    }
    
    @Override
    public BigDecimal calculateUserTotalAmount(Long userId) {
        return orderRepository.sumTotalAmountByUserId(userId);
    }
    
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + IdUtil.randomUUID().substring(0, 6);
    }
}
