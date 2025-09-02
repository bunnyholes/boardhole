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
    private Logging logging = new Logging();
    
    @Data
    public static class Logging {
        private long fastThresholdMs = 100;
        private long normalThresholdMs = 500;
        private long slowThresholdMs = 1000;
        private long durationThresholdMs = 1000;
    }
}