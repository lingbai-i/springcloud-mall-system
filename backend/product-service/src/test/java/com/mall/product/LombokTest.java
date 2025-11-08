package com.mall.product;

import lombok.Getter;
import lombok.Setter;

/**
 * Lombok测试类
 * 用于验证Lombok注解是否正常工作
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Getter
@Setter
public class LombokTest {
    
    private String name;
    private Integer age;
    
    public static void main(String[] args) {
        LombokTest test = new LombokTest();
        test.setName("测试");
        test.setAge(25);
        
        System.out.println("姓名: " + test.getName());
        System.out.println("年龄: " + test.getAge());
    }
}