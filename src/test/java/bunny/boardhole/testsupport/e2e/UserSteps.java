package bunny.boardhole.testsupport.e2e;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;

public final class UserSteps {
    private UserSteps() {}

    public static Response me(SessionCookie session) {
        return RestSpecs.auth(session)
            .when()
                .get("users/me")
            .then()
                .extract().response();
    }
}
