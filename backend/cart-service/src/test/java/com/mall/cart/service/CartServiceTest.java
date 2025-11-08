package com.mall.cart.service;

import com.mall.cart.client.ProductClient;
import com.mall.cart.domain.dto.ProductDTO;
import com.mall.cart.service.impl.CartServiceImpl;
import com.mall.common.core.domain.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 购物车服务测试类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductClient productClient;

    private CartServiceImpl cartService;

    private ProductDTO mockProduct;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl();
        ReflectionTestUtils.setField(cartService, "productClient", productClient);

        // 初始化模拟数据
        mockProduct = new ProductDTO();
        mockProduct.setId(1L);
        mockProduct.setName("测试商品");
        mockProduct.setPrice(new BigDecimal("99.99"));
        mockProduct.setStock(100);
        mockProduct.setStatus(1);
        mockProduct.setImage("test-image.jpg");
    }

    @Test
    void testAddToCart_InvalidUserId() {
        // 执行测试
        R<Void> result = cartService.addToCart(null, 1L, 2, "默认规格");

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户ID无效", result.getMessage());
    }

    @Test
    void testAddToCart_InvalidProductId() {
        // 执行测试
        R<Void> result = cartService.addToCart(1L, null, 2, "默认规格");

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("商品ID无效", result.getMessage());
    }

    @Test
    void testAddToCart_InvalidQuantity() {
        // 执行测试
        R<Void> result = cartService.addToCart(1L, 1L, 0, "默认规格");

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("商品数量必须大于0", result.getMessage());
    }

    @Test
    void testAddToCart_ProductNotFound() {
        // 模拟商品不存在
        when(productClient.getProductById(1L)).thenReturn(R.fail("商品不存在"));

        // 执行测试
        R<Void> result = cartService.addToCart(1L, 1L, 2, "默认规格");

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("商品信息不存在", result.getMessage());
    }

    @Test
    void testUpdateQuantity_InvalidUserId() {
        // 执行测试
        R<Void> result = cartService.updateQuantity(null, 1L, 2);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户ID无效", result.getMessage());
    }

    @Test
    void testUpdateQuantity_InvalidProductId() {
        // 执行测试
        R<Void> result = cartService.updateQuantity(1L, null, 2);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("商品ID无效", result.getMessage());
    }

    @Test
    void testRemoveFromCart_InvalidUserId() {
        // 执行测试
        R<Void> result = cartService.removeFromCart(null, 1L);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户ID无效", result.getMessage());
    }

    @Test
    void testRemoveFromCart_InvalidProductId() {
        // 执行测试
        R<Void> result = cartService.removeFromCart(1L, null);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("商品ID无效", result.getMessage());
    }

    @Test
    void testGetCartItems_InvalidUserId() {
        // 执行测试
        R result = cartService.getCartItems(null);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户ID无效", result.getMessage());
    }

    @Test
    void testClearCart_InvalidUserId() {
        // 执行测试
        R<Void> result = cartService.clearCart(null);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户ID无效", result.getMessage());
    }

    @Test
    void testSelectItem_InvalidUserId() {
        // 执行测试
        R<Void> result = cartService.selectItem(null, 1L, true);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户ID无效", result.getMessage());
    }

    @Test
    void testSelectItem_InvalidProductId() {
        // 执行测试
        R<Void> result = cartService.selectItem(1L, null, true);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("商品ID无效", result.getMessage());
    }

    @Test
    void testGetCartCount_InvalidUserId() {
        // 执行测试
        R<Integer> result = cartService.getCartCount(null);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户ID无效", result.getMessage());
    }
}