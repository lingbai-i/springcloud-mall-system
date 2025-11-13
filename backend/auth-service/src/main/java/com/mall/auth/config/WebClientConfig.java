package com.mall.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient配置类
 * 
 * 配置用于调用其他微服务的HTTP客户端
 * 
 * @author lingbai
 * @version 1.0
 * @since 2025-11-11
 */
@Slf4j
@Configuration
public class WebClientConfig {

    /**
     * 配置WebClient Builder
     * 
     * @return WebClient.Builder实例
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        log.info("配置WebClient Builder...");
        return WebClient.builder();
    }
}
