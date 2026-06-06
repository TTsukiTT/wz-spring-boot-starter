package com.kwz.starter.log.request;

import com.kwz.starter.log.annotation.LogHttpMessage;

final class WzHttpMessageLogConstants {

    static final String LOG_HTTP_MESSAGE = "wz.log.httpMessage";
    static final String CACHING_REQUEST = "wz.log.cachingRequest";
    static final String CACHING_RESPONSE = "wz.log.cachingResponse";

    private WzHttpMessageLogConstants() {
    }
}
