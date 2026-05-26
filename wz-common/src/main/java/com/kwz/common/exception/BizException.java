package com.kwz.common.exception;

import lombok.Getter;

/**
 * 业务异常基类
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;
    private final String messageKey;
    private final Object[] args;

    public BizException(int code, String message) {
        this(code, message, (Object[]) null);
    }

    public BizException(int code, String message, Object... args) {
        super(message);
        this.code = code;
        this.messageKey = "wz.error." + code;
        this.args = args;
    }

    public BizException(ErrorCode errorCode) {
        this(errorCode, (Object[]) null);
    }

    public BizException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.messageKey = errorCode.getMessageKey();
        this.args = args;
    }
}
