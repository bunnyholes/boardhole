package bunny.boardhole.common.exception;

import bunny.boardhole.common.config.log.RequestLoggingFilter;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

@RestControllerAdvice
@RequiredArgsConstructor
@Tag(name = "예외 처리", description = "전역 예외 처리 및 에러 응답 관리")
public class GlobalExceptionHandler {
    
    private final MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.not-found", null, LocaleContextHolder.getLocale()));
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ProblemDetail handleConflict(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.conflict", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "CONFLICT");
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({DuplicateUsernameException.class})
    public ProblemDetail handleDuplicateUsername(DuplicateUsernameException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.duplicate-username", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "USER_DUPLICATE_USERNAME");
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({DuplicateEmailException.class})
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.duplicate-email", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "USER_DUPLICATE_EMAIL");
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        // 인증 실패는 401(Unauthorized)이 적합
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.unauthorized", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "UNAUTHORIZED");
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.access-denied", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "FORBIDDEN");
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
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    public ProblemDetail handleBadRequest(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle(messageSource.getMessage("exception.title.bad-request", null, LocaleContextHolder.getLocale()));
        pd.setProperty("code", "BAD_REQUEST");
        addCommon(pd, request);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, 
            messageSource.getMessage("error.internal", null, LocaleContextHolder.getLocale()));
        pd.setTitle(messageSource.getMessage("exception.title.internal-error", null, LocaleContextHolder.getLocale()));
        pd.setType(URI.create("about:blank"));
        pd.setProperty("code", "INTERNAL_ERROR");
        addCommon(pd, request);
        return pd;
    }

    private static void addCommon(ProblemDetail pd, HttpServletRequest request) {
        String traceId = MDC.get(RequestLoggingFilter.TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            pd.setProperty("traceId", traceId);
        }
        if (request != null) {
            pd.setProperty("path", request.getRequestURI());
            pd.setProperty("method", request.getMethod());
        }
        pd.setProperty("timestamp", Instant.now().toString());
    }
}
