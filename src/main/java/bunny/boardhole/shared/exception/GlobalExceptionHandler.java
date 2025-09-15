package bunny.boardhole.shared.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
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
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
import bunny.boardhole.shared.constants.ErrorCode;
import bunny.boardhole.shared.properties.ProblemProperties;
import bunny.boardhole.shared.util.MessageUtils;

import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestControllerAdvice
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Tag(name = "예외 처리", description = "전역 예외 처리 및 에러 응답 관리")
public class GlobalExceptionHandler {

    private final ProblemProperties problemProperties;
    private final EntityManager entityManager;

    private static void addCommon(ProblemDetail pd, @Nullable HttpServletRequest request) {
        // Optional을 사용한 null 체크 제거
        Optional
                .ofNullable(MDC.get(RequestLoggingFilter.TRACE_ID))
                .filter(traceId -> !traceId.isBlank())
                .ifPresent(traceId -> pd.setProperty("traceId", traceId));

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

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, HttpServletRequest request) {
        log.warn("Conflict exception: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.conflict"));
        pd.setProperty("code", ErrorCode.CONFLICT.getCode());
        pd.setType(buildType("conflict"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        // 보안: 원본 메시지는 로깅만 하고 사용자에게는 일반 메시지만 전달
        log.error("Data integrity violation: path={}, method={}, traceId={}",
                request.getRequestURI(), request.getMethod(),
                MDC.get(RequestLoggingFilter.TRACE_ID), ex);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                MessageUtils.get("error.conflict"));
        pd.setTitle(MessageUtils.get("exception.title.conflict"));
        pd.setProperty("code", ErrorCode.CONFLICT.getCode());
        pd.setType(buildType("conflict"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ProblemDetail handleDuplicateUsername(DuplicateUsernameException ex, HttpServletRequest request) {
        log.warn("Duplicate username attempt: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.duplicate-username"));
        pd.setProperty("code", ErrorCode.USER_DUPLICATE_USERNAME.getCode());
        pd.setType(buildType("duplicate-username"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        log.warn("Duplicate email attempt: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.duplicate-email"));
        pd.setProperty("code", ErrorCode.USER_DUPLICATE_EMAIL.getCode());
        pd.setType(buildType("duplicate-email"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        log.warn("Unauthorized access attempt: path={}, message={}", request.getRequestURI(), ex.getMessage());
        // 인증 실패는 401(Unauthorized)이 적합
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.unauthorized"));
        pd.setProperty("code", ErrorCode.UNAUTHORIZED.getCode());
        pd.setType(buildType("unauthorized"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: path={}, message={}", request.getRequestURI(), ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.access-denied"));
        pd.setProperty("code", ErrorCode.FORBIDDEN.getCode());
        pd.setType(buildType("forbidden"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ProblemDetail> handleInvalid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return handleValidationException(ex.getBindingResult(), request);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<ProblemDetail> handleBindException(BindException ex, HttpServletRequest request) {
        return handleValidationException(ex.getBindingResult(), request);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ProblemDetail handleValidationException(ValidationException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.validation-failed"));
        pd.setProperty("code", ErrorCode.VALIDATION_ERROR.getCode());
        pd.setType(buildType("validation-error"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ProblemDetail handleBadRequest(ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.validation-failed"));
        pd.setProperty("code", ErrorCode.VALIDATION_ERROR.getCode());
        pd.setType(buildType("validation-error"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        // 정렬 방향값 오류(asc/desc 이외) 등을 400으로 분류
        if (isSortDirectionError(ex, request)) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                    MessageUtils.get("error.invalid-sort-direction", extractDirectionFromSort(request)));
            pd.setTitle(MessageUtils.get("exception.title.bad-request"));
            pd.setProperty("code", ErrorCode.BAD_REQUEST.getCode());
            String[] sortParams = Optional.ofNullable(request.getParameterValues("sort")).orElse(new String[]{});
            if (sortParams.length > 0)
                pd.setProperty("sort", sortParams);
            pd.setType(buildType("invalid-sort"));
            addCommon(pd, request);
            return pd;
        }

        // 그 외 IllegalArgumentException은 기존 정책(422) 유지
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.validation-failed"));
        pd.setProperty("code", ErrorCode.VALIDATION_ERROR.getCode());
        pd.setType(buildType("validation-error"));
        addCommon(pd, request);
        return pd;
    }

    private static boolean isSortDirectionError(IllegalArgumentException ex, HttpServletRequest request) {
        if (request == null)
            return false;
        String[] sortParams = request.getParameterValues("sort");
        if (sortParams == null || sortParams.length == 0)
            return false;
        String msg = Optional.ofNullable(ex.getMessage()).orElse("").toLowerCase();
        // 방향값 오류로 흔히 보이는 키워드 검사
        if (msg.contains("direction") || msg.contains("order") || msg.contains("sort"))
            return true;
        // 메시지에 의존하지 않도록 파라미터 패턴도 간단 점검: property,dir 형태인데 dir이 asc/desc가 아닐 때
        for (String s : sortParams) {
            String[] parts = s.split(",");
            if (parts.length >= 2) {
                String dir = parts[1].trim().toLowerCase();
                if (!dir.isEmpty() && !dir.equals("asc") && !dir.equals("desc"))
                    return true;
            }
        }
        return false;
    }

    private static String extractDirectionFromSort(HttpServletRequest request) {
        String[] sortParams = Optional.ofNullable(request.getParameterValues("sort")).orElse(new String[]{});
        for (String s : sortParams) {
            String[] parts = s.split(",");
            if (parts.length >= 2)
                return parts[1].trim();
        }
        return "";
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

    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleInvalidSort(PropertyReferenceException ex, HttpServletRequest request) {
        String invalidField = ex.getPropertyName();
        Class<?> domainType = Optional.ofNullable(ex.getType())
                                      .map(org.springframework.data.util.TypeInformation::getType)
                                      .orElse(null);

        List<String> allowedFields = domainType != null ? getSortableAttributes(domainType) : List.of();

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                MessageUtils.get("error.invalid-sort-field", invalidField));
        pd.setTitle(MessageUtils.get("exception.title.bad-request"));
        pd.setProperty("code", ErrorCode.BAD_REQUEST.getCode());
        pd.setProperty("invalidField", invalidField);
        if (domainType != null)
            pd.setProperty("entity", domainType.getSimpleName());
        if (!allowedFields.isEmpty())
            pd.setProperty("allowedFields", allowedFields);
        String[] sortParams = Optional.ofNullable(request.getParameterValues("sort")).orElse(new String[]{});
        if (sortParams.length > 0)
            pd.setProperty("sort", sortParams);
        pd.setType(buildType("invalid-sort"));
        addCommon(pd, request);
        return pd;
    }

    private List<String> getSortableAttributes(Class<?> entityClass) {
        try {
            EntityType<?> type = entityManager.getMetamodel().entity(entityClass);
            return type.getSingularAttributes()
                       .stream()
                       .filter(attr -> isBasicOrId(attr))
                       .map(Attribute::getName)
                       .sorted()
                       .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    private static boolean isBasicOrId(SingularAttribute<?, ?> attr) {
        return attr.isId() || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String[] methods = ex.getSupportedMethods();
        String supportedMethods = methods != null ? String.join(", ", methods) : "None";
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED,
                MessageUtils.get("error.method-not-allowed.detail", supportedMethods));
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
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                MessageUtils.get("error.missing-parameter.detail", ex.getParameterName()));
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

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElement(NoSuchElementException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                ex.getMessage() != null ? ex.getMessage() : MessageUtils.get("error.resource.not-found"));
        pd.setTitle(MessageUtils.get("exception.title.not-found"));
        pd.setProperty("code", ErrorCode.NOT_FOUND.getCode());
        pd.setType(buildType("not-found"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({OptimisticLockException.class, PessimisticLockException.class})
    public ProblemDetail handleLockingException(Exception ex, HttpServletRequest request) {
        log.warn("Locking conflict: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                MessageUtils.get("error.locking.conflict"));
        pd.setTitle(MessageUtils.get("exception.title.conflict"));
        pd.setProperty("code", ErrorCode.CONFLICT.getCode());
        pd.setProperty("lockType", ex instanceof OptimisticLockException ? "optimistic" : "pessimistic");
        pd.setType(buildType("locking-conflict"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("File upload size exceeded: {}", ex.getMaxUploadSize());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE,
                MessageUtils.get("error.upload.size-exceeded", ex.getMaxUploadSize()));
        pd.setTitle(MessageUtils.get("exception.title.payload-too-large"));
        pd.setProperty("code", ErrorCode.PAYLOAD_TOO_LARGE.getCode());
        pd.setProperty("maxSize", ex.getMaxUploadSize());
        pd.setType(buildType("upload-size-exceeded"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ProblemDetail handleAsyncTimeout(AsyncRequestTimeoutException ex, HttpServletRequest request) {
        log.warn("Async request timeout: {}", request.getRequestURI());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                MessageUtils.get("error.request.timeout"));
        pd.setTitle(MessageUtils.get("exception.title.service-unavailable"));
        pd.setProperty("code", ErrorCode.SERVICE_UNAVAILABLE.getCode());
        pd.setType(buildType("request-timeout"));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        // 500 에러는 반드시 로깅
        log.error("Unexpected error occurred: path={}, method={}, traceId={}",
                request.getRequestURI(), request.getMethod(),
                MDC.get(RequestLoggingFilter.TRACE_ID), ex);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, MessageUtils.get("error.internal"));
        pd.setTitle(MessageUtils.get("exception.title.internal-error"));
        pd.setType(buildType("internal-error"));
        pd.setProperty("code", ErrorCode.INTERNAL_ERROR.getCode());
        addCommon(pd, request);
        return pd;
    }

    private URI buildType(String slug) {
        String baseUri = problemProperties.baseUri();
        return Optional.ofNullable(baseUri)
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

    private ResponseEntity<ProblemDetail> handleValidationException(BindingResult bindingResult, HttpServletRequest request) {
        log.warn("Validation failed: {} errors on path={}", bindingResult.getErrorCount(), request.getRequestURI());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        pd.setTitle(MessageUtils.get("exception.title.validation-failed"));
        pd.setDetail(MessageUtils.get("error.validation-failed"));
        List<Map<String, Object>> errors = bindingResult
                .getFieldErrors()
                .stream()
                .map(fe -> Map.of("field", fe.getField(), "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value"),
                        "rejectedValue", Optional.ofNullable(fe.getRejectedValue()).orElse("")))
                .collect(Collectors.toList());
        pd.setProperty("errors", errors);
        pd.setProperty("code", ErrorCode.VALIDATION_ERROR.getCode());
        pd.setType(buildType("validation-error"));
        addCommon(pd, request);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(pd);
    }
}
