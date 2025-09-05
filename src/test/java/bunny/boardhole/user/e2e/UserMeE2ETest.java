package bunny.boardhole.user.e2e;

import bunny.boardhole.testsupport.config.*;
import bunny.boardhole.testsupport.e2e.*;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

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

