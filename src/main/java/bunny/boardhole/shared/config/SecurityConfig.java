package bunny.boardhole.shared.config;

import bunny.boardhole.shared.constants.ApiPaths;
import bunny.boardhole.shared.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
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
@RequiredArgsConstructor
public class SecurityConfig {
    
    /** 정적 리소스 경로 상수 */
    /** Assets 리소스 경로 */
    private static final String ASSETS_PATH = "/assets/**";
    /** CSS 리소스 경로 */
    private static final String CSS_PATH = "/css/**";
    /** JavaScript 리소스 경로 */
    private static final String JS_PATH = "/js/**";
    /** 이미지 리소스 경로 */
    private static final String IMAGES_PATH = "/images/**";
    /** WebJars 리소스 경로 */
    private static final String WEBJARS_PATH = "/webjars/**";
    /** Favicon 경로 */
    private static final String FAVICON_PATH = "/favicon.ico";
    /** 루트 경로 */
    private static final String ROOT_PATH = "/";
    /** HTML 파일 패턴 */
    private static final String HTML_FILES_PATH = "/*.html";
    /** 관리자 HTML 패턴 */
    private static final String ADMIN_HTML_PATH = "/admin*.html";
    /** 게시판 HTML 패턴 */
    private static final String BOARD_HTML_PATH = "/board*.html";
    /** 사용자 HTML 패턴 */
    private static final String USER_HTML_PATH = "/user*.html";
    /** 로그인 페이지 경로 */
    private static final String LOGIN_HTML_PATH = "/login.html";
    /** 회원가입 페이지 경로 */
    private static final String SIGNUP_HTML_PATH = "/signup.html";
    /** 환영 페이지 경로 */
    private static final String WELCOME_HTML_PATH = "/welcome.html";
    /** 마이페이지 경로 */
    private static final String MY_PAGE_HTML_PATH = "/my-page.html";
    /** API 문서 경로 */
    private static final String API_DOCS_PATH = "/v3/api-docs/**";
    /** Swagger UI 경로 */
    private static final String SWAGGER_UI_PATH = "/swagger-ui/**";
    /** 에러 페이지 경로 */
    private static final String ERROR_PATH = "/error";
    /** 게시판 API 경로 */
    private static final String BOARDS_PATH = ApiPaths.BOARDS + "/**";

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
     * @param authConfig Spring Security 인증 설정
     * @return 인증 매니저
     */
    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Spring Security framework requirement
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authConfig) 
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 보안 필터 체인 설정
     * @param httpSecurity HTTP 보안 설정
     * @param contextRepo 보안 컨텍스트 리포지토리
     * @param authEntryPoint 인증 진입점
     * @param accessHandler 접근 거부 핸들러
     * @return 설정된 보안 필터 체인
     */
    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException") // Spring Security framework requirement
    public SecurityFilterChain filterChain(final HttpSecurity httpSecurity, final SecurityContextRepository contextRepo,
                                           final ProblemDetailsAuthenticationEntryPoint authEntryPoint,
                                           final ProblemDetailsAccessDeniedHandler accessHandler) 
            throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(requestAuthConfig -> requestAuthConfig
                        // Static resources and common locations
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // 배포 전까지 유지
                        // Assets - allow all
                        .requestMatchers(ASSETS_PATH).permitAll() // 배포 전까지 유지
                        // Other static resources
                        .requestMatchers(CSS_PATH, JS_PATH, IMAGES_PATH, WEBJARS_PATH, FAVICON_PATH).permitAll() // 배포 전까지 유지
                        // Root and specific HTML files
                        .requestMatchers(ROOT_PATH).permitAll() // 배포 전까지 유지
                        .requestMatchers(HTML_FILES_PATH).permitAll() // 배포 전까지 유지
                        .requestMatchers(ADMIN_HTML_PATH, BOARD_HTML_PATH, USER_HTML_PATH).permitAll() // 배포 전까지 유지
                        .requestMatchers(LOGIN_HTML_PATH, SIGNUP_HTML_PATH, WELCOME_HTML_PATH, MY_PAGE_HTML_PATH).permitAll() // 배포 전까지 유지
                        // Swagger UI - explicitly permit
                        .requestMatchers(API_DOCS_PATH, SWAGGER_UI_PATH).permitAll()
                        // Error page
                        .requestMatchers(ERROR_PATH).permitAll()
                        // Public API endpoints - explicit permit only
                        .requestMatchers(ApiPaths.AUTH + ApiPaths.AUTH_SIGNUP, ApiPaths.AUTH + ApiPaths.AUTH_LOGIN, ApiPaths.AUTH + ApiPaths.AUTH_PUBLIC_ACCESS).permitAll()
                        .requestMatchers(HttpMethod.GET, ApiPaths.BOARDS, BOARDS_PATH).permitAll()
                        // All other requests require authentication by default
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionConfig -> exceptionConfig
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessHandler)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable); // HTTP Basic 인증 비활성화

        httpSecurity.sessionManagement(sessionConfig -> sessionConfig
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .securityContext(contextConfig -> contextConfig
                        .securityContextRepository(contextRepo));
        return httpSecurity.build();
    }

    /**
     * 메소드 보안 표현식 핸들러 설정
     *
     * @param permissionEvaluator 권한 평가자
     * @return 메소드 보안 표현식 핸들러
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(final PermissionEvaluator permissionEvaluator) {
        final DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    /**
     * 보안 컨텍스트 리포지토리 설정
     *
     * @return HTTP 세션 기반 보안 컨텍스트 리포지토리
     */
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
    public ProblemDetailsAuthenticationEntryPoint problemDetailsAuthenticationEntryPoint(final ObjectMapper objectMapper, final MessageSource messageSource, @Value("${boardhole.problem.base-uri:}") final String problemBaseUri) {
        return new ProblemDetailsAuthenticationEntryPoint(objectMapper, messageSource, problemBaseUri);
    }

    /**
     * ProblemDetail 형식의 접근 거부 응답 처리기
     *
     * @param objectMapper  JSON 직렬화를 위한 ObjectMapper
     * @param messageSource 메시지 소스
     * @return 접근 거부 핸들러
     */
    @Bean
    public ProblemDetailsAccessDeniedHandler problemDetailsAccessDeniedHandler(final ObjectMapper objectMapper, final MessageSource messageSource, @Value("${boardhole.problem.base-uri:}") final String problemBaseUri) {
        return new ProblemDetailsAccessDeniedHandler(objectMapper, messageSource, problemBaseUri);
    }
}
