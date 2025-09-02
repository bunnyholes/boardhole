package bunny.boardhole.shared.config;

import bunny.boardhole.shared.security.CurrentUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 웹 MVC 설정
 * - 인자 해결자 등록만 처리
 * - CORS는 spring.mvc.cors 프로퍼티로만 구성(보안과 자동 연동)
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    /** 현재 사용자 인자 해결자 */
    private final CurrentUserArgumentResolver userResolver;

    /**
     * 사용자 정의 인자 해결자 등록
     *
     * @param argumentResolvers 인자 해결자 목록
     */
    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userResolver);
    }
}
