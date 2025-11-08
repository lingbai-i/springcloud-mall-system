package com.mall.cart.service;

import com.mall.cart.domain.entity.CartItem;
import com.mall.cart.service.impl.CartSyncServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 购物车同步服务测试类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@ExtendWith(MockitoExtension.class)
class CartSyncServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private CartSyncServiceImpl cartSyncService;

    private CartItem mockCartItem;

    @BeforeEach
    void setUp() {
        // 初始化模拟数据
        mockCartItem = new CartItem();
        mockCartItem.setUserId(1L);
        mockCartItem.setProductId(1L);
        mockCartItem.setProductName("测试商品");
        mockCartItem.setPrice(new BigDecimal("99.99"));
        mockCartItem.setQuantity(2);
        mockCartItem.setSelected(true);
        mockCartItem.setSpecifications("默认规格");
    }

    @Test
    void testSyncCartToRedis_Success() {
        // 准备测试数据
        Long userId = 1L;
        List<CartItem> cartItems = Arrays.asList(mockCartItem);

        // 模拟Redis操作
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));

        // 执行测试
        boolean result = cartSyncService.syncCartToRedis(userId, cartItems);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testSyncCartToRedis_NullUserId() {
        // 准备测试数据
        List<CartItem> cartItems = Arrays.asList(mockCartItem);

        // 执行测试
        boolean result = cartSyncService.syncCartToRedis(null, cartItems);

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testSyncCartFromRedis_Success() {
        // 准备测试数据
        Long userId = 1L;

        // 模拟Redis操作
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));

        // 执行测试
        List<CartItem> result = cartSyncService.syncCartFromRedis(userId);

        // 验证结果
        assertNotNull(result);
    }

    @Test
    void testCleanExpiredCartData_Success() {
        // 准备测试数据
        Long userId = 1L;

        // 执行测试
        boolean result = cartSyncService.cleanExpiredCartData(userId);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testBatchSyncCartData_Success() {
        // 准备测试数据
        List<Long> userIds = Arrays.asList(1L, 2L);

        // 执行测试
        int result = cartSyncService.batchSyncCartData(userIds);

        // 验证结果
        assertEquals(2, result);
    }

    @Test
    void testCheckCartDataConsistency_Success() {
        // 准备测试数据
        Long userId = 1L;

        // 模拟Redis操作
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));

        // 执行测试
        boolean result = cartSyncService.checkCartDataConsistency(userId);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testRepairCartDataInconsistency_Success() {
        // 准备测试数据
        Long userId = 1L;

        // 执行测试
        boolean result = cartSyncService.repairCartDataInconsistency(userId);

        // 验证结果
        assertTrue(result);
    }
}