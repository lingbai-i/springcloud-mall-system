package com.mall.payment.service;

import com.mall.payment.BaseTest;
import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.entity.RefundOrder;
import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
import com.mall.payment.enums.RefundStatus;
import com.mall.payment.repository.PaymentOrderRepository;
import com.mall.payment.repository.RefundOrderRepository;
import com.mall.payment.service.impl.RefundServiceImpl;
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
 * RefundService单元测试
 * 测试退款服务的核心业务逻辑
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-01
 */
@SpringBootTest
@DisplayName("退款服务测试")
class RefundServiceTest extends BaseTest {
    
    @Mock
    private RefundOrderRepository refundOrderRepository;
    
    @Mock
    private PaymentOrderRepository paymentOrderRepository;
    
    @InjectMocks
    private RefundServiceImpl refundService;
    
    @Override
    protected void initTestData() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("创建退款订单 - 成功")
    void testCreateRefundOrder_Success() {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        Long userId = generateTestUserId();
        BigDecimal refundAmount = new BigDecimal("50.00");
        String reason = "商品质量问题";
        
        PaymentOrder paymentOrder = createTestPaymentOrder(paymentOrderId, PaymentStatus.SUCCESS);
        paymentOrder.setAmount(new BigDecimal("100.00"));
        
        RefundOrder expectedRefund = new RefundOrder();
        expectedRefund.setId(1L);
        expectedRefund.setRefundOrderId("REF_" + System.currentTimeMillis());
        expectedRefund.setPaymentOrderId(paymentOrderId);
        expectedRefund.setUserId(userId);
        expectedRefund.setRefundAmount(refundAmount);
        expectedRefund.setReason(reason);
        expectedRefund.setStatus(RefundStatus.PENDING);
        expectedRefund.setCreateTime(LocalDateTime.now());
        
        // 模拟Repository行为
        when(paymentOrderRepository.findByPaymentOrderId(paymentOrderId))
            .thenReturn(Optional.of(paymentOrder));
        when(refundOrderRepository.save(any(RefundOrder.class)))
            .thenReturn(expectedRefund);
        
        // 执行测试
        RefundOrder result = refundService.createRefundOrder(
            paymentOrderId, userId, refundAmount, reason);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(paymentOrderId, result.getPaymentOrderId());
        assertEquals(userId, result.getUserId());
        assertEquals(refundAmount, result.getRefundAmount());
        assertEquals(reason, result.getReason());
        assertEquals(RefundStatus.PENDING, result.getStatus());
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByPaymentOrderId(paymentOrderId);
        verify(refundOrderRepository).save(any(RefundOrder.class));
    }
    
