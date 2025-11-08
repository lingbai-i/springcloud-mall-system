package com.mall.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.cart.domain.entity.CartItem;
import com.mall.cart.service.CartService;
import com.mall.common.core.domain.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 购物车控制器测试类
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @WithMockUser(username = "testuser")
    void testAddToCart_Success() throws Exception {
        // 模拟服务返回
        when(cartService.addToCart(anyLong(), anyLong(), anyInt(), anyString()))
                .thenReturn(R.ok());

        // 执行测试
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "2")
                        .param("specifications", "默认规格")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testUpdateQuantity_Success() throws Exception {
        // 模拟服务返回
        when(cartService.updateQuantity(anyLong(), anyLong(), anyInt()))
                .thenReturn(R.ok());

        // 执行测试
        mockMvc.perform(put("/cart/update")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "3")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testRemoveFromCart_Success() throws Exception {
        // 模拟服务返回
        when(cartService.removeFromCart(anyLong(), anyLong()))
                .thenReturn(R.ok());

        // 执行测试
        mockMvc.perform(delete("/cart/remove")
                        .with(csrf())
                        .param("productId", "1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetCartItems_Success() throws Exception {
        // 模拟服务返回
        List<CartItem> cartItems = Arrays.asList(mockCartItem);
        when(cartService.getCartItems(anyLong()))
                .thenReturn(R.ok(cartItems));

        // 执行测试
        mockMvc.perform(get("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].productId").value(1))
                .andExpect(jsonPath("$.data[0].productName").value("测试商品"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testClearCart_Success() throws Exception {
        // 模拟服务返回
        when(cartService.clearCart(anyLong()))
                .thenReturn(R.ok());

        // 执行测试
        mockMvc.perform(delete("/cart/clear")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testSelectItem_Success() throws Exception {
        // 模拟服务返回
        when(cartService.selectItem(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(R.ok());

        // 执行测试
        mockMvc.perform(put("/cart/select")
                        .with(csrf())
                        .param("productId", "1")
                        .param("selected", "true")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetCartCount_Success() throws Exception {
        // 模拟服务返回
        when(cartService.getCartCount(anyLong()))
                .thenReturn(R.ok(5));

        // 执行测试
        mockMvc.perform(get("/cart/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void testAddToCart_Unauthorized() throws Exception {
        // 执行测试（未认证用户）
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "2")
                        .param("specifications", "默认规格")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testAddToCart_ServiceError() throws Exception {
        // 模拟服务返回错误
        when(cartService.addToCart(anyLong(), anyLong(), anyInt(), anyString()))
                .thenReturn(R.fail("商品不存在"));

        // 执行测试
        mockMvc.perform(post("/cart/add")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "2")
                        .param("specifications", "默认规格")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.msg").value("商品不存在"));
    }
}