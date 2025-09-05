package bunny.boardhole.shared.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import bunny.boardhole.shared.constants.ErrorCode;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        pd.setType(ProblemDetailsHelper.buildType(problemBaseUri, "forbidden"));
        ProblemDetailsHelper.addCommonProperties(pd, request, ErrorCode.FORBIDDEN.getCode());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), pd);
    }


}
