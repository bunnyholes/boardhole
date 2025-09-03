package bunny.boardhole.shared.web;

import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.*;
import org.springframework.lang.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.IOException;

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
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class converterType) {
        // 모든 컨트롤러 응답을 처리
        return true;
    }

    @Override
    @Nullable
    public Object beforeBodyWrite(@Nullable Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {

        if (request instanceof ServletServerHttpRequest servletRequest &&
                response instanceof ServletServerHttpResponse servletResponse) {

            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            HttpServletResponse httpResponse = servletResponse.getServletResponse();

            // redirect 파라미터 확인
            String redirectPath = httpRequest.getParameter("redirect");

            if (redirectPath != null && !redirectPath.isBlank()) try {
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