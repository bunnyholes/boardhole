package bunny.boardhole.shared.config;

import bunny.boardhole.shared.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.*;
import org.springframework.core.env.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.*;
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
import org.springframework.security.web.context.*;

/**
 * Spring Security 설정
 * 인증, 인가, 세션 관리 및 CORS 설정을 담당합니다.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private Environment environment;

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
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Static resources and common locations
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        // Assets - allow all
                        .requestMatchers("/assets/**").permitAll()
                        // Other static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        // Root and specific HTML files
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/*.html").permitAll()
                        .requestMatchers("/admin*.html", "/board*.html", "/user*.html").permitAll()
                        .requestMatchers("/login.html", "/signup.html", "/welcome.html", "/my-page.html").permitAll()
                        // Swagger UI - explicitly permit
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // Error page
                        .requestMatchers("/error").permitAll()
                        // Public API endpoints - explicit permit only
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/public-access").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/boards", "/api/boards/**").permitAll()
                        // All other requests require authentication by default
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(problemDetailsAuthenticationEntryPoint())
                        .accessDeniedHandler(problemDetailsAccessDeniedHandler())
                )
                .formLogin(AbstractHttpConfigurer::disable);

        // dev 프로파일일 때만 HTTP Basic 인증 활성화
        if (environment.acceptsProfiles(Profiles.of("dev"))) {
            http.httpBasic(Customizer.withDefaults());
        }

        http.sessionManagement(session -> session
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
