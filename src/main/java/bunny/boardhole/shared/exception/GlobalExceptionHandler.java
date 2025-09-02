package bunny.boardhole.shared.exception;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
import bunny.boardhole.shared.constants.ErrorCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리를 담당하는 핸들러 클래스입니다.
 * 애플리케이션에서 발생하는 모든 예외를 처리하고 RFC 7807 Problem Details 형식으로 응답합니다.
 * 로깅, 추적 ID, 다국어 메시지 지원을 포함한 포괄적인 예외 처리를 제공합니다.
 */
@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Tag(name = "예외 처리", description = "전역 예외 처리 및 에러 응답 관리")
@SuppressWarnings("PMD.TooManyMethods") // 각 예외 타입별로 핸들러가 필요함
public class GlobalExceptionHandler {

    /** 다국어 메시지 처리를 위한 메시지 소스 */
    private final MessageSource messageSource;
    
    /** Problem Details URI 구성을 위한 기본 URI */
    @Value("${boardhole.problem.base-uri:}")
    private String problemBaseUri;
    
    // 상수 정의 - 중복 문자열 제거
    /** ProblemDetail 속성명: 추적 ID */
    private static final String PROPERTY_TRACE_ID = "traceId";
    /** ProblemDetail 속성명: 요청 경로 */
    private static final String PROPERTY_PATH = "path";
    /** ProblemDetail 속성명: HTTP 메소드 */
    private static final String PROPERTY_METHOD = "method";
    /** ProblemDetail 속성명: 타임스탬프 */
    private static final String PROPERTY_TIMESTAMP = "timestamp";
    /** ProblemDetail 속성명: 오류 코드 */
    private static final String PROPERTY_CODE = "code";
    /** ProblemDetail 속성명: 오류 목록 */
    private static final String PROPERTY_ERRORS = "errors";
    /** ProblemDetail 속성명: 파라미터 */
    private static final String PROPERTY_PARAMETER = "parameter";
    /** ProblemDetail 속성명: 파라미터 타입 */
    private static final String PROPERTY_PARAMETER_TYPE = "parameterType";
    /** ProblemDetail 속성명: 속성 */
    private static final String PROPERTY_PROPERTY = "property";
    /** ProblemDetail 속성명: 필수 타입 */
    private static final String PROPERTY_REQUIRED_TYPE = "requiredType";
    /** ProblemDetail 속성명: 지원되는 메소드 */
    private static final String PROPERTY_SUPPORTED_METHODS = "supportedMethods";
    
    /** 메시지 키: Not Found 제목 */
    private static final String MESSAGE_KEY_TITLE_NOT_FOUND = "exception.title.not-found";
    /** 메시지 키: Conflict 제목 */
    private static final String MESSAGE_KEY_TITLE_CONFLICT = "exception.title.conflict";
    /** 메시지 키: 중복 사용자명 제목 */
    private static final String MESSAGE_KEY_TITLE_DUPLICATE_USERNAME = "exception.title.duplicate-username";
    /** 메시지 키: 중복 이메일 제목 */
    private static final String MESSAGE_KEY_TITLE_DUPLICATE_EMAIL = "exception.title.duplicate-email";
    /** 메시지 키: 인증 실패 제목 */
    private static final String MESSAGE_KEY_TITLE_UNAUTHORIZED = "exception.title.unauthorized";
    /** 메시지 키: 접근 거부 제목 */
    private static final String MESSAGE_KEY_TITLE_ACCESS_DENIED = "exception.title.access-denied";
    /** 메시지 키: 유효성 검증 실패 제목 */
    private static final String MESSAGE_KEY_TITLE_VALIDATION_FAILED = "exception.title.validation-failed";
    /** 메시지 키: 잘못된 요청 제목 */
    private static final String MESSAGE_KEY_TITLE_BAD_REQUEST = "exception.title.bad-request";
    /** 메시지 키: 지원되지 않는 메소드 제목 */
    private static final String MESSAGE_KEY_TITLE_METHOD_NOT_ALLOWED = "exception.title.method-not-allowed";
    /** 메시지 키: 지원되지 않는 미디어 타입 제목 */
    private static final String MESSAGE_KEY_TITLE_UNSUPPORTED_MEDIA_TYPE = "exception.title.unsupported-media-type";
    /** 메시지 키: 누락된 파라미터 제목 */
    private static final String MESSAGE_KEY_TITLE_MISSING_PARAMETER = "exception.title.missing-parameter";
    /** 메시지 키: 타입 불일치 제목 */
    private static final String MESSAGE_KEY_TITLE_TYPE_MISMATCH = "exception.title.type-mismatch";
    /** 메시지 키: 내부 오류 제목 */
    private static final String MESSAGE_KEY_TITLE_INTERNAL_ERROR = "exception.title.internal-error";
    
