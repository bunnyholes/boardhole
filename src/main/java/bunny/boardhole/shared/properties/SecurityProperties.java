package bunny.boardhole.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 보안 관련 설정
 */
@ConfigurationProperties(prefix = "boardhole.security")
public record SecurityProperties(
        /**
         * 역할 이름 접두사
         * Spring Security에서 권한을 확인할 때 사용
         */
        String rolePrefix,

        /**
         * 세션 타임아웃 시간 (분 단위)
         */
        int sessionTimeoutMinutes
) {
}