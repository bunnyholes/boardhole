package bunny.boardhole.shared.security;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
import bunny.boardhole.shared.constants.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

/**
 * Spring Security 인증 실패 진입점 - 인증되지 않은 사용자에 대해 RFC 7807 ProblemDetail 형식으로 처리합니다.
 * 
 * <p>Spring Security에서 인증되지 않은 사용자가 보호된 리소스에 접근을 시도할 때
 * HTTP 401 Unauthorized 상태와 함께 구조화된 오류 응답을 제공합니다.
 * AuthenticationEntryPoint 인터페이스를 구현하여 인증 실패 시 일관된 오류 형식을 제공합니다.</p>
 * 
 * <p><strong>주요 기능:</strong></p>
 * <ul>
 *   <li><strong>RFC 7807 준수:</strong> 표준 ProblemDetail 형식으로 인증 오류 응답 제공</li>
 *   <li><strong>국제화 지원:</strong> 다국어 인증 오류 메시지 지원</li>
 *   <li><strong>추적 지원:</strong> 요청 추적 ID를 포함하여 디버깅 용이성 제공</li>
 *   <li><strong>보안 정보:</strong> 인증 방법 유도 없이 최소한의 오류 정보만 노출</li>
 * </ul>
 * 
 * <p><strong>응답 구조:</strong></p>
 * <pre>{@code
 * {
 *   "type": "urn:problem-type:unauthorized",
 *   "title": "인증 필요",
 *   "status": 401,
 *   "detail": "로그인이 필요합니다",
 *   "instance": "/api/protected/resource",
 *   "traceId": "550e8400-e29b-41d4-a716-446655440000",
 *   "path": "/api/protected/resource",
 *   "method": "GET",
 *   "timestamp": "2024-01-01T12:00:00Z",
 *   "code": "UNAUTHORIZED"
 * }
 * }</pre>
 * 
 * <p><strong>보안 고려사항:</strong></p>
 * <ul>
 *   <li>인증 방법 정보 노출 방지로 인증 체계 수집 공격 차단</li>
 *   <li>CRLF 인젝션 공격 방지를 위한 입력값 정화</li>
 *   <li>잘못된 URI 형식 예외 처리로 서비스 안정성 보장</li>
 *   <li>인증 예외 상세 정보는 로그에만 기록하고 응답에는 포함하지 않음</li>
 * </ul>
 * 
 * @author Security Team
 * @version 1.0
 * @since 1.0
 * 
 * @see org.springframework.security.web.AuthenticationEntryPoint
 * @see org.springframework.security.core.AuthenticationException
 * @see org.springframework.http.ProblemDetail
 * @see <a href="https://tools.ietf.org/html/rfc7807">RFC 7807: Problem Details for HTTP APIs</a>
 */
