package bunny.boardhole.board.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import bunny.boardhole.testsupport.e2e.AuthSteps;
import bunny.boardhole.testsupport.e2e.BoardSteps;
import bunny.boardhole.testsupport.e2e.E2ETestBase;
import bunny.boardhole.testsupport.e2e.SessionCookie;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("게시판 E2E — 목록/검색/페이지네이션")
@Tag("e2e")
@Tag("board")

class BoardListE2ETest extends E2ETestBase {

    private String uid;
    private SessionCookie owner;

    @BeforeEach
    void setUpUser() {
        uid = java.util.UUID.randomUUID().toString().substring(0, 8);
        String u = "list_" + uid;
        String p = "Passw0rd!";
        String e = u + "@example.com";
        AuthSteps.signup(u, p, "Lister", e);
        owner = AuthSteps.login(u, p);
    }

    @Test
    @DisplayName("여러 게시글 작성 후 목록/페이지네이션/검색이 동작한다")
    void listAndSearch() {
        // Seed boards
        BoardSteps.create(owner, "Alpha " + uid, "First");
        BoardSteps.create(owner, "Beta  " + uid, "Second");
        BoardSteps.create(owner, "Gamma " + uid, "Third");

        // Public list (no auth)
        given().when().get("boards?page=0&size=2&sort=createdAt,desc").then().statusCode(200).body("content.size()", greaterThanOrEqualTo(1)).body("pageable.pageSize", notNullValue());

        // Search by unique token in title
        given().when().get("boards?search=" + uid).then().statusCode(200).body("content.title", hasItem(org.hamcrest.Matchers.containsString(uid)));
    }
}
