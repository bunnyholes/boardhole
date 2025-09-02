package bunny.boardhole.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAPI 설정 프로퍼티
 * application.yml의 boardhole.api 설정값을 매핑
 */
@ConfigurationProperties(prefix = "boardhole.api")
@Data
public class OpenApiConfig {

    /** API 문서 제목 */
    private String title;

    /** API 버전 */
    private String version;

    /** API 설명 */
    private String description;

    /** 서비스 약관 URL */
    private String termsOfService;

    /** 연락처 정보 */
    private final Contact contact = new Contact();

    /** 라이선스 정보 */
    private final License license = new License();

    /** 보안 설정 정보 */
    private final Security security = new Security();

    /**
     * API 연락처 정보 설정 클래스
     */
    @Data
    public static class Contact {

        /** 연락처 이름 */
        private String name;

        /** 연락처 이메일 */
        private String email;

        /** 연락처 URL */
        private String url;
    }

    /**
     * API 라이선스 정보 설정 클래스
     */
    @Data
    public static class License {

        /** 라이선스 이름 */
        private String name;

        /** 라이선스 URL */
        private String url;
    }

    /**
     * API 보안 설정 클래스
     */
    @Data
    public static class Security {

        /** 세션 보안 설정 */
        private final Session session = new Session();

        /**
         * 세션 보안 설정 클래스
         */
        @Data
        public static class Session {

            /** 세션 보안 스키마 이름 */
            private String name;

            /** 세션 보안 설명 */
            private String description;
        }
    }
}