    @Test
    @DisplayName("创建退款订单 - 支付订单不存在")
    void testCreateRefundOrder_PaymentOrderNotFound() {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        Long userId = generateTestUserId();
        BigDecimal refundAmount = new BigDecimal("50.00");
        String reason = "商品质量问题";
        
        // 模拟Repository行为 - 支付订单不存在
        when(paymentOrderRepository.findByPaymentOrderId(paymentOrderId))
            .thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            refundService.createRefundOrder(paymentOrderId, userId, refundAmount, reason);
        });
        
        assertTrue(exception.getMessage().contains("支付订单不存在"));
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByPaymentOrderId(paymentOrderId);
        verify(refundOrderRepository, never()).save(any(RefundOrder.class));
    }
    
    @Test
    @DisplayName("创建退款订单 - 支付订单状态不允许退款")
    void testCreateRefundOrder_PaymentOrderStatusNotAllowed() {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        Long userId = generateTestUserId();
        BigDecimal refundAmount = new BigDecimal("50.00");
        String reason = "商品质量问题";
        
        PaymentOrder paymentOrder = createTestPaymentOrder(paymentOrderId, PaymentStatus.PENDING);
        
        // 模拟Repository行为
        when(paymentOrderRepository.findByPaymentOrderId(paymentOrderId))
            .thenReturn(Optional.of(paymentOrder));
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            refundService.createRefundOrder(paymentOrderId, userId, refundAmount, reason);
        });
        
        assertTrue(exception.getMessage().contains("支付订单状态不允许退款"));
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByPaymentOrderId(paymentOrderId);
        verify(refundOrderRepository, never()).save(any(RefundOrder.class));
    }
    
    @Test
    @DisplayName("创建退款订单 - 退款金额超过支付金额")
    void testCreateRefundOrder_RefundAmountExceedsPaymentAmount() {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        Long userId = generateTestUserId();
        BigDecimal refundAmount = new BigDecimal("150.00"); // 超过支付金额
        String reason = "商品质量问题";
        
        PaymentOrder paymentOrder = createTestPaymentOrder(paymentOrderId, PaymentStatus.SUCCESS);
        paymentOrder.setAmount(new BigDecimal("100.00"));
        
        // 模拟Repository行为
        when(paymentOrderRepository.findByPaymentOrderId(paymentOrderId))
            .thenReturn(Optional.of(paymentOrder));
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            refundService.createRefundOrder(paymentOrderId, userId, refundAmount, reason);
        });
        
        assertTrue(exception.getMessage().contains("退款金额不能超过支付金额"));
        
        // 验证Repository调用
        verify(paymentOrderRepository).findByPaymentOrderId(paymentOrderId);
        verify(refundOrderRepository, never()).save(any(RefundOrder.class));
    }
    
    @Test
    @DisplayName("根据退款订单ID查询 - 成功")
    void testGetRefundOrderById_Success() {
        // 准备测试数据
        String refundOrderId = "REF_" + System.currentTimeMillis();
        RefundOrder expectedRefund = new RefundOrder();
        expectedRefund.setRefundOrderId(refundOrderId);
        expectedRefund.setStatus(RefundStatus.PENDING);
        
        // 模拟Repository行为
        when(refundOrderRepository.findByRefundOrderId(refundOrderId))
            .thenReturn(Optional.of(expectedRefund));
        
        // 执行测试
        Optional<RefundOrder> result = refundService.getRefundOrderById(refundOrderId);
        
        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(refundOrderId, result.get().getRefundOrderId());
        
        // 验证Repository调用
        verify(refundOrderRepository).findByRefundOrderId(refundOrderId);
    }
    
    @Test
    @DisplayName("根据支付订单ID查询退款订单列表 - 成功")
    void testGetRefundOrdersByPaymentOrderId_Success() {
        // 准备测试数据
        String paymentOrderId = generateTestPaymentOrderId();
        List<RefundOrder> expectedRefunds = Arrays.asList(
            createTestRefundOrder("REF_001", paymentOrderId, RefundStatus.PENDING),
            createTestRefundOrder("REF_002", paymentOrderId, RefundStatus.SUCCESS)
        );
        
        // 模拟Repository行为
        when(refundOrderRepository.findByPaymentOrderIdOrderByCreateTimeDesc(paymentOrderId))
            .thenReturn(expectedRefunds);
        
        // 执行测试
        List<RefundOrder> result = refundService.getRefundOrdersByPaymentOrderId(paymentOrderId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(paymentOrderId, result.get(0).getPaymentOrderId());
        assertEquals(paymentOrderId, result.get(1).getPaymentOrderId());
        
        // 验证Repository调用
        verify(refundOrderRepository).findByPaymentOrderIdOrderByCreateTimeDesc(paymentOrderId);
    }
    
    @Test
    @DisplayName("分页查询退款订单 - 成功")
    void testQueryRefundOrders_Success() {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 10);
        List<RefundOrder> refunds = Arrays.asList(
            createTestRefundOrder("REF_001", "PAY_001", RefundStatus.PENDING),
            createTestRefundOrder("REF_002", "PAY_002", RefundStatus.SUCCESS)
        );
        Page<RefundOrder> expectedPage = new PageImpl<>(refunds, pageable, refunds.size());
        
        // 模拟Repository行为
        when(refundOrderRepository.findAll(pageable))
            .thenReturn(expectedPage);
        
        // 执行测试
        Page<RefundOrder> result = refundService.queryRefundOrders(pageable);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("REF_001", result.getContent().get(0).getRefundOrderId());
        assertEquals("REF_002", result.getContent().get(1).getRefundOrderId());
        
        // 验证Repository调用
        verify(refundOrderRepository).findAll(pageable);
    }
    
    @Test
    @DisplayName("查询用户退款订单 - 成功")
    void testGetUserRefundOrders_Success() {
        // 准备测试数据
        Long userId = generateTestUserId();
        List<RefundOrder> expectedRefunds = Arrays.asList(
            createTestRefundOrder("REF_001", "PAY_001", RefundStatus.PENDING),
            createTestRefundOrder("REF_002", "PAY_002", RefundStatus.SUCCESS)
        );
        expectedRefunds.forEach(refund -> refund.setUserId(userId));
        
        // 模拟Repository行为
        when(refundOrderRepository.findByUserIdOrderByCreateTimeDesc(userId))
            .thenReturn(expectedRefunds);
        
        // 执行测试
        List<RefundOrder> result = refundService.getUserRefundOrders(userId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(userId, result.get(0).getUserId());
        assertEquals(userId, result.get(1).getUserId());
        
        // 验证Repository调用
        verify(refundOrderRepository).findByUserIdOrderByCreateTimeDesc(userId);
    }
    
    @Test
    @DisplayName("更新退款状态 - 成功")
    void testUpdateRefundStatus_Success() {
        // 准备测试数据
        String refundOrderId = "REF_" + System.currentTimeMillis();
        RefundStatus newStatus = RefundStatus.SUCCESS;
        String thirdPartyRefundId = "THIRD_PARTY_REF_001";
        
        RefundOrder existingRefund = createTestRefundOrder(refundOrderId, "PAY_001", RefundStatus.PENDING);
        RefundOrder updatedRefund = createTestRefundOrder(refundOrderId, "PAY_001", newStatus);
        updatedRefund.setThirdPartyRefundId(thirdPartyRefundId);
        updatedRefund.setRefundTime(LocalDateTime.now());
        
        // 模拟Repository行为
        when(refundOrderRepository.findByRefundOrderId(refundOrderId))
            .thenReturn(Optional.of(existingRefund));
        when(refundOrderRepository.save(any(RefundOrder.class)))
            .thenReturn(updatedRefund);
        
        // 执行测试
        boolean result = refundService.updateRefundStatus(refundOrderId, newStatus, thirdPartyRefundId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证Repository调用
        verify(refundOrderRepository).findByRefundOrderId(refundOrderId);
        verify(refundOrderRepository).save(any(RefundOrder.class));
    }
    
    @Test
    @DisplayName("更新退款状态 - 退款订单不存在")
    void testUpdateRefundStatus_RefundOrderNotFound() {
        // 准备测试数据
        String refundOrderId = "REF_" + System.currentTimeMillis();
        RefundStatus newStatus = RefundStatus.SUCCESS;
        String thirdPartyRefundId = "THIRD_PARTY_REF_001";
        
        // 模拟Repository行为 - 退款订单不存在
        when(refundOrderRepository.findByRefundOrderId(refundOrderId))
            .thenReturn(Optional.empty());
        
        // 执行测试
        boolean result = refundService.updateRefundStatus(refundOrderId, newStatus, thirdPartyRefundId);
        
        // 验证结果
        assertFalse(result);
        
        // 验证Repository调用
        verify(refundOrderRepository).findByRefundOrderId(refundOrderId);
        verify(refundOrderRepository, never()).save(any(RefundOrder.class));
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
        order.setAmount(new BigDecimal("100.00"));
        order.setPaymentMethod(PaymentMethod.ALIPAY);
        order.setStatus(status);
        order.setDescription("测试商品");
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
    
    /**
     * 创建测试用的退款订单
     * 
     * @param refundOrderId 退款订单ID
     * @param paymentOrderId 支付订单ID
     * @param status 退款状态
     * @return 退款订单对象
     */
    private RefundOrder createTestRefundOrder(String refundOrderId, String paymentOrderId, RefundStatus status) {
        RefundOrder refund = new RefundOrder();
        refund.setRefundOrderId(refundOrderId);
        refund.setPaymentOrderId(paymentOrderId);
        refund.setUserId(generateTestUserId());
        refund.setRefundAmount(new BigDecimal("50.00"));
        refund.setReason("测试退款");
        refund.setStatus(status);
        refund.setCreateTime(LocalDateTime.now());
        return refund;
    }
}