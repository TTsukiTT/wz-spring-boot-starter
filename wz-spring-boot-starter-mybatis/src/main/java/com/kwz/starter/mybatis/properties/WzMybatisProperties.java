package com.kwz.starter.mybatis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyBatis 配置，前缀 wz.mybatis.*
 */
@Data
@ConfigurationProperties(prefix = "wz.mybatis")
public class WzMybatisProperties {

    /** 是否启用 MyBatis 模块增强 */
    private boolean enabled = true;

    /** 是否启用逻辑删除全局配置 */
    private boolean logicDelete = true;

    /** 逻辑删除字段名 */
    private String logicDeleteField = "deleted";

    /** 逻辑删除值 */
    private String logicDeletedValue = "1";

    /** 逻辑未删除值 */
    private String logicNotDeletedValue = "0";

    /** 是否启用审计字段自动填充 */
    private boolean audit = true;

    /** 审计时间字段使用的时区，默认东八区 */
    private String auditTimezone = "Asia/Shanghai";

    /** 分页单页最大条数，≤0 表示不限制 */
    private long paginationMaxLimit = 500L;
}
