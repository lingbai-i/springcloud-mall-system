package com.mall.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(SmsProperties smsProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(smsProperties.getPush().getConnectTimeout());
        factory.setReadTimeout(smsProperties.getPush().getReadTimeout());
        return new RestTemplate(factory);
    }
}