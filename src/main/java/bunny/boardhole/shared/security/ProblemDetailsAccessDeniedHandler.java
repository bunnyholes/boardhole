package bunny.boardhole.shared.security;

import bunny.boardhole.shared.config.log.RequestLoggingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

/**
 * 접근 거부 처리 핸들러
 * Spring Security에서 접근 거부 시 ProblemDetail 형식으로 응답합니다.
 */
@Schema(name = "ProblemDetailsAccessDeniedHandler", description = "Spring Security 접근 거부 핸들러 - ProblemDetail 형식 에러 응답")
@RequiredArgsConstructor
public class ProblemDetailsAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    @Value("${boardhole.problem.base-uri:}")
    private String problemBaseUri;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("접근 거부");
        pd.setDetail("권한이 부족합니다.");
        pd.setType(buildType("forbidden"));
        try {
            pd.setInstance(URI.create(request.getRequestURI()));
        } catch (IllegalArgumentException ignored) {
        }
        String traceId = MDC.get(RequestLoggingFilter.TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            pd.setProperty("traceId", traceId);
        }
        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("method", request.getMethod());
        pd.setProperty("timestamp", Instant.now().toString());

        response.setStatus(HttpStatus.FORBIDDEN.value());
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
