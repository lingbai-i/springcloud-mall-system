package com.mall.payment.controller;

import com.mall.payment.BaseTest;
import com.mall.payment.dto.PaymentOrderCreateRequest;
import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
import com.mall.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PaymentController集成测试
 * 测试支付控制器的HTTP接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-01
 */
@SpringBootTest
@AutoConfigureTestMvc
@DisplayName("支付控制器测试")
class PaymentControllerTest extends BaseTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PaymentService paymentService;
    
    @Test
    @DisplayName("创建支付订单 - 成功")
    @WithMockUser(roles = "USER")
    void testCreatePaymentOrder_Success() throws Exception {
        // 准备测试数据
        PaymentOrderCreateRequest request = new PaymentOrderCreateRequest();
        request.setBusinessOrderId(generateTestBusinessOrderId());
        request.setUserId(generateTestUserId());
        request.setAmount(new BigDecimal("99.99"));
        request.setPaymentMethod(PaymentMethod.ALIPAY);
        request.setDescription("测试商品");
        
        PaymentOrder expectedOrder = createTestPaymentOrder();
        
        // 模拟Service行为
        when(paymentService.createPaymentOrder(
            anyString(), anyLong(), any(BigDecimal.class), 
            any(PaymentMethod.class), anyString()))
            .thenReturn(expectedOrder);
        
        // 执行测试
        mockMvc.perform(post("/api/payment/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentOrderId").value(expectedOrder.getPaymentOrderId()))
                .andExpect(jsonPath("$.data.businessOrderId").value(expectedOrder.getBusinessOrderId()))
                .andExpect(jsonPath("$.data.amount").value(expectedOrder.getAmount().doubleValue()))
                .andExpect(jsonPath("$.data.paymentMethod").value(expectedOrder.getPaymentMethod().toString()))
                .andExpect(jsonPath("$.data.status").value(expectedOrder.getStatus().toString()));
    }
    
    @Test
    @DisplayName("创建支付订单 - 参数验证失败")
    @WithMockUser(roles = "USER")
    void testCreatePaymentOrder_ValidationFailed() throws Exception {
        // 准备测试数据 - 缺少必要参数
        PaymentOrderCreateRequest request = new PaymentOrderCreateRequest();
        // 不设置必要参数，触发验证失败
        
        // 执行测试
        mockMvc.perform(post("/api/payment/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("创建支付订单 - 未授权访问")
    void testCreatePaymentOrder_Unauthorized() throws Exception {
        // 准备测试数据
        PaymentOrderCreateRequest request = new PaymentOrderCreateRequest();
        request.setBusinessOrderId(generateTestBusinessOrderId());
        request.setUserId(generateTestUserId());
        request.setAmount(new BigDecimal("99.99"));
        request.setPaymentMethod(PaymentMethod.ALIPAY);
        request.setDescription("测试商品");
        
        // 执行测试 - 不提供认证信息
        mockMvc.perform(post("/api/payment/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("根据支付订单ID查询 - 成功")
    @WithMockUser(roles = "USER")
    void testGetPaymentOrder_Success() throws Exception {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        PaymentOrder expectedOrder = createTestPaymentOrder();
        expectedOrder.setPaymentOrderId(paymentOrderId);
        
        // 模拟Service行为
        when(paymentService.getPaymentOrderById(paymentOrderId))
            .thenReturn(Optional.of(expectedOrder));
        
        // 执行测试
        mockMvc.perform(get("/api/payment/orders/{paymentOrderId}", paymentOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentOrderId").value(paymentOrderId))
                .andExpect(jsonPath("$.data.businessOrderId").value(expectedOrder.getBusinessOrderId()))
                .andExpect(jsonPath("$.data.amount").value(expectedOrder.getAmount().doubleValue()));
    }
    
    @Test
    @DisplayName("根据支付订单ID查询 - 订单不存在")
    @WithMockUser(roles = "USER")
    void testGetPaymentOrder_NotFound() throws Exception {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        
        // 模拟Service行为 - 订单不存在
        when(paymentService.getPaymentOrderById(paymentOrderId))
            .thenReturn(Optional.empty());
        
        // 执行测试
        mockMvc.perform(get("/api/payment/orders/{paymentOrderId}", paymentOrderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("支付订单不存在"));
    }
    
    @Test
    @DisplayName("根据业务订单ID查询支付订单 - 成功")
    @WithMockUser(roles = "USER")
    void testGetPaymentOrderByBusinessOrderId_Success() throws Exception {
        // 准备测试数据
        String businessOrderId = generateTestBusinessOrderId();
        PaymentOrder expectedOrder = createTestPaymentOrder();
        expectedOrder.setBusinessOrderId(businessOrderId);
        
        // 模拟Service行为
        when(paymentService.getPaymentOrderByBusinessOrderId(businessOrderId))
            .thenReturn(Optional.of(expectedOrder));
        
        // 执行测试
        mockMvc.perform(get("/api/payment/orders/business/{businessOrderId}", businessOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.businessOrderId").value(businessOrderId))
                .andExpect(jsonPath("$.data.paymentOrderId").value(expectedOrder.getPaymentOrderId()));
    }
    
    @Test
    @DisplayName("分页查询支付订单 - 成功")
    @WithMockUser(roles = "ADMIN")
    void testQueryPaymentOrders_Success() throws Exception {
        // 准备测试数据
        List<PaymentOrder> orders = Arrays.asList(
            createTestPaymentOrder(),
            createTestPaymentOrder()
        );
        Page<PaymentOrder> page = new PageImpl<>(orders, PageRequest.of(0, 10), orders.size());
        
        // 模拟Service行为
        when(paymentService.queryPaymentOrders(any()))
            .thenReturn(page);
        
        // 执行测试
        mockMvc.perform(get("/api/payment/orders")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }
    
    @Test
    @DisplayName("分页查询支付订单 - 权限不足")
    @WithMockUser(roles = "USER")
    void testQueryPaymentOrders_AccessDenied() throws Exception {
        // 执行测试 - 普通用户无权限访问管理员接口
        mockMvc.perform(get("/api/payment/orders")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("查询用户支付订单列表 - 成功")
    @WithMockUser(roles = "USER")
    void testGetUserPaymentOrders_Success() throws Exception {
        // 准备测试数据
        Long userId = generateTestUserId();
        List<PaymentOrder> orders = Arrays.asList(
            createTestPaymentOrder(),
            createTestPaymentOrder()
        );
        orders.forEach(order -> order.setUserId(userId));
        
        // 模拟Service行为
        when(paymentService.getUserPaymentOrders(userId))
            .thenReturn(orders);
        
        // 执行测试
        mockMvc.perform(get("/api/payment/orders/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].userId").value(userId.intValue()))
                .andExpect(jsonPath("$.data[1].userId").value(userId.intValue()));
    }
    
    @Test
    @DisplayName("发起支付 - 成功")
    @WithMockUser(roles = "USER")
    void testInitiatePayment_Success() throws Exception {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        String expectedPaymentUrl = "https://example.com/pay?order=" + paymentOrderId;
        
        // 模拟Service行为
        when(paymentService.initiatePayment(paymentOrderId, anyString()))
            .thenReturn(expectedPaymentUrl);
        
        // 执行测试
        mockMvc.perform(post("/api/payment/orders/{paymentOrderId}/initiate", paymentOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentUrl").value(expectedPaymentUrl));
    }
    
    @Test
    @DisplayName("取消支付订单 - 成功")
    @WithMockUser(roles = "USER")
    void testCancelPaymentOrder_Success() throws Exception {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        
        // 模拟Service行为
        when(paymentService.cancelPaymentOrder(paymentOrderId))
            .thenReturn(true);
        
        // 执行测试
        mockMvc.perform(post("/api/payment/orders/{paymentOrderId}/cancel", paymentOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("支付订单取消成功"));
    }
    
    @Test
    @DisplayName("查询支付状态 - 成功")
    @WithMockUser(roles = "USER")
    void testQueryPaymentStatus_Success() throws Exception {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        PaymentOrder order = createTestPaymentOrder();
        order.setPaymentOrderId(paymentOrderId);
        order.setStatus(PaymentStatus.SUCCESS);
        
        // 模拟Service行为
        when(paymentService.getPaymentOrderById(paymentOrderId))
            .thenReturn(Optional.of(order));
        
        // 执行测试
        mockMvc.perform(get("/api/payment/orders/{paymentOrderId}/status", paymentOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentOrderId").value(paymentOrderId))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }
    
    /**
     * 创建测试用的支付订单
     * 
     * @return 支付订单对象
     */
    private PaymentOrder createTestPaymentOrder() {
        PaymentOrder order = new PaymentOrder();
        order.setPaymentOrderId(generateTestPaymentOrderId());
        order.setBusinessOrderId(generateTestBusinessOrderId());
        order.setUserId(generateTestUserId());
        order.setAmount(new BigDecimal("99.99"));
        order.setPaymentMethod(PaymentMethod.ALIPAY);
        order.setStatus(PaymentStatus.PENDING);
        order.setDescription("测试商品");
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
}