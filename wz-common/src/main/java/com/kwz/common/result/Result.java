package com.kwz.common.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kwz.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 统一 API 响应体
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;
    private long timestamp;

    /** i18n 内部元数据，不参与 JSON 序列化 */
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final I18nMeta i18nMeta = new I18nMeta();

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static <T> Result<T> ok(T data, String messageKey, Object... messageArgs) {
        Result<T> result = ok(data);
        result.setMessageKey(messageKey);
        result.setMessageArgs(messageArgs);
        return result;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 返回原始 message，不做 i18n 解析（如参数校验明细）
     */
    public static <T> Result<T> failRaw(int code, String message) {
        Result<T> result = fail(code, message);
        result.setResolveI18n(false);
        return result;
    }

    public static <T> Result<T> fail(ErrorCode errorCode, Object... messageArgs) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        result.setMessageKey(errorCode.getMessageKey());
        result.setMessageArgs(messageArgs);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    public static <T> Result<T> fail(int code, String messageKey, String defaultMessage, Object... messageArgs) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(defaultMessage);
        result.setMessageKey(messageKey);
        result.setMessageArgs(messageArgs);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    @JsonIgnore
    public String getMessageKey() {
        return i18nMeta.messageKey;
    }

    public void setMessageKey(String messageKey) {
        i18nMeta.messageKey = messageKey;
    }

    @JsonIgnore
    public Object[] getMessageArgs() {
        return i18nMeta.messageArgs;
    }

    public void setMessageArgs(Object[] messageArgs) {
        i18nMeta.messageArgs = messageArgs;
    }

    @JsonIgnore
    public boolean isResolveI18n() {
        return i18nMeta.resolveI18n;
    }

    public void setResolveI18n(boolean resolveI18n) {
        i18nMeta.resolveI18n = resolveI18n;
    }

    private static final class I18nMeta implements Serializable {
        private String messageKey;
        private Object[] messageArgs;
        private boolean resolveI18n = true;
    }
}
