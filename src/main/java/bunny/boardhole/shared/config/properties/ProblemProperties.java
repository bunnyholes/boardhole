package bunny.boardhole.shared.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Problem Details API 설정 속성
 */
@ConfigurationProperties(prefix = "boardhole.problem")
public record ProblemProperties(
        String baseUri
) {
    // baseUri는 null일 수 있음 (의도적으로 비워두면 자동 생성)
}