package com.mall.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * 测试基类
 * 提供通用的测试配置和工具方法
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-01
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
public abstract class BaseTest {
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    /**
     * 测试前置操作
     * 在每个测试方法执行前调用
     */
    @BeforeEach
    public void setUp() {
        // 可以在这里添加通用的测试前置操作
        initTestData();
    }
    
    /**
     * 初始化测试数据
     * 子类可以重写此方法来初始化特定的测试数据
     */
    protected void initTestData() {
        // 默认实现为空，子类可以重写
    }
    
    /**
     * 将对象转换为JSON字符串
     * 
     * @param object 要转换的对象
     * @return JSON字符串
     */
    protected String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("转换为JSON失败", e);
        }
    }
    
    /**
     * 将JSON字符串转换为对象
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 转换后的对象
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("从JSON转换失败", e);
        }
    }
    
    /**
     * 生成测试用的用户ID
     * 
     * @return 用户ID
     */
    protected Long generateTestUserId() {
        return 1001L;
    }
    
    /**
     * 生成测试用的业务订单ID
     * 
     * @return 业务订单ID
     */
    protected String generateTestBusinessOrderId() {
        return "BIZ_" + System.currentTimeMillis();
    }
    
    /**
     * 生成测试用的支付订单ID
     * 
     * @return 支付订单ID
     */
    protected String generateTestPaymentOrderId() {
        return "PAY_" + System.currentTimeMillis();
    }
}