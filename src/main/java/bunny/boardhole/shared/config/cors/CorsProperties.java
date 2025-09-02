package bunny.boardhole.shared.config.cors;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * CORS 설정 프로퍼티
 * application.yml의 boardhole.cors 설정을 매핑
 *
 * @param pathPatterns CORS를 적용할 경로 패턴 목록
 * @param allowedOrigins 허용된 출처 목록
 * @param allowedMethods 허용된 HTTP 메서드 목록
 * @param allowedHeaders 허용된 헤더 목록
 * @param exposedHeaders 노출할 헤더 목록
 * @param allowCredentials 자격 증명 허용 여부
 * @param maxAge 프리플라이트 요청 캐시 시간 (초)
 */
@ConfigurationProperties(prefix = "boardhole.cors")
public record CorsProperties(
        List<String> pathPatterns,
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        boolean allowCredentials,
        long maxAge
) {
}
