package com.kwz.starter.mybatis.autoconfigure;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusInnerInterceptorAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.kwz.starter.mybatis.handler.WzMetaObjectHandler;
import com.kwz.starter.mybatis.properties.WzMybatisProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(
        after = MybatisPlusAutoConfiguration.class,
        before = MybatisPlusInnerInterceptorAutoConfiguration.class
)
@ConditionalOnClass(PaginationInnerInterceptor.class)
@ConditionalOnProperty(prefix = "wz.mybatis", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WzMybatisProperties.class)
public class WzMybatisAutoConfiguration {

    /**
     * 显式注册分页拦截器链。仅声明 {@link PaginationInnerInterceptor} Bean 时，
     * {@link MybatisPlusInnerInterceptorAutoConfiguration} 可能因加载顺序错过聚合，导致 total 恒为 0。
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor wzMybatisPlusInterceptor(WzMybatisProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor();
        if (properties.getPaginationMaxLimit() > 0) {
            pagination.setMaxLimit(properties.getPaginationMaxLimit());
        }
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    @ConditionalOnProperty(prefix = "wz.mybatis", name = "audit", havingValue = "true", matchIfMissing = true)
    public WzMetaObjectHandler wzMetaObjectHandler(
            ObjectProvider<com.kwz.starter.mybatis.handler.AuditUserProvider> auditUserProvider,
            WzMybatisProperties properties) {
        return new WzMetaObjectHandler(auditUserProvider, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "wz.mybatis", name = "logic-delete", havingValue = "true", matchIfMissing = true)
    public MybatisPlusPropertiesCustomizer wzLogicDeleteCustomizer(WzMybatisProperties properties) {
        return plusProperties -> {
            GlobalConfig globalConfig = plusProperties.getGlobalConfig();
            if (globalConfig == null) {
                globalConfig = new GlobalConfig();
                plusProperties.setGlobalConfig(globalConfig);
            }
            GlobalConfig.DbConfig dbConfig = globalConfig.getDbConfig();
            if (dbConfig == null) {
                dbConfig = new GlobalConfig.DbConfig();
                globalConfig.setDbConfig(dbConfig);
            }
            dbConfig.setLogicDeleteField(properties.getLogicDeleteField());
            dbConfig.setLogicDeleteValue(properties.getLogicDeletedValue());
            dbConfig.setLogicNotDeleteValue(properties.getLogicNotDeletedValue());
        };
    }
}
