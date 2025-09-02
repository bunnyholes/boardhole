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
    
    /** 기본 역할 접두사 */
    private static final String DEFAULT_ROLE_PREFIX = "ROLE_";
    
    /** 기본 세션 타임아웃 시간(분) */
    private static final int DEFAULT_SESSION_TIMEOUT_MINUTES = 30;
    
    /** 인증 코드 설정 */
    private final VerificationCode verificationCode = new VerificationCode();
    
    /** 역할 접두사 */
    private String rolePrefix = DEFAULT_ROLE_PREFIX;
    
    /** 세션 타임아웃 시간 (분) */
    private int sessionTimeoutMinutes = DEFAULT_SESSION_TIMEOUT_MINUTES;

    /**
     * 인증 코드 설정 클래스
     */
    @Data
    public static class VerificationCode {
        
        /** 기본 코드 길이 */
        private static final int DEFAULT_LENGTH = 6;
        
        /** 기본 만료 시간(분) */
        private static final int DEFAULT_EXPIRY_MINUTES = 30;
        
        /** 기본 문자 집합 */
        private static final String DEFAULT_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        /** 인증 코드 길이 */
        private int length = DEFAULT_LENGTH;
        
        /** 인증 코드 만료 시간 (분) */
        private int expiryMinutes = DEFAULT_EXPIRY_MINUTES;
        
        /** 인증 코드에 사용될 문자 집합 */
        private String charset = DEFAULT_CHARSET;
    }
}