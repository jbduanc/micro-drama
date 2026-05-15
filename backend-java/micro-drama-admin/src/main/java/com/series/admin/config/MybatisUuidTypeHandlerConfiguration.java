package com.series.admin.config;

import com.series.admin.typehandler.UuidTypeHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册 {@link java.util.UUID} 与 JDBC 的映射，供 MyBatis-Plus 插入/查询 {@code UUID} 主键使用。
 */
@Configuration
public class MybatisUuidTypeHandlerConfiguration {

    @Bean
    public ConfigurationCustomizer registerUuidTypeHandler() {
        return (Configuration configuration) -> {
            TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
            registry.register(UuidTypeHandler.class);
        };
    }
}
