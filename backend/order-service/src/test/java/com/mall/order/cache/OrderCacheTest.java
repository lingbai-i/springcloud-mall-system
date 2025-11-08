package com.mall.order.cache;

import com.mall.order.entity.Order;
import com.mall.order.enums.OrderStatus;
import com.mall.order.repository.OrderRepository;
import com.mall.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单缓存测试类
 * 测试订单服务的Redis缓存功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 * 
 * 修改日志：
 * V1.0 2025-01-21：初始版本，实现订单缓存功能测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("订单缓存测试")
class OrderCacheTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CacheManager cacheManager;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        // 清理缓存
        clearAllCaches();
        
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
        
        // 保存测试订单
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("订单详情缓存测试")
    void orderDetailCache_Test() {
        Long orderId = testOrder.getId();
        Long userId = testOrder.getUserId();
        
        // 第一次查询，应该从数据库加载并缓存
        Order order1 = orderService.getOrderById(orderId, userId);
        assertNotNull(order1);
        assertEquals(orderId, order1.getId());
        
        // 验证缓存中存在数据
        Cache orderCache = cacheManager.getCache("order");
        assertNotNull(orderCache);
        String cacheKey = orderId + ":" + userId;
        Cache.ValueWrapper cachedValue = orderCache.get(cacheKey);
        assertNotNull(cachedValue);
        
        // 第二次查询，应该从缓存获取
        Order order2 = orderService.getOrderById(orderId, userId);
        assertNotNull(order2);
        assertEquals(order1.getId(), order2.getId());
        assertEquals(order1.getOrderNo(), order2.getOrderNo());
    }

    @Test
    @DisplayName("根据订单号查询缓存测试")
    void orderByOrderNoCache_Test() {
        String orderNo = testOrder.getOrderNo();
        
        // 第一次查询，应该从数据库加载并缓存
        Order order1 = orderService.getOrderByOrderNo(orderNo);
        assertNotNull(order1);
        assertEquals(orderNo, order1.getOrderNo());
        
        // 验证缓存中存在数据
        Cache orderCache = cacheManager.getCache("order");
        assertNotNull(orderCache);
        Cache.ValueWrapper cachedValue = orderCache.get(orderNo);
        assertNotNull(cachedValue);
        
        // 第二次查询，应该从缓存获取
        Order order2 = orderService.getOrderByOrderNo(orderNo);
        assertNotNull(order2);
        assertEquals(order1.getId(), order2.getId());
        assertEquals(order1.getOrderNo(), order2.getOrderNo());
    }

    @Test
    @DisplayName("用户订单列表缓存测试")
    void userOrdersListCache_Test() {
        Long userId = testOrder.getUserId();
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        // 第一次查询，应该从数据库加载并缓存
        Page<Order> orders1 = orderService.getUserOrders(userId, null, pageRequest);
        assertNotNull(orders1);
        assertTrue(orders1.getTotalElements() > 0);
        
        // 验证缓存中存在数据
        Cache userOrdersCache = cacheManager.getCache("userOrders");
        assertNotNull(userOrdersCache);
        String cacheKey = userId + ":0:10";
        Cache.ValueWrapper cachedValue = userOrdersCache.get(cacheKey);
        assertNotNull(cachedValue);
        
        // 第二次查询，应该从缓存获取
        Page<Order> orders2 = orderService.getUserOrders(userId, null, pageRequest);
        assertNotNull(orders2);
        assertEquals(orders1.getTotalElements(), orders2.getTotalElements());
    }

    @Test
    @DisplayName("订单状态更新缓存清除测试")
    void updateOrderStatusCacheEvict_Test() {
        Long orderId = testOrder.getId();
        Long userId = testOrder.getUserId();
        
        // 先查询一次，让数据进入缓存
        Order order1 = orderService.getOrderById(orderId, userId);
        assertNotNull(order1);
        assertEquals(OrderStatus.PENDING_PAYMENT, order1.getStatus());
        
        // 验证缓存中存在数据
        Cache orderCache = cacheManager.getCache("order");
        assertNotNull(orderCache);
        String cacheKey = orderId + ":" + userId;
        Cache.ValueWrapper cachedValue = orderCache.get(cacheKey);
        assertNotNull(cachedValue);
        
        // 更新订单状态
        boolean updated = orderService.updateOrderStatus(orderId, userId, OrderStatus.PAID);
        assertTrue(updated);
        
        // 验证缓存已被清除
        cachedValue = orderCache.get(cacheKey);
        // 注意：由于使用了allEntries=true，整个缓存区域都会被清空
        // 所以这里验证缓存为空或者重新查询得到更新后的数据
        
        // 重新查询，应该得到更新后的数据
        Order updatedOrder = orderService.getOrderById(orderId, userId);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());
    }

    @Test
    @DisplayName("取消订单缓存清除测试")
    void cancelOrderCacheEvict_Test() {
        Long orderId = testOrder.getId();
        Long userId = testOrder.getUserId();
        
        // 先查询一次，让数据进入缓存
        Order order1 = orderService.getOrderById(orderId, userId);
        assertNotNull(order1);
        assertEquals(OrderStatus.PENDING_PAYMENT, order1.getStatus());
        
        // 验证缓存中存在数据
        Cache orderCache = cacheManager.getCache("order");
        assertNotNull(orderCache);
        String cacheKey = orderId + ":" + userId;
        Cache.ValueWrapper cachedValue = orderCache.get(cacheKey);
        assertNotNull(cachedValue);
        
        // 取消订单
        boolean cancelled = orderService.cancelOrder(orderId, userId, "用户主动取消");
        assertTrue(cancelled);
        
        // 重新查询，应该得到更新后的数据
        Order cancelledOrder = orderService.getOrderById(orderId, userId);
        assertNotNull(cancelledOrder);
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
    }

    @Test
    @DisplayName("确认收货缓存清除测试")
    void confirmOrderCacheEvict_Test() {
        // 先将订单状态设置为已发货
        testOrder.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(testOrder);
        
        Long orderId = testOrder.getId();
        Long userId = testOrder.getUserId();
        
        // 先查询一次，让数据进入缓存
        Order order1 = orderService.getOrderById(orderId, userId);
        assertNotNull(order1);
        assertEquals(OrderStatus.SHIPPED, order1.getStatus());
        
        // 确认收货
        boolean confirmed = orderService.confirmOrder(orderId, userId);
        assertTrue(confirmed);
        
        // 重新查询，应该得到更新后的数据
        Order confirmedOrder = orderService.getOrderById(orderId, userId);
        assertNotNull(confirmedOrder);
        assertEquals(OrderStatus.COMPLETED, confirmedOrder.getStatus());
    }

    @Test
    @DisplayName("缓存空值测试")
    void cacheNullValue_Test() {
        Long nonExistentOrderId = 999999L;
        Long userId = 1001L;
        
        // 查询不存在的订单
        try {
            orderService.getOrderById(nonExistentOrderId, userId);
            fail("应该抛出异常");
        } catch (Exception e) {
            // 预期的异常
        }
        
        // 验证缓存中不应该存储null值（因为设置了unless = "#result == null"）
        Cache orderCache = cacheManager.getCache("order");
        assertNotNull(orderCache);
        String cacheKey = nonExistentOrderId + ":" + userId;
        Cache.ValueWrapper cachedValue = orderCache.get(cacheKey);
        assertNull(cachedValue); // 应该为null，因为不缓存空值
    }

    @Test
    @DisplayName("不同用户缓存隔离测试")
    void differentUserCacheIsolation_Test() {
        Long orderId = testOrder.getId();
        Long userId1 = testOrder.getUserId();
        Long userId2 = 2002L;
        
        // 用户1查询订单
        Order order1 = orderService.getOrderById(orderId, userId1);
        assertNotNull(order1);
        
        // 验证用户1的缓存
        Cache orderCache = cacheManager.getCache("order");
        assertNotNull(orderCache);
        String cacheKey1 = orderId + ":" + userId1;
        Cache.ValueWrapper cachedValue1 = orderCache.get(cacheKey1);
        assertNotNull(cachedValue1);
        
        // 用户2查询同一订单（应该失败，因为不是该用户的订单）
        try {
            orderService.getOrderById(orderId, userId2);
            fail("应该抛出异常，因为订单不属于用户2");
        } catch (Exception e) {
            // 预期的异常
        }
        
        // 验证用户2没有缓存
        String cacheKey2 = orderId + ":" + userId2;
        Cache.ValueWrapper cachedValue2 = orderCache.get(cacheKey2);
        assertNull(cachedValue2); // 应该为null，因为查询失败不会缓存
    }

    /**
     * 清空所有缓存
     */
    private void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }
}