package bunny.boardhole.shared.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import bunny.boardhole.shared.constants.ErrorCode;
import bunny.boardhole.shared.util.MessageUtils;

/**
 * 인증 실패 진입점 핸들러
 * Spring Security에서 인증 실패 시 ProblemDetail 형식으로 응답합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class ProblemDetailsAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    @Value("${boardhole.problem.base-uri:}")
    private String problemBaseUri;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle(MessageUtils.get("exception.title.unauthorized"));
        pd.setDetail(MessageUtils.get("error.auth.not-logged-in"));
        pd.setType(ProblemDetailsHelper.buildType(problemBaseUri, "unauthorized"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.UNAUTHORIZED.getCode());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), pd);
    }

}
