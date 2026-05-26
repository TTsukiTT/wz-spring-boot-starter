package com.kwz.starter.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 多语言配置，前缀 wz.i18n.*
 */
@Data
@ConfigurationProperties(prefix = "wz.i18n")
public class WzI18nProperties {

    /** 是否启用多语言 */
    private boolean enabled = true;

    /** 消息资源文件 basename 列表 */
    private List<String> basenames = new ArrayList<>(List.of("i18n/messages"));

    /** 默认语言 */
    private Locale defaultLocale = Locale.SIMPLIFIED_CHINESE;

    /** 资源文件编码 */
    private String encoding = "UTF-8";

    /** URL 参数名，用于切换语言（如 ?lang=en），设为空则禁用 */
    private String paramName = "lang";

    /** 找不到 key 时是否直接返回 key 本身 */
    private boolean useCodeAsDefaultMessage = false;
}
