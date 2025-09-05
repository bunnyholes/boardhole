package bunny.boardhole.testsupport.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class E2ETestBase {

    @LocalServerPort
    protected int port;

    @BeforeAll
    void restAssuredSetup() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
