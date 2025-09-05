package bunny.boardhole.auth.e2e;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import io.restassured.RestAssured;
import bunny.boardhole.testsupport.e2e.E2ETestBase;
@Tag("e2e")
public class SimpleRestAssuredTest extends E2ETestBase {

    @Test
    public void testHealthEndpoint() {
        given()
        .when()
            .get("auth/public-access")
        .then()
            .statusCode(204); // No Content
    }
}
