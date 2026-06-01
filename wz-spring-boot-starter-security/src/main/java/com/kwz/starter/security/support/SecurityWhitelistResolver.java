package com.kwz.starter.security.support;

import com.kwz.starter.security.annotation.PermitAll;
import com.kwz.starter.security.properties.WzSecurityProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 合并 YAML 白名单与 {@link PermitAll} 注解扫描结果
 */
public final class SecurityWhitelistResolver {

    private SecurityWhitelistResolver() {
    }

    public static String[] resolve(WzSecurityProperties properties,
                                   ApplicationContext applicationContext) {
        Set<String> paths = new LinkedHashSet<>(properties.getWhitelist());
        if (properties.isAnnotationWhitelistEnabled()) {
            RequestMappingHandlerMapping mapping = getMainRequestMappingHandlerMapping(applicationContext);
            if (mapping != null) {
                paths.addAll(collectAnnotatedPaths(mapping));
            }
        }
        return paths.toArray(String[]::new);
    }

    private static RequestMappingHandlerMapping getMainRequestMappingHandlerMapping(ApplicationContext context) {
        if (context.containsBean("requestMappingHandlerMapping")) {
            return context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        }
        Map<String, RequestMappingHandlerMapping> mappings = context.getBeansOfType(RequestMappingHandlerMapping.class);
        return mappings.getOrDefault("requestMappingHandlerMapping", mappings.values().stream().findFirst().orElse(null));
    }

    static Set<String> collectAnnotatedPaths(RequestMappingHandlerMapping mapping) {
        Set<String> paths = new LinkedHashSet<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mapping.getHandlerMethods().entrySet()) {
            if (!hasPermitAll(entry.getValue())) {
                continue;
            }
            appendPaths(paths, entry.getKey());
        }
        return paths;
    }

    private static boolean hasPermitAll(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(PermitAll.class)
                || handlerMethod.getBeanType().isAnnotationPresent(PermitAll.class);
    }

    private static void appendPaths(Set<String> paths, RequestMappingInfo info) {
        if (info.getPathPatternsCondition() != null) {
            for (PathPattern pattern : info.getPathPatternsCondition().getPatterns()) {
                paths.add(pattern.getPatternString());
            }
        }
        if (info.getPatternsCondition() != null) {
            paths.addAll(info.getPatternsCondition().getPatterns());
        }
    }
}
