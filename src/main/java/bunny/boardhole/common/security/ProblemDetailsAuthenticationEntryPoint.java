package bunny.boardhole.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.net.URI;

/**
 * 인증 실패 진입점 핸들러
 * Spring Security에서 인증 실패 시 ProblemDetail 형식으로 응답합니다.
 */
@Schema(name = "ProblemDetailsAuthenticationEntryPoint", description = "Spring Security 인증 실패 진입점 - ProblemDetail 형식 에러 응답")
public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${boardhole.problem.base-uri:}")
    private String problemBaseUri;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("인증 필요");
        pd.setDetail("로그인이 필요합니다.");
        pd.setType(buildType("unauthorized"));
        try {
            pd.setInstance(URI.create(request.getRequestURI()));
        } catch (IllegalArgumentException ignored) {
        }

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
