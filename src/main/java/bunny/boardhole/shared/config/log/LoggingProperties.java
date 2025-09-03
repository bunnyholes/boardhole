package bunny.boardhole.shared.config.log;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "boardhole.logging")
public record LoggingProperties(
        Performance performance
) {

    @ConstructorBinding
    public LoggingProperties(Performance performance) {
        this.performance = performance != null ? performance : new Performance(100L, 500L);
    }

    public LoggingProperties() {
        this(new Performance(100L, 500L));
    }

    boolean isFast(long ms) {
        return ms < performance.fastThreshold();
    }

    boolean isNormal(long ms) {
        return ms < performance.normalThreshold();
    }

    boolean isSlow(long ms) {
        return ms >= performance.normalThreshold();
    }

    public record Performance(
            long fastThreshold,
            long normalThreshold
    ) {
        public Performance() {
            this(100L, 500L);
        }
    }
}