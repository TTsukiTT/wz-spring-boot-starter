package com.kwz.starter.security.spi;

import com.kwz.starter.security.jwt.ParsedAccessToken;

/**
 * Token 黑名单 SPI，业务项目可注册 Bean 覆盖默认空实现（如基于 Redis）。
 * <p>
 * 典型场景：
 * <ul>
 *   <li>{@link #blockAccessToken(ParsedAccessToken)} — 登出当前设备</li>
 *   <li>{@link #invalidateUser(Long)} — 改密 / 强制下线全部设备（配合 JWT {@code ver}  claim）</li>
 * </ul>
 */
public interface TokenBlacklistService {

    /**
     * 当前 Access Token 是否已失效
     */
    boolean isBlocked(ParsedAccessToken accessToken);

    /**
     * 拉黑单个 Access Token，TTL 建议使用 {@link ParsedAccessToken#remainingTtl()}
     */
    void blockAccessToken(ParsedAccessToken accessToken);

    /**
     * 使用户所有 Token 失效（业务实现通常递增 Redis 中的会话版本号）
     */
    void invalidateUser(Long userId);
}
