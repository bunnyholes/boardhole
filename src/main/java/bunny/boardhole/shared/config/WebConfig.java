package bunny.boardhole.shared.config;

import bunny.boardhole.shared.security.CurrentUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

/**
 * 웹 MVC 설정
 * CORS, 인자 해결자, HTTP 메소드 오버라이드 등 웹 계층 설정을 담당합니다.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    @Value("${boardhole.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${boardhole.cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${boardhole.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${boardhole.cors.allow-credentials}")
    private boolean allowCredentials;

    @Value("${boardhole.cors.max-age}")
    private long maxAge;

    /**
     * 커스텀 인자 해결자 등록
     *
     * @param resolvers 인자 해결자 목록
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    /**
     * CORS 매핑 설정
     *
     * @param registry CORS 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }


    // No custom message converters or resource handlers.
}
