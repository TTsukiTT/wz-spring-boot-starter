package com.kwz.starter.security.support;

import com.kwz.starter.security.annotation.PermitAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityWhitelistResolverTest {

    @Test
    void shouldCollectClassAndMethodPermitAllPaths() throws Exception {
        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
        mapping.setApplicationContext(new org.springframework.context.annotation.AnnotationConfigApplicationContext(
                ClassLevelController.class, MethodLevelController.class));
        mapping.afterPropertiesSet();

        Set<String> paths = SecurityWhitelistResolver.collectAnnotatedPaths(mapping);

        assertThat(paths).contains("/api/public/info", "/api/auth/login");
        assertThat(paths).doesNotContain("/api/secure/data");
    }

    @PermitAll
    @RestController
    @RequestMapping("/api/public")
    static class ClassLevelController {

        @GetMapping("/info")
        String info() {
            return "ok";
        }
    }

    @RestController
    @RequestMapping("/api")
    static class MethodLevelController {

        @PermitAll
        @PostMapping("/auth/login")
        void login() {
        }

        @GetMapping("/secure/data")
        void secure() {
        }
    }
}
