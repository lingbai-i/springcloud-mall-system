package com.mall.merchant.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置
 * 用于服务间调用（如调用SMS服务）
 */
@Configuration
public class RestTemplateConfig {

  @Bean
  @LoadBalanced // 启用负载均衡，支持服务名调用
  public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000); // 连接超时5秒
    factory.setReadTimeout(30000); // 读取超时30秒
    return new RestTemplate(factory);
  }
}
