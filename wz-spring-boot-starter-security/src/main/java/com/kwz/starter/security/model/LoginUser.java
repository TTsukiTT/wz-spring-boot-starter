package com.kwz.starter.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 登录用户信息，作为 {@link org.springframework.security.core.Authentication} 的 principal。
 * <p>
 * 仅供 Security 内部使用；对外 API 请返回 {@link UserInfo}。
 */
@Data
@Builder
public class LoginUser implements UserDetails {

    private Long userId;
    private String username;
    @Builder.Default
    private Set<String> roles = new LinkedHashSet<>();
    @Builder.Default
    private Set<String> permissions = new LinkedHashSet<>();

    /** 会话版本号，配合 {@link com.kwz.starter.security.spi.TokenBlacklistService#invalidateUser(Long)} 使用 */
    @JsonIgnore
    private Long tokenVersion;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Stream<SimpleGrantedAuthority> roleAuthorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
        Stream<SimpleGrantedAuthority> permissionAuthorities = permissions.stream()
                .map(SimpleGrantedAuthority::new);
        return Stream.concat(roleAuthorities, permissionAuthorities).collect(Collectors.toSet());
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return null;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    public Set<String> safeRoles() {
        return roles != null ? roles : Collections.emptySet();
    }

    public Set<String> safePermissions() {
        return permissions != null ? permissions : Collections.emptySet();
    }
}
