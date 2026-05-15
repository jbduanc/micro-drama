package com.series.content.config;

import com.series.common.typehandler.UuidTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisUuidTypeHandlerConfiguration {

    @Bean
    public ConfigurationCustomizer registerUuidTypeHandler() {
        return configuration -> {
            TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
            registry.register(UuidTypeHandler.class);
        };
    }
}
