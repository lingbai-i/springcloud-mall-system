package com.mall.order.service;

import com.mall.order.client.CartClient;
import com.mall.order.client.ProductClient;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.dto.OrderStatsResponse;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.enums.OrderStatus;
import com.mall.order.exception.OrderException;
import com.mall.order.repository.OrderItemRepository;
import com.mall.order.repository.OrderRepository;
import com.mall.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 订单服务测试类
 * 测试OrderServiceImpl的各个业务方法
 * 
 * @author lingbai
 * @version 1.1
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2024-01-01：初始版本，基础订单服务测试
 * V1.1 2025-01-21：扩展测试用例，增加创建订单、统计、缓存等测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务测试")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private ProductClient productClient;
    
    @Mock
    private CartClient cartClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderItem testOrderItem;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试订单
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORDER20250121001");
        testOrder.setUserId(1001L);
        testOrder.setStatus(OrderStatus.PENDING_PAYMENT);
        testOrder.setTotalAmount(new BigDecimal("299.00"));
        testOrder.setActualAmount(new BigDecimal("279.00"));
        testOrder.setFreight(new BigDecimal("10.00"));
        testOrder.setDiscountAmount(new BigDecimal("20.00"));
        testOrder.setReceiverName("张三");
        testOrder.setReceiverPhone("13800138000");
        testOrder.setReceiverAddress("北京市朝阳区测试地址");
        testOrder.setRemark("测试订单");
        testOrder.setCreateTime(LocalDateTime.now());
        testOrder.setUpdateTime(LocalDateTime.now());
        
        // 初始化测试订单项
        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrderId(1L);
        testOrderItem.setProductId(1001L);
        testOrderItem.setProductName("测试商品");
        testOrderItem.setProductSku("SKU001");
        testOrderItem.setProductPrice(new BigDecimal("99.00"));
        testOrderItem.setQuantity(2);
        testOrderItem.setTotalPrice(new BigDecimal("198.00"));
        
        // 初始化创建订单请求
        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(1001L);
        createOrderRequest.setReceiverName("张三");
        createOrderRequest.setReceiverPhone("13800138000");
        createOrderRequest.setReceiverAddress("北京市朝阳区测试地址");
        createOrderRequest.setFreight(new BigDecimal("10.00"));
        createOrderRequest.setDiscountAmount(new BigDecimal("20.00"));
        createOrderRequest.setRemark("测试订单");
        
        CreateOrderRequest.OrderItem orderItem = new CreateOrderRequest.OrderItem();
        orderItem.setProductId(1001L);
        orderItem.setProductSku("SKU001");
        orderItem.setQuantity(2);
        createOrderRequest.setOrderItems(Collections.singletonList(orderItem));
    }
    
    @Test
    @DisplayName("根据ID获取订单 - 成功")
    void getOrderById_Success() {
        // Given
        when(orderRepository.findByIdAndUserId(1L, 1001L)).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.getOrderById(1L, 1001L);

        // Then
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testOrder.getOrderNo(), result.getOrderNo());
        verify(orderRepository).findByIdAndUserId(1L, 1001L);
    }

    @Test
    @DisplayName("根据ID获取订单 - 订单不存在")
    void getOrderById_NotFound() {
        // Given
        when(orderRepository.findByIdAndUserId(999L, 1001L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderException.class, () -> orderService.getOrderById(999L, 1001L));
        verify(orderRepository).findByIdAndUserId(999L, 1001L);
    }

    @Test
    @DisplayName("根据订单号获取订单 - 成功")
    void getOrderByOrderNo_Success() {
        // Given
        when(orderRepository.findByOrderNo("ORDER20250121001")).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.getOrderByOrderNo("ORDER20250121001");

        // Then
        assertNotNull(result);
        assertEquals(testOrder.getOrderNo(), result.getOrderNo());
        verify(orderRepository).findByOrderNo("ORDER20250121001");
    }

    @Test
    @DisplayName("获取用户订单列表 - 成功")
    void getUserOrders_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        when(orderRepository.findByUserIdOrderByCreateTimeDesc(1001L, pageable)).thenReturn(orderPage);

        // When
        Page<Order> result = orderService.getUserOrders(1001L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOrder.getId(), result.getContent().get(0).getId());
        verify(orderRepository).findByUserIdOrderByCreateTimeDesc(1001L, pageable);
    }

    @Test
    @DisplayName("根据状态获取用户订单列表 - 成功")
    void getUserOrdersByStatus_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        when(orderRepository.findByUserIdAndStatusOrderByCreateTimeDesc(1001L, OrderStatus.PENDING_PAYMENT, pageable))
                .thenReturn(orderPage);

        // When
        Page<Order> result = orderService.getUserOrdersByStatus(1001L, OrderStatus.PENDING_PAYMENT, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(OrderStatus.PENDING_PAYMENT, result.getContent().get(0).getStatus());
        verify(orderRepository).findByUserIdAndStatusOrderByCreateTimeDesc(1001L, OrderStatus.PENDING_PAYMENT, pageable);
    }

    @Test
    @DisplayName("创建订单 - 成功")
    void createOrder_Success() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderItemRepository.saveAll(any())).thenReturn(Collections.singletonList(testOrderItem));

        // When
        Order result = orderService.createOrder(createOrderRequest);

        // Then
        assertNotNull(result);
        assertEquals(testOrder.getOrderNo(), result.getOrderNo());
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(any());
    }

    @Test
    @DisplayName("更新订单状态 - 成功")
    void updateOrderStatus_Success() {
        // Given
        when(orderRepository.findByIdAndUserId(1L, 1001L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Boolean result = orderService.updateOrderStatus(1L, 1001L, OrderStatus.PAID);

        // Then
        assertTrue(result);
        assertEquals(OrderStatus.PAID, testOrder.getStatus());
        verify(orderRepository).findByIdAndUserId(1L, 1001L);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("取消订单 - 成功")
    void cancelOrder_Success() {
        // Given
        when(orderRepository.findByIdAndUserId(1L, 1001L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Boolean result = orderService.cancelOrder(1L, 1001L, "用户主动取消");

        // Then
        assertTrue(result);
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).findByIdAndUserId(1L, 1001L);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("取消订单 - 状态不允许取消")
    void cancelOrder_InvalidStatus() {
        // Given
        testOrder.setStatus(OrderStatus.COMPLETED); // 已完成的订单不能取消
        when(orderRepository.findByIdAndUserId(1L, 1001L)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThrows(OrderException.class, () -> orderService.cancelOrder(1L, 1001L, "用户主动取消"));
        verify(orderRepository).findByIdAndUserId(1L, 1001L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("确认收货 - 成功")
    void confirmOrder_Success() {
        // Given
        testOrder.setStatus(OrderStatus.DELIVERED); // 已发货状态可以确认收货
        when(orderRepository.findByIdAndUserId(1L, 1001L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Boolean result = orderService.confirmOrder(1L, 1001L);

        // Then
        assertTrue(result);
        assertEquals(OrderStatus.COMPLETED, testOrder.getStatus());
        verify(orderRepository).findByIdAndUserId(1L, 1001L);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("获取用户订单统计 - 成功")
    void getUserOrderStats_Success() {
        // Given
        when(orderRepository.countByUserIdAndStatus(1001L, OrderStatus.PENDING_PAYMENT)).thenReturn(2L);
        when(orderRepository.countByUserIdAndStatus(1001L, OrderStatus.PAID)).thenReturn(5L);
        when(orderRepository.countByUserIdAndStatus(1001L, OrderStatus.SHIPPED)).thenReturn(3L);
        when(orderRepository.countByUserIdAndStatus(1001L, OrderStatus.DELIVERED)).thenReturn(8L);
        when(orderRepository.countByUserIdAndStatus(1001L, OrderStatus.COMPLETED)).thenReturn(10L);
        when(orderRepository.countByUserIdAndStatus(1001L, OrderStatus.CANCELLED)).thenReturn(1L);
        when(orderRepository.countByUserId(1001L)).thenReturn(29L);
        when(orderRepository.sumActualAmountByUserId(1001L)).thenReturn(new BigDecimal("2890.00"));

        // When
        OrderStatsResponse result = orderService.getUserOrderStats(1001L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getPendingPaymentCount());
        assertEquals(5, result.getPaidCount());
        assertEquals(3, result.getShippedCount());
        assertEquals(8, result.getDeliveredCount());
        assertEquals(10, result.getCompletedCount());
        assertEquals(1, result.getCancelledCount());
        assertEquals(29, result.getTotalCount());
        assertEquals(new BigDecimal("2890.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("批量更新订单状态 - 成功")
    void batchUpdateOrderStatus_Success() {
        // Given
        List<Long> orderIds = Arrays.asList(1L, 2L, 3L);
        when(orderRepository.findAllById(orderIds)).thenReturn(Arrays.asList(testOrder));
        when(orderRepository.saveAll(any())).thenReturn(Arrays.asList(testOrder));

        // When
        Boolean result = orderService.batchUpdateOrderStatus(orderIds, OrderStatus.SHIPPED);

        // Then
        assertTrue(result);
        verify(orderRepository).findAllById(orderIds);
        verify(orderRepository).saveAll(any());
    }

    @Test
    @DisplayName("处理超时订单 - 成功")
    void processTimeoutOrders_Success() {
        // Given
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(30);
        when(orderRepository.findByStatusAndCreateTimeBefore(OrderStatus.PENDING_PAYMENT, timeoutTime))
                .thenReturn(Arrays.asList(testOrder));
        when(orderRepository.saveAll(any())).thenReturn(Arrays.asList(testOrder));

        // When
        orderService.processTimeoutOrders();

        // Then
        verify(orderRepository).findByStatusAndCreateTimeBefore(eq(OrderStatus.PENDING_PAYMENT), any(LocalDateTime.class));
        verify(orderRepository).saveAll(any());
    }
}