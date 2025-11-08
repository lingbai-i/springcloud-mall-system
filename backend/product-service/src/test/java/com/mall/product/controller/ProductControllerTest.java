package com.mall.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.product.domain.entity.Product;
import com.mall.product.domain.entity.ProductSku;
import com.mall.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 商品控制器集成测试
 * 测试商品相关的REST API接口
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@WebMvcTest(ProductController.class)
@DisplayName("商品控制器测试")
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;
    private ProductSku testSku;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试商品");
        testProduct.setDescription("这是一个测试商品");
        testProduct.setPrice(99.99);
        testProduct.setStock(100);
        testProduct.setCategoryId(1L);
        testProduct.setBrandId(1L);
        testProduct.setStatus(1);

        testSku = new ProductSku();
        testSku.setId(1L);
        testSku.setProductId(1L);
        testSku.setSkuCode("TEST-SKU-001");
        testSku.setSkuName("测试SKU");
        testSku.setPrice(199.99);
        testSku.setStock(50);
        testSku.setStatus(1);
    }

    // ==================== 商品基础管理测试 ====================

    @Test
    @DisplayName("创建商品API测试")
    void testCreateProduct() throws Exception {
        // Mock服务层
        when(productService.addProduct(any(Product.class))).thenReturn(true);

        // 执行测试
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("商品创建成功"));

        // 验证服务层调用
        verify(productService, times(1)).addProduct(any(Product.class));
    }

    @Test
    @DisplayName("根据ID获取商品API测试")
    void testGetProductById() throws Exception {
        // Mock服务层
        when(productService.getProductById(1L)).thenReturn(testProduct);

        // 执行测试
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试商品"));

        // 验证服务层调用
        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("更新商品API测试")
    void testUpdateProduct() throws Exception {
        // Mock服务层
        when(productService.updateProduct(any(Product.class))).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("商品更新成功"));

        // 验证服务层调用
        verify(productService, times(1)).updateProduct(any(Product.class));
    }

    @Test
    @DisplayName("删除商品API测试")
    void testDeleteProduct() throws Exception {
        // Mock服务层
        when(productService.deleteProduct(1L)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("商品删除成功"));

        // 验证服务层调用
        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    @DisplayName("批量删除商品API测试")
    void testBatchDeleteProducts() throws Exception {
        // Mock服务层
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);
        when(productService.batchDeleteProducts(productIds)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/api/products/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("批量删除商品成功"));

        // 验证服务层调用
        verify(productService, times(1)).batchDeleteProducts(productIds);
    }

    @Test
    @DisplayName("更新商品状态API测试")
    void testUpdateProductStatus() throws Exception {
        // Mock服务层
        when(productService.updateProductStatus(1L, 0)).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/products/1/status")
                .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("商品状态更新成功"));

        // 验证服务层调用
        verify(productService, times(1)).updateProductStatus(1L, 0);
    }

    // ==================== 多规格商品管理测试 ====================

    @Test
    @DisplayName("创建商品SKU API测试")
    void testCreateProductSku() throws Exception {
        // Mock服务层
        when(productService.addProductSku(any(ProductSku.class))).thenReturn(true);

        // 执行测试
        mockMvc.perform(post("/api/products/skus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSku)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("SKU创建成功"));

        // 验证服务层调用
        verify(productService, times(1)).addProductSku(any(ProductSku.class));
    }

    @Test
    @DisplayName("根据商品ID获取SKU列表API测试")
    void testGetSkusByProductId() throws Exception {
        // Mock服务层
        List<ProductSku> skus = Arrays.asList(testSku);
        when(productService.getProductSkus(1L)).thenReturn(skus);

        // 执行测试
        mockMvc.perform(get("/api/products/1/skus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].skuName").value("测试SKU"));

        // 验证服务层调用
        verify(productService, times(1)).getProductSkus(1L);
    }

    @Test
    @DisplayName("批量保存SKU API测试")
    void testBatchSaveSkus() throws Exception {
        // Mock服务层
        List<ProductSku> skus = Arrays.asList(testSku);
        when(productService.batchSaveProductSkus(eq(1L), anyList())).thenReturn(true);

        // 执行测试
        mockMvc.perform(post("/api/products/skus/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(skus)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("批量保存SKU成功"));

        // 验证服务层调用
        verify(productService, times(1)).batchSaveProductSkus(eq(1L), anyList());
    }

    // ==================== 库存管理测试 ====================

    @Test
    @DisplayName("更新商品库存API测试")
    void testUpdateProductStock() throws Exception {
        // Mock服务层
        when(productService.updateStock(1L, 200)).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/products/1/stock")
                .param("stock", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("库存更新成功"));

        // 验证服务层调用
        verify(productService, times(1)).updateStock(1L, 200);
    }

    @Test
    @DisplayName("批量更新商品库存API测试")
    void testBatchUpdateProductStock() throws Exception {
        // 准备测试数据
        ProductService.StockUpdate update = new ProductService.StockUpdate();
        update.setProductId(1L);
        update.setQuantity(150);
        update.setReason("批量更新");
        update.setOperatorId(1L);
        List<ProductService.StockUpdate> updates = Arrays.asList(update);

        // Mock服务层
        when(productService.batchUpdateStock(anyList())).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/products/stock/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("批量库存更新成功"));

        // 验证服务层调用
        verify(productService, times(1)).batchUpdateStock(anyList());
    }

    @Test
    @DisplayName("获取库存预警商品API测试")
    void testGetStockWarningProducts() throws Exception {
        // Mock服务层
        List<Product> warningProducts = Arrays.asList(testProduct);
        when(productService.getStockWarningProducts()).thenReturn(warningProducts);

        // 执行测试
        mockMvc.perform(get("/api/products/stock/warning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        // 验证服务层调用
        verify(productService, times(1)).getStockWarningProducts();
    }

    // ==================== 价格管理测试 ====================

    @Test
    @DisplayName("更新商品价格API测试")
    void testUpdateProductPrice() throws Exception {
        // Mock服务层
        when(productService.updateProductPrice(eq(1L), any(Double.class), anyString(), eq(1L))).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/products/1/price")
                .param("price", "299.99")
                .param("reason", "价格调整"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("价格更新成功"));

        // 验证服务层调用
        verify(productService, times(1)).updateProductPrice(eq(1L), any(Double.class), eq("价格调整"), eq(1L));
    }

    @Test
    @DisplayName("批量更新商品价格API测试")
    void testBatchUpdateProductPrice() throws Exception {
        // 准备测试数据
        ProductService.PriceUpdate update = new ProductService.PriceUpdate();
        update.setProductId(1L);
        update.setNewPrice(199.99);
        update.setReason("批量调价");
        update.setOperatorId(1L);
        List<ProductService.PriceUpdate> updates = Arrays.asList(update);

        // Mock服务层
        when(productService.batchUpdatePrices(anyList())).thenReturn(true);

        // 执行测试
        mockMvc.perform(put("/api/products/price/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("批量价格更新成功"));

        // 验证服务层调用
        verify(productService, times(1)).batchUpdatePrices(anyList());
    }

    // ==================== 商品查询测试 ====================

    @Test
    @DisplayName("根据分类获取商品API测试")
    void testGetProductsByCategory() throws Exception {
        // Mock服务层
        when(productService.getProductsByCategoryId(1L, 10L, 1L)).thenReturn("分页数据");

        // 执行测试
        mockMvc.perform(get("/api/products/category/1")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());

        // 验证服务层调用
        verify(productService, times(1)).getProductsByCategoryId(1L, 10L, 1L);
    }

    @Test
    @DisplayName("搜索商品API测试")
    void testSearchProducts() throws Exception {
        // Mock服务层
        when(productService.searchProducts(1L, 10L, "手机")).thenReturn("搜索结果");

        // 执行测试
        mockMvc.perform(get("/api/products/search")
                .param("keyword", "手机")
                .param("current", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("搜索结果"));

        // 验证服务层调用
        verify(productService, times(1)).searchProducts(1L, 10L, "手机");
    }

    @Test
    @DisplayName("获取热门商品API测试")
    void testGetHotProducts() throws Exception {
        // Mock服务层
        List<Product> hotProducts = Arrays.asList(testProduct);
        when(productService.getHotProducts(10)).thenReturn(hotProducts);

        // 执行测试
        mockMvc.perform(get("/api/products/hot")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        // 验证服务层调用
        verify(productService, times(1)).getHotProducts(10);
    }

    @Test
    @DisplayName("获取推荐商品API测试")
    void testGetRecommendedProducts() throws Exception {
        // Mock服务层
        List<Product> recommendedProducts = Arrays.asList(testProduct);
        when(productService.getRecommendProducts(10)).thenReturn(recommendedProducts);

        // 执行测试
        mockMvc.perform(get("/api/products/recommended")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        // 验证服务层调用
        verify(productService, times(1)).getRecommendProducts(10);
    }

    // ==================== 异常情况测试 ====================

    @Test
    @DisplayName("获取不存在商品API测试")
    void testGetNonExistentProduct() throws Exception {
        // Mock服务层
        when(productService.getProductById(99999L)).thenReturn(null);

        // 执行测试
        mockMvc.perform(get("/api/products/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("商品不存在"));

        // 验证服务层调用
        verify(productService, times(1)).getProductById(99999L);
    }

    @Test
    @DisplayName("创建商品失败API测试")
    void testCreateProductFailure() throws Exception {
        // Mock服务层
        when(productService.addProduct(any(Product.class))).thenReturn(false);

        // 执行测试
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("商品创建失败"));

        // 验证服务层调用
        verify(productService, times(1)).addProduct(any(Product.class));
    }

    @Test
    @DisplayName("无效参数测试")
    void testInvalidParameters() throws Exception {
        // 测试无效的商品ID
        mockMvc.perform(get("/api/products/invalid"))
                .andExpect(status().isBadRequest());

        // 测试无效的状态值
        mockMvc.perform(put("/api/products/1/status")
                .param("status", "invalid"))
                .andExpect(status().isBadRequest());

        // 测试无效的库存值
        mockMvc.perform(put("/api/products/1/stock")
                .param("stock", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("空请求体测试")
    void testEmptyRequestBody() throws Exception {
        // 测试创建商品时空请求体
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());

        // 测试更新商品时空请求体
        mockMvc.perform(put("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("服务层异常处理测试")
    void testServiceException() throws Exception {
        // Mock服务层抛出异常
        when(productService.getProductById(1L)).thenThrow(new RuntimeException("数据库连接失败"));

        // 执行测试
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("获取商品详情失败"));

        // 验证服务层调用
        verify(productService, times(1)).getProductById(1L);
    }
}