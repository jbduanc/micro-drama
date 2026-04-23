package com.series.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 1. 设置代理服务器地址和端口
        // 使用 Type.HTTP 对应 v2rayN 的 HTTP 代理端口
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10808));
        // 如果要使用 SOCKS5 代理，改为 new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 10808));

        // 2. 创建请求工厂，并注入代理设置
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setProxy(proxy);

        // 可选：设置连接和读取超时（毫秒）
        factory.setConnectTimeout(10000); // 10秒
        factory.setReadTimeout(10000);    // 10秒

        // 3. 返回使用该工厂的 RestTemplate 实例
        return new RestTemplate(factory);
    }
}