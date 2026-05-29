package com.kwz.starter.security.context;

import com.kwz.starter.security.model.LoginUser;
import com.kwz.starter.security.model.UserInfo;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 当前登录用户上下文工具类
 */
public final class SecurityContext {

    private SecurityContext() {
    }

    public static Optional<LoginUser> getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return Optional.of(loginUser);
        }
        return Optional.empty();
    }

    public static Long getCurrentUserId() {
        return getLoginUser().map(LoginUser::getUserId).orElse(null);
    }

    public static String getCurrentUsername() {
        return getLoginUser().map(LoginUser::getUsername).orElse(null);
    }

    public static UserInfo getUserInfo() {
        return UserInfo.from(getLoginUser().orElse(null));
    }

    public static boolean hasPermission(String permission) {
        return getLoginUser().map(user -> user.safePermissions().contains(permission)).orElse(false);
    }

    public static boolean hasRole(String role) {
        return getLoginUser().map(user -> user.safeRoles().contains(role)).orElse(false);
    }
}
