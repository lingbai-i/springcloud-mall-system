package com.mall.payment.service;

import com.mall.payment.BaseTest;
import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
import com.mall.payment.repository.PaymentOrderRepository;
import com.mall.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentService单元测试
 * 测试支付服务的核心业务逻辑
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-01
 */
@SpringBootTest
@DisplayName("支付服务测试")
class PaymentServiceTest extends BaseTest {
    
    @Mock
    private PaymentOrderRepository paymentOrderRepository;
    
    @InjectMocks
    private PaymentServiceImpl paymentService;
    
    @Override
    protected void initTestData() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("创建支付订单 - 成功")
    void testCreatePaymentOrder_Success() {
        // 准备测试数据
        String businessOrderId = generateTestBusinessOrderId();
        Long userId = generateTestUserId();
        BigDecimal amount = new BigDecimal("99.99");
        PaymentMethod paymentMethod = PaymentMethod.ALIPAY;
        String description = "测试商品";
        
        PaymentOrder expectedOrder = new PaymentOrder();
        expectedOrder.setId(1L);
        expectedOrder.setPaymentOrderId(generateTestPaymentOrderId());
        expectedOrder.setBusinessOrderId(businessOrderId);
        expectedOrder.setUserId(userId);
        expectedOrder.setAmount(amount);
        expectedOrder.setPaymentMethod(paymentMethod);
        expectedOrder.setStatus(PaymentStatus.PENDING);
        expectedOrder.setDescription(description);
        expectedOrder.setCreateTime(LocalDateTime.now());
        
        // 模拟Repository行为
        when(paymentOrderRepository.findByBusinessOrderId(businessOrderId))
            .thenReturn(Optional.empty());
        when(paymentOrderRepository.save(any(PaymentOrder.class)))
            .thenReturn(expectedOrder);
        
        // 执行测试
        PaymentOrder result = paymentService.createPaymentOrder(
            businessOrderId, userId, amount, paymentMethod, description);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(businessOrderId, result.getBusinessOrderId());
        assertEquals(userId, result.getUserId());
        assertEquals(amount, result.getAmount());
        assertEquals(paymentMethod, result.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals(description, result.getDescription());
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByBusinessOrderId(businessOrderId);
        verify(paymentOrderRepository).save(any(PaymentOrder.class));
    }
    
    @Test
    @DisplayName("创建支付订单 - 业务订单已存在")
    void testCreatePaymentOrder_BusinessOrderExists() {
        // 准备测试数据
        String businessOrderId = generateTestBusinessOrderId();
        Long userId = generateTestUserId();
        BigDecimal amount = new BigDecimal("99.99");
        PaymentMethod paymentMethod = PaymentMethod.ALIPAY;
        String description = "测试商品";
        
        PaymentOrder existingOrder = new PaymentOrder();
        existingOrder.setBusinessOrderId(businessOrderId);
        
        // 模拟Repository行为 - 业务订单已存在
        when(paymentOrderRepository.findByBusinessOrderId(businessOrderId))
            .thenReturn(Optional.of(existingOrder));
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPaymentOrder(
                businessOrderId, userId, amount, paymentMethod, description);
        });
        
