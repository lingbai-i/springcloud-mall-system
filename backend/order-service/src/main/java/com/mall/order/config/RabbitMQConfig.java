package com.mall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 配置订单事件相关的Exchange和Queue
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-01-21
 */
@Configuration
public class RabbitMQConfig {
    
    /**
     * 订单事件Exchange
     */
    public static final String ORDER_EXCHANGE = "order.exchange";
    
    /**
     * 订单创建队列
     */
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    
    /**
     * 订单支付队列
     */
    public static final String ORDER_PAID_QUEUE = "order.paid.queue";
    
    /**
     * 订单取消队列
     */
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";
    
    /**
     * 声明订单事件Exchange
     */
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE, true, false);
    }
    
    /**
     * 声明订单创建队列
     */
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }
    
    /**
     * 声明订单支付队列
     */
    @Bean
    public Queue orderPaidQueue() {
        return new Queue(ORDER_PAID_QUEUE, true);
    }
    
    /**
     * 声明订单取消队列
     */
    @Bean
    public Queue orderCancelledQueue() {
        return new Queue(ORDER_CANCELLED_QUEUE, true);
    }
    
    /**
     * 绑定订单创建队列到Exchange
     */
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with("order.created");
    }
    
    /**
     * 绑定订单支付队列到Exchange
     */
    @Bean
    public Binding orderPaidBinding() {
        return BindingBuilder
                .bind(orderPaidQueue())
                .to(orderExchange())
                .with("order.paid");
    }
    
    /**
     * 绑定订单取消队列到Exchange
     */
    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder
                .bind(orderCancelledQueue())
                .to(orderExchange())
                .with("order.cancelled");
    }
}
