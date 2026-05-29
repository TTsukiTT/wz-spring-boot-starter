package com.kwz.starter.security.aspect;

import com.kwz.common.exception.BizException;
import com.kwz.starter.security.annotation.RequirePermission;
import com.kwz.starter.security.annotation.RequireRole;
import com.kwz.starter.security.context.SecurityContext;
import com.kwz.starter.security.exception.SecurityErrorCode;
import com.kwz.starter.security.model.LoginUser;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

import java.util.Arrays;

/**
 * {@link RequirePermission} / {@link RequireRole} 注解切面
 */
@Aspect
@Order(0)
public class AuthorizationAspect {

    @Before("@annotation(requirePermission)")
    public void checkMethodPermission(RequirePermission requirePermission) {
        checkPermissions(requirePermission.value());
    }

    @Before("@within(requirePermission) && !@annotation(com.kwz.starter.security.annotation.RequirePermission)")
    public void checkClassPermission(RequirePermission requirePermission) {
        checkPermissions(requirePermission.value());
    }

    @Before("@annotation(requireRole)")
    public void checkMethodRole(RequireRole requireRole) {
        checkRoles(requireRole.value());
    }

    @Before("@within(requireRole) && !@annotation(com.kwz.starter.security.annotation.RequireRole)")
    public void checkClassRole(RequireRole requireRole) {
        checkRoles(requireRole.value());
    }

    private void checkPermissions(String[] permissions) {
        LoginUser loginUser = SecurityContext.getLoginUser()
                .orElseThrow(() -> new BizException(SecurityErrorCode.TOKEN_MISSING));
        boolean matched = Arrays.stream(permissions).allMatch(loginUser.safePermissions()::contains);
        if (!matched) {
            throw new BizException(SecurityErrorCode.PERMISSION_DENIED);
        }
    }

    private void checkRoles(String[] roles) {
        LoginUser loginUser = SecurityContext.getLoginUser()
                .orElseThrow(() -> new BizException(SecurityErrorCode.TOKEN_MISSING));
        boolean matched = Arrays.stream(roles).allMatch(loginUser.safeRoles()::contains);
        if (!matched) {
            throw new BizException(SecurityErrorCode.ROLE_DENIED);
        }
    }
}
