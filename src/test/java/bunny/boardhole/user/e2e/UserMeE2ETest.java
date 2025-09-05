package bunny.boardhole.user.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import bunny.boardhole.testsupport.config.TestEmailConfig;
import bunny.boardhole.testsupport.config.TestSecurityOverrides;
import bunny.boardhole.testsupport.e2e.AuthSteps;
import bunny.boardhole.testsupport.e2e.E2ETestBase;
import bunny.boardhole.testsupport.e2e.SessionCookie;

@DisplayName("사용자 E2E — /users/me 인증/미인증")
@Tag("e2e")
@Tag("user")
@Import({TestEmailConfig.class, TestSecurityOverrides.class})
public class UserMeE2ETest extends E2ETestBase {

    @Test
    @DisplayName("미인증은 401 ProblemDetails")
    void me_Unauthorized() {
        given()
        .when()
            .get("users/me")
        .then()
            .statusCode(401)
            .body("type", equalTo("urn:problem-type:unauthorized"));
    }

    @Test
    @DisplayName("인증 시 사용자 정보 반환")
    void me_Authorized() {
        String uid = java.util.UUID.randomUUID().toString().substring(0, 8);
        String u = "me_" + uid;
        String p = "Passw0rd!";
        String e = u + "@example.com";
        AuthSteps.signup(u, p, "Me User", e);
        SessionCookie sc = AuthSteps.login(u, p);

        given()
            .cookie(sc.name(), sc.value())
        .when()
            .get("users/me")
        .then()
            .statusCode(200)
            .body("username", equalTo(u))
            .body("email", equalTo(e));
    }
}

