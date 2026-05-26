package com.kwz.starter.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.kwz.starter.mybatis.handler.WzMetaObjectHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = WzMybatisAutoConfigurationTest.TestApplication.class)
class WzMybatisAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldRegisterMybatisEnhancements() {
        MybatisPlusInterceptor interceptor = applicationContext.getBean(MybatisPlusInterceptor.class);
        assertThat(interceptor.getInterceptors())
                .anyMatch(PaginationInnerInterceptor.class::isInstance);
        assertThat(applicationContext.getBean(MetaObjectHandler.class)).isInstanceOf(WzMetaObjectHandler.class);
    }

    @SpringBootApplication
    static class TestApplication {
    }
}