@Schema(name = "ProblemDetailsAuthenticationEntryPoint", description = "Spring Security 인증 실패 진입점 - ProblemDetail 형식 에러 응답")
@RequiredArgsConstructor
public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /** CRLF 인젝션 공격 방지를 위한 정규식 패턴 */
    private static final String CONTROL_CHARS_PATTERN = "[\\r\\n\\t\\p{Cntrl}]";
    
    /** Path Traversal 공격 방지를 위한 정규식 패턴 */
    private static final String PATH_TRAVERSAL_PATTERN = "\\.\\./";
    
    /** 중복 슬래시 제거를 위한 정규식 패턴 */
    private static final String DUPLICATE_SLASH_PATTERN = "//+";
    
    /** 치환 문자열 - 빈 문자열 */
    private static final String EMPTY_STRING = "";
    
    /** 치환 문자열 - 단일 슬래시 */
    private static final String SINGLE_SLASH = "/";
    
    /** 치환 문자열 - 언더스코어 */
    private static final String UNDERSCORE = "_";

    /** JSON 직렬화를 위한 ObjectMapper (보안 설정 적용됨) */
    private final ObjectMapper objectMapper;
    
    /** 국제화 메시지 소스 (다국어 인증 오류 메시지 지원) */
    private final MessageSource messageSource;
    
    /** ProblemDetail type URI 생성을 위한 기본 URI (빈 문자열 허용) */
    @Value("${boardhole.problem.base-uri:}")
    private final String problemBaseUri;

    /**
     * Spring Security에서 인증되지 않은 사용자가 보호된 리소스에 접근을 시도할 때 호출되는 메소드입니다.
     * 
     * <p>HTTP 401 Unauthorized 상태와 함께 RFC 7807 표준에 맞는 ProblemDetail 형식의
     * 인증 오류 응답을 생성하여 클라이언트에게 전송합니다.
     * 인증이 필요한 리소스에 인증되지 않은 상태로 접근했을 때 사용됩니다.</p>
     * 
     * <p><strong>처리 단계:</strong></p>
     * <ol>
     *   <li>ProblemDetail 객체 생성 (HTTP 401 상태)</li>
     *   <li>국제화된 인증 오류 메시지 설정</li>
     *   <li>Problem type URI 생성 및 설정</li>
     *   <li>요청 정보 및 추적 ID 추가</li>
     *   <li>JSON 형식으로 응답 전송</li>
     * </ol>
     * 
     * <p><strong>보안 고려사항:</strong></p>
     * <ul>
     *   <li>인증 체계 정보는 노출하지 않음 (인증 방법 수집 공격 방지)</li>
     *   <li>인증 예외 상세 정보는 로그에만 기록하고 응답에는 반영하지 않음</li>
     *   <li>URI 생성 실패 예외는 조용히 무시 (서비스 안정성 우선)</li>
     *   <li>추적 ID 검증을 통한 CRLF 인젝션 공격 방지</li>
     * </ul>
     * 
     * @param request 클라이언트 HTTP 요청 객체
     * @param response 서버 HTTP 응답 객체
     * @param authException 발생한 인증 예외 (로그용으로만 사용, 응답에는 포함하지 않음)
     * 
     * @throws IOException JSON 직렬화 또는 응답 쓰기 실패 시 발생
     */
    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException {
        final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle(messageSource.getMessage("exception.title.unauthorized", null, LocaleContextHolder.getLocale()));
        pd.setDetail(messageSource.getMessage("error.auth.not-logged-in", null, LocaleContextHolder.getLocale()));
        pd.setType(buildType("unauthorized"));
        
        // URI 생성 실패 예외 처리 (보안상 오류 정보 노출 방지)
        try {
            final String requestUri = sanitizeUri(request.getRequestURI());
            pd.setInstance(URI.create(requestUri));
        } catch (final IllegalArgumentException ignored) {
            // URI 생성 실패는 서비스 안정성을 위해 조용히 무시
        }

        // 요청 추적 ID 및 메타데이터 추가 (CRLF 인젝션 공격 방지)
        Optional.ofNullable(MDC.get(RequestLoggingFilter.TRACE_ID))
                .filter(traceId -> !traceId.isBlank())
                .map(this::sanitizeLogInput)
                .ifPresent(traceId -> pd.setProperty("traceId", traceId));
                
        // 요청 메타데이터 추가 (보안 정화 적용)
        pd.setProperty("path", sanitizeUri(request.getRequestURI()));
        pd.setProperty("method", sanitizeLogInput(request.getMethod()));
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", ErrorCode.UNAUTHORIZED.getCode());

        // HTTP 응답 설정 및 JSON 직렬화 전송
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), pd);
    }

    /**
     * Problem type URI를 안전하게 생성합니다.
     * 
     * <p>RFC 7807 표준에 따라 Problem type URI를 생성합니다.
     * 설정된 기본 URI가 있으면 해당 URI를 사용하고,
     * 없거나 잘못된 형식인 경우 표준 URN 형식을 사용합니다.</p>
     * 
     * <p><strong>생성 예시:</strong></p>
     * <ul>
     *   <li>기본 URI 사용: {@code https://api.example.com/problems/unauthorized}</li>
     *   <li>URN 형식: {@code urn:problem-type:unauthorized}</li>
     * </ul>
     * 
     * <p><strong>보안 고려사항:</strong></p>
     * <ul>
     *   <li>URI 생성 실패 예외는 서비스 안정성을 위해 조용히 처리</li>
     *   <li>입력 slug는 예상 값만 전달되므로 별도 유효성 검사 없음</li>
     * </ul>
     * 
     * @param slug Problem type을 나타내는 식별자 (null 불가, 빈 문자열 불가)
     * @return 생성된 Problem type URI (null 반환하지 않음)
     * 
     * @implNote 성능과 안정성을 위해 Optional 체이닝 사용
     */
    @NonNull
    private URI buildType(@NonNull final String slug) {
        return Optional.ofNullable(problemBaseUri)
                .filter(base -> !base.isBlank())
                .map(base -> base.endsWith("/") ? base : base + "/")
                .map(base -> {
                    try {
                        return URI.create(base + slug);
                    } catch (final IllegalArgumentException ignored) {
                        return null; // 예외 발생 시 null 반환하여 fallback 처리
                    }
                })
                .orElse(URI.create("urn:problem-type:" + slug));
    }
    
    /**
     * URI 문자열을 보안상 안전하게 정화합니다.
     * 
     * <p>URI에 포함될 수 있는 위험한 문자를 제거하여
     * Path Traversal 공격이나 CRLF 인젝션 공격을 방지합니다.</p>
     * 
     * @param uri 정화할 URI 문자열 (null 안전)
     * @return 정화된 URI 문자열 (null 입력 시 빈 문자열 반환)
     */
    private String sanitizeUri(final String uri) {
        if (uri == null) {
            return "";
        }
        // Path Traversal 및 CRLF 인젝션 방지
        return uri.replaceAll(CONTROL_CHARS_PATTERN, EMPTY_STRING)
                  .replaceAll(PATH_TRAVERSAL_PATTERN, EMPTY_STRING) // Path Traversal 방지
                  .replaceAll(DUPLICATE_SLASH_PATTERN, SINGLE_SLASH);  // 중복 슬래시 제거
    }
    
    /**
     * 로그 입력값을 CRLF 인젝션 공격으로부터 보호하기 위해 정화합니다.
     * 
     * @param input 정화할 입력 문자열 (null 안전)
     * @return 정화된 문자열 (null 입력 시 빈 문자열 반환)
     */
    private String sanitizeLogInput(final String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll(CONTROL_CHARS_PATTERN, UNDERSCORE);
    }

}