        assertTrue(exception.getMessage().contains("业务订单已存在"));
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByBusinessOrderId(businessOrderId);
        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
    }
    
    @Test
    @DisplayName("根据支付订单ID查询 - 成功")
    void testGetPaymentOrderById_Success() {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        PaymentOrder expectedOrder = new PaymentOrder();
        expectedOrder.setPaymentOrderId(paymentOrderId);
        expectedOrder.setStatus(PaymentStatus.PENDING);
        
        // 模拟Repository行为
        when(paymentOrderRepository.findByPaymentOrderId(paymentOrderId))
            .thenReturn(Optional.of(expectedOrder));
        
        // 执行测试
        Optional<PaymentOrder> result = paymentService.getPaymentOrderById(paymentOrderId);
        
        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(paymentOrderId, result.get().getPaymentOrderId());
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByPaymentOrderId(paymentOrderId);
    }
    
    @Test
    @DisplayName("根据支付订单ID查询 - 订单不存在")
    void testGetPaymentOrderById_NotFound() {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        
        // 模拟Repository行为 - 订单不存在
        when(paymentOrderRepository.findByPaymentOrderId(paymentOrderId))
            .thenReturn(Optional.empty());
        
        // 执行测试
        Optional<PaymentOrder> result = paymentService.getPaymentOrderById(paymentOrderId);
        
        // 验证结果
        assertFalse(result.isPresent());
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByPaymentOrderId(paymentOrderId);
    }
    
    @Test
    @DisplayName("根据业务订单ID查询 - 成功")
    void testGetPaymentOrderByBusinessOrderId_Success() {
        // 准备测试数据
        String businessOrderId = generateTestBusinessOrderId();
        PaymentOrder expectedOrder = new PaymentOrder();
        expectedOrder.setBusinessOrderId(businessOrderId);
        expectedOrder.setStatus(PaymentStatus.PENDING);
        
        // 模拟Repository行为
        when(paymentOrderRepository.findByBusinessOrderId(businessOrderId))
            .thenReturn(Optional.of(expectedOrder));
        
        // 执行测试
        Optional<PaymentOrder> result = paymentService.getPaymentOrderByBusinessOrderId(businessOrderId);
        
        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(businessOrderId, result.get().getBusinessOrderId());
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByBusinessOrderId(businessOrderId);
    }
    
    @Test
    @DisplayName("分页查询支付订单 - 成功")
    void testQueryPaymentOrders_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<PaymentOrder> orders = Arrays.asList(
            createTestPaymentOrder("PAY_001", PaymentStatus.PENDING),
            createTestPaymentOrder("PAY_002", PaymentStatus.SUCCESS)
        );
        Page<PaymentOrder> expectedPage = new PageImpl<>(orders, pageable, orders.size());
        
        // 模拟Repository行为
        when(paymentOrderRepository.findAll(pageable))
            .thenReturn(expectedPage);
        
        // 执行测试
        Page<PaymentOrder> result = paymentService.queryPaymentOrders(pageable);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("PAY_001", result.getContent().get(0).getPaymentOrderId());
        assertEquals("PAY_002", result.getContent().get(1).getPaymentOrderId());
        
        // 验证Repository调用
        verify(paymentOrderRepository).findAll(pageable);
    }
    
    @Test
    @DisplayName("查询用户支付订单 - 成功")
    void testGetUserPaymentOrders_Success() {
        // 准备测试数据
        Long userId = generateTestUserId();
        List<PaymentOrder> expectedOrders = Arrays.asList(
            createTestPaymentOrder("PAY_001", PaymentStatus.PENDING),
            createTestPaymentOrder("PAY_002", PaymentStatus.SUCCESS)
        );
        expectedOrders.forEach(order -> order.setUserId(userId));
        
        // 模拟Repository行为
        when(paymentOrderRepository.findByUserIdOrderByCreateTimeDesc(userId))
            .thenReturn(expectedOrders);
        
        // 执行测试
        List<PaymentOrder> result = paymentService.getUserPaymentOrders(userId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userId, result.get(1).getUserId());
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByUserIdOrderByCreateTimeDesc(userId);
    }
    
    @Test
    @DisplayName("更新支付状态 - 成功")
    void testUpdatePaymentStatus_Success() {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        PaymentStatus newStatus = PaymentStatus.SUCCESS;
        String thirdPartyOrderId = "THIRD_PARTY_001";
        
        PaymentOrder existingOrder = createTestPaymentOrder(paymentOrderId, PaymentStatus.PENDING);
        PaymentOrder updatedOrder = createTestPaymentOrder(paymentOrderId, newStatus);
        updatedOrder.setThirdPartyOrderId(thirdPartyOrderId);
        updatedOrder.setPayTime(LocalDateTime.now());
        
        // 模拟Repository行为
        when(paymentOrderRepository.findByPaymentOrderId(paymentOrderId))
            .thenReturn(Optional.of(existingOrder));
        when(paymentOrderRepository.save(any(PaymentOrder.class)))
            .thenReturn(updatedOrder);
        
        // 执行测试
        boolean result = paymentService.updatePaymentStatus(paymentOrderId, newStatus, thirdPartyOrderId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByPaymentOrderId(paymentOrderId);
        verify(paymentOrderRepository).save(any(PaymentOrder.class));
    }
    
    @Test
    @DisplayName("更新支付状态 - 订单不存在")
    void testUpdatePaymentStatus_OrderNotFound() {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        PaymentStatus newStatus = PaymentStatus.SUCCESS;
        String thirdPartyOrderId = "THIRD_PARTY_001";
        
        // 模拟Repository行为 - 订单不存在
        when(paymentOrderRepository.findByPaymentOrderId(paymentOrderId))
            .thenReturn(Optional.empty());
        
        // 执行测试
        boolean result = paymentService.updatePaymentStatus(paymentOrderId, newStatus, thirdPartyOrderId);
        
        // 验证结果
        assertFalse(result);
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByPaymentOrderId(paymentOrderId);
        verify(paymentOrderRepository, never()).save(any(PaymentOrder.class));
    }
    
    /**
     * 创建测试用的支付订单
     * 
     * @param paymentOrderId 支付订单ID
     * @param status 支付状态
     * @return 支付订单对象
     */
    private PaymentOrder createTestPaymentOrder(String paymentOrderId, PaymentStatus status) {
        PaymentOrder order = new PaymentOrder();
        order.setPaymentOrderId(paymentOrderId);
        order.setBusinessOrderId(generateTestBusinessOrderId());
        order.setUserId(generateTestUserId());
        order.setAmount(new BigDecimal("99.99"));
        order.setPaymentMethod(PaymentMethod.ALIPAY);
        order.setStatus(status);
        order.setDescription("测试商品");
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
}