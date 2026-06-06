package com.kwz.starter.log.request;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

final class WzRequestLogSupport {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private WzRequestLogSupport() {
    }

    static boolean isExcluded(String path, List<String> excludePatterns) {
        for (String pattern : excludePatterns) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    static String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    static String resolveQueryString(HttpServletRequest request) {
        if (StringUtils.hasText(request.getQueryString())) {
            return request.getQueryString();
        }
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.isEmpty()) {
            return null;
        }
        StringBuilder query = new StringBuilder();
        parameterMap.forEach((name, values) -> {
            for (String value : values) {
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(name).append('=').append(value);
            }
        });
        return query.toString();
    }

    static String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
