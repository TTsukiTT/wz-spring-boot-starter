package com.kwz.starter.security.support;

import com.kwz.starter.security.annotation.PermitAll;
import com.kwz.starter.security.properties.WzSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        mapping.setApplicationContext(new AnnotationConfigApplicationContext(ClassLevelController.class, MethodLevelController.class));
        mapping.afterPropertiesSet();

        Set<String> paths = SecurityWhitelistResolver.collectAnnotatedPaths(mapping);

        assertThat(paths).contains("/api/public/info", "/api/auth/login");
        assertThat(paths).doesNotContain("/api/secure/data");
    }

    @Test
    void shouldResolveWhitelistWhenMultipleHandlerMappingsExist() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MultiMappingConfig.class);
        WzSecurityProperties properties = new WzSecurityProperties();
        properties.getWhitelist().add("/api/open/**");
        properties.setAnnotationWhitelistEnabled(true);

        String[] whitelist = SecurityWhitelistResolver.resolve(properties, context);

        assertThat(whitelist).contains("/api/open/**", "/api/public/info", "/api/auth/login");
        context.close();
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

    @Configuration
    static class MultiMappingConfig {

        @Bean(name = "requestMappingHandlerMapping")
        RequestMappingHandlerMapping requestMappingHandlerMapping() throws Exception {
            RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
            mapping.setApplicationContext(new AnnotationConfigApplicationContext(
                    ClassLevelController.class, MethodLevelController.class));
            mapping.afterPropertiesSet();
            return mapping;
        }

        @Bean(name = "controllerEndpointHandlerMapping")
        RequestMappingHandlerMapping controllerEndpointHandlerMapping() {
            return new RequestMappingHandlerMapping();
        }
    }
}
