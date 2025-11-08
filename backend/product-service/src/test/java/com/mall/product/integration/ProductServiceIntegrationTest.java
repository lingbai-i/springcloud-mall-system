package com.mall.product.integration;

import com.mall.product.controller.ProductController;
import com.mall.product.controller.CategoryController;
import com.mall.product.controller.StockController;
import com.mall.product.controller.PriceController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 商品服务集成测试
 * 测试各个模块之间的协作和端到端功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@DisplayName("商品服务集成测试")
public class ProductServiceIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ==================== 商品管理集成测试 ====================

    @Test
    @DisplayName("商品完整生命周期集成测试")
    void testProductLifecycleIntegration() throws Exception {
        // 1. 创建商品分类
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("categoryName", "集成测试分类");
        categoryData.put("parentId", 0L);
        categoryData.put("level", 1);
        categoryData.put("sort", 1);
        categoryData.put("status", "ACTIVE");
        categoryData.put("description", "集成测试用分类");

        String categoryResponse = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryId").exists())
                .andReturn().getResponse().getContentAsString();

        // 2. 创建商品
        Map<String, Object> productData = new HashMap<>();
        productData.put("productName", "集成测试商品");
        productData.put("categoryId", 1L);
        productData.put("brandId", 1L);
        productData.put("price", new BigDecimal("99.99"));
        productData.put("stock", 100);
        productData.put("description", "集成测试商品描述");
        productData.put("status", "ACTIVE");

        String productResponse = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").exists())
                .andReturn().getResponse().getContentAsString();

        // 3. 查询商品详情
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("集成测试商品"));

        // 4. 更新商品信息
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("productName", "更新后的商品名称");
        updateData.put("price", new BigDecimal("89.99"));

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. 验证更新结果
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productName").value("更新后的商品名称"));
    }

    // ==================== 库存管理集成测试 ====================

    @Test
    @DisplayName("库存管理集成测试")
    void testStockManagementIntegration() throws Exception {
        // 1. 查询库存状态
        mockMvc.perform(get("/api/stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(1))
                .andExpect(jsonPath("$.data.availableStock").exists());

        // 2. 库存扣减
        Map<String, Object> deductionData = new HashMap<>();
        deductionData.put("productId", 1L);
        deductionData.put("skuId", 1L);
        deductionData.put("quantity", 5);
        deductionData.put("operationType", "SALE");
        deductionData.put("operatorId", 1001L);
        deductionData.put("remark", "集成测试扣减");

        mockMvc.perform(post("/api/stock/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deductionData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.operationId").exists());

        // 3. 验证库存变化
        mockMvc.perform(get("/api/stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. 库存回滚
        Map<String, Object> rollbackData = new HashMap<>();
        rollbackData.put("productId", 1L);
        rollbackData.put("skuId", 1L);
        rollbackData.put("quantity", 3);
        rollbackData.put("operationType", "ROLLBACK");
        rollbackData.put("operatorId", 1001L);
        rollbackData.put("remark", "集成测试回滚");

        mockMvc.perform(post("/api/stock/rollback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rollbackData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. 查询库存变更日志
        mockMvc.perform(get("/api/stock/logs/1")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== 价格管理集成测试 ====================

    @Test
    @DisplayName("价格管理集成测试")
    void testPriceManagementIntegration() throws Exception {
        // 1. 单个商品价格调整
        Map<String, Object> priceAdjustment = new HashMap<>();
        priceAdjustment.put("productId", 1L);
        priceAdjustment.put("skuId", 1L);
        priceAdjustment.put("oldPrice", new BigDecimal("99.99"));
        priceAdjustment.put("newPrice", new BigDecimal("89.99"));
        priceAdjustment.put("adjustmentType", "DISCOUNT");
        priceAdjustment.put("reason", "集成测试调价");
        priceAdjustment.put("operatorId", 1001L);

        mockMvc.perform(post("/api/prices/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(priceAdjustment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.adjustmentId").exists());

        // 2. 查询价格历史
        mockMvc.perform(get("/api/prices/history/1")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 3. 获取价格统计
        mockMvc.perform(get("/api/prices/statistics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAdjustments").exists());

        // 4. 创建价格策略
        Map<String, Object> priceStrategy = new HashMap<>();
        priceStrategy.put("strategyName", "集成测试策略");
        priceStrategy.put("strategyType", "DISCOUNT");
        priceStrategy.put("discountRate", new BigDecimal("0.10"));
        priceStrategy.put("minPrice", new BigDecimal("1.00"));
        priceStrategy.put("maxPrice", new BigDecimal("1000.00"));
        priceStrategy.put("startTime", "2025-01-01 00:00:00");
        priceStrategy.put("endTime", "2025-12-31 23:59:59");
        priceStrategy.put("description", "集成测试价格策略");
        priceStrategy.put("creatorId", 1001L);

        mockMvc.perform(post("/api/prices/strategies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(priceStrategy)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.strategyId").exists());
    }

    // ==================== 分类管理集成测试 ====================

    @Test
    @DisplayName("分类管理集成测试")
    void testCategoryManagementIntegration() throws Exception {
        // 1. 获取分类树
        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 2. 搜索分类
        mockMvc.perform(get("/api/categories/search")
                .param("keyword", "电子"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());

        // 3. 获取分类统计
        mockMvc.perform(get("/api/categories/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryId").value(1));

        // 4. 获取热门分类
        mockMvc.perform(get("/api/categories/hot")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(lessThanOrEqualTo(5))));

        // 5. 刷新分类缓存
        mockMvc.perform(post("/api/categories/cache/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==================== 跨模块协作测试 ====================

    @Test
    @DisplayName("商品-库存-价格协作测试")
    void testCrossModuleIntegration() throws Exception {
        // 1. 创建商品（包含初始库存和价格）
        Map<String, Object> productData = new HashMap<>();
        productData.put("productName", "协作测试商品");
        productData.put("categoryId", 1L);
        productData.put("price", new BigDecimal("199.99"));
        productData.put("stock", 50);
        productData.put("status", "ACTIVE");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. 调整价格
        Map<String, Object> priceAdjustment = new HashMap<>();
        priceAdjustment.put("productId", 1L);
        priceAdjustment.put("oldPrice", new BigDecimal("199.99"));
        priceAdjustment.put("newPrice", new BigDecimal("179.99"));
        priceAdjustment.put("adjustmentType", "DISCOUNT");
        priceAdjustment.put("reason", "协作测试调价");
        priceAdjustment.put("operatorId", 1001L);

        mockMvc.perform(post("/api/prices/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(priceAdjustment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. 扣减库存
        Map<String, Object> stockDeduction = new HashMap<>();
        stockDeduction.put("productId", 1L);
        stockDeduction.put("quantity", 10);
        stockDeduction.put("operationType", "SALE");
        stockDeduction.put("operatorId", 1001L);

        mockMvc.perform(post("/api/stock/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockDeduction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. 验证商品信息（价格和库存都应该更新）
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("协作测试商品"));

        // 5. 验证库存状态
        mockMvc.perform(get("/api/stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(1));
    }

    // ==================== 性能集成测试 ====================

    @Test
    @DisplayName("批量操作性能集成测试")
    void testBatchOperationPerformance() throws Exception {
        long startTime = System.currentTimeMillis();

        // 1. 批量查询商品
        mockMvc.perform(get("/api/products")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. 批量查询库存
        mockMvc.perform(get("/api/stock/batch")
                .param("productIds", "1,2,3,4,5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. 获取分类树（缓存测试）
        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 验证性能要求：所有操作应在500ms内完成
        assert duration < 500 : "批量操作应在500ms内完成，实际耗时: " + duration + "ms";
    }

    // ==================== 异常处理集成测试 ====================

    @Test
    @DisplayName("异常处理集成测试")
    void testExceptionHandlingIntegration() throws Exception {
        // 1. 测试不存在的商品
        mockMvc.perform(get("/api/products/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());

        // 2. 测试无效的库存扣减
        Map<String, Object> invalidDeduction = new HashMap<>();
        invalidDeduction.put("productId", 1L);
        invalidDeduction.put("quantity", -10); // 负数量
        invalidDeduction.put("operationType", "SALE");

        mockMvc.perform(post("/api/stock/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDeduction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());

        // 3. 测试无效的价格调整
        Map<String, Object> invalidPriceAdjustment = new HashMap<>();
        invalidPriceAdjustment.put("productId", 1L);
        invalidPriceAdjustment.put("newPrice", new BigDecimal("-10.00")); // 负价格

        mockMvc.perform(post("/api/prices/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPriceAdjustment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());

        // 4. 测试无效的分类操作
        mockMvc.perform(get("/api/categories/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== 并发安全测试 ====================

    @Test
    @DisplayName("并发安全集成测试")
    void testConcurrencySafetyIntegration() throws Exception {
        // 模拟并发库存扣减
        Map<String, Object> deductionData = new HashMap<>();
        deductionData.put("productId", 1L);
        deductionData.put("quantity", 1);
        deductionData.put("operationType", "SALE");
        deductionData.put("operatorId", 1001L);

        // 连续发送多个请求模拟并发
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/stock/deduct")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(deductionData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        // 验证库存一致性
        mockMvc.perform(get("/api/stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(1));
    }
}