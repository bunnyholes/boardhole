package bunny.boardhole.testsupport.e2e;

import io.restassured.http.ContentType;

import java.util.Map;

import static org.hamcrest.Matchers.*;

public final class AuthSteps {

    private AuthSteps() {
    }

    public static void signup(String username, String password, String name, String email) {
        RestSpecs.anon()
                .contentType(ContentType.URLENC)
                .formParams(Map.of(
                        "username", username,
                        "password", password,
                        "name", name,
                        "email", email
                ))
                .when()
                .post("auth/signup")
                .then()
                .statusCode(anyOf(is(204), is(409)));
    }

    public static SessionCookie login(String username, String password) {
        var res = RestSpecs.anon()
                .contentType(ContentType.URLENC)
                .formParams(Map.of(
                        "username", username,
                        "password", password
                ))
                .when()
                .post("auth/login")
                .then()
                .statusCode(204)
                .extract().response();

        String cookieName = res.getCookie("SESSION") != null ? "SESSION" : "JSESSIONID";
        String cookieValue = res.getCookie(cookieName);
        return new SessionCookie(cookieName, cookieValue);
    }

    public static void logout(SessionCookie session) {
        RestSpecs.auth(session)
                .when()
                .post("auth/logout")
                .then()
                .statusCode(anyOf(is(204), is(401)));
    }
}
