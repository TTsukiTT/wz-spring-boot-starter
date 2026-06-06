package com.kwz.starter.log.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

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
    private Request request = new Request();

    @Data
    public static class File {

        /** 是否启用文件滚动日志 */
        private boolean enabled = true;

        /** 日志目录 */
        private String path = "./logs";

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

    @Data
    public static class Request {

        /** 是否记录 HTTP 请求访问日志 */
        private boolean enabled = true;

        /** 是否在 URI 后附加 query string */
        private boolean includeQueryString = true;

        /** 是否记录客户端 IP（支持 X-Forwarded-For / X-Real-IP） */
        private boolean includeClientInfo = true;

        /** 是否打印 User-Agent */
        private boolean includeUserAgent = true;

        /** HTTP 报文日志是否打印请求头 */
        private boolean includeRequestHeaders = true;

        /** 慢请求阈值（毫秒），超过则 WARN；0 表示不按耗时升级级别 */
        private long slowThresholdMs = 1000;

        /** 排除路径（Ant 风格），默认跳过监控与错误页 */
        private List<String> excludePatterns = List.of(
                "/actuator/**",
                "/error",
                "/favicon.ico"
        );

        /** 全局打印 HTTP 请求/响应报文（标准报文格式） */
        private boolean logPayload = false;

        /** 报文最大长度，超出后截断 */
        private int maxPayloadLength = 2048;
    }
}
