package bunny.boardhole.common.config.log;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "boardhole.logging")
public class LoggingProperties {

    private Performance performance = new Performance();

    public boolean isFast(long ms) {
        return ms < performance.fastThreshold;
    }

    public boolean isNormal(long ms) {
        return ms < performance.normalThreshold;
    }

    public boolean isSlow(long ms) {
        return ms >= performance.normalThreshold;
    }

    @Data
    public static class Performance {
        private long fastThreshold = 100;
        private long normalThreshold = 500;
    }
}