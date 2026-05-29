package com.kwz.starter.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwz.common.exception.BizException;
import com.kwz.starter.security.exception.SecurityErrorCode;
import com.kwz.starter.security.jwt.JwtService;
import com.kwz.starter.security.jwt.ParsedAccessToken;
import com.kwz.starter.security.model.LoginUser;
import com.kwz.starter.security.properties.WzSecurityProperties;
import com.kwz.starter.security.spi.TokenBlacklistService;
import com.kwz.starter.security.support.SecurityFilterResponses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 从请求头解析 JWT 并写入 Spring Security 上下文
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final WzSecurityProperties properties;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   WzSecurityProperties properties,
                                   TokenBlacklistService tokenBlacklistService,
                                   ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.properties = properties;
        this.tokenBlacklistService = tokenBlacklistService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                ParsedAccessToken accessToken = jwtService.parseAccessTokenPayload(token);
                if (tokenBlacklistService.isBlocked(accessToken)) {
                    SecurityFilterResponses.writeUnauthorized(objectMapper, response, SecurityErrorCode.TOKEN_REVOKED);
                    return;
                }
                LoginUser loginUser = accessToken.getLoginUser();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (BizException ex) {
                SecurityFilterResponses.writeUnauthorized(objectMapper, response, ex);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String headerName = properties.getJwt().getHeader();
        String headerValue = request.getHeader(StringUtils.hasText(headerName) ? headerName : HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }
        String prefix = properties.getJwt().getPrefix();
        if (StringUtils.hasText(prefix) && headerValue.startsWith(prefix)) {
            return headerValue.substring(prefix.length()).trim();
        }
        return headerValue.trim();
    }
}
