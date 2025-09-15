package bunny.boardhole.testsupport.jpa;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.testsupport.container.ContainersConfig;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(ContainersConfig.class)
public abstract class EntityTestBase {

    protected static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    protected static final String TEST_PASSWORD = "Password123!";
    protected static final String TEST_NAME = "Test User";
    protected static final String TEST_EMAIL = "test@example.com";
    protected static final String TEST_BOARD_TITLE = "Test Board Title";
    protected static final String TEST_BOARD_CONTENT = "Test Board Content";
    // 테스트 데이터 상수
    private static final String TEST_USERNAME = "testuser";
    @Autowired
    protected TestEntityManager entityManager;

    protected static String createUniqueEmail() {
        return UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    protected static String createUniqueUsername() {
        return TEST_USERNAME + "_" + UUID.randomUUID().toString().substring(0, 8);
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
        return entityManager.persistAndFlush(user);
    }

}
