package bunny.boardhole.shared.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 보안 관련 설정 프로퍼티
 * application.yml의 boardhole.security 설정을 매핑
 */
@Component
@ConfigurationProperties(prefix = "boardhole.security")
@Data
public class SecurityProperties {
    private VerificationCode verificationCode = new VerificationCode();
    private String rolePrefix = "ROLE_";
    private int sessionTimeoutMinutes = 30;

    @Data
    public static class VerificationCode {
        private int length = 6;
        private int expiryMinutes = 30;
        private String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    }
}