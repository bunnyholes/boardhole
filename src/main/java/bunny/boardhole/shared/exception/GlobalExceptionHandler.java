package bunny.boardhole.shared.exception;

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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
import bunny.boardhole.shared.constants.ErrorCode;
import bunny.boardhole.shared.security.ProblemDetailsHelper;
import bunny.boardhole.shared.util.MessageUtils;

import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
@RequiredArgsConstructor
@Tag(name = "예외 처리", description = "전역 예외 처리 및 에러 응답 관리")
public class GlobalExceptionHandler {

    private final EntityManager entityManager;

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

    private static boolean isBasicOrId(SingularAttribute<?, ?> attr) {
        return attr.isId() || attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, MessageUtils.get("error.resource.not-found"));
        pd.setTitle(MessageUtils.get("exception.title.not-found"));
        pd.setType(ProblemDetailsHelper.buildType("not-found"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.NOT_FOUND.getCode());
        return pd;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.not-found"));
        pd.setType(ProblemDetailsHelper.buildType("not-found"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.NOT_FOUND.getCode());
        return pd;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex, HttpServletRequest request) {
        log.warn("Conflict exception: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.conflict"));
        pd.setType(ProblemDetailsHelper.buildType("conflict"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.CONFLICT.getCode());
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
        pd.setType(ProblemDetailsHelper.buildType("conflict"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.CONFLICT.getCode());
        return pd;
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ProblemDetail handleDuplicateUsername(DuplicateUsernameException ex, HttpServletRequest request) {
        log.warn("Duplicate username attempt: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.duplicate-username"));
        pd.setType(ProblemDetailsHelper.buildType("duplicate-username"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.USER_DUPLICATE_USERNAME.getCode());
        return pd;
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        log.warn("Duplicate email attempt: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.duplicate-email"));
        pd.setType(ProblemDetailsHelper.buildType("duplicate-email"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.USER_DUPLICATE_EMAIL.getCode());
        return pd;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        log.warn("Unauthorized access attempt: path={}, message={}", request.getRequestURI(), ex.getMessage());
        // 인증 실패는 401(Unauthorized)이 적합
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.unauthorized"));
        pd.setType(ProblemDetailsHelper.buildType("unauthorized"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.UNAUTHORIZED.getCode());
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: path={}, message={}", request.getRequestURI(), ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.access-denied"));
        pd.setType(ProblemDetailsHelper.buildType("forbidden"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.FORBIDDEN.getCode());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static ResponseEntity<ProblemDetail> handleInvalid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        return handleValidationException(ex.getBindingResult(), request);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static ResponseEntity<ProblemDetail> handleBindException(BindException ex, HttpServletRequest request) {
        return handleValidationException(ex.getBindingResult(), request);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ProblemDetail handleValidationException(ValidationException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.validation-failed"));
        pd.setType(ProblemDetailsHelper.buildType("validation-error"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.VALIDATION_ERROR.getCode());
        return pd;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ProblemDetail handleBadRequest(ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.validation-failed"));
        pd.setType(ProblemDetailsHelper.buildType("validation-error"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.VALIDATION_ERROR.getCode());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        // 정렬 방향값 오류(asc/desc 이외) 등을 400으로 분류
        if (isSortDirectionError(ex, request)) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                    MessageUtils.get("error.invalid-sort-direction", extractDirectionFromSort(request)));
            pd.setTitle(MessageUtils.get("exception.title.bad-request"));
            String[] sortParams = Optional.ofNullable(request.getParameterValues("sort")).orElse(new String[]{});
            if (sortParams.length > 0)
                pd.setProperty("sort", sortParams);
            pd.setType(ProblemDetailsHelper.buildType("invalid-sort"));
            ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.BAD_REQUEST.getCode());
            return pd;
        }

        // 그 외 IllegalArgumentException은 기존 정책(422) 유지
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle(MessageUtils.get("exception.title.validation-failed"));
        pd.setType(ProblemDetailsHelper.buildType("validation-error"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.VALIDATION_ERROR.getCode());
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, MessageUtils.get("error.invalid-json"));
        pd.setTitle(MessageUtils.get("exception.title.bad-request"));
        pd.setType(ProblemDetailsHelper.buildType("invalid-json"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.INVALID_JSON.getCode());
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
        pd.setProperty("invalidField", invalidField);
        if (domainType != null)
            pd.setProperty("entity", domainType.getSimpleName());
        if (!allowedFields.isEmpty())
            pd.setProperty("allowedFields", allowedFields);
        String[] sortParams = Optional.ofNullable(request.getParameterValues("sort")).orElse(new String[]{});
        if (sortParams.length > 0)
            pd.setProperty("sort", sortParams);
        pd.setType(ProblemDetailsHelper.buildType("invalid-sort"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.BAD_REQUEST.getCode());
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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String[] methods = ex.getSupportedMethods();
        String supportedMethods = methods != null ? String.join(", ", methods) : "None";
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED,
                MessageUtils.get("error.method-not-allowed.detail", supportedMethods));
        pd.setTitle(MessageUtils.get("exception.title.method-not-allowed"));
        pd.setProperty("supportedMethods", ex.getSupportedMethods());
        pd.setType(ProblemDetailsHelper.buildType("method-not-allowed"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.METHOD_NOT_ALLOWED.getCode());
        return pd;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, MessageUtils.get("error.unsupported-media-type"));
        pd.setTitle(MessageUtils.get("exception.title.unsupported-media-type"));
        pd.setType(ProblemDetailsHelper.buildType("unsupported-media-type"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode());
        return pd;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                MessageUtils.get("error.missing-parameter.detail", ex.getParameterName()));
        pd.setTitle(MessageUtils.get("exception.title.missing-parameter"));
        pd.setProperty("parameter", ex.getParameterName());
        pd.setProperty("parameterType", ex.getParameterType());
        pd.setType(ProblemDetailsHelper.buildType("missing-parameter"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.MISSING_PARAMETER.getCode());
        return pd;
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request) {
        String propertyName = ex.getPropertyName() != null ? ex.getPropertyName() : "unknown";
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, MessageUtils.get("error.type-mismatch.detail", propertyName));
        pd.setTitle(MessageUtils.get("exception.title.type-mismatch"));
        pd.setProperty("property", propertyName);
        Optional.ofNullable(ex.getRequiredType()).map(Class::getSimpleName).ifPresent(type -> pd.setProperty("requiredType", type));
        pd.setType(ProblemDetailsHelper.buildType("type-mismatch"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.TYPE_MISMATCH.getCode());
        return pd;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElement(NoSuchElementException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                ex.getMessage() != null ? ex.getMessage() : MessageUtils.get("error.resource.not-found"));
        pd.setTitle(MessageUtils.get("exception.title.not-found"));
        pd.setType(ProblemDetailsHelper.buildType("not-found"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.NOT_FOUND.getCode());
        return pd;
    }

    @ExceptionHandler({OptimisticLockException.class, PessimisticLockException.class})
    public ProblemDetail handleLockingException(Exception ex, HttpServletRequest request) {
        log.warn("Locking conflict: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                MessageUtils.get("error.locking.conflict"));
        pd.setTitle(MessageUtils.get("exception.title.conflict"));
        pd.setProperty("lockType", ex instanceof OptimisticLockException ? "optimistic" : "pessimistic");
        pd.setType(ProblemDetailsHelper.buildType("locking-conflict"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.CONFLICT.getCode());
        return pd;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("File upload size exceeded: {}", ex.getMaxUploadSize());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE,
                MessageUtils.get("error.upload.size-exceeded", ex.getMaxUploadSize()));
        pd.setTitle(MessageUtils.get("exception.title.payload-too-large"));
        pd.setProperty("maxSize", ex.getMaxUploadSize());
        pd.setType(ProblemDetailsHelper.buildType("upload-size-exceeded"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.PAYLOAD_TOO_LARGE.getCode());
        return pd;
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ProblemDetail handleAsyncTimeout(AsyncRequestTimeoutException ex, HttpServletRequest request) {
        log.warn("Async request timeout: {}", request.getRequestURI());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                MessageUtils.get("error.request.timeout"));
        pd.setTitle(MessageUtils.get("exception.title.service-unavailable"));
        pd.setType(ProblemDetailsHelper.buildType("request-timeout"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.SERVICE_UNAVAILABLE.getCode());
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
        pd.setType(ProblemDetailsHelper.buildType("internal-error"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.INTERNAL_ERROR.getCode());
        return pd;
    }

    private static ResponseEntity<ProblemDetail> handleValidationException(BindingResult bindingResult, HttpServletRequest request) {
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
        pd.setType(ProblemDetailsHelper.buildType("validation-error"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.VALIDATION_ERROR.getCode());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(pd);
    }
}
