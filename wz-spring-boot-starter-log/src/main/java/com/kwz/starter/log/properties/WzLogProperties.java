package com.kwz.starter.log.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志配置，前缀 wz.log.*
 */
@Data
@ConfigurationProperties(prefix = "wz.log")
public class WzLogProperties {

    private File file = new File();
    private Console console = new Console();
    private Pattern pattern = new Pattern();
    private Trace trace = new Trace();

    @Data
    public static class File {

        /** 是否启用文件滚动日志 */
        private boolean enabled = true;

        /** 日志目录 */
        private String path = "logs";

        /** 日志文件名（不含扩展名） */
        private String name = "app";

        /** 单文件最大大小，超出后同日内按序号分片 */
        private String maxFileSize = "10MB";

        /** 保留天数（按日期分文件） */
        private int maxHistory = 30;

        /** 所有归档日志总大小上限，超出后删除最旧文件 */
        private String totalSizeCap = "3GB";

        /** 归档文件是否 gzip 压缩 */
        private boolean compress = true;

        /** 启动时是否清理过期历史 */
        private boolean cleanHistoryOnStart = false;
    }

    @Data
    public static class Console {

        /** 是否输出到控制台 */
        private boolean enabled = true;
    }

    @Data
    public static class Pattern {

        /**
         * 控制台格式，留空使用内置美化格式。
         * 支持 Spring Boot %clr 彩色语法。
         */
        private String console;

        /** 文件格式，留空使用内置格式（无颜色） */
        private String file;
    }

    @Data
    public static class Trace {

        /** 是否启用 TLog 链路追踪（Spring Boot 3 使用 Jakarta Filter 适配） */
        private boolean enabled = true;

        /** 写入 MDC 的键名，对应日志格式 %X{traceId} */
        private String mdcKey = "traceId";

        /** TLog TraceId 生成器类名，留空使用 TLog 内置默认 */
        private String idGenerator;
    }
}
