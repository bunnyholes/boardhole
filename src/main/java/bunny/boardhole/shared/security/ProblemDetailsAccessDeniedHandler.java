package bunny.boardhole.shared.security;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
import bunny.boardhole.shared.constants.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

/**
 * 접근 거부 처리 핸들러
 * Spring Security에서 접근 거부 시 ProblemDetail 형식으로 응답합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class ProblemDetailsAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    @Value("${boardhole.problem.base-uri:}")
    private String problemBaseUri;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle(messageSource.getMessage("exception.title.access-denied", null, LocaleContextHolder.getLocale()));
        pd.setDetail(messageSource.getMessage("error.auth.access-denied", null, LocaleContextHolder.getLocale()));
        pd.setType(buildType("forbidden"));
        try {
            pd.setInstance(URI.create(request.getRequestURI()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request URI: {}", request.getRequestURI(), e);
        }
        // Optional을 사용한 null 체크 제거
        Optional.ofNullable(MDC.get(RequestLoggingFilter.TRACE_ID))
                .filter(traceId -> !traceId.isBlank())
                .ifPresent(traceId -> pd.setProperty("traceId", traceId));
        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("method", request.getMethod());
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("code", ErrorCode.FORBIDDEN.getCode());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), pd);
    }

    @NonNull
    private URI buildType(@NonNull String slug) {
        return Optional.ofNullable(problemBaseUri)
                .filter(base -> !base.isBlank())
                .map(base -> base.endsWith("/") ? base : base + "/")
                .map(base -> {
                    try {
                        return URI.create(base + slug);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid problem type URI: {}{}", base, slug, e);
                        return null;
                    }
                })
                .orElse(URI.create("urn:problem-type:" + slug));
    }

}
