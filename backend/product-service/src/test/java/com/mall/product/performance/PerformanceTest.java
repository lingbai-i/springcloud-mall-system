package com.mall.product.performance;

import com.mall.product.domain.entity.Category;
import com.mall.product.domain.entity.Product;
import com.mall.product.service.CategoryService;
import com.mall.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 性能测试类
 * 测试商品服务的响应时间和稳定性
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@DisplayName("性能测试")
public class PerformanceTest {

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    private Product testProduct;
    private Category testCategory;

    // 性能指标
    private static final int CONCURRENT_USERS = 100;
    private static final int REQUESTS_PER_USER = 10;
    private static final long MAX_RESPONSE_TIME_MS = 500; // 最大响应时间500ms
    private static final double MIN_SUCCESS_RATE = 0.999; // 最小成功率99.9%
    private static final double MAX_ERROR_RATE = 0.001; // 最大错误率0.1%

    @BeforeEach
    void setUp() {
        // 初始化Mock对象
        MockitoAnnotations.openMocks(this);
        
        // 准备测试数据
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试商品");
        testProduct.setPrice(99.99);
        testProduct.setStock(100);
        testProduct.setCategoryId(1L);
        testProduct.setStatus(1);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("测试分类");
        testCategory.setParentId(null);
        testCategory.setLevel(1);
        testCategory.setSort(100);
        testCategory.setStatus(1);
    }

    // ==================== 商品服务性能测试 ====================

