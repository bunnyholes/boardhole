package bunny.boardhole.shared.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import bunny.boardhole.testsupport.integration.IntegrationTestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class ApplicationTestProfileLoadTest extends IntegrationTestBase {

    @Autowired
    Environment env;

    @Test
    @DisplayName("application-test.yml이 로드되어 기본 설정을 오버라이드한다")
    void shouldLoadApplicationTestYaml() {
        // JPA DDL 설정(테스트전용): create-drop
        assertEquals("create-drop", env.getProperty("spring.jpa.hibernate.ddl-auto"));

        // H2 인메모리 데이터베이스 URL(테스트전용)
        assertEquals("jdbc:h2:mem:testdb;MODE=MySQL", env.getProperty("spring.datasource.url"));

        // CORS allow-credentials: main=false, test=true
        Boolean allowCred = env.getProperty("boardhole.cors.allow-credentials", Boolean.class);
        assertNotNull(allowCred);
        assertTrue(allowCred);
    }

    @Test
    @DisplayName("application.yml 기본 속성도 함께 로드된다")
    void shouldLoadBaseApplicationYamlProperties() {
        // spring.mvc.problemdetails.enabled (base=true)
        assertEquals(Boolean.TRUE, env.getProperty("spring.mvc.problemdetails.enabled", Boolean.class));

        // boardhole.auth.default-role (base="USER")
        assertEquals("USER", env.getProperty("boardhole.auth.default-role"));

        // boardhole.validation.* (base 값들)
        assertEquals(200, env.getProperty("boardhole.validation.board.title-max-length", Integer.class));
        assertEquals(3, env.getProperty("boardhole.validation.user.username-min-length", Integer.class));

        // 기본 사용자(테스트에서 오버라이드 없음 → base 값 그대로)
        assertEquals("admin", env.getProperty("boardhole.default-users.admin.username"));
        assertEquals("user", env.getProperty("boardhole.default-users.regular.username"));
    }
}
