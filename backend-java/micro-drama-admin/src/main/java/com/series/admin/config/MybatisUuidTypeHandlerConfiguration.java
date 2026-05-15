package com.series.admin.config;

import com.series.admin.typehandler.UuidTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * 注册 {@link java.util.UUID} 与 JDBC 的映射，供 MyBatis-Plus 插入/查询 {@code UUID} 主键使用。
 * 注意：勿与 {@link org.apache.ibatis.session.Configuration} 做单类型 import 同名冲突。
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
