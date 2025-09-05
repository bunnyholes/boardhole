package bunny.boardhole.board.e2e;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import bunny.boardhole.testsupport.config.TestEmailConfig;
import bunny.boardhole.testsupport.config.TestSecurityOverrides;
import bunny.boardhole.testsupport.e2e.AuthSteps;
import bunny.boardhole.testsupport.e2e.BoardSteps;
import bunny.boardhole.testsupport.e2e.E2ETestBase;
import bunny.boardhole.testsupport.e2e.SessionCookie;

@DisplayName("게시판 E2E — 작성/조회/권한/삭제")
@Tag("e2e")
@Tag("board")
@Import({TestEmailConfig.class, TestSecurityOverrides.class})
public class BoardCrudE2ETest extends E2ETestBase {

    @Test
    @DisplayName("작성자 생성→게시글 작성→조회→타 사용자 수정 불가→작성자 삭제")
    void boardCrudWithOwnership() {
        String uid = java.util.UUID.randomUUID().toString().substring(0, 8);
        String ownerU = "owner_" + uid;
        String ownerP = "Passw0rd!";
        String ownerE = ownerU + "@example.com";
        AuthSteps.signup(ownerU, ownerP, "Owner", ownerE);
        SessionCookie owner = AuthSteps.login(ownerU, ownerP);

        // Create board
        var createRes = BoardSteps.create(owner, "Hello " + uid, "Content " + uid);
        createRes.then().statusCode(201).body("id", notNullValue());
        long id = createRes.jsonPath().getLong("id");

        // Get board
        var getRes = BoardSteps.get(owner, id);
        getRes.then().statusCode(200).body("title", equalTo("Hello " + uid));

        // Owner update success
        var upd = BoardSteps.update(owner, id, "Updated " + uid, "Updated content");
        upd.then().statusCode(200).body("title", equalTo("Updated " + uid));

        // Another user cannot update
        String otherU = "other_" + uid;
        String otherP = "Passw0rd!";
        String otherE = otherU + "@example.com";
        AuthSteps.signup(otherU, otherP, "Other", otherE);
        SessionCookie other = AuthSteps.login(otherU, otherP);
        BoardSteps.update(other, id, "Hacked", "Hacked").then().statusCode(403);

        // Owner deletes
        BoardSteps.delete(owner, id).then().statusCode(204);
        BoardSteps.get(owner, id).then().statusCode(404);
    }
}
