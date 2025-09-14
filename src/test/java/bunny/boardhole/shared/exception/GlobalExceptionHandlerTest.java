package bunny.boardhole.shared.exception;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
import bunny.boardhole.shared.properties.ProblemProperties;
import bunny.boardhole.shared.util.MessageUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 테스트")
@Tag("unit")
class GlobalExceptionHandlerTest {

    private static final String TRACE_ID = "test-trace-id-123";
    private static final String REQUEST_PATH = "/api/test";
    private static final String REQUEST_METHOD = "POST";

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        // Create handler with test ProblemProperties
        ProblemProperties problemProperties = new ProblemProperties("");
        handler = new GlobalExceptionHandler(problemProperties);

        LocaleContextHolder.setLocale(Locale.KOREAN);
        MDC.put(RequestLoggingFilter.TRACE_ID, TRACE_ID);

        // Initialize MessageUtils
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        ReflectionTestUtils.setField(MessageUtils.class, "messageSource", messageSource);
    }

    private void setupRequestMock() {
        when(request.getRequestURI()).thenReturn(REQUEST_PATH);
        when(request.getMethod()).thenReturn(REQUEST_METHOD);
    }

    @Test
    @DisplayName("ResourceNotFoundException 처리 - 404 응답")
    void handleNotFound() {
        // Given
        setupRequestMock();
        final String errorMessage = "게시글을 찾을 수 없습니다";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        // When
        ProblemDetail result = handler.handleNotFound(ex, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getTitle()).isEqualTo("리소스를 찾을 수 없음");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:not-found"));
        assertThat(result.getInstance()).isEqualTo(URI.create(REQUEST_PATH));
        assertThat(result.getProperties()).containsKeys("path", "method", "timestamp", "traceId");
        Map<String, Object> properties = result.getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties.get("traceId")).isEqualTo(TRACE_ID);
        assertThat(properties.get("path")).isEqualTo(REQUEST_PATH);
        assertThat(properties.get("method")).isEqualTo(REQUEST_METHOD);
    }

    @Test
    @DisplayName("ConflictException 처리 - 409 응답")
    void handleConflict() {
        // Given
        setupRequestMock();
        final String errorMessage = "중복된 데이터입니다";
        ConflictException ex = new ConflictException(errorMessage);

        // When
        ProblemDetail result = handler.handleConflict(ex, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("데이터 충돌");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:conflict"));
        Map<String, Object> conflictProperties = result.getProperties();
        assertThat(conflictProperties).isNotNull();
        assertThat(conflictProperties.get("code")).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("DuplicateUsernameException 처리 - 409 응답")
    void handleDuplicateUsername() {
        // Given
        setupRequestMock();
        final String errorMessage = "이미 사용 중인 사용자명입니다";
        DuplicateUsernameException ex = new DuplicateUsernameException(errorMessage);

        // When
        ProblemDetail result = handler.handleDuplicateUsername(ex, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("중복된 사용자명");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:duplicate-username"));
        Map<String, Object> usernameProperties = result.getProperties();
        assertThat(usernameProperties).isNotNull();
        assertThat(usernameProperties.get("code")).isEqualTo("USER_DUPLICATE_USERNAME");
    }

    @Test
    @DisplayName("DuplicateEmailException 처리 - 409 응답")
    void handleDuplicateEmail() {
        // Given
        setupRequestMock();
        final String errorMessage = "이미 사용 중인 이메일입니다";
        DuplicateEmailException ex = new DuplicateEmailException(errorMessage);

        // When
        ProblemDetail result = handler.handleDuplicateEmail(ex, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("중복된 이메일");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:duplicate-email"));
        Map<String, Object> emailProperties = result.getProperties();
        assertThat(emailProperties).isNotNull();
        assertThat(emailProperties.get("code")).isEqualTo("USER_DUPLICATE_EMAIL");
    }

    @Test
    @DisplayName("UnauthorizedException 처리 - 401 응답")
    void handleUnauthorized() {
        // Given
        setupRequestMock();
        final String errorMessage = "인증이 필요합니다";
        UnauthorizedException ex = new UnauthorizedException(errorMessage);

        // When
        ProblemDetail result = handler.handleUnauthorized(ex, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getTitle()).isEqualTo("인증 실패");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:unauthorized"));
        Map<String, Object> unauthorizedProperties = result.getProperties();
        assertThat(unauthorizedProperties).isNotNull();
        assertThat(unauthorizedProperties.get("code")).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("AccessDeniedException 처리 - 403 응답")
    void handleAccessDenied() {
        // Given
        setupRequestMock();
        final String errorMessage = "접근 권한이 없습니다";
        AccessDeniedException ex = new AccessDeniedException(errorMessage);

        // When
        ProblemDetail result = handler.handleAccessDenied(ex, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getTitle()).isEqualTo("접근 거부");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:forbidden"));
        Map<String, Object> forbiddenProperties = result.getProperties();
        assertThat(forbiddenProperties).isNotNull();
        assertThat(forbiddenProperties.get("code")).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 처리 - 422 응답 with validation errors")
    void handleMethodArgumentNotValid() {
        // Given
        setupRequestMock();
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("board", "title", "제목", false, null, null, "제목을 입력해주세요");
        FieldError fieldError2 = new FieldError("board", "content", "", false, null, null, "내용을 입력해주세요");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<ProblemDetail> response = handler.handleInvalid(ex, request);
        ProblemDetail result = response.getBody();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(result.getTitle()).isEqualTo("유효성 검증 실패");
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:validation-error"));
        Map<String, Object> validationProperties = result.getProperties();
        assertThat(validationProperties).isNotNull();
        assertThat(validationProperties.get("code")).isEqualTo("VALIDATION_ERROR");

        @SuppressWarnings("unchecked") List<Map<String, Object>> errors = (List<Map<String, Object>>) validationProperties.get("errors");
        assertThat(errors).hasSize(2);
        assertThat(errors.getFirst()).containsEntry("field", "title").containsEntry("message", "제목을 입력해주세요").containsEntry("rejectedValue", "제목");
        assertThat(errors.get(1)).containsEntry("field", "content").containsEntry("message", "내용을 입력해주세요").containsEntry("rejectedValue", "");
    }

    @Test
    @DisplayName("IllegalArgumentException 처리 - 422 응답")
    void handleIllegalArgument() {
        // Given
        setupRequestMock();
        final String errorMessage = "잘못된 인자입니다";
        IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

        // When
        ProblemDetail result = handler.handleBadRequest(ex, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(result.getTitle()).isEqualTo("유효성 검증 실패");
        assertThat(result.getDetail()).isEqualTo(errorMessage);
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:validation-error"));
        Map<String, Object> badRequestProperties = result.getProperties();
        assertThat(badRequestProperties).isNotNull();
        assertThat(badRequestProperties.get("code")).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("DataIntegrityViolationException 처리 - 409 응답")
    void handleDataIntegrityViolation() {
        // Given
        setupRequestMock();
        final String errorMessage = "데이터 무결성 위반";
        DataIntegrityViolationException ex = new DataIntegrityViolationException(errorMessage);

        // When
        ProblemDetail result = handler.handleDataIntegrityViolation(ex, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getTitle()).isEqualTo("데이터 충돌");
        assertThat(result.getDetail()).isEqualTo("데이터 충돌이 발생했습니다"); // 보안상 일반 메시지
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:conflict"));
        Map<String, Object> dataIntegrityProperties = result.getProperties();
        assertThat(dataIntegrityProperties).isNotNull();
        assertThat(dataIntegrityProperties.get("code")).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("Exception 처리 - 500 응답")
    void handleGeneralException() {
        // Given
        setupRequestMock();
        Exception ex = new RuntimeException("Unexpected error");

        // When
        ProblemDetail result = handler.handleUnexpected(ex, request);

        // Then
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("내부 서버 오류");
        assertThat(result.getDetail()).isEqualTo("서버 내부 오류가 발생했습니다");
        assertThat(result.getType()).isEqualTo(URI.create("urn:problem-type:internal-error"));
        Map<String, Object> internalErrorProperties = result.getProperties();
        assertThat(internalErrorProperties).isNotNull();
        assertThat(internalErrorProperties.get("code")).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    @DisplayName("problemBaseUri 설정 시 type URI 생성")
    void buildTypeWithBaseUri() {
        // Given
        setupRequestMock();
        ProblemProperties problemProperties = new ProblemProperties("https://api.boardhole.com/problems");
        GlobalExceptionHandler handlerWithBaseUri = new GlobalExceptionHandler(problemProperties);
        final String errorMessage = "테스트 오류";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        // When
        ProblemDetail result = handlerWithBaseUri.handleNotFound(ex, request);

        // Then
        assertThat(result.getType()).isEqualTo(URI.create("https://api.boardhole.com/problems/not-found"));
    }

    @Test
    @DisplayName("problemBaseUri가 슬래시로 끝날 때 처리")
    void buildTypeWithBaseUriEndingWithSlash() {
        // Given
        setupRequestMock();
        ProblemProperties problemProperties = new ProblemProperties("https://api.boardhole.com/problems/");
        GlobalExceptionHandler handlerWithBaseUri = new GlobalExceptionHandler(problemProperties);
        final String errorMessage = "테스트 오류";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        // When
        ProblemDetail result = handlerWithBaseUri.handleNotFound(ex, request);

        // Then
        assertThat(result.getType()).isEqualTo(URI.create("https://api.boardhole.com/problems/not-found"));
    }

    @Test
    @DisplayName("MDC에 traceId가 없을 때 처리")
    void handleWithoutTraceId() {
        // Given
        setupRequestMock();
        MDC.clear(); // Remove traceId
        final String errorMessage = "테스트 오류";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        // When
        ProblemDetail result = handler.handleNotFound(ex, request);

        // Then
        assertThat(result.getProperties()).doesNotContainKey("traceId");
        assertThat(result.getProperties()).containsKeys("path", "method", "timestamp");
    }

    @Test
    @DisplayName("HttpServletRequest가 null일 때 처리")
    void handleWithNullRequest() {
        // Given
        final String errorMessage = "테스트 오류";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        // When - null request for testing null handling behavior
        @SuppressWarnings("DataFlowIssue") ProblemDetail result = handler.handleNotFound(ex, null);

        // Then
        assertThat(result.getProperties()).containsKey("timestamp");
        assertThat(result.getProperties()).doesNotContainKeys("path", "method");
        // instance는 request가 null일 때 설정되지 않으므로 null이 아니라 기본값
        // ProblemDetail의 getInstance()는 기본적으로 null을 반환
        assertThat(result.getInstance()).isNull();
    }
}
