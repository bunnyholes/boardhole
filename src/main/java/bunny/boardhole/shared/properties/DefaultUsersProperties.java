package bunny.boardhole.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 기본 사용자 설정
 * 애플리케이션 초기화 시 생성되는 기본 사용자 정보
 */
@ConfigurationProperties(prefix = "boardhole.default-users")
public record DefaultUsersProperties(
        /**
         * 관리자 계정 정보
         */
        UserInfo admin,
        
        /**
         * 일반 사용자 계정 정보
         */
        UserInfo regular
) {
    
    /**
     * 사용자 정보
     */
    public record UserInfo(
            /**
             * 사용자명
             */
            String username,
            
            /**
             * 비밀번호 (환경별 오버라이드 권장)
             */
            String password,
            
            /**
             * 이름
             */
            String name,
            
            /**
             * 이메일 주소
             */
            String email
    ) {}
}