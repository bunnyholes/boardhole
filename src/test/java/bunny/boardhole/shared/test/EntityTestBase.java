package bunny.boardhole.shared.test;

import bunny.boardhole.shared.config.*;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({TestDataConfig.class, TestJpaConfig.class})
public abstract class EntityTestBase {

    @Autowired
    protected TestEntityManager entityManager;

    @Autowired
    protected TestDataConfig.TestDataProperties testData;

    @Autowired
    protected MessageSource messageSource;

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

    protected String getMessage(String key) {
        return messageSource.getMessage(key, null, Locale.ENGLISH);
    }

    protected String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, Locale.ENGLISH);
    }

    protected User createTestUser() {
        return User.builder()
                .username(createUniqueUsername())
                .password(testData.password())
                .name(testData.name())
                .email(createUniqueEmail())
                .roles(Set.of(Role.USER))
                .build();
    }

    protected User createAndPersistUser() {
        User user = createTestUser();
        return entityManager.persistAndFlush(user);
    }

    protected LocalDateTime getTestExpirationTime() {
        return LocalDateTime.now(ZoneId.systemDefault()).plusHours(testData.expirationHours());
    }
}