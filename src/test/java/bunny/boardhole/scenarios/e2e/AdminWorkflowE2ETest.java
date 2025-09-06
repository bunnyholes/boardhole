package bunny.boardhole.scenarios.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;

import bunny.boardhole.testsupport.config.TestEmailConfig;
import bunny.boardhole.testsupport.config.TestSecurityOverrides;
import bunny.boardhole.testsupport.e2e.AuthSteps;
import bunny.boardhole.testsupport.e2e.E2ETestBase;
import bunny.boardhole.testsupport.e2e.SessionCookie;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@DisplayName("관리자 워크플로우 — 사용자 관리")
@Tag("e2e")
@Tag("scenario")
@Import({TestEmailConfig.class, TestSecurityOverrides.class})
class AdminWorkflowE2ETest extends E2ETestBase {

    @Value("${boardhole.default-users.admin.username}")
    String adminUsername;
    @Value("${boardhole.default-users.admin.password}")
    String adminPassword;

    @Test
    @DisplayName("관리자가 사용자 목록을 조회하고, 일반 사용자는 403")
    void adminListsUsers_andUserForbidden() {
        // admin 로그인 후 목록 조회 200
        SessionCookie admin = AuthSteps.login(adminUsername, adminPassword);
        given().cookie(admin.name(), admin.value()).when().get("users").then().statusCode(200).body("pageable.pageNumber", equalTo(0));

        // 일반 사용자 회원가입/로그인 후 목록 조회 403
        String uid = java.util.UUID.randomUUID().toString().substring(0, 8);
        String user = "normal_" + uid;
        String pass = "Passw0rd!";
        String email = user + "@example.com";
        AuthSteps.signup(user, pass, "Normal User", email);
        SessionCookie normal = AuthSteps.login(user, pass);
        given().cookie(normal.name(), normal.value()).when().get("users").then().statusCode(403);
    }
}
