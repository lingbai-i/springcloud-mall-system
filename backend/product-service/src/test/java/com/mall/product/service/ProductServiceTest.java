package com.mall.product.service;

import com.mall.product.domain.entity.Product;
import com.mall.product.domain.entity.ProductSku;
import com.mall.product.domain.entity.PriceHistory;
import com.mall.product.domain.entity.StockLog;
import com.mall.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 商品服务单元测试
 * 测试商品管理、多规格商品、库存管理、价格管理等核心功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@DisplayName("商品服务测试")
public class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl();
    }

    // ==================== 商品基础管理测试 ====================

    @Test
    @DisplayName("创建商品测试")
    void testCreateProduct() {
        // 准备测试数据
        Product product = new Product();
        product.setName("测试商品");
        product.setDescription("这是一个测试商品");
        product.setPrice(99.99);
        product.setStock(100);
        product.setCategoryId(1L);
        product.setBrandId(1L);

        // 执行测试
        boolean result = productService.addProduct(product);

        // 验证结果
        assertTrue(result, "商品创建应该成功");
        assertEquals("测试商品", product.getName(), "商品名称应该正确");
    }

    @Test
    @DisplayName("根据ID获取商品测试")
    void testGetProductById() {
        // 执行测试
        Product product = productService.getProductById(1L);

        // 验证结果
        assertNotNull(product, "应该能获取到商品");
        assertEquals(1L, product.getId(), "商品ID应该正确");
        assertNotNull(product.getName(), "商品名称不应该为空");
    }

    @Test
    @DisplayName("更新商品测试")
    void testUpdateProduct() {
        // 准备测试数据
        Product product = productService.getProductById(1L);
        assertNotNull(product, "商品应该存在");

        String originalName = product.getName();
        product.setName("更新后的商品名称");

        // 执行测试
        boolean result = productService.updateProduct(product);

        // 验证结果
        assertTrue(result, "商品更新应该成功");
        
        Product updatedProduct = productService.getProductById(1L);
        assertEquals("更新后的商品名称", updatedProduct.getName(), "商品名称应该被更新");
    }

    @Test
    @DisplayName("删除商品测试")
    void testDeleteProduct() {
        // 先创建一个商品
        Product product = new Product();
        product.setName("待删除商品");
        product.setPrice(50.00);
        product.setStock(10);
        productService.addProduct(product);

        Long productId = 1L; // 使用固定ID进行测试

        // 执行删除
        boolean result = productService.deleteProduct(productId);

        // 验证结果
        assertTrue(result, "商品删除应该成功");
    }

    @Test
    @DisplayName("批量删除商品测试")
    void testBatchDeleteProducts() {
        // 准备测试数据
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);

        // 执行测试
        boolean result = productService.batchDeleteProducts(productIds);

        // 验证结果
        assertTrue(result, "批量删除应该成功");
    }

    @Test
    @DisplayName("更新商品状态测试")
    void testUpdateProductStatus() {
        // 执行测试
        boolean result = productService.updateProductStatus(1L, 0);

        // 验证结果
        assertTrue(result, "状态更新应该成功");
    }

    // ==================== 多规格商品管理测试 ====================

    @Test
    @DisplayName("创建商品SKU测试")
    void testCreateProductSku() {
        // 准备测试数据
        ProductSku sku = new ProductSku();
        sku.setProductId(1L);
        sku.setSkuCode("TEST-SKU-001");
        sku.setSkuName("测试SKU");
        sku.setPrice(199.99);
        sku.setStock(50);
        sku.setSpecValues("颜色:红色,尺寸:L");

        // 执行测试
        boolean result = productService.addProductSku(sku);

        // 验证结果
        assertTrue(result, "SKU创建应该成功");
    }

    @Test
    @DisplayName("根据商品ID获取SKU列表测试")
    void testGetSkusByProductId() {
        // 执行测试
        List<ProductSku> skus = productService.getProductSkus(1L);

        // 验证结果
        assertNotNull(skus, "SKU列表不应该为空");
    }

    @Test
    @DisplayName("批量保存SKU测试")
    void testBatchSaveSkus() {
        // 准备测试数据
        ProductSku sku1 = new ProductSku();
        sku1.setProductId(1L);
        sku1.setSkuCode("BATCH-SKU-001");
        sku1.setSkuName("批量SKU1");
        sku1.setPrice(99.99);
        sku1.setStock(30);

        ProductSku sku2 = new ProductSku();
        sku2.setProductId(1L);
        sku2.setSkuCode("BATCH-SKU-002");
        sku2.setSkuName("批量SKU2");
        sku2.setPrice(129.99);
        sku2.setStock(20);

        List<ProductSku> skus = Arrays.asList(sku1, sku2);

        // 执行测试
        boolean result = productService.batchSaveProductSkus(1L, skus);

        // 验证结果
        assertTrue(result, "批量保存SKU应该成功");
    }

    // ==================== 库存管理测试 ====================

    @Test
    @DisplayName("更新商品库存测试")
    void testUpdateProductStock() {
        // 执行测试
        boolean result = productService.updateStock(1L, 200);

        // 验证结果
        assertTrue(result, "库存更新应该成功");
    }

    @Test
    @DisplayName("批量更新商品库存测试")
    void testBatchUpdateProductStock() {
        // 准备测试数据
        ProductService.StockUpdate update1 = new ProductService.StockUpdate();
        update1.setProductId(1L);
        update1.setQuantity(50);
        update1.setReason("批量库存调整");
        update1.setOperatorId(1L);

        ProductService.StockUpdate update2 = new ProductService.StockUpdate();
        update2.setProductId(2L);
        update2.setQuantity(30);
        update2.setReason("批量库存调整");
        update2.setOperatorId(1L);

        List<ProductService.StockUpdate> updates = Arrays.asList(update1, update2);

        // 执行测试
        boolean result = productService.batchUpdateStock(updates);

        // 验证结果
        assertTrue(result, "批量库存更新应该成功");
    }

    @Test
    @DisplayName("更新SKU库存测试")
    void testUpdateSkuStock() {
        // 执行测试
        boolean result = productService.updateSkuStock(1L, 75);

        // 验证结果
        assertTrue(result, "SKU库存更新应该成功");
    }

    @Test
    @DisplayName("获取库存预警商品测试")
    void testGetStockWarningProducts() {
        // 执行测试
        List<Product> warningProducts = productService.getStockWarningProducts();

        // 验证结果
        assertNotNull(warningProducts, "预警商品列表不应该为空");
    }

    @Test
    @DisplayName("获取库存变更日志测试")
    void testGetStockLogs() {
        // 执行测试
        Object logs = productService.getStockLogs(1L, 1L, 10L);

        // 验证结果
        assertNotNull(logs, "库存日志列表不应该为空");
    }

    // ==================== 价格管理测试 ====================

    @Test
    @DisplayName("更新商品价格测试")
    void testUpdateProductPrice() {
        // 执行测试
        boolean result = productService.updateProductPrice(1L, 299.99, "价格调整", 1L);

        // 验证结果
        assertTrue(result, "价格更新应该成功");
    }

    @Test
    @DisplayName("批量更新商品价格测试")
    void testBatchUpdateProductPrice() {
        // 准备测试数据
        ProductService.PriceUpdate update1 = new ProductService.PriceUpdate();
        update1.setProductId(1L);
        update1.setNewPrice(199.99);
        update1.setReason("批量调价");
        update1.setOperatorId(1L);

        ProductService.PriceUpdate update2 = new ProductService.PriceUpdate();
        update2.setProductId(2L);
        update2.setNewPrice(149.99);
        update2.setReason("批量调价");
        update2.setOperatorId(1L);

        List<ProductService.PriceUpdate> updates = Arrays.asList(update1, update2);

        // 执行测试
        boolean result = productService.batchUpdatePrices(updates);

        // 验证结果
        assertTrue(result, "批量价格更新应该成功");
    }

    @Test
    @DisplayName("更新SKU价格测试")
    void testUpdateSkuPrice() {
        // 执行测试
        boolean result = productService.updateSkuPrice(1L, 179.99, "SKU价格调整", 1L);

        // 验证结果
        assertTrue(result, "SKU价格更新应该成功");
    }

    @Test
    @DisplayName("获取价格历史记录测试")
    void testGetPriceHistory() {
        // 执行测试
        Object history = productService.getPriceHistory(1L, 1L, 10L);

        // 验证结果
        assertNotNull(history, "价格历史记录不应该为空");
    }

    // ==================== 商品查询测试 ====================

    @Test
    @DisplayName("分页查询商品测试")
    void testGetProductsByCategory() {
        // 执行测试
        Object pageData = productService.getProductsByCategoryId(1L, 10L, 1L);

        // 验证结果
        assertNotNull(pageData, "分页数据不应该为空");
    }

    @Test
    @DisplayName("搜索商品测试")
    void testSearchProducts() {
        // 执行测试
        Object searchResult = productService.searchProducts(1L, 10L, "手机");

        // 验证结果
        assertNotNull(searchResult, "搜索结果不应该为空");
    }

    @Test
    @DisplayName("获取热门商品测试")
    void testGetHotProducts() {
        // 执行测试
        List<Product> hotProducts = productService.getHotProducts(10);

        // 验证结果
        assertNotNull(hotProducts, "热门商品列表不应该为空");
        assertTrue(hotProducts.size() <= 10, "热门商品数量不应该超过限制");
    }

    @Test
    @DisplayName("获取推荐商品测试")
    void testGetRecommendedProducts() {
        // 执行测试
        List<Product> recommendedProducts = productService.getRecommendProducts(10);

        // 验证结果
        assertNotNull(recommendedProducts, "推荐商品列表不应该为空");
        assertTrue(recommendedProducts.size() <= 10, "推荐商品数量不应该超过限制");
    }

    // ==================== 异常情况测试 ====================

    @Test
    @DisplayName("获取不存在的商品测试")
    void testGetNonExistentProduct() {
        // 执行测试
        Product product = productService.getProductById(99999L);

        // 验证结果
        assertNull(product, "不存在的商品应该返回null");
    }

    @Test
    @DisplayName("更新不存在商品的库存测试")
    void testUpdateNonExistentProductStock() {
        // 执行测试
        boolean result = productService.updateStock(99999L, 100);

        // 验证结果
        assertFalse(result, "更新不存在商品的库存应该失败");
    }

    @Test
    @DisplayName("创建空商品测试")
    void testCreateNullProduct() {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            productService.addProduct(null);
        }, "创建空商品应该抛出异常");
    }

    @Test
    @DisplayName("负库存更新测试")
    void testUpdateNegativeStock() {
        // 执行测试
        boolean result = productService.updateStock(1L, -10);

        // 验证结果
        assertFalse(result, "设置负库存应该失败");
    }

    @Test
    @DisplayName("负价格更新测试")
    void testUpdateNegativePrice() {
        // 执行测试
        boolean result = productService.updateProductPrice(1L, -100.0, "测试负价格", 1L);

        // 验证结果
        assertFalse(result, "设置负价格应该失败");
    }
}