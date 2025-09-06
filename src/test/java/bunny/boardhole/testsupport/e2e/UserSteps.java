package bunny.boardhole.testsupport.e2e;

import io.restassured.response.Response;

final class UserSteps {
    private UserSteps() {
    }

    public static Response me(SessionCookie session) {
        return RestSpecs.auth(session)
                .when()
                .get("users/me")
                .then()
                .extract().response();
    }
}
