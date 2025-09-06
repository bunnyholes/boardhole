package bunny.boardhole.shared.web;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 전역 응답 처리 어드바이스
 * <p>
 * 1. 성공 응답에 대한 리디렉트 처리 (?redirect=/path)
 * 2. 컨트롤러의 @ResponseStatus 애노테이션 적용
 */
@Slf4j
@ControllerAdvice
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 모든 컨트롤러 응답을 처리
        return true;
    }

    @Override
    @Nullable
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        if (request instanceof ServletServerHttpRequest servletRequest && response instanceof ServletServerHttpResponse servletResponse) {

            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            HttpServletResponse httpResponse = servletResponse.getServletResponse();

            // redirect 파라미터 확인
            String redirectPath = httpRequest.getParameter("redirect");

            if (redirectPath != null && !redirectPath.isBlank())
                try {
                    // 리디렉트 실행
                    httpResponse.sendRedirect(redirectPath);
                    return null; // body를 반환하지 않음 (리디렉트이므로)
                } catch (IOException e) {
                    log.warn("리디렉트 실패: {}", redirectPath, e);
                    // 리디렉트 실패 시 정상 응답 처리
                }
        }

        return body; // 정상 응답 반환
    }
}