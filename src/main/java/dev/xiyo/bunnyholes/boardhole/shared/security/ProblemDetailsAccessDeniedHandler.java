package dev.xiyo.bunnyholes.boardhole.shared.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import tools.jackson.databind.ObjectMapper;

import dev.xiyo.bunnyholes.boardhole.shared.constants.ErrorCode;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;

/**
 * 접근 거부 처리 핸들러
 * Spring Security에서 접근 거부 시 ProblemDetail 형식으로 응답합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class ProblemDetailsAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle(MessageUtils.get("exception.title.access-denied"));
        pd.setDetail(MessageUtils.get("error.auth.access-denied"));
        pd.setType(ProblemDetailsHelper.buildType("forbidden"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.FORBIDDEN.getCode());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), pd);
    }

}
