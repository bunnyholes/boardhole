package bunny.boardhole.testsupport.integration;

import bunny.boardhole.testsupport.config.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestEmailConfig.class, TestSecurityOverrides.class})
public abstract class IntegrationTestBase {
}
