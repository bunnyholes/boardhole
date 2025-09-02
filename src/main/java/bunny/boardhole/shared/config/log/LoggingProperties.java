package bunny.boardhole.shared.config.log;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.*;

/**
 * 로깅 설정 프로퍼티
 * application.yml의 boardhole.logging 설정을 매핑
 *
 * @param performance 성능 측정 설정
 */
@ConfigurationProperties(prefix = "boardhole.logging")
public record LoggingProperties(
        @DefaultValue Performance performance
) {

    /** 기본 빠른 응답 임계값 (밀리초) */
    private static final long DEFAULT_FAST_THRESHOLD = 100L;
    /** 기본 보통 응답 임계값 (밀리초) */
    private static final long DEFAULT_NORMAL_THRESHOLD = 500L;

    @ConstructorBinding
    public LoggingProperties(final Performance performance) {
        this.performance = performance != null ? performance : new Performance(DEFAULT_FAST_THRESHOLD, DEFAULT_NORMAL_THRESHOLD);
    }

    public LoggingProperties() {
        this(new Performance(DEFAULT_FAST_THRESHOLD, DEFAULT_NORMAL_THRESHOLD));
    }

    /**
     * 응답 시간이 빠른지 확인
     *
     * @param responseTimeMs 응답 시간 (밀리초)
     * @return 빠른 응답이면 true
     */
    public boolean isFast(final long responseTimeMs) {
        return responseTimeMs < performance.fastThreshold();
    }

    /**
     * 응답 시간이 보통인지 확인
     *
     * @param responseTimeMs 응답 시간 (밀리초)
     * @return 보통 응답이면 true
     */
    public boolean isNormal(final long responseTimeMs) {
        return responseTimeMs < performance.normalThreshold();
    }

    /**
     * 응답 시간이 느린지 확인
     *
     * @param responseTimeMs 응답 시간 (밀리초)
     * @return 느린 응답이면 true
     */
    public boolean isSlow(final long responseTimeMs) {
        return responseTimeMs >= performance.normalThreshold();
    }

    /**
     * 성능 측정 임계값 설정
     *
     * @param fastThreshold 빠른 응답 임계값 (밀리초)
     * @param normalThreshold 보통 응답 임계값 (밀리초)
     */
    public record Performance(
            long fastThreshold,
            long normalThreshold
    ) {
        /** 기본 빠른 응답 임계값 (밀리초) */
        private static final long DEFAULT_FAST_THRESHOLD = 100L;
        /** 기본 보통 응답 임계값 (밀리초) */
        private static final long DEFAULT_NORMAL_THRESHOLD = 500L;

        public Performance() {
            this(DEFAULT_FAST_THRESHOLD, DEFAULT_NORMAL_THRESHOLD);
        }
    }
}