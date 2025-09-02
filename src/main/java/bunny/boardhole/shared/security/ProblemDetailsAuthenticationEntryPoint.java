package bunny.boardhole.shared.security;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

/**
 * 인증 실패 진입점 핸들러
 * Spring Security에서 인증 실패 시 ProblemDetail 형식으로 응답합니다.
 */
@Schema(name = "ProblemDetailsAuthenticationEntryPoint", description = "Spring Security 인증 실패 진입점 - ProblemDetail 형식 에러 응답")
@RequiredArgsConstructor
public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    @Value("${boardhole.problem.base-uri:}")
    private String problemBaseUri;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle(messageSource.getMessage("exception.title.unauthorized", null, LocaleContextHolder.getLocale()));
        pd.setDetail(messageSource.getMessage("error.auth.not-logged-in", null, LocaleContextHolder.getLocale()));
        pd.setType(buildType("unauthorized"));
        try {
            pd.setInstance(URI.create(request.getRequestURI()));
        } catch (IllegalArgumentException ignored) {
        }

        // common extensions
        String traceId = MDC.get(RequestLoggingFilter.TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            pd.setProperty("traceId", traceId);
        }
        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("method", request.getMethod());
        pd.setProperty("timestamp", Instant.now().toString());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), pd);
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
