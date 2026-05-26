package com.kwz.starter.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.kwz.starter.mybatis.properties.WzMybatisProperties;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 审计字段自动填充
 */
public class WzMetaObjectHandler implements MetaObjectHandler {

    private final ObjectProvider<AuditUserProvider> auditUserProvider;
    private final ZoneId auditZoneId;

    public WzMetaObjectHandler(ObjectProvider<AuditUserProvider> auditUserProvider,
                               WzMybatisProperties properties) {
        this.auditUserProvider = auditUserProvider;
        this.auditZoneId = ZoneId.of(properties.getAuditTimezone());
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = now();
        fillIfNull(metaObject, "createTime", now);
        fillIfNull(metaObject, "updateTime", now);
        fillUserId(metaObject, true);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        fillIfNull(metaObject, "updateTime", now());
        fillUserId(metaObject, false);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(auditZoneId);
    }

    private void fillUserId(MetaObject metaObject, boolean insert) {
        AuditUserProvider provider = auditUserProvider.getIfAvailable();
        if (provider == null) {
            return;
        }
        Long userId = provider.getCurrentUserId();
        if (userId == null) {
            return;
        }
        if (insert) {
            fillIfNull(metaObject, "createBy", userId);
        }
        fillIfNull(metaObject, "updateBy", userId);
    }

    private void fillIfNull(MetaObject metaObject, String field, Object value) {
        if (getFieldValByName(field, metaObject) == null) {
            setFieldValByName(field, value, metaObject);
        }
    }
}
