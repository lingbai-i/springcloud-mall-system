package com.mall.product;

import com.mall.product.service.ProductService;
import com.mall.product.service.impl.ProductServiceImpl;
import com.mall.product.domain.entity.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单测试类
 * 不依赖Spring Boot上下文的基础单元测试
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-10-26
 */
@DisplayName("简单测试")
public class SimpleTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl();
    }

    @Test
    @DisplayName("商品服务基础功能测试")
    void testProductServiceBasic() {
        // 测试商品创建
        Product product = new Product();
        product.setName("测试商品");
        product.setDescription("这是一个测试商品");
        product.setPrice(99.99);
        product.setStock(100);
        product.setCategoryId(1L);
        product.setBrandId(1L);

        boolean result = productService.addProduct(product);
        assertTrue(result, "商品创建应该成功");
        
        // 测试商品查询
        Object searchResult = productService.searchProducts(1L, 10L, "测试");
        assertNotNull(searchResult, "搜索结果不应该为空");
    }

    @Test
    @DisplayName("商品分页查询测试")
    void testProductPagination() {
        Object pageData = productService.getProductsByCategoryId(1L, 10L, 1L);
        assertNotNull(pageData, "分页数据不应该为空");
    }

    @Test
    @DisplayName("商品推荐功能测试")
    void testProductRecommendation() {
        var recommendedProducts = productService.getRecommendProducts(5);
        assertNotNull(recommendedProducts, "推荐商品列表不应该为空");
        assertTrue(recommendedProducts.size() <= 5, "推荐商品数量不应该超过限制");
    }
}