package bunny.boardhole.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestDataConfig {

    @Value("${boardhole.test-data.user.username}")
    private String testUsername;

    @Value("${boardhole.test-data.user.password}")
    private String testPassword;

    @Value("${boardhole.test-data.user.name}")
    private String testName;

    @Value("${boardhole.test-data.user.email}")
    private String testEmail;

    @Value("${boardhole.test-data.board.title}")
    private String testBoardTitle;

    @Value("${boardhole.test-data.board.content}")
    private String testBoardContent;

    @Value("${boardhole.test-data.email-verification.code}")
    private String testVerificationCode;

    @Bean
    public TestDataProperties testDataProperties() {
        return new TestDataProperties(
                testUsername, testPassword, testName, testEmail,
                testBoardTitle, testBoardContent,
                testVerificationCode
        );
    }


    public record TestDataProperties(
            String username,
            String password,
            String name,
            String email,
            String boardTitle,
            String boardContent,
            String verificationCode
    ) {
    }
}