    /** 메시지 키: 유효성 검증 실패 오류 */
    private static final String MESSAGE_KEY_ERROR_VALIDATION_FAILED = "error.validation-failed";
    /** 메시지 키: 잘못된 JSON 오류 */
    private static final String MESSAGE_KEY_ERROR_INVALID_JSON = "error.invalid-json";
    /** 메시지 키: 메소드 허용 안됨 오류 */
    private static final String MESSAGE_KEY_ERROR_METHOD_NOT_ALLOWED = "error.method-not-allowed.detail";
    /** 메시지 키: 지원되지 않는 미디어 타입 오류 */
    private static final String MESSAGE_KEY_ERROR_UNSUPPORTED_MEDIA_TYPE = "error.unsupported-media-type";
    /** 메시지 키: 누락된 파라미터 오류 */
    private static final String MESSAGE_KEY_ERROR_MISSING_PARAMETER = "error.missing-parameter.detail";
    /** 메시지 키: 타입 불일치 오류 */
    private static final String MESSAGE_KEY_ERROR_TYPE_MISMATCH = "error.type-mismatch.detail";
    /** 메시지 키: 내부 오류 */
    private static final String MESSAGE_KEY_ERROR_INTERNAL = "error.internal";
    
    /** Problem type slug: 리소스를 찾을 수 없음 */
    private static final String TYPE_SLUG_NOT_FOUND = "not-found";
    /** Problem type slug: 충돌 */
    private static final String TYPE_SLUG_CONFLICT = "conflict";
    /** Problem type slug: 중복 사용자명 */
    private static final String TYPE_SLUG_DUPLICATE_USERNAME = "duplicate-username";
    /** Problem type slug: 중복 이메일 */
    private static final String TYPE_SLUG_DUPLICATE_EMAIL = "duplicate-email";
    /** Problem type slug: 인증 실패 */
    private static final String TYPE_SLUG_UNAUTHORIZED = "unauthorized";
    /** Problem type slug: 접근 금지 */
    private static final String TYPE_SLUG_FORBIDDEN = "forbidden";
    /** Problem type slug: 유효성 검증 오류 */
    private static final String TYPE_SLUG_VALIDATION_ERROR = "validation-error";
    /** Problem type slug: 잘못된 요청 */
    private static final String TYPE_SLUG_BAD_REQUEST = "bad-request";
    /** Problem type slug: 잘못된 JSON */
    private static final String TYPE_SLUG_INVALID_JSON = "invalid-json";
    /** Problem type slug: 메소드 허용 안됨 */
    private static final String TYPE_SLUG_METHOD_NOT_ALLOWED = "method-not-allowed";
    /** Problem type slug: 지원되지 않는 미디어 타입 */
    private static final String TYPE_SLUG_UNSUPPORTED_MEDIA_TYPE = "unsupported-media-type";
    /** Problem type slug: 누락된 파라미터 */
    private static final String TYPE_SLUG_MISSING_PARAMETER = "missing-parameter";
    /** Problem type slug: 타입 불일치 */
    private static final String TYPE_SLUG_TYPE_MISMATCH = "type-mismatch";
    /** Problem type slug: 내부 오류 */
    private static final String TYPE_SLUG_INTERNAL_ERROR = "internal-error";
    
    /** Problem type URN 접두사 */
    private static final String URN_PROBLEM_TYPE_PREFIX = "urn:problem-type:";
    /** 기본 잘못된 값 메시지 */
    private static final String DEFAULT_INVALID_VALUE = "Invalid value";

