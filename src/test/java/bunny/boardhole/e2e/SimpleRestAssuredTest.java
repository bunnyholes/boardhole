package bunny.boardhole.e2e;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SimpleRestAssuredTest {

    @LocalServerPort
    private int port;

    @Test
    public void testHealthEndpoint() {
        RestAssured.port = port;

        given()
        .when()
            .get("/api/auth/public-access")
        .then()
            .statusCode(204); // No Content
    }
}
