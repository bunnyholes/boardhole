package bunny.boardhole.scenarios.e2e;

import bunny.boardhole.testsupport.config.*;
import bunny.boardhole.testsupport.e2e.E2ETestBase;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("사용자 가입→로그인→내 정보 시나리오")
@Tag("e2e")
@Tag("scenario")
@Import({TestEmailConfig.class, TestSecurityOverrides.class})
public class UserJourneyE2ETest extends E2ETestBase {

    @Test
    @DisplayName("신규 유저 가입 후 로그인하고 /users/me 확인")
    void userSignupLoginAndMe() {
        String uid = java.util.UUID.randomUUID().toString().substring(0, 8);
        String username = "scenario_" + uid;
        String password = "Passw0rd!";
        String email = "scenario_" + uid + "@example.com";

        Map<String, String> signup = new HashMap<>();
        signup.put("username", username);
        signup.put("password", password);
        signup.put("name", "Scenario User");
        signup.put("email", email);

        given()
                .contentType(ContentType.URLENC)
                .formParams(signup)
                .when()
                .post("auth/signup")
                .then()
                .statusCode(anyOf(is(204), is(409))); // idempotent

        var loginRes = given()
                .contentType(ContentType.URLENC)
                .formParams(Map.of("username", username, "password", password))
                .when()
                .post("auth/login")
                .then()
                .statusCode(204)
                .extract().response();

        String cookieName = loginRes.getCookie("SESSION") != null ? "SESSION" : "JSESSIONID";
        String session = loginRes.getCookie(cookieName);

        given()
                .cookie(cookieName, session)
                .when()
                .get("users/me")
                .then()
                .statusCode(200)
                .body("username", equalTo(username))
                .body("email", equalTo(email));
    }
}
