package bunny.boardhole.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestUserConfig {

    @Value("${boardhole.default-users.admin.username}")
    private String adminUsername;

    @Value("${boardhole.default-users.admin.password}")
    private String adminPassword;

    @Value("${boardhole.default-users.admin.email}")
    private String adminEmail;

    @Value("${boardhole.default-users.regular.username}")
    private String regularUsername;

    @Value("${boardhole.default-users.regular.password}")
    private String regularPassword;

    @Value("${boardhole.default-users.regular.email}")
    private String regularEmail;

    @Bean
    public TestUserProperties testUserProperties() {
        return new TestUserProperties(
                adminUsername, adminPassword, adminEmail,
                regularUsername, regularPassword, regularEmail
        );
    }

    public record TestUserProperties(String adminUsername, String adminPassword, String adminEmail,
                                     String regularUsername, String regularPassword, String regularEmail) {
    }
}