package com.mall.product.performance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 简化的性能测试类
 * 测试基础性能指标
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@DisplayName("简化性能测试")
public class SimplePerformanceTest {

    // 性能指标
    private static final int CONCURRENT_USERS = 100;
    private static final int REQUESTS_PER_USER = 10;
    private static final long MAX_RESPONSE_TIME_MS = 500; // 最大响应时间500ms
    private static final double MIN_SUCCESS_RATE = 0.99; // 最小成功率99%

    @BeforeEach
    void setUp() {
        System.out.println("开始性能测试...");
    }

    @Test
    @DisplayName("基础响应时间测试")
    void testBasicResponseTime() throws InterruptedException {
        PerformanceResult result = executePerformanceTest("基础响应时间", () -> {
            // 模拟业务处理时间
            try {
                Thread.sleep(1); // 模拟1ms处理时间
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        });

        assertPerformanceMetrics(result, "基础响应时间");
    }

    @Test
    @DisplayName("并发处理能力测试")
    void testConcurrentProcessing() throws InterruptedException {
        PerformanceResult result = executePerformanceTest("并发处理", () -> {
            // 模拟并发处理
            try {
                Thread.sleep(2); // 模拟2ms处理时间
                return Math.random() > 0.001; // 99.9%成功率
            } catch (InterruptedException e) {
                return false;
            }
        });

        assertPerformanceMetrics(result, "并发处理");
    }

    @Test
    @DisplayName("高负载压力测试")
    void testHighLoadStress() throws InterruptedException {
        PerformanceResult result = executeStressTest("高负载压力", 500, 20);
        assertStressTestMetrics(result, "高负载压力");
    }

    @Test
    @DisplayName("内存使用效率测试")
    void testMemoryEfficiency() throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        PerformanceResult result = executePerformanceTest("内存效率", () -> {
            // 模拟内存使用
            String[] data = new String[100];
            for (int i = 0; i < 100; i++) {
                data[i] = "test_data_" + i;
            }
            return data.length == 100;
        });

        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterMemory - beforeMemory;
        
        System.out.println("内存使用量: " + (memoryUsed / 1024) + " KB");
        assertPerformanceMetrics(result, "内存效率");
    }

    @Test
    @DisplayName("CPU密集型操作测试")
    void testCpuIntensiveOperation() throws InterruptedException {
        PerformanceResult result = executePerformanceTest("CPU密集型", () -> {
            // 模拟CPU密集型计算
            int sum = 0;
            for (int i = 0; i < 1000; i++) {
                sum += i * i;
            }
            return sum > 0;
        });

        assertPerformanceMetrics(result, "CPU密集型");
    }

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
        CountDownLatch latch = new CountDownLatch(concurrentUsers);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        
        long startTime = System.currentTimeMillis();
        
        // 提交任务
        for (int i = 0; i < concurrentUsers; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerUser; j++) {
                        long requestStart = System.currentTimeMillis();
                        
                        try {
                            boolean success = operation.call();
                            long responseTime = System.currentTimeMillis() - requestStart;
                            
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
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有任务完成
        latch.await();
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        int totalRequests = concurrentUsers * requestsPerUser;
        long avgResponseTime = totalRequests > 0 ? totalResponseTime.get() / totalRequests : 0;
        double tps = totalTime > 0 ? (double) totalRequests / totalTime * 1000 : 0;
        
        return new PerformanceResult(
            testName,
            totalRequests,
            successCount.get(),
            errorCount.get(),
            totalTime,
            avgResponseTime,
            maxResponseTime.get(),
            minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get(),
            tps
        );
    }

    /**
     * 执行压力测试
     */
    private PerformanceResult executeStressTest(String testName, int totalRequests, int concurrentUsers) 
            throws InterruptedException {
        
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        
        long startTime = System.currentTimeMillis();
        
        // 提交压力测试任务
        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    long requestStart = System.currentTimeMillis();
                    
                    // 模拟混合操作
                    boolean success = performMixedOperations();
                    
                    long responseTime = System.currentTimeMillis() - requestStart;
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
            });
        }
        
        // 等待所有任务完成
        latch.await();
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        long avgResponseTime = totalRequests > 0 ? totalResponseTime.get() / totalRequests : 0;
        double tps = totalTime > 0 ? (double) totalRequests / totalTime * 1000 : 0;
        
        return new PerformanceResult(
            testName,
            totalRequests,
            successCount.get(),
            errorCount.get(),
            totalTime,
            avgResponseTime,
            maxResponseTime.get(),
            minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get(),
            tps
        );
    }

    /**
     * 执行混合操作
     */
    private boolean performMixedOperations() {
        try {
            // 模拟数据库查询
            Thread.sleep(1);
            
            // 模拟业务逻辑处理
            int result = 0;
            for (int i = 0; i < 100; i++) {
                result += i;
            }
            
            // 模拟缓存操作
            Thread.sleep(1);
            
            return result > 0 && Math.random() > 0.02; // 98%成功率
        } catch (InterruptedException e) {
            return false;
        }
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
        assert result.avgResponseTime <= MAX_RESPONSE_TIME_MS : 
            "平均响应时间超过" + MAX_RESPONSE_TIME_MS + "ms: " + result.avgResponseTime + "ms";
        assert result.getSuccessRate() >= MIN_SUCCESS_RATE : 
            "成功率低于" + (MIN_SUCCESS_RATE * 100) + "%: " + (result.getSuccessRate() * 100) + "%";
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
        assert result.getSuccessRate() >= 0.95 : 
            "压力测试成功率过低: " + (result.getSuccessRate() * 100) + "%";
        assert result.getErrorRate() <= 0.05 : 
            "压力测试错误率过高: " + (result.getErrorRate() * 100) + "%";
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