package bunny.boardhole.testsupport.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

import bunny.boardhole.testsupport.config.TestEmailConfig;
import bunny.boardhole.testsupport.config.TestSecurityOverrides;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestEmailConfig.class, TestSecurityOverrides.class})
public abstract class IntegrationTestBase {
}
