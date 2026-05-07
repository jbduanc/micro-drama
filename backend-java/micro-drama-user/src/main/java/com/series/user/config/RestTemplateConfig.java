package com.series.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;

@Configuration
public class RestTemplateConfig {
    @Bean
    @LoadBalanced // 启用负载均衡功能
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
