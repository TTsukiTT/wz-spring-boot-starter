package com.kwz.starter.log.request;

import com.kwz.starter.log.annotation.LogHttpMessage;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 启动时扫描 {@link LogHttpMessage} 标注的接口路径，供 Filter 决定是否缓存报文。
 */
public class WzPayloadLoggingPathRegistry {

    private final List<MappingEntry> entries = new ArrayList<>();
    private volatile boolean initialized;

    private static final String REQUEST_MAPPING_HANDLER_MAPPING = "requestMappingHandlerMapping";

    public void initializeIfNecessary(ApplicationContext applicationContext) {
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (initialized) {
                return;
            }
            if (!applicationContext.containsBean(REQUEST_MAPPING_HANDLER_MAPPING)) {
                return;
            }
            RequestMappingHandlerMapping mapping = applicationContext.getBean(
                    REQUEST_MAPPING_HANDLER_MAPPING, RequestMappingHandlerMapping.class);
            mapping.getHandlerMethods().forEach(this::registerHandler);
            initialized = true;
        }
    }

    public boolean matches(String httpMethod, String path) {
        if (entries.isEmpty()) {
            return false;
        }
        for (MappingEntry entry : entries) {
            if (entry.matches(httpMethod, path)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnnotatedEndpoints() {
        return !entries.isEmpty();
    }

    private void registerHandler(RequestMappingInfo info, HandlerMethod handlerMethod) {
        if (resolveAnnotation(handlerMethod) == null) {
            return;
        }
        if (info.getPathPatternsCondition() == null) {
            return;
        }
        for (PathPattern pattern : info.getPathPatternsCondition().getPatterns()) {
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            if (methods.isEmpty()) {
                for (RequestMethod method : RequestMethod.values()) {
                    entries.add(new MappingEntry(method.name(), pattern));
                }
            } else {
                for (RequestMethod method : methods) {
                    entries.add(new MappingEntry(method.name(), pattern));
                }
            }
        }
    }

    static LogHttpMessage resolveAnnotation(HandlerMethod handlerMethod) {
        LogHttpMessage annotation = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), LogHttpMessage.class);
        if (annotation != null) {
            return annotation;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), LogHttpMessage.class);
    }

    private record MappingEntry(String httpMethod, PathPattern pattern) {

        boolean matches(String httpMethod, String path) {
            if (!this.httpMethod.equals(httpMethod)) {
                return false;
            }
            return pattern.matches(org.springframework.http.server.PathContainer.parsePath(path));
        }
    }
}
