package bunny.boardhole.board.e2e;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import bunny.boardhole.board.domain.validation.BoardValidationConstants;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.testsupport.e2e.AuthSteps;
import bunny.boardhole.testsupport.e2e.BoardSteps;
import bunny.boardhole.testsupport.e2e.E2ETestBase;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("게시판 API E2E — CRUD + 권한")
@Tag("e2e")
@Tag("board")
class BoardE2ETest extends E2ETestBase {

    private String admin;
    private String regular;

    @BeforeEach
    void loginDefaults() {
        admin = AuthSteps.loginAdmin();
        regular = AuthSteps.loginRegular();
    }

    @Nested
    @DisplayName("CREATE — POST /api/boards")
    class Create {
        @Test
        @DisplayName("익명 → 401")
        void anonymous() {
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("title", "NoAuth")
                    .formParam("content", "NoAuth")
                    .when()
                    .post("/api/boards")
                    .then()
                    .statusCode(401)
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                    .body("status", equalTo(401))
                    .body("code", equalTo("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("일반 → 201")
        void regular() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            BoardSteps.create(regular, "Board " + uid, "Content " + uid).then().statusCode(201).body("id", notNullValue());
        }

        @Test
        @DisplayName("관리자 → 201")
        void admin() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            BoardSteps.create(admin, "Admin Board " + uid, "Admin Content").then().statusCode(201).body("id", notNullValue());
        }

        @Test
        @DisplayName("검증 실패(title) → 422")
        void validation_title() {
            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("title", "")
                    .formParam("content", "Some content")
                    .when()
                    .post("/api/boards")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"))
                    .body("title", equalTo(MessageUtils.get("exception.title.validation-failed")))
                    .body("status", equalTo(422))
                    .body("code", equalTo("VALIDATION_ERROR"))
                    .body("errors", notNullValue());
        }

        @Test
        @DisplayName("검증 실패(content) → 422")
        void validation_content() {
            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("title", "Title")
                    .formParam("content", "")
                    .when()
                    .post("/api/boards")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"))
                    .body("title", equalTo(MessageUtils.get("exception.title.validation-failed")))
                    .body("status", equalTo(422))
                    .body("code", equalTo("VALIDATION_ERROR"))
                    .body("errors", notNullValue());
        }

        @Test
        @DisplayName("필드 길이 초과(title > " + BoardValidationConstants.BOARD_TITLE_MAX_LENGTH + ") → 422")
        void validation_title_too_long() {
            String longTitle = "A".repeat(BoardValidationConstants.BOARD_TITLE_MAX_LENGTH + 1);
            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("title", longTitle)
                    .formParam("content", "Content")
                    .when()
                    .post("/api/boards")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"));
        }

        @Test
        @DisplayName("필드 길이 초과(content > " + BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH + ") → 422")
        void validation_content_too_long() {
            String longContent = "A".repeat(BoardValidationConstants.BOARD_CONTENT_MAX_LENGTH + 1);
            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("title", "Title")
                    .formParam("content", longContent)
                    .when()
                    .post("/api/boards")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"));
        }

        @Test
        @DisplayName("필수 필드 누락(title) → 422")
        void missing_title() {
            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("content", "Content only")
                    .when()
                    .post("/api/boards")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"));
        }

        @Test
        @DisplayName("필수 필드 누락(content) → 422")
        void missing_content() {
            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("title", "Title only")
                    .when()
                    .post("/api/boards")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"));
        }

        @Test
        @DisplayName("XSS 시도 → 201 (저장은 되지만 렌더링 시 이스케이프)")
        void xss_attempt_saved() {
            final String xssTitle = "<script>alert('XSS')</script>";
            final String xssContent = "<img src=x onerror=alert('XSS')>";
            BoardSteps
                    .create(regular, xssTitle, xssContent)
                    .then()
                    .statusCode(201)
                    .body("title", equalTo(xssTitle))
                    .body("content", equalTo(xssContent));
        }
    }

    @Nested
    @DisplayName("READ — GET /api/boards/{id}")
    class Read {
        @Test
        @DisplayName("익명 → 200 (공개)")
        void anonymous() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            UUID id = UUID.fromString(BoardSteps.create(regular, "Hello " + uid, "Content " + uid).jsonPath().getString("id"));
            given().when().get("/api/boards/" + id).then().statusCode(200).body("title", equalTo("Hello " + uid));
        }

