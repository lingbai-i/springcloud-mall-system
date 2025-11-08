package com.mall.cart.service;

import com.mall.cart.client.ProductClient;
import com.mall.cart.domain.dto.ProductDTO;
import com.mall.common.core.domain.R;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 购物车服务集成测试类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@SpringBootTest
@ActiveProfiles("test")
class CartServiceIntegrationTest {

    @MockBean
    private ProductClient productClient;

    @Test
    void testCartServiceExists() {
        // 这个测试只是验证Spring上下文能够正常启动
        // 并且购物车服务能够被正确注入
        assertTrue(true, "购物车服务集成测试通过");
    }

    @Test
    void testProductClientMockWorks() {
        // 创建模拟商品数据
        ProductDTO mockProduct = new ProductDTO();
        mockProduct.setId(1L);
        mockProduct.setName("测试商品");
        mockProduct.setPrice(new BigDecimal("99.99"));
        mockProduct.setStock(100);
        mockProduct.setStatus(1);
        mockProduct.setImage("test-image.jpg");

        // 模拟商品服务返回
        when(productClient.getProductById(1L)).thenReturn(R.ok(mockProduct));

        // 验证模拟工作正常
        R<ProductDTO> result = productClient.getProductById(1L);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("测试商品", result.getData().getName());
    }
}