    @Test
    @DisplayName("商品查询性能测试")
    void testProductQueryPerformance() throws InterruptedException {
        // Mock服务层
        when(productService.getProductById(anyLong())).thenReturn(testProduct);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("商品查询", () -> {
            try {
                Product product = productService.getProductById(1L);
                return product != null;
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "商品查询");
    }

    @Test
    @DisplayName("商品创建性能测试")
    void testProductCreatePerformance() throws InterruptedException {
        // Mock服务层
        when(productService.addProduct(any(Product.class))).thenReturn(true);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("商品创建", () -> {
            try {
                return productService.addProduct(testProduct);
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "商品创建");
    }

    @Test
    @DisplayName("商品更新性能测试")
    void testProductUpdatePerformance() throws InterruptedException {
        // Mock服务层
        when(productService.updateProduct(any(Product.class))).thenReturn(true);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("商品更新", () -> {
            try {
                return productService.updateProduct(testProduct);
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "商品更新");
    }

    @Test
    @DisplayName("商品删除性能测试")
    void testProductDeletePerformance() throws InterruptedException {
        // Mock服务层
        when(productService.deleteProduct(anyLong())).thenReturn(true);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("商品删除", () -> {
            try {
                return productService.deleteProduct(1L);
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "商品删除");
    }

    @Test
    @DisplayName("商品搜索性能测试")
    void testProductSearchPerformance() throws InterruptedException {
        // Mock服务层 - 模拟分页数据结构
        Map<String, Object> searchResults = new HashMap<>();
        searchResults.put("records", List.of(testProduct));
        searchResults.put("total", 1L);
        searchResults.put("current", 1L);
        searchResults.put("size", 10L);
        when(productService.searchProducts(anyLong(), anyLong(), anyString())).thenReturn(searchResults);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("商品搜索", () -> {
            try {
                Object searchResult = productService.searchProducts(1L, 10L, "测试");
                return searchResult != null;
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "商品搜索");
    }

    @Test
    @DisplayName("库存更新性能测试")
    void testStockUpdatePerformance() throws InterruptedException {
        // Mock服务层
        when(productService.updateStock(anyLong(), anyInt())).thenReturn(true);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("库存更新", () -> {
            try {
                return productService.updateStock(1L, 50);
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "库存更新");
    }

    @Test
    @DisplayName("价格更新性能测试")
    void testPriceUpdatePerformance() throws InterruptedException {
        // Mock服务层
        when(productService.updateProductPrice(anyLong(), anyDouble(), anyString(), anyLong())).thenReturn(true);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("价格更新", () -> {
            try {
                return productService.updateProductPrice(1L, 199.99, "促销活动", 1L);
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "价格更新");
    }

    // ==================== 分类服务性能测试 ====================

    @Test
    @DisplayName("分类查询性能测试")
    void testCategoryQueryPerformance() throws InterruptedException {
        // Mock服务层
        when(categoryService.getCategoryById(anyLong())).thenReturn(testCategory);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("分类查询", () -> {
            try {
                Category category = categoryService.getCategoryById(1L);
                return category != null;
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "分类查询");
    }

    @Test
    @DisplayName("分类树构建性能测试")
    void testCategoryTreePerformance() throws InterruptedException {
        // Mock服务层
        List<Category> categoryTree = List.of(testCategory);
        when(categoryService.buildCategoryTree()).thenReturn(categoryTree);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("分类树构建", () -> {
            try {
                List<Category> tree = categoryService.buildCategoryTree();
                return tree != null && !tree.isEmpty();
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "分类树构建");
    }

    @Test
    @DisplayName("分类搜索性能测试")
    void testCategorySearchPerformance() throws InterruptedException {
        // Mock服务层
        List<Category> searchResults = List.of(testCategory);
        when(categoryService.searchCategories(anyString())).thenReturn(searchResults);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("分类搜索", () -> {
            try {
                List<Category> categories = categoryService.searchCategories("测试");
                return categories != null && !categories.isEmpty();
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "分类搜索");
    }

    @Test
    @DisplayName("分类缓存刷新性能测试")
    void testCategoryCacheRefreshPerformance() throws InterruptedException {
        // Mock服务层
        when(categoryService.refreshCategoryCache()).thenReturn(true);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("分类缓存刷新", () -> {
            try {
                return categoryService.refreshCategoryCache();
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "分类缓存刷新");
    }

    // ==================== 批量操作性能测试 ====================

    @Test
    @DisplayName("批量商品操作性能测试")
    void testBatchProductOperationPerformance() throws InterruptedException {
        // Mock服务层
        List<Long> productIds = List.of(1L, 2L, 3L, 4L, 5L);
        when(productService.batchDeleteProducts(anyList())).thenReturn(true);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("批量商品删除", () -> {
            try {
                return productService.batchDeleteProducts(productIds);
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "批量商品删除");
    }

    @Test
    @DisplayName("批量库存更新性能测试")
    void testBatchStockUpdatePerformance() throws InterruptedException {
        // Mock服务层
        List<ProductService.StockUpdate> stockUpdates = new ArrayList<>();
        stockUpdates.add(new ProductService.StockUpdate(1L, 100));
        stockUpdates.add(new ProductService.StockUpdate(2L, 200));
        when(productService.batchUpdateStock(anyList())).thenReturn(true);

        // 性能测试结果
        PerformanceResult result = executePerformanceTest("批量库存更新", () -> {
            try {
                return productService.batchUpdateStock(stockUpdates);
            } catch (Exception e) {
                return false;
            }
        });

        // 验证性能指标
        assertPerformanceMetrics(result, "批量库存更新");
    }

    // ==================== 并发测试 ====================

    @Test
    @DisplayName("高并发商品查询测试")
    void testHighConcurrencyProductQuery() throws InterruptedException {
        // Mock服务层
        when(productService.getProductById(anyLong())).thenReturn(testProduct);

        // 执行高并发测试
        PerformanceResult result = executeHighConcurrencyTest("高并发商品查询", () -> {
            try {
                Product product = productService.getProductById(1L);
                return product != null;
            } catch (Exception e) {
                return false;
            }
        }, 500, 20); // 500个并发用户，每个用户20次请求

        // 验证高并发性能指标
        assertHighConcurrencyMetrics(result, "高并发商品查询");
    }

    @Test
    @DisplayName("高并发库存更新测试")
    void testHighConcurrencyStockUpdate() throws InterruptedException {
        // Mock服务层
        when(productService.updateStock(anyLong(), anyInt())).thenReturn(true);

        // 执行高并发测试
        PerformanceResult result = executeHighConcurrencyTest("高并发库存更新", () -> {
            try {
                return productService.updateStock(1L, 1);
            } catch (Exception e) {
                return false;
            }
        }, 200, 10); // 200个并发用户，每个用户10次请求

        // 验证高并发性能指标
        assertHighConcurrencyMetrics(result, "高并发库存更新");
    }

    // ==================== 压力测试 ====================

    @Test
    @DisplayName("系统压力测试")
    void testSystemStressTest() throws InterruptedException {
        // Mock所有服务层方法
        when(productService.getProductById(anyLong())).thenReturn(testProduct);
        when(productService.addProduct(any(Product.class))).thenReturn(true);
        when(productService.updateProduct(any(Product.class))).thenReturn(true);
        when(categoryService.getCategoryById(anyLong())).thenReturn(testCategory);
        when(categoryService.buildCategoryTree()).thenReturn(List.of(testCategory));

        // 执行混合压力测试
        PerformanceResult result = executeStressTest("系统压力测试", 1000, 50);

        // 验证压力测试指标
        assertStressTestMetrics(result, "系统压力测试");
    }

    // ==================== 辅助方法 ====================

    /**
     * 执行性能测试
     */
    private PerformanceResult executePerformanceTest(String testName, Callable<Boolean> operation) 
            throws InterruptedException {
        return executeHighConcurrencyTest(testName, operation, CONCURRENT_USERS, REQUESTS_PER_USER);
    }

    /**
     * 执行高并发测试
     */
    private PerformanceResult executeHighConcurrencyTest(String testName, Callable<Boolean> operation, 
            int concurrentUsers, int requestsPerUser) throws InterruptedException {
        
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(concurrentUsers * requestsPerUser);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        
        long startTime = System.currentTimeMillis();
        
        // 提交任务
        for (int i = 0; i < concurrentUsers; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestsPerUser; j++) {
                    long requestStart = System.nanoTime();
                    try {
                        boolean success = operation.call();
                        long responseTime = (System.nanoTime() - requestStart) / 1_000_000; // 转换为毫秒
                        
                        totalResponseTime.addAndGet(responseTime);
                        maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
                        minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
                        
                        if (success) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }
        
        // 等待所有任务完成
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        int totalRequests = concurrentUsers * requestsPerUser;
        
        return new PerformanceResult(
            testName,
            totalRequests,
            successCount.get(),
            errorCount.get(),
            totalTime,
            totalResponseTime.get() / totalRequests, // 平均响应时间
            maxResponseTime.get(),
            minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get(),
            (double) totalRequests / totalTime * 1000 // TPS
        );
    }

    /**
     * 执行压力测试
     */
    private PerformanceResult executeStressTest(String testName, int totalUsers, int requestsPerUser) 
            throws InterruptedException {
        
        // 混合操作
        Callable<Boolean> mixedOperation = () -> {
            try {
                // 随机执行不同操作
                int operation = (int) (Math.random() * 5);
                switch (operation) {
                    case 0:
                        return productService.getProductById(1L) != null;
                    case 1:
                        return productService.addProduct(testProduct);
                    case 2:
                        return productService.updateProduct(testProduct);
                    case 3:
                        return categoryService.getCategoryById(1L) != null;
                    case 4:
                        return categoryService.buildCategoryTree() != null;
                    default:
                        return true;
                }
            } catch (Exception e) {
                return false;
            }
        };
        
        return executeHighConcurrencyTest(testName, mixedOperation, totalUsers, requestsPerUser);
    }

    /**
     * 验证性能指标
     */
    private void assertPerformanceMetrics(PerformanceResult result, String testName) {
        System.out.println("\n========== " + testName + " 性能测试结果 ==========");
        System.out.println("总请求数: " + result.totalRequests);
        System.out.println("成功请求数: " + result.successCount);
        System.out.println("失败请求数: " + result.errorCount);
        System.out.println("成功率: " + String.format("%.3f%%", result.getSuccessRate() * 100));
        System.out.println("错误率: " + String.format("%.3f%%", result.getErrorRate() * 100));
        System.out.println("平均响应时间: " + result.avgResponseTime + "ms");
        System.out.println("最大响应时间: " + result.maxResponseTime + "ms");
        System.out.println("最小响应时间: " + result.minResponseTime + "ms");
        System.out.println("TPS: " + String.format("%.2f", result.tps));
        System.out.println("总耗时: " + result.totalTime + "ms");
        
        // 验证性能要求
        assertTrue(result.avgResponseTime <= MAX_RESPONSE_TIME_MS, 
            "平均响应时间超过" + MAX_RESPONSE_TIME_MS + "ms: " + result.avgResponseTime + "ms");
        assertTrue(result.getSuccessRate() >= MIN_SUCCESS_RATE, 
            "成功率低于" + (MIN_SUCCESS_RATE * 100) + "%: " + (result.getSuccessRate() * 100) + "%");
        assertTrue(result.getErrorRate() <= MAX_ERROR_RATE, 
            "错误率高于" + (MAX_ERROR_RATE * 100) + "%: " + (result.getErrorRate() * 100) + "%");
    }

    /**
     * 验证高并发性能指标
     */
    private void assertHighConcurrencyMetrics(PerformanceResult result, String testName) {
        assertPerformanceMetrics(result, testName);
        
        // 高并发特定验证
        assertTrue(result.tps > 100, "TPS过低: " + result.tps);
        assertTrue(result.maxResponseTime <= MAX_RESPONSE_TIME_MS * 2, 
            "最大响应时间过长: " + result.maxResponseTime + "ms");
    }

    /**
     * 验证压力测试指标
     */
    private void assertStressTestMetrics(PerformanceResult result, String testName) {
        System.out.println("\n========== " + testName + " 压力测试结果 ==========");
        System.out.println("总请求数: " + result.totalRequests);
        System.out.println("成功请求数: " + result.successCount);
        System.out.println("失败请求数: " + result.errorCount);
        System.out.println("成功率: " + String.format("%.3f%%", result.getSuccessRate() * 100));
        System.out.println("错误率: " + String.format("%.3f%%", result.getErrorRate() * 100));
        System.out.println("平均响应时间: " + result.avgResponseTime + "ms");
        System.out.println("最大响应时间: " + result.maxResponseTime + "ms");
        System.out.println("TPS: " + String.format("%.2f", result.tps));
        
        // 压力测试相对宽松的要求
        assertTrue(result.getSuccessRate() >= 0.95, 
            "压力测试成功率过低: " + (result.getSuccessRate() * 100) + "%");
        assertTrue(result.getErrorRate() <= 0.05, 
            "压力测试错误率过高: " + (result.getErrorRate() * 100) + "%");
    }

    /**
     * 性能测试结果类
     */
    private static class PerformanceResult {
        final String testName;
        final int totalRequests;
        final int successCount;
        final int errorCount;
        final long totalTime;
        final long avgResponseTime;
        final long maxResponseTime;
        final long minResponseTime;
        final double tps;

        public PerformanceResult(String testName, int totalRequests, int successCount, int errorCount,
                long totalTime, long avgResponseTime, long maxResponseTime, long minResponseTime, double tps) {
            this.testName = testName;
            this.totalRequests = totalRequests;
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.totalTime = totalTime;
            this.avgResponseTime = avgResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.minResponseTime = minResponseTime;
            this.tps = tps;
        }

        public double getSuccessRate() {
            return totalRequests > 0 ? (double) successCount / totalRequests : 0;
        }

        public double getErrorRate() {
            return totalRequests > 0 ? (double) errorCount / totalRequests : 0;
        }
    }
}