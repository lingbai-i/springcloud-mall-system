package com.mall.payment.annotation;

import java.lang.annotation.*;

/**
 * 权限检查注解
 * 用于方法级别的权限控制
 * 
 * @author lingbai
 * @version 1.0
 * @since 2024-12-01
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 所需权限列表
     * 用户必须拥有其中至少一个权限才能访问
     * 
     * @return 权限数组
     */
    String[] value() default {};

    /**
     * 是否需要拥有所有权限
     * true: 用户必须拥有所有指定权限
     * false: 用户只需拥有其中一个权限即可（默认）
     * 
     * @return 是否需要所有权限
     */
    boolean requireAll() default false;

    /**
     * 权限检查失败时的错误消息
     * 
     * @return 错误消息
     */
    String message() default "权限不足";

    /**
     * 是否允许资源所有者访问
     * 当设置为true时，如果当前用户是资源的所有者，则允许访问
     * 
     * @return 是否允许所有者访问
     */
    boolean allowOwner() default false;

    /**
     * 资源所有者字段名
     * 当allowOwner为true时，用于指定资源中表示所有者的字段名
     * 
     * @return 所有者字段名
     */
    String ownerField() default "userId";
}