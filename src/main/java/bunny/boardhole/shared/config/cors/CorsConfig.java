package bunny.boardhole.shared.config.cors;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;

/**
 * CORS(Cross-Origin Resource Sharing) 설정
 * 웹 애플리케이션의 교차 출처 리소스 공유 정책을 관리합니다.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    /**
     * CORS 설정 소스를 생성합니다.
     *
     * @param corsProperties CORS 관련 설정 프로퍼티
     * @return 구성된 CORS 설정 소스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(final CorsProperties corsProperties) {
        final UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();

        final CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(corsProperties.allowedOrigins());
        corsConfig.setAllowedMethods(corsProperties.allowedMethods());
        corsConfig.setAllowedHeaders(corsProperties.allowedHeaders());
        corsConfig.setExposedHeaders(corsProperties.exposedHeaders());
        corsConfig.setAllowCredentials(corsProperties.allowCredentials());
        corsConfig.setMaxAge(corsProperties.maxAge());

        for (final String pathPattern : corsProperties.pathPatterns()) {
            corsSource.registerCorsConfiguration(pathPattern, corsConfig);
        }
        return corsSource;
    }
}
