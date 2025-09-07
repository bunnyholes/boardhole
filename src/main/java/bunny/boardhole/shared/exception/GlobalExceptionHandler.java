package bunny.boardhole.shared.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import lombok.RequiredArgsConstructor;

import org.slf4j.MDC;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
import bunny.boardhole.shared.constants.ErrorCode;
import bunny.boardhole.shared.util.MessageUtils;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Tag(name = "예외 처리", description = "전역 예외 처리 및 에러 응답 관리")
public class GlobalExceptionHandler {

    @Value("${boardhole.problem.base-uri:}")
    private String problemBaseUri;

    private static void addCommon(ProblemDetail pd, @Nullable HttpServletRequest request) {
        // Optional을 사용한 null 체크 제거
        Optional.ofNullable(MDC.get(RequestLoggingFilter.TRACE_ID)).filter(traceId -> !traceId.isBlank()).ifPresent(traceId -> pd.setProperty("traceId", traceId));

        Optional.ofNullable(request).ifPresent(req -> {
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
        pd.setTitle(MessageUtils.get("exception.title.not-found"));
        pd.setType(buildType("not-found"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ProblemDetail handleConflict(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.conflict"));
        pd.setProperty("code", ErrorCode.CONFLICT.getCode());
        pd.setType(buildType("conflict"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ProblemDetail handleDuplicateUsername(DuplicateUsernameException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.duplicate-username"));
        pd.setProperty("code", ErrorCode.USER_DUPLICATE_USERNAME.getCode());
        pd.setType(buildType("duplicate-username"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.duplicate-email"));
        pd.setProperty("code", ErrorCode.USER_DUPLICATE_EMAIL.getCode());
        pd.setType(buildType("duplicate-email"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        // 인증 실패는 401(Unauthorized)이 적합
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.unauthorized"));
        pd.setProperty("code", ErrorCode.UNAUTHORIZED.getCode());
        pd.setType(buildType("unauthorized"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(EmailVerificationRequiredException.class)
    public ProblemDetail handleEmailVerificationRequired(EmailVerificationRequiredException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.email-verification-required"));
        pd.setProperty("code", "EMAIL_VERIFICATION_REQUIRED");
        pd.setProperty("resendUrl", "/api/auth/resend-verification");
        pd.setType(buildType("email-verification-required"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.access-denied"));
        pd.setProperty("code", ErrorCode.FORBIDDEN.getCode());
        pd.setType(buildType("forbidden"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleInvalid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return handleValidationException(ex.getBindingResult(), request);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleBindException(BindException ex, HttpServletRequest request) {
        return handleValidationException(ex.getBindingResult(), request);
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.bad-request"));
        pd.setProperty("code", ErrorCode.BAD_REQUEST.getCode());
        pd.setType(buildType("bad-request"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, MessageUtils.get("error.invalid-json"));
        pd.setTitle(MessageUtils.get("exception.title.bad-request"));
        pd.setProperty("code", ErrorCode.INVALID_JSON.getCode());
        pd.setType(buildType("invalid-json"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String[] methods = ex.getSupportedMethods();
        String supportedMethods = methods != null ? String.join(", ", methods) : "None";
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, MessageUtils.get("error.method-not-allowed.detail", supportedMethods));
        pd.setTitle(MessageUtils.get("exception.title.method-not-allowed"));
        pd.setProperty("code", ErrorCode.METHOD_NOT_ALLOWED.getCode());
        pd.setProperty("supportedMethods", ex.getSupportedMethods());
        pd.setType(buildType("method-not-allowed"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, MessageUtils.get("error.unsupported-media-type"));
        pd.setTitle(MessageUtils.get("exception.title.unsupported-media-type"));
        pd.setProperty("code", ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode());
        pd.setType(buildType("unsupported-media-type"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, MessageUtils.get("error.missing-parameter.detail", ex.getParameterName()));
        pd.setTitle(MessageUtils.get("exception.title.missing-parameter"));
        pd.setProperty("code", ErrorCode.MISSING_PARAMETER.getCode());
        pd.setProperty("parameter", ex.getParameterName());
        pd.setProperty("parameterType", ex.getParameterType());
        pd.setType(buildType("missing-parameter"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request) {
        String propertyName = ex.getPropertyName() != null ? ex.getPropertyName() : "unknown";
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, MessageUtils.get("error.type-mismatch.detail", propertyName));
        pd.setTitle(MessageUtils.get("exception.title.type-mismatch"));
        pd.setProperty("code", ErrorCode.TYPE_MISMATCH.getCode());
        pd.setProperty("property", propertyName);
        Optional.ofNullable(ex.getRequiredType()).map(Class::getSimpleName).ifPresent(type -> pd.setProperty("requiredType", type));
        pd.setType(buildType("type-mismatch"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, MessageUtils.get("error.internal"));
        pd.setTitle(MessageUtils.get("exception.title.internal-error"));
        pd.setType(buildType("internal-error"));
        pd.setProperty("code", ErrorCode.INTERNAL_ERROR.getCode());
        addCommon(pd, request);
        return pd;
    }

    private URI buildType(String slug) {
        return Optional.of(problemBaseUri).filter(base -> !base.isBlank()).map(base -> base.endsWith("/") ? base : base + "/").map(base -> {
            try {
                return URI.create(base + slug);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }).orElse(URI.create("urn:problem-type:" + slug));
    }

    private ResponseEntity<ProblemDetail> handleValidationException(BindingResult bindingResult, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle(MessageUtils.get("exception.title.validation-failed"));
        pd.setDetail(MessageUtils.get("error.validation-failed"));
        List<Map<String, Object>> errors = bindingResult.getFieldErrors().stream().map(fe -> Map.of("field", fe.getField(), "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"), "rejectedValue", Optional.ofNullable(fe.getRejectedValue()).orElse(""))).collect(Collectors.toList());
        pd.setProperty("errors", errors);
        pd.setProperty("code", ErrorCode.VALIDATION_ERROR.getCode());
        pd.setType(buildType("validation-error"));
        addCommon(pd, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }
}
