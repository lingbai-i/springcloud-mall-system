package com.mall.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 支付服务启动类
 * 基于Spring Boot的支付微服务应用程序入口
 * 
 * <p>服务功能：</p>
 * <ul>
 *   <li>支付订单管理：创建、查询、更新支付订单</li>
 *   <li>多渠道支付：支持支付宝、微信支付、银行卡、余额支付</li>
 *   <li>退款处理：全额退款、部分退款、退款查询</li>
 *   <li>回调处理：处理第三方支付平台的异步通知</li>
 *   <li>数据统计：支付数据分析和报表生成</li>
 * </ul>
 * 
 * <p>技术架构：</p>
 * <ul>
 *   <li>Spring Boot 2.7.x：应用框架</li>
 *   <li>Spring Cloud：微服务治理</li>
 *   <li>MyBatis Plus：数据访问层</li>
 *   <li>Redis：缓存和分布式锁</li>
 *   <li>MySQL：数据持久化</li>
 *   <li>RabbitMQ：消息队列</li>
 * </ul>
 * 
 * <p>启动配置：</p>
 * <ul>
 *   <li>端口：默认8082</li>
 *   <li>数据库：payment_service</li>
 *   <li>注册中心：Nacos</li>
 *   <li>配置中心：Nacos Config</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加服务功能和技术架构说明
 * V1.1 2024-12-15：增加微服务配置和消息队列支持
 * V1.0 2024-12-01：初始版本，基础支付服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class PaymentServiceApplication {

    /**
     * 支付服务主入口方法
     * 启动Spring Boot应用程序，初始化支付服务的所有组件
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}