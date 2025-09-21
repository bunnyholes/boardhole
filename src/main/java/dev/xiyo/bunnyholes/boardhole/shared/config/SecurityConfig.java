package dev.xiyo.bunnyholes.boardhole.shared.config;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.xiyo.bunnyholes.boardhole.auth.infrastructure.security.CustomAuthenticationSuccessHandler;
import dev.xiyo.bunnyholes.boardhole.shared.constants.ApiPaths;
import dev.xiyo.bunnyholes.boardhole.shared.security.ProblemDetailsAccessDeniedHandler;
import dev.xiyo.bunnyholes.boardhole.shared.security.ProblemDetailsAuthenticationEntryPoint;

/**
 * Spring Security 설정
 * REST API와 View Controller에 각각 최적화된 보안 설정을 제공합니다.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 비밀번호 인코더 빈 설정
     *
     * @return BCrypt 를 사용하는 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 매니저 빈 설정
     *
     * @param configuration Spring Security 인증 설정
     * @return 인증 매니저
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * REST API 전용 보안 필터 체인 (우선순위 높음)
     * - /api/** 경로만 처리
     * - formLogin 비활성화, 세션 기반 인증 사용
     * - 401 JSON 응답 반환
     * - RequestCache 비활성화로 불필요한 세션 생성 방지
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(
            HttpSecurity http,
            ProblemDetailsAuthenticationEntryPoint authenticationEntryPoint,
            ProblemDetailsAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .securityMatcher("/api/**")  // /api/** 경로만 이 필터체인 적용
                .csrf(AbstractHttpConfigurer::disable)  // REST API는 CSRF 비활성화
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Public API endpoints
                        .requestMatchers(ApiPaths.AUTH + ApiPaths.AUTH_SIGNUP,
                                ApiPaths.AUTH + ApiPaths.AUTH_LOGIN,
                                ApiPaths.AUTH + ApiPaths.AUTH_PUBLIC_ACCESS).permitAll()
                        .requestMatchers(HttpMethod.GET, ApiPaths.BOARDS, ApiPaths.BOARDS + "/**").permitAll()
                        // All other API requests require authentication
                        .anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)  // formLogin 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)  // HTTP Basic 비활성화
                .requestCache(cache -> cache
                        .requestCache(new NullRequestCache()))  // RequestCache 비활성화 (불필요한 세션 생성 방지)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))  // 세션 필요 시 생성
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)  // 401 JSON 응답
                        .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    /**
     * View Controller 전용 보안 필터 체인 (우선순위 낮음)
     * - 모든 나머지 경로 처리
     * - formLogin 활성화, Spring Security 표준 동작
     * - 로그인 페이지로 자동 리다이렉트
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository,
            LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint,
            CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler
    ) throws Exception {
        http
                .csrf(Customizer.withDefaults())  // CSRF 기본값 사용
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Static resources
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/assets/**", "/favicon.ico", "/css/**", "/js/**").permitAll()
                        // Public pages
                        .requestMatchers("/", "/auth/login", "/auth/signup", "/auth/logout/success", "/welcome").permitAll()
                        .requestMatchers("/boards", "/boards/*").permitAll()  // 게시글 목록/상세 공개
                        // Error pages
                        .requestMatchers("/error", "/error/**").permitAll()
                        // Swagger UI (개발용)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // Actuator
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Authentication required pages
                        .requestMatchers("/users", "/users/**").authenticated()
                        .requestMatchers("/mypage", "/mypage/**").authenticated()
                        .requestMatchers("/boards/write", "/boards/*/edit", "/boards/*/delete").authenticated()
                        // All other requests require authentication
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(customAuthenticationSuccessHandler)  // 커스텀 성공 핸들러 추가
                        .failureUrl("/auth/login?error")
                        .defaultSuccessUrl("/boards")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/logout/success")
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                // RequestCache 기본값 사용 (자동으로 원래 페이지로 리다이렉트)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(loginUrlAuthenticationEntryPoint));  // 로그인 페이지로 리다이렉트

        return http.build();
    }

    /**
     * View Controller용 로그인 페이지 리다이렉트 EntryPoint
     */
    @Bean
    public LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint("/auth/login");
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(PermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * ProblemDetail 형식의 인증 실패 응답 처리기 (API 전용)
     *
     * @param objectMapper      JSON 직렬화를 위한 ObjectMapper
     * @param problemProperties 문제 세부사항 설정
     * @return 인증 실패 진입점 핸들러
     */
    @Bean
    public ProblemDetailsAuthenticationEntryPoint problemDetailsAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new ProblemDetailsAuthenticationEntryPoint(objectMapper);
    }

    /**
     * ProblemDetail 형식의 접근 거부 응답 처리기 (API 전용)
     *
     * @param objectMapper      JSON 직렬화를 위한 ObjectMapper
     * @param problemProperties 문제 세부사항 설정
     * @return 접근 거부 핸들러
     */
    @Bean
    public ProblemDetailsAccessDeniedHandler problemDetailsAccessDeniedHandler(ObjectMapper objectMapper) {
        return new ProblemDetailsAccessDeniedHandler(objectMapper);
    }
}
