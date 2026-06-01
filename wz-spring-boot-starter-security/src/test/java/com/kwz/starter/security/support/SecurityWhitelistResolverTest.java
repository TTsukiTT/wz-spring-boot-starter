package com.kwz.starter.security.support;

import com.kwz.starter.security.annotation.PermitAll;
import com.kwz.starter.security.properties.WzSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityWhitelistResolverTest {

    @Test
    void shouldCollectClassAndMethodPermitAllPaths() throws Exception {
        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                ClassLevelController.class, MethodLevelController.class);
        mapping.setApplicationContext(context);
        mapping.afterPropertiesSet();

        Set<String> paths = SecurityWhitelistResolver.collectAnnotatedPaths(mapping);

        assertThat(paths).contains("/api/public/info", "/api/auth/login");
        assertThat(paths).doesNotContain("/api/secure/data");
        context.close();
    }

    @Test
    void shouldResolveWhitelistWhenMultipleHandlerMappingsExist() throws Exception {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>();
        handlerMethods.put(
                RequestMappingInfo.paths("/api/public/info").build(),
                new HandlerMethod(new ClassLevelController(), ClassLevelController.class.getDeclaredMethod("info"))
        );
        handlerMethods.put(
                RequestMappingInfo.paths("/api/auth/login").build(),
                new HandlerMethod(new MethodLevelController(), MethodLevelController.class.getDeclaredMethod("login"))
        );
        handlerMethods.put(
                RequestMappingInfo.paths("/api/secure/data").build(),
                new HandlerMethod(new MethodLevelController(), MethodLevelController.class.getDeclaredMethod("secure"))
        );

        RequestMappingHandlerMapping mainMapping = new StubRequestMappingHandlerMapping(handlerMethods);
        RequestMappingHandlerMapping endpointMapping = new StubRequestMappingHandlerMapping(Map.of());
        StaticApplicationContext context = new StaticApplicationContext();
        context.getBeanFactory().registerSingleton("requestMappingHandlerMapping", mainMapping);
        context.getBeanFactory().registerSingleton("controllerEndpointHandlerMapping", endpointMapping);
        context.refresh();

        WzSecurityProperties properties = new WzSecurityProperties();
        properties.getWhitelist().add("/api/open/**");
        properties.setAnnotationWhitelistEnabled(true);

        String[] whitelist = SecurityWhitelistResolver.resolve(properties, context);

        assertThat(whitelist).contains("/api/open/**", "/api/public/info", "/api/auth/login");
        assertThat(whitelist).doesNotContain("/api/secure/data");
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

    static class StubRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

        private final Map<RequestMappingInfo, HandlerMethod> handlerMethods;

        StubRequestMappingHandlerMapping(Map<RequestMappingInfo, HandlerMethod> handlerMethods) {
            this.handlerMethods = handlerMethods;
        }

        @Override
        public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
            return handlerMethods;
        }
    }

}
