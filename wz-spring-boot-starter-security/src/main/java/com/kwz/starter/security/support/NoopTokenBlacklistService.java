package com.kwz.starter.security.support;

import com.kwz.starter.security.jwt.ParsedAccessToken;
import com.kwz.starter.security.spi.TokenBlacklistService;

/**
 * 默认空实现，不启用黑名单能力
 */
public class NoopTokenBlacklistService implements TokenBlacklistService {

    @Override
    public boolean isBlocked(ParsedAccessToken accessToken) {
        return false;
    }

    @Override
    public void blockAccessToken(ParsedAccessToken accessToken) {
        // no-op
    }

    @Override
    public void invalidateUser(Long userId) {
        // no-op
    }
}
