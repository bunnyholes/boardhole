package bunny.boardhole.shared.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import bunny.boardhole.shared.constants.ErrorCode;
import bunny.boardhole.shared.properties.ProblemProperties;
import bunny.boardhole.shared.util.MessageUtils;

/**
 * REST API 전용 인증 실패 진입점 핸들러
 * Spring Security에서 인증 실패 시 ProblemDetail 형식의 JSON으로 응답합니다.
 * View Controller는 LoginUrlAuthenticationEntryPoint를 사용합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final ProblemProperties problemProperties;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // API 전용 - 항상 JSON 응답 반환
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle(MessageUtils.get("exception.title.unauthorized"));
        pd.setDetail(MessageUtils.get("error.auth.not-logged-in"));
        pd.setType(ProblemDetailsHelper.buildType(problemProperties.baseUri(), "unauthorized"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.UNAUTHORIZED.getCode());

        log.debug("🔒 Unauthorized API request: {} - returning 401 JSON response", request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), pd);
    }

}
