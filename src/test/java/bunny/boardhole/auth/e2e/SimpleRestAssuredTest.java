package bunny.boardhole.auth.e2e;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import bunny.boardhole.testsupport.e2e.E2ETestBase;

import static io.restassured.RestAssured.given;

@Tag("e2e")
class SimpleRestAssuredTest extends E2ETestBase {

    @Test
    void testHealthEndpoint() {
        given().when().get("auth/public-access").then().statusCode(204); // No Content
    }
}
