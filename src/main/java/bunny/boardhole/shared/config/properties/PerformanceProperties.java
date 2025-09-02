package bunny.boardhole.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 성능 관련 설정 프로퍼티
 * application.yml의 boardhole.performance 설정을 매핑
 */
@Component
@ConfigurationProperties(prefix = "boardhole.performance")
@Data
public class PerformanceProperties {
    /** 로깅 성능 설정 */
    private final Logging logging = new Logging();

    /**
     * 로깅 성능 설정 클래스
     */
    @Data
    public static class Logging {
        
        /** 기본 빠른 응답 임계값(밀리초) */
        private static final long DEFAULT_FAST_THRESHOLD_MS = 100;
        
        /** 기본 보통 응답 임계값(밀리초) */
        private static final long DEFAULT_NORMAL_THRESHOLD_MS = 500;
        
        /** 기본 느린 응답 임계값(밀리초) */
        private static final long DEFAULT_SLOW_THRESHOLD_MS = 1000;
        
        /** 기본 실행 시간 임계값(밀리초) */
        private static final long DEFAULT_DURATION_THRESHOLD_MS = 1000;
        
        /** 빠른 응답 임계값 (밀리초) */
        private long fastThresholdMs = DEFAULT_FAST_THRESHOLD_MS;
        
        /** 보통 응답 임계값 (밀리초) */
        private long normalThresholdMs = DEFAULT_NORMAL_THRESHOLD_MS;
        
        /** 느린 응답 임계값 (밀리초) */
        private long slowThresholdMs = DEFAULT_SLOW_THRESHOLD_MS;
        
        /** 실행 시간 임계값 (밀리초) */
        private long durationThresholdMs = DEFAULT_DURATION_THRESHOLD_MS;
    }
}