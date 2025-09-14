package bunny.boardhole.shared.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAPI 문서 관련 설정
 */
@ConfigurationProperties(prefix = "boardhole.api")
public record ApiProperties(
        /**
         * API 제목
         */
        String title,

        /**
         * API 설명
         */
        String description,

        /**
         * 서비스 약관 URL
         */
        String termsOfService,

        /**
         * 연락처 정보
         */
        Contact contact,

        /**
         * 라이센스 정보
         */
        License license
) {

    /**
     * API 문서 연락처 정보
     */
    public record Contact(
            /**
             * 담당자 이름
             */
            String name,

            /**
             * 이메일 주소
             */
            String email,

            /**
             * URL 주소
             */
            String url
    ) {
    }

    /**
     * API 라이센스 정보
     */
    public record License(
            /**
             * 라이센스 이름
             */
            String name,

            /**
             * 라이센스 URL
             */
            String url
    ) {
    }
}