package bunny.boardhole.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.net.URI;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 인증 실패 진입점 핸들러
 * Spring Security에서 인증 실패 시 ProblemDetail 형식으로 응답합니다.
 */
@Schema(name = "ProblemDetailsAuthenticationEntryPoint", description = "Spring Security 인증 실패 진입점 - ProblemDetail 형식 에러 응답")
public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Authentication required");
        pd.setDetail("Authentication required");
        pd.setType(URI.create("about:blank"));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), pd);
    }

}
