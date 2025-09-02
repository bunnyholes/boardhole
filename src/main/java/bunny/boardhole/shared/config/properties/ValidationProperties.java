package bunny.boardhole.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 검증 관련 설정 프로퍼티
 * application.yml의 boardhole.validation 설정을 매핑
 */
@Component
@ConfigurationProperties(prefix = "boardhole.validation")
@Data
public class ValidationProperties {
    private Board board = new Board();
    private User user = new User();
    private EmailVerification emailVerification = new EmailVerification();

    @Data
    public static class Board {
        private int titleMaxLength = 200;
        private int contentMaxLength = 10000;
    }

    @Data
    public static class User {
        private int usernameMinLength = 3;
        private int usernameMaxLength = 20;
        private int passwordMinLength = 8;
        private int passwordMaxLength = 100;
        private int emailMaxLength = 255;
        private int nameMinLength = 1;
        private int nameMaxLength = 50;
    }

    @Data
    public static class EmailVerification {
        private int expirationMinutes = 30;
        private int codeLength = 6;
    }
}