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
    /** 게시판 검증 설정 */
    private final Board board = new Board();
    
    /** 사용자 검증 설정 */
    private final User user = new User();
    
    /** 이메일 인증 검증 설정 */
    private final EmailVerification emailVerification = new EmailVerification();

    /**
     * 게시판 검증 설정 클래스
     */
    @Data
    public static class Board {
        
        /** 기본 제목 최대 길이 */
        private static final int DEFAULT_TITLE_MAX_LENGTH = 200;
        
        /** 기본 내용 최대 길이 */
        private static final int DEFAULT_CONTENT_MAX_LENGTH = 10000;
        
        /** 게시글 제목 최대 길이 */
        private int titleMaxLength = DEFAULT_TITLE_MAX_LENGTH;
        
        /** 게시글 내용 최대 길이 */
        private int contentMaxLength = DEFAULT_CONTENT_MAX_LENGTH;
    }

    /**
     * 사용자 검증 설정 클래스
     */
    @Data
    public static class User {
        
        /** 기본 사용자명 최소 길이 */
        private static final int DEFAULT_USERNAME_MIN_LENGTH = 3;
        
        /** 기본 사용자명 최대 길이 */
        private static final int DEFAULT_USERNAME_MAX_LENGTH = 20;
        
        /** 기본 패스워드 최소 길이 */
        private static final int DEFAULT_PASSWORD_MIN_LENGTH = 8;
        
        /** 기본 패스워드 최대 길이 */
        private static final int DEFAULT_PASSWORD_MAX_LENGTH = 100;
        
        /** 기본 이메일 최대 길이 */
        private static final int DEFAULT_EMAIL_MAX_LENGTH = 255;
        
        /** 기본 이름 최소 길이 */
        private static final int DEFAULT_NAME_MIN_LENGTH = 1;
        
        /** 기본 이름 최대 길이 */
        private static final int DEFAULT_NAME_MAX_LENGTH = 50;
        
        /** 사용자명 최소 길이 */
        private int usernameMinLength = DEFAULT_USERNAME_MIN_LENGTH;
        
        /** 사용자명 최대 길이 */
        private int usernameMaxLength = DEFAULT_USERNAME_MAX_LENGTH;
        
        /** 패스워드 최소 길이 */
        private int passwordMinLength = DEFAULT_PASSWORD_MIN_LENGTH;
        
        /** 패스워드 최대 길이 */
        private int passwordMaxLength = DEFAULT_PASSWORD_MAX_LENGTH;
        
        /** 이메일 최대 길이 */
        private int emailMaxLength = DEFAULT_EMAIL_MAX_LENGTH;
        
        /** 이름 최소 길이 */
        private int nameMinLength = DEFAULT_NAME_MIN_LENGTH;
        
        /** 이름 최대 길이 */
        private int nameMaxLength = DEFAULT_NAME_MAX_LENGTH;
    }

    /**
     * 이메일 인증 검증 설정 클래스
     */
    @Data
    public static class EmailVerification {
        
        /** 기본 만료 시간(분) */
        private static final int DEFAULT_EXPIRATION_MINUTES = 30;
        
        /** 기본 코드 길이 */
        private static final int DEFAULT_CODE_LENGTH = 6;
        
        /** 인증 코드 만료 시간 (분) */
        private int expirationMinutes = DEFAULT_EXPIRATION_MINUTES;
        
        /** 인증 코드 길이 */
        private int codeLength = DEFAULT_CODE_LENGTH;
    }
}