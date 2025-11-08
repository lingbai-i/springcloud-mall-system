package com.mall.product.service;

import com.mall.product.service.impl.PriceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 价格服务单元测试
 * 测试价格调整、历史查询、审核机制、策略管理等核心功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@DisplayName("价格服务测试")
public class PriceServiceTest {

    private PriceService priceService;

    @BeforeEach
    void setUp() {
        priceService = new PriceServiceImpl();
    }

    // ==================== 价格调整测试 ====================

    @Test
    @DisplayName("单个商品价格调整测试")
    void testAdjustPrice() {
        // 执行测试 - 使用正确的SKU ID（商品1的SKU ID是11）
        PriceService.PriceAdjustmentResult result = priceService.adjustPrice(1L, 11L, 89.99, 1001L, "促销活动");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getPriceHistoryId());
        assertEquals(89.99, result.getNewPrice());
    }

    @Test
    @DisplayName("批量价格调整测试")
    void testBatchAdjustPrice() {
        // 准备测试数据 - 使用正确的SKU ID
        PriceService.PriceAdjustment adjustment1 = new PriceService.PriceAdjustment();
        adjustment1.setProductId(1L);
        adjustment1.setSkuId(11L); // 商品1的SKU ID是11
        adjustment1.setNewPrice(89.99);
        adjustment1.setReason("批量促销");
        adjustment1.setOperatorId(1001L);
        
        PriceService.PriceAdjustment adjustment2 = new PriceService.PriceAdjustment();
        adjustment2.setProductId(2L);
        adjustment2.setSkuId(21L); // 商品2的SKU ID是21
        adjustment2.setNewPrice(179.99);
        adjustment2.setReason("批量促销");
        adjustment2.setOperatorId(1001L);
        
        List<PriceService.PriceAdjustment> adjustments = Arrays.asList(adjustment1, adjustment2);
        
        // 执行测试
        PriceService.BatchPriceAdjustmentResult result = priceService.batchAdjustPrice(adjustments);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
        assertEquals(2, result.getResults().size());
    }

    @Test
    @DisplayName("无效价格调整测试")
    void testInvalidPriceAdjustment() {
        // 执行测试 - 负价格，使用正确的SKU ID
        PriceService.PriceAdjustmentResult result = priceService.adjustPrice(1L, 11L, -10.0, 1001L, "测试");
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("价格"));
    }

    // ==================== 价格历史测试 ====================

    @Test
    @DisplayName("获取价格历史记录测试")
    void testGetPriceHistory() {
        // 先创建一些价格调整记录 - 使用正确的SKU ID
        priceService.adjustPrice(1L, 11L, 89.99, 1001L, "历史记录测试1");
        priceService.adjustPrice(1L, 11L, 79.99, 1001L, "历史记录测试2");
        
        // 执行测试 - 使用正确的SKU ID
        Object result = priceService.getPriceHistory(1L, 11L, 1L, 10L);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("records"));
        assertTrue(resultMap.containsKey("total"));
    }

    @Test
    @DisplayName("获取价格变动统计测试")
    void testGetPriceChangeStatistics() {
        // 准备测试数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();
        
        // 执行测试
        Object result = priceService.getPriceChangeStatistics(startTime, endTime);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> statistics = (Map<String, Object>) result;
        assertTrue(statistics.containsKey("totalAdjustments"));
        assertTrue(statistics.containsKey("increaseCount"));
        assertTrue(statistics.containsKey("decreaseCount"));
    }

    // ==================== 价格审核测试 ====================

    @Test
    @DisplayName("价格审核测试")
    void testAuditPrice() {
        // 先创建一个价格调整 - 使用正确的SKU ID
        PriceService.PriceAdjustmentResult adjustmentResult = priceService.adjustPrice(1L, 11L, 89.99, 1001L, "需要审核的调价");
        assertTrue(adjustmentResult.isSuccess());
        
        // 执行审核测试
        PriceService.PriceAuditResult result = priceService.auditPrice(
            adjustmentResult.getPriceHistoryId(), 
            true, 
            "审核通过", 
            2001L
        );
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("APPROVED", result.getAuditStatus());
        assertEquals(adjustmentResult.getPriceHistoryId(), result.getPriceHistoryId());
    }

    @Test
    @DisplayName("价格审核拒绝测试")
    void testRejectPriceAudit() {
        // 先创建一个价格调整 - 使用正确的SKU ID
        PriceService.PriceAdjustmentResult adjustmentResult = priceService.adjustPrice(1L, 11L, 89.99, 1001L, "需要审核的调价");
        assertTrue(adjustmentResult.isSuccess());
        
        // 执行审核拒绝测试
        PriceService.PriceAuditResult result = priceService.auditPrice(
            adjustmentResult.getPriceHistoryId(), 
            false, 
            "价格调整幅度过大", 
            2001L
        );
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("REJECTED", result.getAuditStatus());
        assertEquals(adjustmentResult.getPriceHistoryId(), result.getPriceHistoryId());
    }

    @Test
    @DisplayName("获取待审核价格列表测试")
    void testGetPendingAuditPrices() {
        // 先创建一些待审核的价格调整 - 使用正确的SKU ID
        priceService.adjustPrice(1L, 11L, 89.99, 1001L, "待审核调价1");
        priceService.adjustPrice(2L, 21L, 179.99, 1001L, "待审核调价2");
        
        // 执行测试
        Object result = priceService.getPendingAuditPrices(1L, 10L);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("records"));
        assertTrue(resultMap.containsKey("total"));
    }

    // ==================== 价格策略测试 ====================

    @Test
    @DisplayName("创建价格策略测试")
    void testCreatePriceStrategy() {
        // 准备测试数据
        PriceService.PriceStrategy strategy = new PriceService.PriceStrategy();
        strategy.setName("春季促销策略");
        strategy.setStrategyType("2"); // 百分比调整
        strategy.setAdjustmentValue(-15.0); // 降价15%
        strategy.setDescription("春季商品15%折扣");
        strategy.setCreatorId(1001L);
        
        // 执行测试
        PriceService.PriceStrategyResult result = priceService.createPriceStrategy(strategy);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getStrategyId());
        assertEquals("价格策略创建成功", result.getMessage());
    }

    @Test
    @DisplayName("应用价格策略测试")
    void testApplyPriceStrategy() {
        // 先创建策略
        PriceService.PriceStrategy strategy = new PriceService.PriceStrategy();
        strategy.setName("测试策略");
        strategy.setStrategyType("2"); // 百分比调整
        strategy.setAdjustmentValue(-10.0); // 降价10%
        strategy.setDescription("测试策略描述");
        strategy.setCreatorId(1001L);
        
        PriceService.PriceStrategyResult createResult = priceService.createPriceStrategy(strategy);
        assertTrue(createResult.isSuccess());
        
        // 应用策略
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);
        PriceService.PriceStrategyResult result = priceService.applyPriceStrategy(
            createResult.getStrategyId(), 
            productIds, 
            1001L
        );
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(createResult.getStrategyId(), result.getStrategyId());
        assertTrue(result.getAffectedCount() >= 0);
    }

    @Test
    @DisplayName("获取价格策略列表测试")
    void testGetPriceStrategies() {
        // 执行测试
        Object result = priceService.getPriceStrategies(1L, 10L);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("records"));
        assertTrue(resultMap.containsKey("total"));
        assertTrue(resultMap.containsKey("current"));
        assertTrue(resultMap.containsKey("size"));
    }

    // ==================== 异常情况测试 ====================

    @Test
    @DisplayName("空价格调整对象测试")
    void testNullPriceAdjustment() {
        // 执行测试
        PriceService.PriceAdjustmentResult result = priceService.adjustPrice(null, null, null, null, null);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("商品ID不能为空"));
    }

    @Test
    @DisplayName("无效商品ID价格调整测试")
    void testInvalidProductIdPriceAdjustment() {
        // 执行测试 - 使用无效的商品ID，但使用正确的SKU ID格式
        PriceService.PriceAdjustmentResult result = priceService.adjustPrice(-1L, 11L, 89.99, 1001L, "测试");
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("商品"));
    }

    @Test
    @DisplayName("无效审核ID测试")
    void testInvalidAuditId() {
        // 执行测试
        PriceService.PriceAuditResult result = priceService.auditPrice(999L, true, "审核通过", 2001L);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不存在"));
    }

    @Test
    @DisplayName("空价格策略对象测试")
    void testNullPriceStrategy() {
        // 执行测试
        PriceService.PriceStrategyResult result = priceService.createPriceStrategy(null);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("策略名称不能为空", result.getMessage());
    }

    // ==================== 性能测试 ====================

    @Test
    @DisplayName("批量价格调整性能测试")
    void testBatchPriceAdjustmentPerformance() {
        // 准备大量测试数据 - 使用正确的SKU ID
        List<PriceService.PriceAdjustment> adjustments = Arrays.asList(
            createPriceAdjustment(1L, 11L, 89.99),
            createPriceAdjustment(2L, 21L, 179.99),
            createPriceAdjustment(3L, 31L, 269.99),
            createPriceAdjustment(4L, 41L, 359.99),
            createPriceAdjustment(5L, 51L, 449.99)
        );
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 执行批量调价
        PriceService.BatchPriceAdjustmentResult result = priceService.batchAdjustPrice(adjustments);
        
        // 记录结束时间
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证结果和性能
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(5, result.getSuccessCount());
        assertTrue(duration < 1000, "批量价格调整应在1秒内完成，实际耗时: " + duration + "ms");
    }

    // ==================== 辅助方法 ====================

    private PriceService.PriceAdjustment createPriceAdjustment(Long productId, Long skuId, Double newPrice) {
        PriceService.PriceAdjustment adjustment = new PriceService.PriceAdjustment();
        adjustment.setProductId(productId);
        adjustment.setSkuId(skuId);
        adjustment.setNewPrice(newPrice);
        adjustment.setReason("性能测试");
        adjustment.setOperatorId(1001L);
        return adjustment;
    }
}