package com.series.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * 供 Spring Security {@code http.cors()} 使用，保证 CORS 在 Security 过滤器链中顺序正确。
     * {@code cors.allowed-origins} 支持逗号分隔多个 Origin，不可整串传给 {@link CorsConfiguration#addAllowedOrigin}。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        for (String origin : allowedOrigins.split(",")) {
            String o = origin.trim();
            if (!o.isEmpty()) {
                config.addAllowedOrigin(o);
            }
        }
        config.setAllowCredentials(true);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
