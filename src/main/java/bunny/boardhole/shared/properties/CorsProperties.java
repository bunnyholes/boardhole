package bunny.boardhole.shared.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CORS(Cross-Origin Resource Sharing) 관련 설정
 */
@ConfigurationProperties(prefix = "boardhole.cors")
public record CorsProperties(
        /**
         * CORS를 적용할 경로 패턴
         */
        List<String> pathPatterns,
        
        /**
         * 허용할 출처 목록
         */
        List<String> allowedOrigins,
        
        /**
         * 허용할 HTTP 메서드 목록
         */
        List<String> allowedMethods,
        
        /**
         * 허용할 요청 헤더 목록
         */
        List<String> allowedHeaders,
        
        /**
         * 노출할 응답 헤더 목록
         */
        List<String> exposedHeaders,
        
        /**
         * 자격 증명 허용 여부
         */
        boolean allowCredentials,
        
        /**
         * preflight 요청 캐시 시간 (초 단위)
         */
        long maxAge
) {
}