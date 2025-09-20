package dev.xiyo.bunnyholes.boardhole.shared.config;

import java.io.Serializable;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class ViewSecurityConfig {

    /**
     * 테스트용 PermissionEvaluator 빈 설정
     * 
     * 기본값으로 모든 권한 요청에 대해 false를 반환합니다.
     * 각 테스트 클래스에서 @MockitoBean으로 이 빈을 재정의하여 
     * 테스트 시나리오에 맞는 권한 로직을 구현해야 합니다.
     * 
     * 예시:
     * {@code @MockitoBean private PermissionEvaluator permissionEvaluator;}
     * 
     * {@code when(permissionEvaluator.hasPermission(any(), eq(BOARD_ID), eq("BOARD"), eq("WRITE")))
     *     .thenReturn(true);}
     */
    @Bean
    public PermissionEvaluator permissionEvaluator() {
        return new PermissionEvaluator() {
            @Override
            public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
                // 기본값: false - 각 테스트에서 @MockitoBean으로 재정의 필요
                return false;
            }

            @Override
            public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
                // 기본값: false - 각 테스트에서 @MockitoBean으로 재정의 필요
                return false;
            }
        };
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(PermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(403, "Access Denied");
                        })
                )
                .build();
    }
}