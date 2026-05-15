package com.series.admin.config;

import com.series.admin.typehandler.UuidTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * 注册 {@link UuidTypeHandler}（兼容 mybatis-spring-boot 2.1.x，无 SqlSessionFactoryBeanCustomizer）。
 * 另见 {@code application.yml} 中 {@code mybatis-plus.type-handlers-package}。
 */
@org.springframework.context.annotation.Configuration
public class MybatisUuidTypeHandlerConfiguration {

    @Bean
    public ConfigurationCustomizer registerUuidTypeHandler() {
        return (org.apache.ibatis.session.Configuration configuration) -> {
            TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
            registry.register(UuidTypeHandler.class);
        };
    }
}
