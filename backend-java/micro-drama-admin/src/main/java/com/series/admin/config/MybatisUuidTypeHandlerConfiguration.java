package com.series.admin.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SqlSessionFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * 在 {@link SqlSessionFactoryBean} 阶段注册 UUID TypeHandler 扫描包，早于 Mapper 解析参数映射，
 * 避免仅使用 {@code ConfigurationCustomizer} 时 MyBatis-Plus 已缓存「无 TypeHandler」的映射。
 */
@org.springframework.context.annotation.Configuration
public class MybatisUuidTypeHandlerConfiguration {

    private static final String HANDLER_PACKAGE = "com.series.admin.typehandler";

    @Bean
    public SqlSessionFactoryBeanCustomizer registerUuidTypeHandlersPackage() {
        return (SqlSessionFactoryBean factoryBean) -> {
            String existing = factoryBean.getTypeHandlersPackage();
            if (existing == null || existing.isEmpty()) {
                factoryBean.setTypeHandlersPackage(HANDLER_PACKAGE);
            } else if (!existing.contains(HANDLER_PACKAGE)) {
                factoryBean.setTypeHandlersPackage(existing + "," + HANDLER_PACKAGE);
            }
        };
    }
}
