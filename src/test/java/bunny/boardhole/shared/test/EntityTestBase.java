package bunny.boardhole.shared.test;

import bunny.boardhole.shared.config.TestDataConfig;
import bunny.boardhole.user.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestDataConfig.class)
public abstract class EntityTestBase {

    @Autowired
    protected TestEntityManager entityManager;

    @Autowired
    protected TestDataConfig.TestDataProperties testData;

    @Autowired
    protected MessageSource messageSource;

    protected String createUniqueUsername() {
        return testData.username() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected String createUniqueEmail() {
        return UUID.randomUUID().toString().substring(0, 8) + "@example.com";
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