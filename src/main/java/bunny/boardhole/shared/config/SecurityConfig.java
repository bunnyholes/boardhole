package bunny.boardhole.shared.config;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import bunny.boardhole.shared.constants.ApiPaths;
import bunny.boardhole.shared.security.ProblemDetailsAccessDeniedHandler;
import bunny.boardhole.shared.security.ProblemDetailsAuthenticationEntryPoint;

/**
 * Spring Security 설정
 * 인증, 인가, 세션 관리 및 CORS 설정을 담당합니다.
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
     * 보안 필터 체인 설정
     *
     * @param http                      HTTP 보안 설정 객체
     * @param securityContextRepository 보안 컨텍스트 리포지토리
     * @return 설정된 보안 필터 체인
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository, ProblemDetailsAuthenticationEntryPoint authenticationEntryPoint, ProblemDetailsAccessDeniedHandler accessDeniedHandler) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults()).authorizeHttpRequests(auth -> auth
                // Static resources and common locations
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // 현재는 실제 배포 전까지는 항상 유지
                // Assets - allow all
                .requestMatchers("/assets/**").permitAll() // 현재는 실제 배포 전까지는 항상 유지
                // Other static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll() // 현재는 실제 배포 전까지는 항상 유지
                // Root and specific HTML files
                .requestMatchers("/").permitAll() // 현재는 실제 배포 전까지는 항상 유지
                .requestMatchers("/*.html").permitAll() // 현재는 실제 배포 전까지는 항상 유지
                .requestMatchers("/admin*.html", "/board*.html", "/user*.html").permitAll() // 현재는 실제 배포 전까지는 항상 유지
                .requestMatchers("/login.html", "/signup.html", "/welcome.html", "/my-page.html").permitAll() // 현재는 실제 배포 전까지는 항상 유지
                // Swagger UI - explicitly permit
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                // Error page
                .requestMatchers("/error").permitAll()
                // Public API endpoints - explicit permit only
                .requestMatchers(ApiPaths.AUTH + ApiPaths.AUTH_SIGNUP, ApiPaths.AUTH + ApiPaths.AUTH_LOGIN, ApiPaths.AUTH + ApiPaths.AUTH_PUBLIC_ACCESS).permitAll().requestMatchers(ApiPaths.AUTH + "/verify-email", ApiPaths.AUTH + "/resend-verification").permitAll().requestMatchers(HttpMethod.GET, ApiPaths.USERS + "/{id}/email/verify").permitAll().requestMatchers(HttpMethod.POST, ApiPaths.USERS + "/{id}/email/resend").permitAll().requestMatchers(HttpMethod.GET, ApiPaths.BOARDS, ApiPaths.BOARDS + "/**").permitAll()
                // All other requests require authentication by default
                .anyRequest().authenticated()).exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint).accessDeniedHandler(accessDeniedHandler)).formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable); // HTTP Basic 인증 비활성화

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).sessionFixation().migrateSession().maximumSessions(1).maxSessionsPreventsLogin(false)).securityContext((securityContext) -> securityContext.securityContextRepository(securityContextRepository));
        return http.build();
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
     * ProblemDetail 형식의 인증 실패 응답 처리기
     *
     * @param objectMapper  JSON 직렬화를 위한 ObjectMapper
     * @param messageSource 메시지 소스
     * @return 인증 실패 진입점 핸들러
     */
    @Bean
    public ProblemDetailsAuthenticationEntryPoint problemDetailsAuthenticationEntryPoint(ObjectMapper objectMapper, MessageSource messageSource) {
        return new ProblemDetailsAuthenticationEntryPoint(objectMapper, messageSource);
    }

    /**
     * ProblemDetail 형식의 접근 거부 응답 처리기
     *
     * @param objectMapper  JSON 직렬화를 위한 ObjectMapper
     * @param messageSource 메시지 소스
     * @return 접근 거부 핸들러
     */
    @Bean
    public ProblemDetailsAccessDeniedHandler problemDetailsAccessDeniedHandler(ObjectMapper objectMapper, MessageSource messageSource) {
        return new ProblemDetailsAccessDeniedHandler(objectMapper, messageSource);
    }
}
