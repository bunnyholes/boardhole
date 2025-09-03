package bunny.boardhole.shared.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 이메일 관련 설정 속성
 */
@ConfigurationProperties(prefix = "boardhole.email")
public record EmailProperties(
        String baseUrl,
        String fromName,
        String fromEmail,
        Integer verificationExpirationHours,
        RateLimit rateLimit
) {
    public EmailProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080";
        }
        if (fromName == null || fromName.isBlank()) {
            fromName = "Board-Hole";
        }
        if (verificationExpirationHours == null) {
            verificationExpirationHours = 24;
        }
        if (rateLimit == null) {
            rateLimit = new RateLimit(5, 20);
        }
    }

    public record RateLimit(
            Integer perHour,
            Integer perDay
    ) {
        public RateLimit {
            if (perHour == null) perHour = 5;
            if (perDay == null) perDay = 20;
        }
    }
}