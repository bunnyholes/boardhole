package dev.xiyo.bunnyholes.boardhole.testsupport.jpa;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;

@DataJpaTest
@ActiveProfiles("test")
public abstract class EntityTestBase {

    protected static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    protected static final String TEST_PASSWORD = "Password123!";
    protected static final String TEST_NAME = "Test User";
    protected static final String TEST_EMAIL = "test@example.com";
    protected static final String TEST_BOARD_TITLE = "Test Board Title";
    protected static final String TEST_BOARD_CONTENT = "Test Board Content";
    // 테스트 데이터 상수
    private static final String TEST_USERNAME = "testuser";
    @PersistenceContext
    protected EntityManager entityManager;

    protected static String createUniqueEmail() {
        return UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    protected static String createUniqueUsername() {
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return TEST_USERNAME + randomSuffix;
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

    @BeforeEach
    void initializeMessageUtils() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        MessageUtils.setMessageSource(ms);
    }

    protected User createAndPersistUser() {
        User user = createTestUser();
        return persistAndFlush(user);
    }

    protected <T> T persistAndFlush(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

}
