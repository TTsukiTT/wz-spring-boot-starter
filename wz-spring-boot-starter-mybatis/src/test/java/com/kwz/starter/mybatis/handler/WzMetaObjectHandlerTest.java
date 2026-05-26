package com.kwz.starter.mybatis.handler;

import com.kwz.starter.mybatis.entity.BaseEntity;
import com.kwz.starter.mybatis.properties.WzMybatisProperties;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WzMetaObjectHandlerTest {

    @Test
    void shouldFillAuditFieldsOnInsert() {
        TestEntity entity = new TestEntity();
        WzMetaObjectHandler handler = new WzMetaObjectHandler(fixedUserProvider(100L), defaultProperties());

        handler.insertFill(SystemMetaObject.forObject(entity));

        assertThat(entity.getCreateTime()).isNotNull();
        assertThat(entity.getUpdateTime()).isNotNull();
        assertThat(entity.getCreateBy()).isEqualTo(100L);
        assertThat(entity.getUpdateBy()).isEqualTo(100L);
    }

    @Test
    void shouldFillUpdateFieldsOnUpdate() {
        TestEntity entity = new TestEntity();
        entity.setCreateTime(LocalDateTime.now().minusDays(1));
        entity.setCreateBy(100L);
        WzMetaObjectHandler handler = new WzMetaObjectHandler(fixedUserProvider(200L), defaultProperties());

        handler.updateFill(SystemMetaObject.forObject(entity));

        assertThat(entity.getUpdateTime()).isNotNull();
        assertThat(entity.getUpdateBy()).isEqualTo(200L);
        assertThat(entity.getCreateBy()).isEqualTo(100L);
        assertThat(entity.getCreateTime()).isBefore(entity.getUpdateTime());
    }

    @Test
    void shouldUseConfiguredTimezone() {
        WzMybatisProperties properties = new WzMybatisProperties();
        properties.setAuditTimezone("Asia/Shanghai");
        WzMetaObjectHandler handler = new WzMetaObjectHandler(fixedUserProvider(null), properties);
        TestEntity entity = new TestEntity();

        handler.insertFill(SystemMetaObject.forObject(entity));

        LocalDateTime expected = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        assertThat(entity.getCreateTime()).isBetween(expected.minusSeconds(2), expected.plusSeconds(2));
    }

    @Test
    void shouldNotUpdateCreateTimeOnUpdate() {
        LocalDateTime originalCreateTime = LocalDateTime.of(2026, 1, 1, 10, 0);
        TestEntity entity = new TestEntity();
        entity.setCreateTime(originalCreateTime);
        entity.setCreateBy(100L);
        WzMetaObjectHandler handler = new WzMetaObjectHandler(fixedUserProvider(200L), defaultProperties());

        handler.updateFill(SystemMetaObject.forObject(entity));

        assertThat(entity.getCreateTime()).isEqualTo(originalCreateTime);
    }

    private WzMybatisProperties defaultProperties() {
        return new WzMybatisProperties();
    }

    private ObjectProvider<AuditUserProvider> fixedUserProvider(Long userId) {
        @SuppressWarnings("unchecked")
        ObjectProvider<AuditUserProvider> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(() -> userId);
        return provider;
    }

    static class TestEntity extends BaseEntity {
    }
}