    /**
     * ProblemDetail에 공통 속성을 추가하는 정적 메소드입니다.
     * 추적 ID, 요청 경로, HTTP 메소드, 타임스탬프 등을 설정합니다.
     * 
     * @param problemDetail 속성을 추가할 ProblemDetail 객체 (null이 아님)
     * @param request HTTP 요청 객체 (null 허용)
     */
    private static void addCommon(@NonNull final ProblemDetail problemDetail, @Nullable final HttpServletRequest request) {
        // Optional을 사용한 null 체크 제거
        Optional.ofNullable(MDC.get(RequestLoggingFilter.TRACE_ID))
                .filter(traceId -> !traceId.isBlank())
                .ifPresent(traceId -> problemDetail.setProperty(PROPERTY_TRACE_ID, sanitizeForLog(traceId)));

        Optional.ofNullable(request)
                .ifPresent(req -> {
                    problemDetail.setProperty(PROPERTY_PATH, sanitizeForLog(req.getRequestURI()));
                    problemDetail.setProperty(PROPERTY_METHOD, sanitizeForLog(req.getMethod()));
                    try {
                        problemDetail.setInstance(URI.create(req.getRequestURI()));
                    } catch (final IllegalArgumentException ignored) {
                        // URI 생성 실패 시 무시
                    }
                });

        problemDetail.setProperty(PROPERTY_TIMESTAMP, Instant.now().toString());
    }
    
    /**
     * 로그 인젝션 공격을 방지하기 위해 문자열을 안전하게 처리하는 메소드입니다.
     * 
     * <p>CRLF 문자와 제어 문자를 제거하여 로그 위조를 방지하고,
     * 길이 제한을 통해 DoS 공격을 방지합니다. 단일 출구점 원칙을 준수합니다.</p>
     * 
     * @param input 처리할 입력 문자열 (null 허용)
     * @return 안전하게 처리된 문자열 (null 입력 시 null 반환)
     */
    private static String sanitizeForLog(final String input) {
        final String result;
        
        if (input == null) {
            result = null;
        } else {
            // CRLF 및 제어 문자 제거
            final String sanitized = input.replaceAll("[\\r\\n\\t\\p{Cntrl}]", "_");
            
            // 길이 제한 (DoS 방지)
            if (sanitized.length() > 200) {
                result = sanitized.substring(0, 197) + "...";
            } else {
                result = sanitized;
            }
        }
        
        return result;
    }