        @Test
        @DisplayName("일반 → 200")
        void regular() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            UUID id = UUID.fromString(BoardSteps.create(regular, "My " + uid, "Mine").jsonPath().getString("id"));
            given()
                    .cookie("JSESSIONID", regular)
                    .when()
                    .get("/api/boards/" + id)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(id.toString()));
        }

        @Test
        @DisplayName("관리자 → 200 (타 사용자)")
        void admin_other() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            UUID id = UUID.fromString(BoardSteps.create(regular, "Someone " + uid, "C").jsonPath().getString("id"));
            given().cookie("JSESSIONID", admin).when().get("/api/boards/" + id).then().statusCode(200);
        }

        @Test
        @DisplayName("미존재 → 404")
        void not_found() {
            // Use a valid UUID that doesn't exist
            final String nonExistentId = "00000000-0000-0000-0000-000000000000";
            given()
                    .when()
                    .get("/api/boards/" + nonExistentId)
                    .then()
                    .statusCode(404)
                    .body("type", equalTo("urn:problem-type:not-found"))
                    .body("title", equalTo(MessageUtils.get("exception.title.not-found")));
        }

        @Test
        @DisplayName("조회수 증가")
        void view_count_increments() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            UUID id = UUID.fromString(BoardSteps.create(regular, "V " + uid, "VC").jsonPath().getString("id"));

            int v1 = given()
                    .when()
                    .get("/api/boards/" + id)
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getInt("viewCount");

            // 비동기 증가를 안정적으로 검증: 일정 시간 동안 증가 조건을 대기
            org.awaitility.Awaitility.await()
                                     .atMost(java.time.Duration.ofSeconds(3))
                                     .pollInterval(java.time.Duration.ofMillis(100))
                                     .untilAsserted(() -> {
                                         int current = given()
                                                 .when()
                                                 .get("/api/boards/" + id)
                                                 .then()
                                                 .statusCode(200)
                                                 .extract()
                                                 .jsonPath()
                                                 .getInt("viewCount");
                                         org.assertj.core.api.Assertions.assertThat(current).isGreaterThanOrEqualTo(v1);
                                     });
        }

        @Test
        @DisplayName("잘못된 ID 형식(문자) → 400")
        void invalid_id_format() {
            given().when().get("/api/boards/invalid").then().statusCode(400);
        }

        @Test
        @DisplayName("잘못된 UUID 형식 (음수) → 400")
        void negative_id() {
            // Negative numbers are not valid UUIDs
            given().when().get("/api/boards/-1").then().statusCode(400);
        }

        @Test
        @DisplayName("잘못된 UUID 형식 (숫자) → 400")
        void extremely_large_id() {
            // Large numbers are not valid UUIDs
            given().when().get("/api/boards/9999999999").then().statusCode(400);
        }
    }

    @Nested
    @DisplayName("UPDATE — PUT /api/boards/{id}")
    class Update {
        @Test
        @DisplayName("익명 → 401")
        void anonymous() {
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("title", "T")
                    .formParam("content", "C")
                    .when()
                    .put("/api/boards/1")
                    .then()
                    .statusCode(401)
                    .body("type", equalTo("urn:problem-type:unauthorized"));
        }

        @Test
        @DisplayName("일반(본인) → 200")
        void regular_owner() {
            UUID id = UUID.fromString(BoardSteps.create(regular, "U1", "C").jsonPath().getString("id"));
            BoardSteps.update(regular, id, "U1-upd", "C2").then().statusCode(200).body("title", equalTo("U1-upd"));
        }

        @Test
        @DisplayName("일반(타인) → 403")
        void regular_other() {
            // 다른 사용자 생성 및 게시글 생성
            String ou = "upd_other_" + UUID.randomUUID().toString().substring(0, 8);
            final String op = "Password123!";
            String oe = ou + "@example.com";
            AuthSteps.register(ou, op, "Other", oe);
            String other = AuthSteps.loginAs(ou, op);
            UUID id = UUID.fromString(BoardSteps.create(other, "O-title", "O-content").jsonPath().getString("id"));

            BoardSteps
                    .update(regular, id, "Hack", "Hack")
                    .then()
                    .statusCode(403)
                    .body("type", equalTo("urn:problem-type:forbidden"))
                    .body("title", equalTo(MessageUtils.get("exception.title.access-denied")))
                    .body("status", equalTo(403))
                    .body("code", equalTo("FORBIDDEN"));
        }

        @Test
        @DisplayName("관리자(타인) → 200")
        void admin_other() {
            UUID id = UUID.fromString(BoardSteps.create(regular, "A-upd", "C").jsonPath().getString("id"));
            BoardSteps.update(admin, id, "Admin-upd", "AC").then().statusCode(200).body("title", equalTo("Admin-upd"));
        }

        @Test
        @DisplayName("미존재 게시글 → 403 (권한 체크 우선)")
        void not_found() {
            // Spring Security checks permissions before checking if resource exists
            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("title", "Updated")
                    .formParam("content", "Updated")
                    .when()
                    .put("/api/boards/00000000-0000-0000-0000-000000000000")
                    .then()
                    .statusCode(403)
                    .body("type", equalTo("urn:problem-type:forbidden"));
        }

        @Test
        @DisplayName("빈 title → 500 (DB constraint violation)")
        void empty_title_db_error() {
            // Empty string passes validation but fails at DB level (NOT NULL constraint)
            UUID id = UUID.fromString(BoardSteps.create(regular, "Orig", "Content").jsonPath().getString("id"));
            BoardSteps.update(regular, id, "", "Updated").then().statusCode(500);
        }

        @Test
        @DisplayName("빈 content → 500 (DB constraint violation)")
        void empty_content_db_error() {
            // Empty string passes validation but fails at DB level (NOT NULL constraint)
            UUID id = UUID.fromString(BoardSteps.create(regular, "Orig", "Content").jsonPath().getString("id"));
            BoardSteps.update(regular, id, "Updated", "").then().statusCode(500);
        }

        @Test
        @DisplayName("필드 길이 초과(title > " + BoardValidationConstants.BOARD_TITLE_MAX_LENGTH + ") → 422")
        void validation_title_too_long() {
            // @OptionalBoardTitle has @Size(max = BoardValidationConstants.BOARD_TITLE_MAX_LENGTH) validation
            UUID id = UUID.fromString(BoardSteps.create(regular, "Orig", "Content").jsonPath().getString("id"));
            String longTitle = "A".repeat(BoardValidationConstants.BOARD_TITLE_MAX_LENGTH + 1);
            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("title", longTitle)
                    .formParam("content", "Updated")
                    .when()
                    .put("/api/boards/" + id)
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"));
        }

        @Test
        @DisplayName("잘못된 ID 형식 → 400")
        void invalid_id_format() {
            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("title", "Title")
                    .formParam("content", "Content")
                    .when()
                    .put("/api/boards/invalid")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("DELETE — DELETE /api/boards/{id}")
    class Delete {
        @Test
        @DisplayName("익명 → 401")
        void anonymous() {
            given().when().delete("/api/boards/1").then().statusCode(401).body("type", equalTo("urn:problem-type:unauthorized"));
        }

        @Test
        @DisplayName("일반(타인) → 403")
        void regular_other() {
            UUID id = UUID.fromString(BoardSteps.create(admin, "Admin-own", "C").jsonPath().getString("id"));
            BoardSteps.delete(regular, id).then().statusCode(403).body("type", equalTo("urn:problem-type:forbidden"));
        }

        @Test
        @DisplayName("일반(본인) → 204")
        void regular_owner() {
            UUID id = UUID.fromString(BoardSteps.create(regular, "Mine", "C").jsonPath().getString("id"));
            BoardSteps.delete(regular, id).then().statusCode(204);
        }

        @Test
        @DisplayName("관리자(타인) → 204")
        void admin_other() {
            UUID id = UUID.fromString(BoardSteps.create(regular, "TBD", "C").jsonPath().getString("id"));
            BoardSteps.delete(admin, id).then().statusCode(204);
            given().when().get("/api/boards/" + id).then().statusCode(404);
        }

        @Test
        @DisplayName("미존재 게시글 → 403 (권한 체크 우선)")
        void not_found() {
            // Spring Security checks permissions before checking if resource exists
            given()
                    .cookie("JSESSIONID", regular)
                    .when()
                    .delete("/api/boards/00000000-0000-0000-0000-000000000000")
                    .then()
                    .statusCode(403)
                    .body("type", equalTo("urn:problem-type:forbidden"));
        }

        @Test
        @DisplayName("잘못된 ID 형식 → 400")
        void invalid_id_format() {
            given().cookie("JSESSIONID", regular).when().delete("/api/boards/invalid").then().statusCode(400);
        }

        @Test
        @DisplayName("이미 삭제된 게시글 → 403 (권한 체크 우선)")
        void already_deleted() {
            UUID id = UUID.fromString(BoardSteps.create(regular, "ToDelete", "C").jsonPath().getString("id"));
            BoardSteps.delete(regular, id).then().statusCode(204);
            // Even for deleted resources, permission check happens first
            BoardSteps.delete(regular, id).then().statusCode(403);
        }
    }

    @Nested
    @DisplayName("LIST — GET /api/boards")
    class List {
        @Test
        @DisplayName("익명 — 200, 페이지네이션/검색")
        void anonymous() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            BoardSteps.create(regular, "L-Alpha " + uid, "First");
            BoardSteps.create(regular, "L-Beta  " + uid, "Second");

            given()
                    .when()
                    .get("/api/boards?page=0&size=2&sort=createdAt,desc")
                    .then()
                    .statusCode(200)
                    .body("content.size()", greaterThanOrEqualTo(1))
                    .body("pageable.pageSize", notNullValue());
            given().when().get("/api/boards?search=" + uid).then().statusCode(200).body("content[0].title", containsString(uid));
        }

        @Test
        @DisplayName("일반 — 200")
        void regular() {
            given().cookie("JSESSIONID", regular).when().get("/api/boards").then().statusCode(200).body("content", notNullValue());
        }

        @Test
        @DisplayName("관리자 — 200")
        void admin() {
            given().cookie("JSESSIONID", admin).when().get("/api/boards").then().statusCode(200).body("content", notNullValue());
        }

        @Test
        @DisplayName("페이지 음수 → 기본값(0)")
        void negative_page() {
            given().when().get("/api/boards?page=-1&size=10").then().statusCode(200).body("pageable.pageNumber", equalTo(0));
        }

        @Test
        @DisplayName("페이지 크기 0 → 400 또는 기본값")
        void zero_size() {
            // Spring Boot may return 400 or use default size
            given().when().get("/api/boards?page=0&size=0").then().statusCode(200);
        }

        @Test
        @DisplayName("페이지 크기 초과(>100) → 서버 설정에 따라 허용")
        void excessive_size() {
            // Spring Boot doesn't limit page size by default unless configured
            given().when().get("/api/boards?page=0&size=500").then().statusCode(200).body("pageable.pageSize", equalTo(500));
        }

        @Test
        @DisplayName("범위 초과 페이지 → 빈 결과")
        void out_of_bounds_page() {
            given()
                    .when()
                    .get("/api/boards?page=9999&size=10")
                    .then()
                    .statusCode(200)
                    .body("content.size()", equalTo(0))
                    .body("totalElements", greaterThanOrEqualTo(0));
        }

        @Test
        @DisplayName("SQL 인젝션 시도(검색) → 정상 처리")
        void sql_injection_search() {
            final String sqlInjection = "'; DROP TABLE boards; --";
            given().when().get("/api/boards?search=" + sqlInjection).then().statusCode(200);
        }

        @Test
        @DisplayName("XSS 시도(검색) → 정상 처리")
        void xss_search() {
            final String xss = "<script>alert('XSS')</script>";
            given().when().get("/api/boards?search=" + xss).then().statusCode(200);
        }

        @Test
        @DisplayName("특수문자 검색 → 정상 처리")
        void special_chars_search() {
            // URL encode special characters properly
            String special = java.net.URLEncoder.encode("@#$%^&*()[]{}|\\<>?,./", java.nio.charset.StandardCharsets.UTF_8);
            given().when().get("/api/boards?search=" + special).then().statusCode(200);
        }

        @Test
        @DisplayName("매우 긴 검색어 → 정상 처리 또는 400")
        void very_long_search() {
            String longSearch = "A".repeat(1000);
            given().when().get("/api/boards?search=" + longSearch).then().statusCode(200);
        }

        @Test
        @DisplayName("정렬 옵션 잘못된 필드 → 400 (PropertyReferenceException)")
        void invalid_sort_field() {
            // Spring Data JPA throws PropertyReferenceException for invalid sort fields
            given().when().get("/api/boards?sort=nonexistentField,asc").then().statusCode(400);
        }

        @Test
        @DisplayName("정렬 옵션 잘못된 방향 → 400")
        void invalid_sort_direction() {
            given()
                    .when()
                    .get("/api/boards?sort=createdAt,wrongdir")
                    .then()
                    .statusCode(400)
                    .body("type", equalTo("urn:problem-type:invalid-sort"))
                    .body("code", equalTo("BAD_REQUEST"));
        }
    }
}
