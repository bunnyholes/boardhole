package bunny.boardhole.testsupport.jpa;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class EntityTestBase {

    // 싱글톤 컨테이너: JVM 당 1회 기동, 모든 테스트 공유
    private static final TestMySQL MYSQL = TestMySQL.INSTANCE;

    @DynamicPropertySource
    static void dataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @Autowired
    protected TestEntityManager entityManager;

    protected static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 테스트 데이터 상수
    private static final String TEST_USERNAME = "testuser";
    protected static final String TEST_PASSWORD = "Password123!";
    protected static final String TEST_NAME = "Test User";
    protected static final String TEST_EMAIL = "test@example.com";
    protected static final String TEST_BOARD_TITLE = "Test Board Title";
    protected static final String TEST_BOARD_CONTENT = "Test Board Content";

    protected static String createUniqueEmail() {
        return UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    @BeforeEach
    void initializeMessageUtils() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);
    }

    protected static String createUniqueUsername() {
        return TEST_USERNAME + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected static String createUniqueCode() {
        // Generate 6-character alphanumeric code to meet @ValidVerificationCode requirements (exactly 6 chars, A-Z0-9)
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        return uuid.substring(0, 6); // Take first 6 characters
    }

    protected static User createTestUser() {
        User user = User.builder()
                        .username(createUniqueUsername())
                        .password(passwordEncoder.encode(TEST_PASSWORD))
                        .name(TEST_NAME)
                        .email(createUniqueEmail())
                        .roles(Set.of(Role.USER))
                        .build();
        user.verifyEmail();
        return user;
    }

    protected User createAndPersistUser() {
        User user = createTestUser();
        return entityManager.persistAndFlush(user);
    }

}
