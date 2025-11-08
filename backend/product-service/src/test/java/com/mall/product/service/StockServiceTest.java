package com.mall.product.service;

import com.mall.product.service.impl.StockServiceImpl;
import com.mall.product.util.RedisDistributedLock;
import com.mall.product.util.OptimisticLockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 库存服务单元测试
 * 测试库存监控、预警、扣减、回滚、日志记录等核心功能
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@DisplayName("库存服务测试")
public class StockServiceTest {

    private StockService stockService;

    /**
     * 测试用的分布式锁实现
     * 总是返回成功获取锁的结果
     */
    private static class TestRedisDistributedLock extends RedisDistributedLock {
        @Override
        public DistributedLockResult tryLock(String lockKey, int expireSeconds, long timeoutMs) {
            return new DistributedLockResult(true, "test_lock_" + lockKey, "test_value", null);
        }
        
        @Override
        public void unlock(DistributedLockResult lockResult) {
            // 测试环境下不需要实际释放锁
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        StockServiceImpl stockServiceImpl = new StockServiceImpl();
        
        // 使用反射注入测试用的RedisDistributedLock
        Field distributedLockField = StockServiceImpl.class.getDeclaredField("distributedLock");
        distributedLockField.setAccessible(true);
        distributedLockField.set(stockServiceImpl, new TestRedisDistributedLock());
        
        // 使用反射注入OptimisticLockHelper
        Field optimisticLockHelperField = StockServiceImpl.class.getDeclaredField("optimisticLockHelper");
        optimisticLockHelperField.setAccessible(true);
        optimisticLockHelperField.set(stockServiceImpl, new OptimisticLockHelper());
        
        this.stockService = stockServiceImpl;
    }

    // ==================== 库存监控测试 ====================

    @Test
    @DisplayName("获取实时库存监控数据测试")
    void testGetRealTimeStockMonitor() {
        // 执行测试
        Object result = stockService.getStockMonitorData();
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("totalProducts"));
        assertTrue(resultMap.containsKey("totalStockValue")); // 修正：实际返回的是totalStockValue而不是totalStock
        assertTrue(resultMap.containsKey("lowStockCount"));
        assertTrue(resultMap.containsKey("outOfStockCount"));
        
        // 验证数据合理性
        assertTrue((Integer) resultMap.get("totalProducts") >= 0);
        assertTrue((Double) resultMap.get("totalStockValue") >= 0); // 修正：totalStockValue是Double类型
    }

    @Test
    @DisplayName("获取库存预警商品测试")
    void testGetStockWarningProducts() {
        // 执行测试 - 获取低库存预警
        List<com.mall.product.domain.entity.Product> lowStockProducts = stockService.getStockWarningProducts(1);
        
        // 验证结果
        assertNotNull(lowStockProducts);
        
        if (!lowStockProducts.isEmpty()) {
            com.mall.product.domain.entity.Product product = lowStockProducts.get(0);
            assertNotNull(product.getId());
            assertNotNull(product.getName());
            assertTrue(product.getStock() >= 0);
        }
    }

    // ==================== 库存扣减测试 ====================

    @Test
    @DisplayName("单个商品库存扣减测试")
    void testDeductStock() {
        // 执行测试 - 使用正确的SKU ID (商品1的SKU ID是11)
        StockService.StockOperationResult result = stockService.deductStock(1L, 11L, 5, "ORDER_001", 1001L);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("扣减成功", result.getMessage());
        assertNotNull(result.getLogId());
        assertTrue(result.getAfterStock() >= 0);
    }

    @Test
    @DisplayName("批量库存扣减测试")
    void testBatchDeductStock() {
        // 准备测试数据 - 使用正确的SKU ID (商品1的SKU ID是11，商品2的SKU ID是21)
        StockService.StockOperation operation1 = new StockService.StockOperation(1L, 11L, 3, "ORDER_002", 1001L, "销售扣减");
        StockService.StockOperation operation2 = new StockService.StockOperation(2L, 21L, 2, "ORDER_002", 1001L, "销售扣减");
        
        List<StockService.StockOperation> operations = Arrays.asList(operation1, operation2);
        
        // 执行测试
        StockService.BatchStockOperationResult result = stockService.batchDeductStock(operations);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
    }

