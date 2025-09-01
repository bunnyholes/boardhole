package bunny.boardhole.shared.config.log;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "boardhole.logging")
public record LoggingProperties(
    @DefaultValue Performance performance
) {
    
    @ConstructorBinding
    public LoggingProperties(Performance performance) {
        this.performance = performance != null ? performance : new Performance(100L, 500L);
    }
    
    public LoggingProperties() {
        this(new Performance(100L, 500L));
    }

    public boolean isFast(long ms) {
        return ms < performance.fastThreshold();
    }

    public boolean isNormal(long ms) {
        return ms < performance.normalThreshold();
    }

    public boolean isSlow(long ms) {
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