package bunny.boardhole.common.config;

import bunny.boardhole.common.security.ProblemDetailsAccessDeniedHandler;
import bunny.boardhole.common.security.ProblemDetailsAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Spring Security 설정
 * 인증, 인가, 세션 관리 및 CORS 설정을 담당합니다.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    /**
     * 비밀번호 인코더 빈 설정
     * @return BCrypt 를 사용하는 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 매니저 빈 설정
     * @param configuration Spring Security 인증 설정
     * @return 인증 매니저
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 보안 필터 체인 설정
     * @param http HTTP 보안 설정 객체
     * @param securityContextRepository 보안 컨텍스트 리포지토리
     * @return 설정된 보안 필터 체인
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Static resources and common locations
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        // Explicitly allow common static paths
                        .requestMatchers("/public/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/", "/index.html", "/login.html", "/signup.html").permitAll()
                        // Swagger UI - explicitly permit
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        // Error page
                        .requestMatchers("/error").permitAll()
                        // Public auth endpoints (backup for @PermitAll)
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/public-access").permitAll()
                        // Public board endpoints (backup for @PermitAll)
                        .requestMatchers(HttpMethod.GET, "/api/boards", "/api/boards/**").permitAll()
                        // Public user endpoints (backup for @PermitAll)
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/{id:[0-9]+}").permitAll()
                        // All other requests require authentication by default
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(problemDetailsAuthenticationEntryPoint())
                        .accessDeniedHandler(problemDetailsAccessDeniedHandler())
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().newSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .securityContext((securityContext) -> securityContext
                        .securityContextRepository(securityContextRepository));
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

    @Bean
    public ProblemDetailsAuthenticationEntryPoint problemDetailsAuthenticationEntryPoint() {
        return new ProblemDetailsAuthenticationEntryPoint();
    }

    @Bean
    public ProblemDetailsAccessDeniedHandler problemDetailsAccessDeniedHandler() {
        return new ProblemDetailsAccessDeniedHandler();
    }
}