    @Test
    @DisplayName("库存不足扣减测试")
    void testDeductStockInsufficientStock() {
        // 执行测试 - 扣减大量库存，使用正确的SKU ID (商品1的SKU ID是11)
        StockService.StockOperationResult result = stockService.deductStock(1L, 11L, 10000, "ORDER_003", 1001L);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("库存"));
    }

    // ==================== 库存回滚测试 ====================

    @Test
    @DisplayName("单个库存回滚测试")
    void testRollbackStock() {
        // 先扣减库存 - 使用正确的SKU ID (商品1的SKU ID是11)
        StockService.StockOperationResult deductResult = stockService.deductStock(1L, 11L, 5, "ORDER_004", 1001L);
        assertTrue(deductResult.isSuccess());
        
        // 执行回滚测试
        StockService.StockOperationResult rollbackResult = stockService.rollbackStock(1L, 11L, 5, "ORDER_004", 1001L);
        
        // 验证结果
        assertNotNull(rollbackResult);
        assertTrue(rollbackResult.isSuccess());
        assertEquals("回滚成功", rollbackResult.getMessage());
        assertEquals(deductResult.getAfterStock() + 5, rollbackResult.getAfterStock());
    }

    @Test
    @DisplayName("批量库存回滚测试")
    void testBatchRollbackStock() {
        // 准备回滚数据 - 使用正确的SKU ID (商品1的SKU ID是11，商品2的SKU ID是21)
        StockService.StockOperation operation1 = new StockService.StockOperation(1L, 11L, 2, "ORDER_005", 1001L, "订单取消回滚");
        StockService.StockOperation operation2 = new StockService.StockOperation(2L, 21L, 1, "ORDER_005", 1001L, "订单取消回滚");
        
        List<StockService.StockOperation> operations = Arrays.asList(operation1, operation2);
        
        // 执行测试
        StockService.BatchStockOperationResult result = stockService.batchRollbackStock(operations);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
    }

    // ==================== 库存日志测试 ====================

    @Test
    @DisplayName("获取库存变更日志测试")
    void testGetStockChangeLogs() {
        // 执行测试
        Object result = stockService.getStockLogs(1L, null, 1L, 10L);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        assertTrue(resultMap.containsKey("records"));
        assertTrue(resultMap.containsKey("total"));
    }

    // ==================== 库存盘点测试 ====================

    @Test
    @DisplayName("库存盘点测试")
    void testStockTaking() {
        // 执行测试 - 使用正确的SKU ID (商品1的SKU ID是11)
        StockService.StockOperationResult result = stockService.stockTaking(1L, 11L, 100, 1001L, "定期盘点");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("盘点成功", result.getMessage());
        assertNotNull(result.getLogId());
    }

    // ==================== 异常情况测试 ====================

    @Test
    @DisplayName("空操作对象测试")
    void testNullOperation() {
        // 执行测试
        StockService.StockOperationResult result = stockService.deductStock(null, null, null, null, null);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("参数") || result.getMessage().contains("空")); // 修正：实际消息包含"空"字符
    }

    @Test
    @DisplayName("无效商品ID测试")
    void testInvalidProductId() {
        // 执行测试 - 使用无效的商品ID，但使用有效的SKU ID格式
        StockService.StockOperationResult result = stockService.deductStock(-1L, 11L, 5, "ORDER001", 1001L);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("商品") || result.getMessage().contains("SKU"));
    }

    @Test
    @DisplayName("负数量扣减测试")
    void testNegativeQuantity() {
        // 执行测试 - 使用正确的SKU ID (商品1的SKU ID是11)
        StockService.StockOperationResult result = stockService.deductStock(1L, 11L, -5, "ORDER001", 1001L);
        
        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("数量") || result.getMessage().contains("参数"));
    }

    // ==================== 并发测试 ====================

    @Test
    @DisplayName("并发库存扣减测试")
    void testConcurrentStockDeduction() throws InterruptedException {
        final int threadCount = 10;
        final int operationsPerThread = 5;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    // 使用正确的SKU ID (商品1的SKU ID是11)
                    stockService.deductStock(1L, 11L, 1, "CONCURRENT_" + threadIndex + "_" + j, 1001L);
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证库存监控数据仍然正常
        Object monitor = stockService.getStockMonitorData();
        assertNotNull(monitor);
        assertTrue(monitor instanceof Map);
    }
}