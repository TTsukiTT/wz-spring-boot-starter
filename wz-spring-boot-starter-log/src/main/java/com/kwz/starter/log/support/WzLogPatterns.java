package com.kwz.starter.log.support;

/**
 * 内置日志格式模板
 * <p>
 * 注意：不可使用 {@code ${LOG_LEVEL_PATTERN:-%5p}} 等 Spring 占位符写法，
 * 通过 EnvironmentPostProcessor 注入时 {@code %5p} 会导致解析失败并残留 {@code -}。
 */
public final class WzLogPatterns {

    /**
     * 级别着色：ERROR 红、WARN 黄、INFO 绿、DEBUG/TRACE 青
     */
    private static final String LEVEL_COLOR =
            "%clr(%-5level){ERROR=bright red,WARN=bright yellow,INFO=green,DEBUG=cyan,TRACE=faint}";

    private WzLogPatterns() {
    }

    /**
     * 控制台：时间 + 彩色级别 + 线程 + TraceId + 青色 Logger
     */
    public static final String CONSOLE =
            "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} "
                    + LEVEL_COLOR + " "
                    + "%clr([%15.15t]){faint} "
                    + "%clr([%X{traceId:-}]){yellow} "
                    + "%clr(%-36logger{36}){cyan} "
                    + "%clr(:){faint} %m%n%wEx";

    /**
     * 文件：纯文本，便于采集与检索（不含 ANSI 颜色）
     */
    public static final String FILE =
            "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] [%X{traceId:-}] %logger{50} : %msg%n%wEx";
}
