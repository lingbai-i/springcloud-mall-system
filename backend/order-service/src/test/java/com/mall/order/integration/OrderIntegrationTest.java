package com.mall.order.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.entity.Order;
import com.mall.order.enums.OrderStatus;
import com.mall.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 订单服务集成测试类
 * 测试订单服务与其他服务的集成功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，实现订单服务的集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("订单服务集成测试")
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Order testOrder;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        orderRepository.deleteAll();

        // 初始化测试订单
        testOrder = new Order();
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
    @DisplayName("完整订单流程测试 - 创建到完成")
    void completeOrderFlow_Success() throws Exception {
        // 1. 创建订单
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"));

        // 获取创建的订单
        Order createdOrder = orderRepository.findAll().get(0);
        Long orderId = createdOrder.getId();
        String orderNo = createdOrder.getOrderNo();

        // 2. 查询订单详情
        mockMvc.perform(get("/api/orders/" + orderId)
                .param("userId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(orderId))
                .andExpect(jsonPath("$.data.orderNo").value(orderNo));

        // 3. 支付订单（更新状态为已支付）
        mockMvc.perform(put("/api/orders/" + orderId + "/status")
                .param("userId", "1001")
                .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        // 4. 发货（更新状态为已发货）
        mockMvc.perform(put("/api/orders/" + orderId + "/status")
                .param("userId", "1001")
                .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 5. 确认收货
        mockMvc.perform(put("/api/orders/" + orderId + "/confirm")
                .param("userId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        // 验证最终状态
        Order finalOrder = orderRepository.findById(orderId).orElse(null);
        assertNotNull(finalOrder);
        assertEquals(OrderStatus.COMPLETED, finalOrder.getStatus());
    }

    @Test
    @DisplayName("订单取消流程测试")
    void cancelOrderFlow_Success() throws Exception {
        // 保存测试订单
        Order savedOrder = orderRepository.save(testOrder);

        // 取消订单
        mockMvc.perform(put("/api/orders/" + savedOrder.getId() + "/cancel")
                .param("userId", "1001")
                .param("reason", "用户主动取消"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        // 验证订单状态
        Order cancelledOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertNotNull(cancelledOrder);
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
    }

    @Test
    @DisplayName("用户订单列表查询测试")
    void getUserOrdersList_Success() throws Exception {
        // 保存多个测试订单
        Order order1 = new Order();
        order1.setOrderNo("ORDER20250121001");
        order1.setUserId(1001L);
        order1.setStatus(OrderStatus.PENDING_PAYMENT);
        order1.setTotalAmount(new BigDecimal("299.00"));
        order1.setActualAmount(new BigDecimal("279.00"));
        order1.setReceiverName("张三");
        order1.setReceiverPhone("13800138000");
        order1.setReceiverAddress("北京市朝阳区测试地址1");
        order1.setCreateTime(LocalDateTime.now());
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setOrderNo("ORDER20250121002");
        order2.setUserId(1001L);
        order2.setStatus(OrderStatus.PAID);
        order2.setTotalAmount(new BigDecimal("199.00"));
        order2.setActualAmount(new BigDecimal("189.00"));
        order2.setReceiverName("张三");
        order2.setReceiverPhone("13800138000");
        order2.setReceiverAddress("北京市朝阳区测试地址2");
        order2.setCreateTime(LocalDateTime.now().minusHours(1));
        orderRepository.save(order2);

        // 查询用户订单列表
        mockMvc.perform(get("/api/orders/user/1001")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].orderNo").value("ORDER20250121001")) // 最新的在前
                .andExpect(jsonPath("$.data.content[1].orderNo").value("ORDER20250121002"));
    }

    @Test
    @DisplayName("根据状态查询订单测试")
    void getUserOrdersByStatus_Success() throws Exception {
        // 保存不同状态的订单
        Order pendingOrder = new Order();
        pendingOrder.setOrderNo("ORDER20250121001");
        pendingOrder.setUserId(1001L);
        pendingOrder.setStatus(OrderStatus.PENDING_PAYMENT);
        pendingOrder.setTotalAmount(new BigDecimal("299.00"));
        pendingOrder.setActualAmount(new BigDecimal("279.00"));
        pendingOrder.setReceiverName("张三");
        pendingOrder.setReceiverPhone("13800138000");
        pendingOrder.setReceiverAddress("北京市朝阳区测试地址");
        pendingOrder.setCreateTime(LocalDateTime.now());
        orderRepository.save(pendingOrder);

        Order paidOrder = new Order();
        paidOrder.setOrderNo("ORDER20250121002");
        paidOrder.setUserId(1001L);
        paidOrder.setStatus(OrderStatus.PAID);
        paidOrder.setTotalAmount(new BigDecimal("199.00"));
        paidOrder.setActualAmount(new BigDecimal("189.00"));
        paidOrder.setReceiverName("张三");
        paidOrder.setReceiverPhone("13800138000");
        paidOrder.setReceiverAddress("北京市朝阳区测试地址");
        paidOrder.setCreateTime(LocalDateTime.now());
        orderRepository.save(paidOrder);

        // 查询待支付订单
        mockMvc.perform(get("/api/orders/user/1001/status/PENDING_PAYMENT")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING_PAYMENT"));

        // 查询已支付订单
        mockMvc.perform(get("/api/orders/user/1001/status/PAID")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("PAID"));
    }

    @Test
    @DisplayName("订单统计信息测试")
    void getUserOrderStats_Success() throws Exception {
        // 保存不同状态的订单用于统计
        createOrderWithStatus(1001L, OrderStatus.PENDING_PAYMENT, new BigDecimal("100.00"));
        createOrderWithStatus(1001L, OrderStatus.PENDING_PAYMENT, new BigDecimal("200.00"));
        createOrderWithStatus(1001L, OrderStatus.PAID, new BigDecimal("150.00"));
        createOrderWithStatus(1001L, OrderStatus.SHIPPED, new BigDecimal("300.00"));
        createOrderWithStatus(1001L, OrderStatus.COMPLETED, new BigDecimal("250.00"));
        createOrderWithStatus(1001L, OrderStatus.CANCELLED, new BigDecimal("80.00"));

        // 查询订单统计
        mockMvc.perform(get("/api/orders/user/1001/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pendingPaymentCount").value(2))
                .andExpect(jsonPath("$.data.paidCount").value(1))
                .andExpect(jsonPath("$.data.shippedCount").value(1))
                .andExpect(jsonPath("$.data.completedCount").value(1))
                .andExpect(jsonPath("$.data.cancelledCount").value(1))
                .andExpect(jsonPath("$.data.totalCount").value(6));
    }

    @Test
    @DisplayName("根据订单号查询测试")
    void getOrderByOrderNo_Success() throws Exception {
        // 保存测试订单
        Order savedOrder = orderRepository.save(testOrder);

        // 根据订单号查询
        mockMvc.perform(get("/api/orders/order-no/" + savedOrder.getOrderNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderNo").value(savedOrder.getOrderNo()))
                .andExpect(jsonPath("$.data.userId").value(1001));
    }

    @Test
    @DisplayName("异常处理测试 - 订单不存在")
    void handleOrderNotFound_Test() throws Exception {
        // 查询不存在的订单
        mockMvc.perform(get("/api/orders/999")
                .param("userId", "1001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("异常处理测试 - 无效状态更新")
    void handleInvalidStatusUpdate_Test() throws Exception {
        // 保存已完成的订单
        testOrder.setStatus(OrderStatus.COMPLETED);
        Order savedOrder = orderRepository.save(testOrder);

        // 尝试取消已完成的订单（应该失败）
        mockMvc.perform(put("/api/orders/" + savedOrder.getId() + "/cancel")
                .param("userId", "1001")
                .param("reason", "用户主动取消"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    /**
     * 创建指定状态的订单用于测试
     * 
     * @param userId 用户ID
     * @param status 订单状态
     * @param amount 订单金额
     */
    private void createOrderWithStatus(Long userId, OrderStatus status, BigDecimal amount) {
        Order order = new Order();
        order.setOrderNo("ORDER" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setStatus(status);
        order.setTotalAmount(amount);
        order.setActualAmount(amount);
        order.setReceiverName("测试用户");
        order.setReceiverPhone("13800138000");
        order.setReceiverAddress("测试地址");
        order.setCreateTime(LocalDateTime.now());
        orderRepository.save(order);
    }
}