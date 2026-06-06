package com.kwz.starter.log.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.starter.log.properties.WzLogProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

final class WzHttpMessageFormatter {

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization",
            "cookie",
            "set-cookie",
            "proxy-authorization"
    );

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private WzHttpMessageFormatter() {
    }

    static String format(HttpServletRequest request,
                         HttpServletResponse response,
                         ContentCachingResponseWrapper cachingResponse,
                         boolean logRequest,
                         boolean logResponse,
                         WzLogProperties properties,
                         long durationMs) {
        WzLogProperties.Request requestProperties = properties.getRequest();

        StringBuilder message = new StringBuilder(512);
        message.append(System.lineSeparator())
                .append("======== HTTP Request ========").append(System.lineSeparator());

        appendRequestLine(message, request);
        if (requestProperties.isIncludeRequestHeaders()) {
            appendHeaders(message, request);
        }
        if (logRequest) {
            appendBody(message, "Request Body", resolveRequestBody(request), requestProperties.getMaxPayloadLength());
        }

        message.append("======== HTTP Response ========").append(System.lineSeparator());
        message.append("HTTP/1.1 ").append(response.getStatus())
                .append(' ').append(resolveReasonPhrase(response.getStatus())).append(System.lineSeparator());
        message.append("Duration: ").append(durationMs).append("ms").append(System.lineSeparator());
        appendHeaders(message, response);
        if (logResponse) {
            appendBody(message, "Response Body", resolveResponseBody(cachingResponse), requestProperties.getMaxPayloadLength());
        }
        message.append("======== End HTTP Message ========");
        return message.toString();
    }

    private static void appendRequestLine(StringBuilder message, HttpServletRequest request) {
        message.append(request.getMethod()).append(' ').append(request.getRequestURI());
        String queryString = WzRequestLogSupport.resolveQueryString(request);
        if (StringUtils.hasText(queryString)) {
            message.append('?').append(queryString);
        }
        message.append(" HTTP/1.1").append(System.lineSeparator());
    }

    private static void appendHeaders(StringBuilder message, HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            appendHeader(message, name, request.getHeaders(name));
        }
    }

    private static void appendHeaders(StringBuilder message, HttpServletResponse response) {
        for (String name : response.getHeaderNames()) {
            appendHeader(message, name, response.getHeaders(name));
        }
    }

    private static void appendHeader(StringBuilder message, String name, Enumeration<String> values) {
        while (values.hasMoreElements()) {
            appendHeader(message, name, values.nextElement());
        }
    }

    private static void appendHeader(StringBuilder message, String name, Collection<String> values) {
        for (String value : values) {
            appendHeader(message, name, value);
        }
    }

    private static void appendHeader(StringBuilder message, String name, String value) {
        if (SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
            value = "******";
        }
        message.append(name).append(": ").append(value).append(System.lineSeparator());
    }

    private static void appendBody(StringBuilder message, String title, String body, int maxPayloadLength) {
        message.append(System.lineSeparator()).append(title).append(':').append(System.lineSeparator());
        if (!StringUtils.hasText(body)) {
            message.append("<empty>").append(System.lineSeparator());
            return;
        }
        message.append(formatBody(body, maxPayloadLength)).append(System.lineSeparator());
    }

    static String formatBody(String body, int maxPayloadLength) {
        String normalized = normalizeJson(body);
        return WzRequestLogSupport.truncate(normalized, maxPayloadLength);
    }

    static String normalizeJson(String body) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(body);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception ignored) {
            return body;
        }
    }

    static boolean isLoggableContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return true;
        }
        String value = contentType.toLowerCase(Locale.ROOT);
        if (value.startsWith("multipart/") || value.contains("octet-stream")) {
            return false;
        }
        return value.contains("json")
                || value.contains("xml")
                || value.contains("text")
                || value.contains("form-urlencoded")
                || value.startsWith("application/");
    }

    private static String resolveRequestBody(HttpServletRequest request) {
        ContentCachingRequestWrapper cachingRequest = resolveCachingRequest(request);
        if (cachingRequest == null) {
            return null;
        }
        byte[] content = cachingRequest.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        if (!isLoggableContentType(cachingRequest.getContentType())) {
            return "<binary omitted>";
        }
        return new String(content, resolveCharset(cachingRequest.getContentType(), cachingRequest.getCharacterEncoding()));
    }

    private static String resolveResponseBody(ContentCachingResponseWrapper cachingResponse) {
        if (cachingResponse == null) {
            return null;
        }
        byte[] content = cachingResponse.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        if (!isLoggableContentType(cachingResponse.getContentType())) {
            return "<binary omitted>";
        }
        return new String(content, resolveCharset(cachingResponse.getContentType(), cachingResponse.getCharacterEncoding()));
    }

    private static ContentCachingRequestWrapper resolveCachingRequest(HttpServletRequest request) {
        Object cached = request.getAttribute(WzHttpMessageLogConstants.CACHING_REQUEST);
        if (cached instanceof ContentCachingRequestWrapper cachingRequest) {
            return cachingRequest;
        }
        ContentCachingRequestWrapper nativeRequest = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (nativeRequest != null) {
            return nativeRequest;
        }
        if (request instanceof ContentCachingRequestWrapper cachingRequest) {
            return cachingRequest;
        }
        return null;
    }

    static Charset resolveCharset(String contentType, String characterEncoding) {
        if (StringUtils.hasText(contentType)) {
            try {
                MediaType mediaType = MediaType.parseMediaType(contentType);
                Charset charset = mediaType.getCharset();
                if (charset != null) {
                    return charset;
                }
                if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)
                        || MediaType.APPLICATION_XML.isCompatibleWith(mediaType)
                        || MediaType.TEXT_PLAIN.isCompatibleWith(mediaType)) {
                    return StandardCharsets.UTF_8;
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        if (StringUtils.hasText(characterEncoding)
                && !StandardCharsets.ISO_8859_1.name().equalsIgnoreCase(characterEncoding)) {
            try {
                return Charset.forName(characterEncoding);
            } catch (Exception ignored) {
                // fall through
            }
        }
        return StandardCharsets.UTF_8;
    }

    private static String resolveReasonPhrase(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "";
        };
    }
}
