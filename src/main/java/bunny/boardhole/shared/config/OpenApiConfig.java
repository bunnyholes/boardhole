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

    private String title;
    private String version;
    private String description;
    private String termsOfService;
    private Contact contact = new Contact();
    private License license = new License();
    private Security security = new Security();

    @Data
    public static class Contact {
        private String name;
        private String email;
        private String url;
    }

    @Data
    public static class License {
        private String name;
        private String url;
    }

    @Data
    public static class Security {
        private Session session = new Session();

        @Data
        public static class Session {
            private String name;
            private String description;
        }
    }
}