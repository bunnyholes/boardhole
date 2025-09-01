package bunny.boardhole.shared.exception;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Tag(name = "예외 처리", description = "전역 예외 처리 및 에러 응답 관리")
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    @Value("${boardhole.problem.base-uri:}")
    private String problemBaseUri;

    private static void addCommon(ProblemDetail pd, HttpServletRequest request) {
        String traceId = MDC.get(RequestLoggingFilter.TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            pd.setProperty("traceId", traceId);
        }
        if (request != null) {
            pd.setProperty("path", request.getRequestURI());
            pd.setProperty("method", request.getMethod());
            try {
                pd.setInstance(URI.create(request.getRequestURI()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        pd.setProperty("timestamp", Instant.now().toString());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.not-found", null, LocaleContextHolder.getLocale()));
        pd.setType(buildType("not-found"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ProblemDetail handleConflict(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.conflict", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "CONFLICT");
        pd.setType(buildType("conflict"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({DuplicateUsernameException.class})
    public ProblemDetail handleDuplicateUsername(DuplicateUsernameException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.duplicate-username", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "USER_DUPLICATE_USERNAME");
        pd.setType(buildType("duplicate-username"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({DuplicateEmailException.class})
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.duplicate-email", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "USER_DUPLICATE_EMAIL");
        pd.setType(buildType("duplicate-email"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        // 인증 실패는 401(Unauthorized)이 적합
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.unauthorized", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "UNAUTHORIZED");
        pd.setType(buildType("unauthorized"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.access-denied", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "FORBIDDEN");
        pd.setType(buildType("forbidden"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ProblemDetail handleInvalid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(messageSource.getMessage("exception.title.validation-failed", null, LocaleContextHolder.getLocale()));
        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage(),
                        "rejectedValue", fe.getRejectedValue()
                ))
                .collect(Collectors.toList());
        pd.setProperty("errors", errors);
        pd.setProperty("code", "VALIDATION_ERROR");
        pd.setType(buildType("validation-error"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBindException(BindException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(messageSource.getMessage("exception.title.validation-failed", null, LocaleContextHolder.getLocale()));
        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage(),
                        "rejectedValue", fe.getRejectedValue()
                ))
                .collect(Collectors.toList());
        pd.setProperty("errors", errors);
        pd.setProperty("code", "VALIDATION_ERROR");
        pd.setType(buildType("validation-error"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.bad-request", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "BAD_REQUEST");
        pd.setType(buildType("bad-request"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage("error.invalid-json", null, "잘못된 JSON 형식입니다.", LocaleContextHolder.getLocale()));
        pd.setTitle(messageSource.getMessage("exception.title.bad-request", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "INVALID_JSON");
        pd.setType(buildType("invalid-json"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED,
                String.format("지원하지 않는 HTTP 메서드입니다. 지원 메서드: %s", String.join(", ", ex.getSupportedMethods())));
        pd.setTitle(messageSource.getMessage("exception.title.method-not-allowed", null, "메서드 허용 안됨", LocaleContextHolder.getLocale()));
        pd.setProperty("code", "METHOD_NOT_ALLOWED");
        pd.setProperty("supportedMethods", ex.getSupportedMethods());
        pd.setType(buildType("method-not-allowed"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                messageSource.getMessage("error.unsupported-media-type", null, "지원하지 않는 미디어 타입입니다.", LocaleContextHolder.getLocale()));
        pd.setTitle(messageSource.getMessage("exception.title.unsupported-media-type", null, "미디어 타입 미지원", LocaleContextHolder.getLocale()));
        pd.setProperty("code", "UNSUPPORTED_MEDIA_TYPE");
        pd.setType(buildType("unsupported-media-type"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                String.format("필수 파라미터가 누락되었습니다: %s", ex.getParameterName()));
        pd.setTitle(messageSource.getMessage("exception.title.missing-parameter", null, "필수 파라미터 누락", LocaleContextHolder.getLocale()));
        pd.setProperty("code", "MISSING_PARAMETER");
        pd.setProperty("parameter", ex.getParameterName());
        pd.setProperty("parameterType", ex.getParameterType());
        pd.setType(buildType("missing-parameter"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                String.format("파라미터 타입이 올바르지 않습니다: %s", ex.getPropertyName()));
        pd.setTitle(messageSource.getMessage("exception.title.type-mismatch", null, "타입 불일치", LocaleContextHolder.getLocale()));
        pd.setProperty("code", "TYPE_MISMATCH");
        pd.setProperty("property", ex.getPropertyName());
        pd.setProperty("requiredType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : null);
        pd.setType(buildType("type-mismatch"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                messageSource.getMessage("error.internal", null, LocaleContextHolder.getLocale()));
        pd.setTitle(messageSource.getMessage("exception.title.internal-error", null, LocaleContextHolder.getLocale()));
        pd.setType(buildType("internal-error"));
        pd.setProperty("code", "INTERNAL_ERROR");
        addCommon(pd, request);
        return pd;
    }

    private URI buildType(String slug) {
        String base = problemBaseUri;
        if (base != null && !base.isBlank()) {
            if (!base.endsWith("/")) base = base + "/";
            try {
                return URI.create(base + slug);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return URI.create("urn:problem-type:" + slug);
    }
}
