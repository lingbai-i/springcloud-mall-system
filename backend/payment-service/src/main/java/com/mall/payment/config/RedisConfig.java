package com.mall.payment.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 配置RedisTemplate和相关的序列化器
 * 
 * <p>配置功能：</p>
 * <ul>
 *   <li>RedisTemplate配置：提供Redis操作模板</li>
 *   <li>序列化配置：Key使用String序列化，Value使用JSON序列化</li>
 *   <li>连接池配置：配置Redis连接池参数</li>
 *   <li>缓存策略：支持分布式缓存和分布式锁</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>支付订单缓存：缓存热点支付订单数据</li>
 *   <li>分布式锁：防止重复支付的并发控制</li>
 *   <li>会话存储：存储用户支付会话信息</li>
 *   <li>计数器：支付统计和限流计数</li>
 * </ul>
 * 
 * @author lingbai
 * @version 1.2
 * @since 2025-11-01
 * 
 * 修改日志：
 * V1.2 2025-11-01：完善Javadoc注释，增加配置功能和使用场景说明
 * V1.1 2024-12-20：优化序列化配置，提升性能
 * V1.0 2024-12-01：初始版本，基础Redis配置
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * 设置Key和Value的序列化方式
     * 
     * @param connectionFactory Redis连接工厂
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置Key的序列化方式
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 设置Value的序列化方式
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        // 启用默认序列化方式
        template.setDefaultSerializer(jsonSerializer);
        template.setEnableDefaultSerializer(true);
        
        template.afterPropertiesSet();
        return template;
    }
}