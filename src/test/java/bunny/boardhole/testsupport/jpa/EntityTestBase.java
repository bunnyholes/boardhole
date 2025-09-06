package bunny.boardhole.testsupport.jpa;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import bunny.boardhole.shared.config.TestDataConfig;
import bunny.boardhole.shared.config.TestJpaConfig;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestDataConfig.class, TestJpaConfig.class})
public abstract class EntityTestBase {

    @Autowired
    protected TestEntityManager entityManager;

    @Autowired
    protected TestDataConfig.TestDataProperties testData;

    @Value("${boardhole.validation.email-verification.expiration-ms}")
    private long validationExpirationMs;

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

    protected String createUniqueUsername() {
        return testData.username() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected String createUniqueCode() {
        return testData.verificationCode() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    protected User createTestUser() {
        return User.builder().username(createUniqueUsername()).password(testData.password()).name(testData.name()).email(createUniqueEmail()).roles(Set.of(Role.USER)).build();
    }

    protected User createAndPersistUser() {
        User user = createTestUser();
        return entityManager.persistAndFlush(user);
    }

    protected LocalDateTime getTestExpirationTime() {
        return LocalDateTime.now(ZoneId.systemDefault()).plus(java.time.Duration.ofMillis(validationExpirationMs));
    }
}
