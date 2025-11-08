package com.mall.payment.repository;

import com.mall.payment.BaseTest;
import com.mall.payment.entity.PaymentOrder;
import com.mall.payment.enums.PaymentMethod;
import com.mall.payment.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PaymentOrderRepository集成测试
 * 测试支付订单数据访问层的数据库操作
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-01
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("支付订单Repository测试")
class PaymentOrderRepositoryTest extends BaseTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private PaymentOrderRepository paymentOrderRepository;
    
    @Test
    @DisplayName("保存支付订单 - 成功")
    void testSavePaymentOrder_Success() {
        // 准备测试数据
        PaymentOrder order = createTestPaymentOrder();
        
        // 执行测试
        PaymentOrder savedOrder = paymentOrderRepository.save(order);
        
        // 验证结果
        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());
        assertEquals(order.getPaymentOrderId(), savedOrder.getPaymentOrderId());
        assertEquals(order.getBusinessOrderId(), savedOrder.getBusinessOrderId());
        assertEquals(order.getUserId(), savedOrder.getUserId());
        assertEquals(order.getAmount(), savedOrder.getAmount());
        assertEquals(order.getPaymentMethod(), savedOrder.getPaymentMethod());
        assertEquals(order.getStatus(), savedOrder.getStatus());
        assertEquals(order.getDescription(), savedOrder.getDescription());
        assertNotNull(savedOrder.getCreateTime());
    }
    
    @Test
    @DisplayName("根据支付订单ID查询 - 成功")
    void testFindByPaymentOrderId_Success() {
        // 准备测试数据
        PaymentOrder order = createTestPaymentOrder();
        entityManager.persistAndFlush(order);
        
        // 执行测试
        Optional<PaymentOrder> result = paymentOrderRepository.findByPaymentOrderId(order.getPaymentOrderId());
        
        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(order.getPaymentOrderId(), result.get().getPaymentOrderId());
        assertEquals(order.getBusinessOrderId(), result.get().getBusinessOrderId());
        assertEquals(order.getUserId(), result.get().getUserId());
    }
    
    @Test
    @DisplayName("根据支付订单ID查询 - 订单不存在")
    void testFindByPaymentOrderId_NotFound() {
        // 准备测试数据
        String nonExistentPaymentOrderId = "NON_EXISTENT_PAY_ORDER";
        
        // 执行测试
        Optional<PaymentOrder> result = paymentOrderRepository.findByPaymentOrderId(nonExistentPaymentOrderId);
        
        // 验证结果
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("根据业务订单ID查询 - 成功")
    void testFindByBusinessOrderId_Success() {
        // 准备测试数据
        PaymentOrder order = createTestPaymentOrder();
        entityManager.persistAndFlush(order);
        
        // 执行测试
        Optional<PaymentOrder> result = paymentOrderRepository.findByBusinessOrderId(order.getBusinessOrderId());
        
        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(order.getBusinessOrderId(), result.get().getBusinessOrderId());
        assertEquals(order.getPaymentOrderId(), result.get().getPaymentOrderId());
        assertEquals(order.getUserId(), result.get().getUserId());
    }
    
    @Test
    @DisplayName("根据业务订单ID查询 - 订单不存在")
    void testFindByBusinessOrderId_NotFound() {
        // 准备测试数据
        String nonExistentBusinessOrderId = "NON_EXISTENT_BIZ_ORDER";
        
        // 执行测试
        Optional<PaymentOrder> result = paymentOrderRepository.findByBusinessOrderId(nonExistentBusinessOrderId);
        
        // 验证结果
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("根据用户ID查询支付订单列表 - 成功")
    void testFindByUserIdOrderByCreateTimeDesc_Success() {
        // 准备测试数据
        Long userId = generateTestUserId();
        
        PaymentOrder order1 = createTestPaymentOrder();
        order1.setUserId(userId);
        order1.setPaymentOrderId("PAY_001");
        order1.setCreateTime(LocalDateTime.now().minusHours(2));
        
        PaymentOrder order2 = createTestPaymentOrder();
        order2.setUserId(userId);
        order2.setPaymentOrderId("PAY_002");
        order2.setCreateTime(LocalDateTime.now().minusHours(1));
        
        PaymentOrder order3 = createTestPaymentOrder();
        order3.setUserId(userId + 1); // 不同用户的订单
        order3.setPaymentOrderId("PAY_003");
        
        entityManager.persistAndFlush(order1);
        entityManager.persistAndFlush(order2);
        entityManager.persistAndFlush(order3);
        
        // 执行测试
        List<PaymentOrder> result = paymentOrderRepository.findByUserIdOrderByCreateTimeDesc(userId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        // 验证按创建时间降序排列
        assertEquals("PAY_002", result.get(0).getPaymentOrderId()); // 最新的订单
        assertEquals("PAY_001", result.get(1).getPaymentOrderId()); // 较早的订单
        // 验证都是指定用户的订单
        result.forEach(order -> assertEquals(userId, order.getUserId()));
    }
    
    @Test
    @DisplayName("根据用户ID查询支付订单列表 - 用户无订单")
    void testFindByUserIdOrderByCreateTimeDesc_NoOrders() {
        // 准备测试数据
        Long userId = generateTestUserId();
        
        // 执行测试
        List<PaymentOrder> result = paymentOrderRepository.findByUserIdOrderByCreateTimeDesc(userId);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("分页查询支付订单 - 成功")
    void testFindAllWithPagination_Success() {
        // 准备测试数据
        for (int i = 1; i <= 15; i++) {
            PaymentOrder order = createTestPaymentOrder();
            order.setPaymentOrderId("PAY_" + String.format("%03d", i));
            entityManager.persistAndFlush(order);
        }
        
        // 执行测试 - 查询第一页，每页10条
        Page<PaymentOrder> result = paymentOrderRepository.findAll(PageRequest.of(0, 10));
        
        // 验证结果
        assertNotNull(result);
        assertEquals(10, result.getContent().size()); // 当前页数据量
        assertEquals(15, result.getTotalElements()); // 总数据量
        assertEquals(2, result.getTotalPages()); // 总页数
        assertEquals(0, result.getNumber()); // 当前页码
        assertTrue(result.hasNext()); // 有下一页
        assertFalse(result.hasPrevious()); // 无上一页
    }
    
    @Test
    @DisplayName("根据状态查询支付订单 - 成功")
    void testFindByStatus_Success() {
        // 准备测试数据
        PaymentOrder pendingOrder1 = createTestPaymentOrder();
        pendingOrder1.setPaymentOrderId("PAY_PENDING_001");
        pendingOrder1.setStatus(PaymentStatus.PENDING);
        
        PaymentOrder pendingOrder2 = createTestPaymentOrder();
        pendingOrder2.setPaymentOrderId("PAY_PENDING_002");
        pendingOrder2.setStatus(PaymentStatus.PENDING);
        
        PaymentOrder successOrder = createTestPaymentOrder();
        successOrder.setPaymentOrderId("PAY_SUCCESS_001");
        successOrder.setStatus(PaymentStatus.SUCCESS);
        
        entityManager.persistAndFlush(pendingOrder1);
        entityManager.persistAndFlush(pendingOrder2);
        entityManager.persistAndFlush(successOrder);
        
        // 执行测试
        List<PaymentOrder> pendingOrders = paymentOrderRepository.findByStatus(PaymentStatus.PENDING);
        List<PaymentOrder> successOrders = paymentOrderRepository.findByStatus(PaymentStatus.SUCCESS);
        
        // 验证结果
        assertNotNull(pendingOrders);
        assertEquals(2, pendingOrders.size());
        pendingOrders.forEach(order -> assertEquals(PaymentStatus.PENDING, order.getStatus()));
        
        assertNotNull(successOrders);
        assertEquals(1, successOrders.size());
        assertEquals(PaymentStatus.SUCCESS, successOrders.get(0).getStatus());
    }
    
    @Test
    @DisplayName("根据支付方式查询支付订单 - 成功")
    void testFindByPaymentMethod_Success() {
        // 准备测试数据
        PaymentOrder alipayOrder1 = createTestPaymentOrder();
        alipayOrder1.setPaymentOrderId("PAY_ALIPAY_001");
        alipayOrder1.setPaymentMethod(PaymentMethod.ALIPAY);
        
        PaymentOrder alipayOrder2 = createTestPaymentOrder();
        alipayOrder2.setPaymentOrderId("PAY_ALIPAY_002");
        alipayOrder2.setPaymentMethod(PaymentMethod.ALIPAY);
        
        PaymentOrder wechatOrder = createTestPaymentOrder();
        wechatOrder.setPaymentOrderId("PAY_WECHAT_001");
        wechatOrder.setPaymentMethod(PaymentMethod.WECHAT);
        
        entityManager.persistAndFlush(alipayOrder1);
        entityManager.persistAndFlush(alipayOrder2);
        entityManager.persistAndFlush(wechatOrder);
        
        // 执行测试
        List<PaymentOrder> alipayOrders = paymentOrderRepository.findByPaymentMethod(PaymentMethod.ALIPAY);
        List<PaymentOrder> wechatOrders = paymentOrderRepository.findByPaymentMethod(PaymentMethod.WECHAT);
        
        // 验证结果
        assertNotNull(alipayOrders);
        assertEquals(2, alipayOrders.size());
        alipayOrders.forEach(order -> assertEquals(PaymentMethod.ALIPAY, order.getPaymentMethod()));
        
        assertNotNull(wechatOrders);
        assertEquals(1, wechatOrders.size());
        assertEquals(PaymentMethod.WECHAT, wechatOrders.get(0).getPaymentMethod());
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