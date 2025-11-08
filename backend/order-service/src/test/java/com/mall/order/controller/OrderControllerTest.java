package com.mall.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.dto.OrderResponse;
import com.mall.order.dto.OrderStatsResponse;
import com.mall.order.entity.Order;
import com.mall.order.enums.OrderStatus;
import com.mall.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 订单控制器测试类
 * 测试OrderController的各个接口功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，实现订单控制器的完整测试用例
 */
@WebMvcTest(OrderController.class)
@DisplayName("订单控制器测试")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private Order testOrder;
    private OrderResponse testOrderResponse;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
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

        // 初始化订单响应DTO
        testOrderResponse = new OrderResponse();
        testOrderResponse.setId(1L);
        testOrderResponse.setOrderNo("ORDER20250121001");
        testOrderResponse.setUserId(1001L);
        testOrderResponse.setStatus(OrderStatus.PENDING_PAYMENT);
        testOrderResponse.setStatusName("待支付");
        testOrderResponse.setTotalAmount(new BigDecimal("299.00"));
        testOrderResponse.setActualAmount(new BigDecimal("279.00"));
        testOrderResponse.setFreight(new BigDecimal("10.00"));
        testOrderResponse.setDiscountAmount(new BigDecimal("20.00"));
        testOrderResponse.setReceiverName("张三");
        testOrderResponse.setReceiverPhone("13800138000");
        testOrderResponse.setReceiverAddress("北京市朝阳区测试地址");
        testOrderResponse.setRemark("测试订单");
        testOrderResponse.setCreateTime(LocalDateTime.now());
        testOrderResponse.setCanCancel(true);
        testOrderResponse.setCanRefund(false);
        testOrderResponse.setCanConfirm(false);

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
        orderItem.setProductId(1L);
        orderItem.setProductSku("SKU001");
        orderItem.setQuantity(2);
        createOrderRequest.setOrderItems(Collections.singletonList(orderItem));
    }

    @Test
    @DisplayName("创建订单 - 成功")
    void createOrder_Success() throws Exception {
        // Mock服务层返回
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(testOrder);

        // 执行请求并验证结果
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data.orderNo").value("ORDER20250121001"))
                .andExpect(jsonPath("$.data.userId").value(1001))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"));
    }

    @Test
    @DisplayName("创建订单 - 参数校验失败")
    void createOrder_ValidationFailed() throws Exception {
        // 创建无效的请求参数（缺少必填字段）
        CreateOrderRequest invalidRequest = new CreateOrderRequest();
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("根据ID获取订单 - 成功")
    void getOrderById_Success() throws Exception {
        // Mock服务层返回
        when(orderService.getOrderById(1L, 1001L)).thenReturn(testOrder);

        mockMvc.perform(get("/api/orders/1")
                .param("userId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.orderNo").value("ORDER20250121001"));
    }

    @Test
    @DisplayName("根据订单号获取订单 - 成功")
    void getOrderByOrderNo_Success() throws Exception {
        // Mock服务层返回
        when(orderService.getOrderByOrderNo("ORDER20250121001")).thenReturn(testOrder);

        mockMvc.perform(get("/api/orders/order-no/ORDER20250121001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value("ORDER20250121001"));
    }

    @Test
    @DisplayName("获取用户订单列表 - 成功")
    void getUserOrders_Success() throws Exception {
        // 创建分页数据
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(testOrder), pageable, 1);
        
        // Mock服务层返回
        when(orderService.getUserOrders(eq(1001L), any(Pageable.class))).thenReturn(orderPage);

        mockMvc.perform(get("/api/orders/user/1001")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].orderNo").value("ORDER20250121001"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("根据状态获取用户订单列表 - 成功")
    void getUserOrdersByStatus_Success() throws Exception {
        // 创建分页数据
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(testOrder), pageable, 1);
        
        // Mock服务层返回
        when(orderService.getUserOrdersByStatus(eq(1001L), eq(OrderStatus.PENDING_PAYMENT), any(Pageable.class)))
                .thenReturn(orderPage);

        mockMvc.perform(get("/api/orders/user/1001/status/PENDING_PAYMENT")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING_PAYMENT"));
    }

    @Test
    @DisplayName("更新订单状态 - 成功")
    void updateOrderStatus_Success() throws Exception {
        // Mock服务层返回
        when(orderService.updateOrderStatus(1L, 1001L, OrderStatus.PAID)).thenReturn(true);

        mockMvc.perform(put("/api/orders/1/status")
                .param("userId", "1001")
                .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("取消订单 - 成功")
    void cancelOrder_Success() throws Exception {
        // Mock服务层返回
        when(orderService.cancelOrder(1L, 1001L, "用户主动取消")).thenReturn(true);

        mockMvc.perform(put("/api/orders/1/cancel")
                .param("userId", "1001")
                .param("reason", "用户主动取消"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("确认收货 - 成功")
    void confirmOrder_Success() throws Exception {
        // Mock服务层返回
        when(orderService.confirmOrder(1L, 1001L)).thenReturn(true);

        mockMvc.perform(put("/api/orders/1/confirm")
                .param("userId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("获取用户订单统计 - 成功")
    void getUserOrderStats_Success() throws Exception {
        // 创建订单统计数据
        OrderStatsResponse statsResponse = new OrderStatsResponse();
        statsResponse.setPendingPaymentCount(2);
        statsResponse.setPaidCount(5);
        statsResponse.setShippedCount(3);
        statsResponse.setDeliveredCount(8);
        statsResponse.setCompletedCount(10);
        statsResponse.setCancelledCount(1);
        statsResponse.setTotalCount(29);
        statsResponse.setTotalAmount(new BigDecimal("2890.00"));
        statsResponse.setMonthOrderCount(5);
        statsResponse.setMonthOrderAmount(new BigDecimal("580.00"));
        statsResponse.setAvgOrderAmount(new BigDecimal("99.66"));
        statsResponse.setLastOrderTime(LocalDateTime.now());

        // Mock服务层返回
        when(orderService.getUserOrderStats(1001L)).thenReturn(statsResponse);

        mockMvc.perform(get("/api/orders/user/1001/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCount").value(29))
                .andExpect(jsonPath("$.data.totalAmount").value(2890.00))
                .andExpect(jsonPath("$.data.pendingPaymentCount").value(2));
    }

    @Test
    @DisplayName("处理异常情况 - 订单不存在")
    void handleOrderNotFound() throws Exception {
        // Mock服务层抛出异常
        when(orderService.getOrderById(999L, 1001L))
                .thenThrow(new RuntimeException("订单不存在"));

        mockMvc.perform(get("/api/orders/999")
                .param("userId", "1001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }
}