package bunny.boardhole.shared.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 인증 관련 설정 속성
 */
@ConfigurationProperties(prefix = "boardhole.auth")
public record AuthProperties(
        String defaultRole
) {
    public AuthProperties {
        if (defaultRole == null || defaultRole.isBlank()) {
            defaultRole = "USER";
        }
    }
}