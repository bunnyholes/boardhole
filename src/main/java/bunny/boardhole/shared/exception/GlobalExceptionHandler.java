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

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Tag(name = "예외 처리", description = "전역 예외 처리 및 에러 응답 관리")
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    @Value("${boardhole.problem.base-uri:}")
    private String problemBaseUri;

    private static void addCommon(@NonNull ProblemDetail pd, @Nullable HttpServletRequest request) {
        // Optional을 사용한 null 체크 제거
        Optional.ofNullable(MDC.get(RequestLoggingFilter.TRACE_ID))
                .filter(traceId -> !traceId.isBlank())
                .ifPresent(traceId -> pd.setProperty("traceId", traceId));

        Optional.ofNullable(request)
                .ifPresent(req -> {
                    pd.setProperty("path", req.getRequestURI());
                    pd.setProperty("method", req.getMethod());
                    try {
                        pd.setInstance(URI.create(req.getRequestURI()));
                    } catch (IllegalArgumentException ignored) {
                    }
                });

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
        pd.setProperty("code", ErrorCode.CONFLICT.getCode());
        pd.setType(buildType("conflict"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ProblemDetail handleDuplicateUsername(DuplicateUsernameException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.duplicate-username", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", ErrorCode.USER_DUPLICATE_USERNAME.getCode());
        pd.setType(buildType("duplicate-username"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.duplicate-email", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", ErrorCode.USER_DUPLICATE_EMAIL.getCode());
        pd.setType(buildType("duplicate-email"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        // 인증 실패는 401(Unauthorized)이 적합
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.unauthorized", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", ErrorCode.UNAUTHORIZED.getCode());
        pd.setType(buildType("unauthorized"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.access-denied", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", ErrorCode.FORBIDDEN.getCode());
        pd.setType(buildType("forbidden"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleInvalid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(messageSource.getMessage("exception.title.validation-failed", null, LocaleContextHolder.getLocale()));
        pd.setDetail(messageSource.getMessage("error.validation-failed", null, LocaleContextHolder.getLocale()));
        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"),
                        "rejectedValue", Optional.ofNullable(fe.getRejectedValue()).orElse("")
                ))
                .collect(Collectors.toList());
        pd.setProperty("errors", errors);
        pd.setProperty("code", ErrorCode.VALIDATION_ERROR.getCode());
        pd.setType(buildType("validation-error"));
        addCommon(pd, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleBindException(BindException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(messageSource.getMessage("exception.title.validation-failed", null, LocaleContextHolder.getLocale()));
        pd.setDetail(messageSource.getMessage("error.validation-failed", null, LocaleContextHolder.getLocale()));
        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"),
                        "rejectedValue", Optional.ofNullable(fe.getRejectedValue()).orElse("")
                ))
                .collect(Collectors.toList());
        pd.setProperty("errors", errors);
        pd.setProperty("code", ErrorCode.VALIDATION_ERROR.getCode());
        pd.setType(buildType("validation-error"));
        addCommon(pd, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.bad-request", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", ErrorCode.BAD_REQUEST.getCode());
        pd.setType(buildType("bad-request"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage("error.invalid-json", null, LocaleContextHolder.getLocale()));
        pd.setTitle(messageSource.getMessage("exception.title.bad-request", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", ErrorCode.INVALID_JSON.getCode());
        pd.setType(buildType("invalid-json"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String[] methods = ex.getSupportedMethods();
        String supportedMethods = methods != null ? String.join(", ", methods) : "None";
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED,
                messageSource.getMessage("error.method-not-allowed.detail",
                        new Object[]{supportedMethods}, LocaleContextHolder.getLocale()));
        pd.setTitle(messageSource.getMessage("exception.title.method-not-allowed", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", ErrorCode.METHOD_NOT_ALLOWED.getCode());
        pd.setProperty("supportedMethods", ex.getSupportedMethods());
        pd.setType(buildType("method-not-allowed"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                messageSource.getMessage("error.unsupported-media-type", null, LocaleContextHolder.getLocale()));
        pd.setTitle(messageSource.getMessage("exception.title.unsupported-media-type", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode());
        pd.setType(buildType("unsupported-media-type"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage("error.missing-parameter.detail",
                        new Object[]{ex.getParameterName()}, LocaleContextHolder.getLocale()));
        pd.setTitle(messageSource.getMessage("exception.title.missing-parameter", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", ErrorCode.MISSING_PARAMETER.getCode());
        pd.setProperty("parameter", ex.getParameterName());
        pd.setProperty("parameterType", ex.getParameterType());
        pd.setType(buildType("missing-parameter"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage("error.type-mismatch.detail",
                        new Object[]{ex.getPropertyName()}, LocaleContextHolder.getLocale()));
        pd.setTitle(messageSource.getMessage("exception.title.type-mismatch", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", ErrorCode.TYPE_MISMATCH.getCode());
        pd.setProperty("property", ex.getPropertyName());
        Optional.ofNullable(ex.getRequiredType())
                .map(Class::getSimpleName)
                .ifPresent(type -> pd.setProperty("requiredType", type));
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
        pd.setProperty("code", ErrorCode.INTERNAL_ERROR.getCode());
        addCommon(pd, request);
        return pd;
    }

    @NonNull
    private URI buildType(@NonNull String slug) {
        return Optional.ofNullable(problemBaseUri)
                .filter(base -> !base.isBlank())
                .map(base -> base.endsWith("/") ? base : base + "/")
                .map(base -> {
                    try {
                        return URI.create(base + slug);
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                })
                .orElse(URI.create("urn:problem-type:" + slug));
    }
}