    /**
     * 리소스를 찾을 수 없는 경우의 예외를 처리합니다.
     * 
     * @param exception 발생한 ResourceNotFoundException
     * @param request HTTP 요청 객체
     * @return 404 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(final ResourceNotFoundException exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_NOT_FOUND, null, LocaleContextHolder.getLocale()));
        problemDetail.setType(buildType(TYPE_SLUG_NOT_FOUND));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 충돌 관련 예외를 처리합니다.
     * ConflictException과 DataIntegrityViolationException을 처리합니다.
     * 
     * @param exception 발생한 예외
     * @param request HTTP 요청 객체
     * @return 409 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ProblemDetail handleConflict(final Exception exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_CONFLICT, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.CONFLICT.getCode());
        problemDetail.setType(buildType(TYPE_SLUG_CONFLICT));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 중복된 사용자명 예외를 처리합니다.
     * 
     * @param exception 발생한 DuplicateUsernameException
     * @param request HTTP 요청 객체
     * @return 409 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler({DuplicateUsernameException.class})
    public ProblemDetail handleDuplicateUsername(final DuplicateUsernameException exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_DUPLICATE_USERNAME, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.USER_DUPLICATE_USERNAME.getCode());
        problemDetail.setType(buildType(TYPE_SLUG_DUPLICATE_USERNAME));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 중복된 이메일 예외를 처리합니다.
     * 
     * @param exception 발생한 DuplicateEmailException
     * @param request HTTP 요청 객체
     * @return 409 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler({DuplicateEmailException.class})
    public ProblemDetail handleDuplicateEmail(final DuplicateEmailException exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_DUPLICATE_EMAIL, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.USER_DUPLICATE_EMAIL.getCode());
        problemDetail.setType(buildType(TYPE_SLUG_DUPLICATE_EMAIL));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 인증 실패 예외를 처리합니다.
     * 
     * @param exception 발생한 UnauthorizedException
     * @param request HTTP 요청 객체
     * @return 401 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(final UnauthorizedException exception, final HttpServletRequest request) {
        // 인증 실패는 401(Unauthorized)이 적합
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_UNAUTHORIZED, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.UNAUTHORIZED.getCode());
        problemDetail.setType(buildType(TYPE_SLUG_UNAUTHORIZED));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 접근 거부 예외를 처리합니다.
     * 
     * @param exception 발생한 AccessDeniedException
     * @param request HTTP 요청 객체
     * @return 403 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(final AccessDeniedException exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_ACCESS_DENIED, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.FORBIDDEN.getCode());
        problemDetail.setType(buildType(TYPE_SLUG_FORBIDDEN));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 메소드 인자 유효성 검증 실패 예외를 처리합니다.
     * 
     * @param exception 발생한 MethodArgumentNotValidException
     * @param request HTTP 요청 객체
     * @return 400 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleInvalid(final MethodArgumentNotValidException exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_VALIDATION_FAILED, null, LocaleContextHolder.getLocale()));
        problemDetail.setDetail(messageSource.getMessage(MESSAGE_KEY_ERROR_VALIDATION_FAILED, null, LocaleContextHolder.getLocale()));
        final List<Map<String, Object>> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> Map.of(
                        "field", fieldError.getField(),
                        "message", Optional.ofNullable(fieldError.getDefaultMessage()).orElse(DEFAULT_INVALID_VALUE),
                        "rejectedValue", Optional.ofNullable(fieldError.getRejectedValue()).orElse("")
                ))
                .collect(Collectors.toList());
        problemDetail.setProperty(PROPERTY_ERRORS, errors);
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.VALIDATION_ERROR.getCode());
        problemDetail.setType(buildType(TYPE_SLUG_VALIDATION_ERROR));
        addCommon(problemDetail, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * 바인딩 예외를 처리합니다.
     * 
     * @param exception 발생한 BindException
     * @param request HTTP 요청 객체
     * @return 400 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleBindException(final BindException exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_VALIDATION_FAILED, null, LocaleContextHolder.getLocale()));
        problemDetail.setDetail(messageSource.getMessage(MESSAGE_KEY_ERROR_VALIDATION_FAILED, null, LocaleContextHolder.getLocale()));
        final List<Map<String, Object>> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> Map.of(
                        "field", fieldError.getField(),
                        "message", Optional.ofNullable(fieldError.getDefaultMessage()).orElse(DEFAULT_INVALID_VALUE),
                        "rejectedValue", Optional.ofNullable(fieldError.getRejectedValue()).orElse("")
                ))
                .collect(Collectors.toList());
        problemDetail.setProperty(PROPERTY_ERRORS, errors);
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.VALIDATION_ERROR.getCode());
        problemDetail.setType(buildType(TYPE_SLUG_VALIDATION_ERROR));
        addCommon(problemDetail, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    /**
     * 잘못된 요청 예외를 처리합니다.
     * ConstraintViolationException과 IllegalArgumentException을 처리합니다.
     * 
     * @param exception 발생한 예외
     * @param request HTTP 요청 객체
     * @return 400 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(final Exception exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_BAD_REQUEST, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.BAD_REQUEST.getCode());
        problemDetail.setType(buildType(TYPE_SLUG_BAD_REQUEST));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * HTTP 메시지 읽기 불가능 예외를 처리합니다.
     * 주로 JSON 파싱 오류 시 발생합니다.
     * 
     * @param exception 발생한 HttpMessageNotReadableException
     * @param request HTTP 요청 객체
     * @return 400 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(final HttpMessageNotReadableException exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage(MESSAGE_KEY_ERROR_INVALID_JSON, null, LocaleContextHolder.getLocale()));
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_BAD_REQUEST, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.INVALID_JSON.getCode());
        problemDetail.setType(buildType(TYPE_SLUG_INVALID_JSON));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 지원되지 않는 HTTP 메소드 예외를 처리합니다.
     * 
     * @param exception 발생한 HttpRequestMethodNotSupportedException
     * @param request HTTP 요청 객체
     * @return 405 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(final HttpRequestMethodNotSupportedException exception, final HttpServletRequest request) {
        final String supportedMethods = String.join(", ", exception.getSupportedMethods());
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED,
                messageSource.getMessage(MESSAGE_KEY_ERROR_METHOD_NOT_ALLOWED,
                        new Object[]{supportedMethods}, LocaleContextHolder.getLocale()));
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_METHOD_NOT_ALLOWED, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.METHOD_NOT_ALLOWED.getCode());
        problemDetail.setProperty(PROPERTY_SUPPORTED_METHODS, exception.getSupportedMethods());
        problemDetail.setType(buildType(TYPE_SLUG_METHOD_NOT_ALLOWED));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 지원되지 않는 미디어 타입 예외를 처리합니다.
     * 
     * @param exception 발생한 HttpMediaTypeNotSupportedException
     * @param request HTTP 요청 객체
     * @return 415 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(final HttpMediaTypeNotSupportedException exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                messageSource.getMessage(MESSAGE_KEY_ERROR_UNSUPPORTED_MEDIA_TYPE, null, LocaleContextHolder.getLocale()));
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_UNSUPPORTED_MEDIA_TYPE, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode());
        problemDetail.setType(buildType(TYPE_SLUG_UNSUPPORTED_MEDIA_TYPE));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 필수 요청 파라미터 누락 예외를 처리합니다.
     * 
     * @param exception 발생한 MissingServletRequestParameterException
     * @param request HTTP 요청 객체
     * @return 400 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(final MissingServletRequestParameterException exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage(MESSAGE_KEY_ERROR_MISSING_PARAMETER,
                        new Object[]{exception.getParameterName()}, LocaleContextHolder.getLocale()));
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_MISSING_PARAMETER, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.MISSING_PARAMETER.getCode());
        problemDetail.setProperty(PROPERTY_PARAMETER, exception.getParameterName());
        problemDetail.setProperty(PROPERTY_PARAMETER_TYPE, exception.getParameterType());
        problemDetail.setType(buildType(TYPE_SLUG_MISSING_PARAMETER));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 타입 불일치 예외를 처리합니다.
     * 
     * @param exception 발생한 TypeMismatchException
     * @param request HTTP 요청 객체
     * @return 400 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(final TypeMismatchException exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage(MESSAGE_KEY_ERROR_TYPE_MISMATCH,
                        new Object[]{exception.getPropertyName()}, LocaleContextHolder.getLocale()));
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_TYPE_MISMATCH, null, LocaleContextHolder.getLocale()));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.TYPE_MISMATCH.getCode());
        problemDetail.setProperty(PROPERTY_PROPERTY, exception.getPropertyName());
        Optional.ofNullable(exception.getRequiredType())
                .map(Class::getSimpleName)
                .ifPresent(type -> problemDetail.setProperty(PROPERTY_REQUIRED_TYPE, type));
        problemDetail.setType(buildType(TYPE_SLUG_TYPE_MISMATCH));
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * 예상치 못한 예외를 처리합니다.
     * 모든 다른 예외 핸들러에서 처리되지 않은 예외를 최종적으로 처리합니다.
     * 
     * @param exception 발생한 예외
     * @param request HTTP 요청 객체
     * @return 500 상태코드와 함께 문제 상세 정보
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(final Exception exception, final HttpServletRequest request) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                messageSource.getMessage(MESSAGE_KEY_ERROR_INTERNAL, null, LocaleContextHolder.getLocale()));
        problemDetail.setTitle(messageSource.getMessage(MESSAGE_KEY_TITLE_INTERNAL_ERROR, null, LocaleContextHolder.getLocale()));
        problemDetail.setType(buildType(TYPE_SLUG_INTERNAL_ERROR));
        problemDetail.setProperty(PROPERTY_CODE, ErrorCode.INTERNAL_ERROR.getCode());
        addCommon(problemDetail, request);
        return problemDetail;
    }

    /**
     * Problem Details의 type URI를 구성하는 메소드입니다.
     * 
     * <p>기본 URI가 설정된 경우 이를 사용하고, 그렇지 않으면 URN 형식을 사용합니다.
     * 단일 출구점 원칙을 준수하며, URI 생성 실패 시 안전하게 기본값으로 처리됩니다.</p>
     * 
     * @param slug Problem type의 식별자 문자열 (null이 아님)
     * @return 구성된 type URI (항상 유효한 URI 반환 보장)
     * @throws IllegalArgumentException slug가 null이거나 빈 문자열인 경우
     */
    @NonNull
    private URI buildType(@NonNull final String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Problem type slug는 null이거나 빈 문자열일 수 없습니다.");
        }
        
        final URI result = Optional.ofNullable(problemBaseUri)
                .filter(base -> !base.isBlank())
                .map(base -> base.endsWith("/") ? base : base + "/")
                .map(base -> {
                    try {
                        return URI.create(base + slug);
                    } catch (final IllegalArgumentException ignored) {
                        return null;
                    }
                })
                .orElse(URI.create(URN_PROBLEM_TYPE_PREFIX + slug));
                
        return result;
    }
}
