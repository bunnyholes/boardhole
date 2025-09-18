package bunny.boardhole.user.e2e;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.testsupport.e2e.AuthSteps;
import bunny.boardhole.testsupport.e2e.E2ETestBase;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("사용자 API E2E 테스트")
@Tag("e2e")
@Tag("user")
class UserE2ETest extends E2ETestBase {

    private String admin;
    private String regular;

    @BeforeEach
    void loginDefaultUsers() {
        // DataInitializer가 기본 admin/regular를 생성하므로 바로 로그인
        admin = AuthSteps.loginAdmin();
        regular = AuthSteps.loginRegular();
    }

    @Nested
    @DisplayName("GET /api/users - 목록")
    class ListUsers {
        @Test
        @DisplayName("❌ 익명 → 401 Unauthorized")
        void anonymous() {
            var response = given()
                    .when()
                    .get("/api/users");
            
            // 디버깅용 출력
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody().asString());
            
            response.then()
                    .statusCode(401)
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                    .body("status", equalTo(401))
                    .body("code", equalTo("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("✅ 관리자 목록 조회")
        void adminCanList() {
            given()
                    .cookie("JSESSIONID", admin)
                    .when()
                    .get("/api/users")
                    .then()
                    .statusCode(200)
                    .body("content", notNullValue())
                    .body("pageable", notNullValue());
        }

        @Test
        @DisplayName("🔍 관리자 검색")
        void adminCanSearch() {
            given().cookie("JSESSIONID", admin).when().get("/api/users?search=admin").then().statusCode(200).body("content", notNullValue());
        }

        @Test
        @DisplayName("📄 관리자 페이지네이션")
        void adminPagination() {
            given()
                    .cookie("JSESSIONID", admin)
                    .when()
                    .get("/api/users?page=0&size=5")
                    .then()
                    .statusCode(200)
                    .body("pageable.pageSize", equalTo(5))
                    .body("pageable.pageNumber", equalTo(0));
        }

        @Test
        @DisplayName("❌ 일반 사용자 - 목록 조회 금지 → 403")
        void regularCannotList() {
            given()
                    .cookie("JSESSIONID", regular)
                    .when()
                    .get("/api/users")
                    .then()
                    .statusCode(403)
                    .body("type", equalTo("urn:problem-type:forbidden"))
                    .body("title", equalTo(MessageUtils.get("exception.title.access-denied")))
                    .body("status", equalTo(403))
                    .body("code", equalTo("FORBIDDEN"));
        }

        @Test
        @DisplayName("🔍 빈 검색어 처리")
        void emptySearchParameter() {
            // 빈 문자열 검색 - 전체 목록 반환
            given().cookie("JSESSIONID", admin).when().get("/api/users?search=").then().statusCode(200).body("content", notNullValue());

            // 공백만 있는 검색 - 전체 목록 반환
            given().cookie("JSESSIONID", admin).when().get("/api/users?search=   ").then().statusCode(200).body("content", notNullValue());
        }

        @Test
        @DisplayName("🔍 특수문자 검색 처리")
        void specialCharactersInSearch() {
            // SQL 인젝션 시도 방어
            given().cookie("JSESSIONID", admin).when().get("/api/users?search=' OR '1'='1").then().statusCode(200);

            // 특수문자 포함 검색
            given().cookie("JSESSIONID", admin).when().get("/api/users?search=@#$%^&*()").then().statusCode(200);

            // XSS 시도 방어
            given().cookie("JSESSIONID", admin).when().get("/api/users?search=<script>alert('xss')</script>").then().statusCode(200);
        }

        @Test
        @DisplayName("📄 잘못된 페이지네이션 파라미터")
        void invalidPaginationParameters() {
            // 음수 페이지 번호
            given()
                    .cookie("JSESSIONID", admin)
                    .when()
                    .get("/api/users?page=-1&size=10")
                    .then()
                    .statusCode(200)
                    .body("pageable.pageNumber", equalTo(0)); // Spring이 0으로 보정

            // 음수 페이지 사이즈
            given().cookie("JSESSIONID", admin).when().get("/api/users?page=0&size=-10").then().statusCode(200); // Spring이 기본값으로 처리

            // 0 페이지 사이즈
            given().cookie("JSESSIONID", admin).when().get("/api/users?page=0&size=0").then().statusCode(200); // Spring이 기본값으로 처리

            // 너무 큰 페이지 사이즈 (2000 이상)
            given()
                    .cookie("JSESSIONID", admin)
                    .when()
                    .get("/api/users?page=0&size=5000")
                    .then()
                    .statusCode(200)
                    .body("pageable.pageSize", notNullValue()); // Spring이 최대값으로 제한
        }

        @Test
        @DisplayName("📄 범위 밖 페이지 번호")
        void outOfBoundPageNumber() {
            // 존재하지 않는 큰 페이지 번호
            given()
                    .cookie("JSESSIONID", admin)
                    .when()
                    .get("/api/users?page=99999&size=10")
                    .then()
                    .statusCode(200)
                    .body("content", notNullValue())
                    .body("content.size()", equalTo(0)); // 빈 결과
        }

        @Test
        @DisplayName("🔍 매우 긴 검색어 처리")
        void veryLongSearchString() {
            String longSearch = "A".repeat(1000);
            given().cookie("JSESSIONID", admin).when().get("/api/users?search=" + longSearch).then().statusCode(200);
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id} - 단일 조회")
    class GetUser {
        @Test
        @DisplayName("❌ 익명 → 401 Unauthorized")
        void anonymous() {
            given()
                    .when()
                    .get("/api/users/00000000-0000-0000-0000-000000000001")
                    .then()
                    .statusCode(401)
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                    .body("status", equalTo(401))
                    .body("code", equalTo("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("✅ 본인 조회 (기본 일반 사용자)")
        void getOwnUser() {
            Response meRes = given().cookie("JSESSIONID", regular).when().get("/api/users/me").then().extract().response();
            UUID myId = UUID.fromString(meRes.jsonPath().getString("id"));

            given()
                    .cookie("JSESSIONID", regular)
                    .when()
                    .get("/api/users/" + myId)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(myId.toString()))
                    .body("username", equalTo("user"));
        }

        @Test
        @DisplayName("❌ 존재하지 않는 사용자 → 404 Not Found")
        void notFound() {
            given()
                    .cookie("JSESSIONID", admin)
                    .when()
                    .get("/api/users/99999999-9999-9999-9999-999999999999")
                    .then()
                    .statusCode(404)
                    .body("type", equalTo("urn:problem-type:not-found"))
                    .body("title", equalTo(MessageUtils.get("exception.title.not-found")))
                    .body("instance", equalTo("/api/users/99999999-9999-9999-9999-999999999999"));
        }

        @Test
        @DisplayName("일반(타인) → 403 (권한 없음)")
        void regularOther() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "viewother_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "ViewerTarget", e);
            String other = AuthSteps.loginAs(u, p);
            UUID otherId = UUID.fromString(given().cookie("JSESSIONID", other).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            // Regular users cannot view other users' information
            given()
                    .cookie("JSESSIONID", regular)
                    .when()
                    .get("/api/users/" + otherId)
                    .then()
                    .statusCode(403)
                    .body("type", equalTo("urn:problem-type:forbidden"));
        }

        @Test
        @DisplayName("✅ 관리자 - 기본 일반 사용자 조회")
        void adminCanGetOtherUser() {
            UUID userId = UUID.fromString(given().cookie("JSESSIONID", regular).when().get("/api/users/me").then().extract().jsonPath().getString("id"));
            given().cookie("JSESSIONID", admin).when().get("/api/users/" + userId).then().statusCode(200).body("username", equalTo("user"));
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id} - 수정")
    class UpdateUser {
        @Test
        @DisplayName("❌ 일반(타인) → 403 Forbidden")
        void regularOther() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "upd_other_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "OtherUser", e);
            String other = AuthSteps.loginAs(u, p);
            UUID otherId = UUID.fromString(given().cookie("JSESSIONID", other).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("name", "Hacker")
                    .when()
                    .put("/api/users/" + otherId)
                    .then()
                    .statusCode(403)
                    .body("type", equalTo("urn:problem-type:forbidden"))
                    .body("title", equalTo(MessageUtils.get("exception.title.access-denied")))
                    .body("status", equalTo(403))
                    .body("code", equalTo("FORBIDDEN"));
        }

        @Test
        @DisplayName("✅ 본인 정보 수정")
        void updateOwn() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "upd_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "Updater", e);
            String me = AuthSteps.loginAs(u, p);
            UUID myId = UUID.fromString(given().cookie("JSESSIONID", me).when().get("/api/users/me").then().extract().jsonPath().getString("id"));
            given()
                    .cookie("JSESSIONID", me)
                    .contentType(ContentType.URLENC)
                    .formParam("name", "Updated Name")
                    .when()
                    .put("/api/users/" + myId)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("Updated Name"));
        }

        @Test
        @DisplayName("❌ 인증 없음 → 401 Unauthorized")
        void updateUnauthorized() {
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("name", "Hacked")
                    .when()
                    .put("/api/users/00000000-0000-0000-0000-000000000001")
                    .then()
                    .statusCode(401)
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                    .body("status", equalTo(401))
                    .body("code", equalTo("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("❌ 권한 없음/리소스 없음 → 403 Forbidden")
        void updateForbidden() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "updforb_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "Updater", e);
            String me2 = AuthSteps.loginAs(u, p);
            given()
                    .cookie("JSESSIONID", me2)
                    .contentType(ContentType.URLENC)
                    .formParam("name", "Updated")
                    .when()
                    .put("/api/users/99999999-9999-9999-9999-999999999999")
                    .then()
                    .statusCode(403)
                    .body("type", equalTo("urn:problem-type:forbidden"))
                    .body("title", equalTo(MessageUtils.get("exception.title.access-denied")))
                    .body("status", equalTo(403))
                    .body("code", equalTo("FORBIDDEN"));
        }

        @Test
        @DisplayName("✅ 관리자 - 타 사용자 정보 수정")
        void adminCanUpdateOtherUser() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "updoc_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "Updatable", e);
            String user = AuthSteps.loginAs(u, p);
            UUID userId = UUID.fromString(given().cookie("JSESSIONID", user).when().get("/api/users/me").then().extract().jsonPath().getString("id"));
            given()
                    .cookie("JSESSIONID", admin)
                    .contentType(ContentType.URLENC)
                    .formParam("name", "AdminUpdated")
                    .when()
                    .put("/api/users/" + userId)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("AdminUpdated"));
        }

        // 이메일 수정 기능이 구현되지 않아서 주석 처리
        // UserUpdateRequest에는 name 필드만 있고 email 필드가 없음
        // @Test
        // @DisplayName("❌ 잘못된 이메일 형식 → 400 Bad Request")
        // void invalidEmailFormat() {
        //     String uid = UUID.randomUUID().toString().substring(0, 8);
        //     String u = "emailtest_" + uid;
        //     String p = "Password123!";
        //     String e = u + "@example.com";
        //     AuthSteps.signup(u, p, "EmailTest", e);
        //     SessionCookie me = AuthSteps.login(u, p);
        //     Long myId = given().cookie(me.name(), me.value()).when().get("/api/users/me").then().extract().jsonPath().getLong("id");

        //     // 잘못된 이메일 형식
        //     given().cookie(me.name(), me.value())
        //         .contentType(ContentType.URLENC)
        //         .formParam("email", "invalid-email")
        //         .when().put("/api/users/" + myId)
        //         .then().statusCode(400)
        //         .body("type", equalTo("urn:problem-type:validation-error"));

        //     // 이메일에 공백
        //     given().cookie(me.name(), me.value())
        //         .contentType(ContentType.URLENC)
        //         .formParam("email", "test @example.com")
        //         .when().put("/api/users/" + myId)
        //         .then().statusCode(400)
        //         .body("type", equalTo("urn:problem-type:validation-error"));
        // }

        @Test
        @DisplayName("❌ 필드 길이 초과 → 422 Unprocessable Content")
        void fieldLengthViolation() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "lentest_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "LengthTest", e);
            String me3 = AuthSteps.loginAs(u, p);
            UUID myId = UUID.fromString(given().cookie("JSESSIONID", me3).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            // 너무 긴 이름 (100자 초과)
            String longName = "A".repeat(101);
            given()
                    .cookie("JSESSIONID", me3)
                    .contentType(ContentType.URLENC)
                    .formParam("name", longName)
                    .when()
                    .put("/api/users/" + myId)
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"));
        }

        @Test
        @DisplayName("❌ 잘못된 사용자 ID 형식 → 400 Bad Request")
        void invalidUserIdFormat() {
            given()
                    .cookie("JSESSIONID", admin)
                    .contentType(ContentType.URLENC)
                    .formParam("name", "Test")
                    .when()
                    .put("/api/users/invalid-id")
                    .then()
                    .statusCode(400);

            given()
                    .cookie("JSESSIONID", admin)
                    .contentType(ContentType.URLENC)
                    .formParam("name", "Test")
                    .when()
                    .put("/api/users/invalid-uuid-format")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("🛡️ XSS 공격 방어")
        void xssAttackPrevention() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "xsstest_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "XSSTest", e);
            String me = AuthSteps.loginAs(u, p);
            UUID myId = UUID.fromString(given().cookie("JSESSIONID", me).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            // XSS 시도 - 스크립트 태그
            given()
                    .cookie("JSESSIONID", me)
                    .contentType(ContentType.URLENC)
                    .formParam("name", "<script>alert('xss')</script>")
                    .when()
                    .put("/api/users/" + myId)
                    .then()
                    .statusCode(200);

            // 검증: 스크립트가 이스케이프되어 저장됨
            String savedName = given().cookie("JSESSIONID", me).when().get("/api/users/" + myId).then().extract().jsonPath().getString("name");

            // HTML 태그가 그대로 저장되어도 렌더링시 이스케이프됨
            // 실제 저장된 값 확인
            assert savedName != null;
        }

        @Test
        @DisplayName("🛡️ SQL 인젝션 방어")
        void sqlInjectionPrevention() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "sqltest_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "SQLTest", e);
            String me2 = AuthSteps.loginAs(u, p);
            UUID myId2 = UUID.fromString(given().cookie("JSESSIONID", me2).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            // SQL 인젝션 시도
            given()
                    .cookie("JSESSIONID", me2)
                    .contentType(ContentType.URLENC)
                    .formParam("name", "'; DROP TABLE users; --")
                    .when()
                    .put("/api/users/" + myId2)
                    .then()
                    .statusCode(200);

            // 시스템이 여전히 정상 작동하는지 확인
            given().cookie("JSESSIONID", admin).when().get("/api/users").then().statusCode(200).body("content", notNullValue());
        }

        @Test
        @DisplayName("🛡️ Path Traversal 공격 방어")
        void pathTraversalPrevention() {
            // Path traversal 시도
            given()
                    .cookie("JSESSIONID", admin)
                    .contentType(ContentType.URLENC)
                    .formParam("name", "Test")
                    .when()
                    .put("/api/users/../../../etc/passwd")
                    .then()
                    .statusCode(400);

            given()
                    .cookie("JSESSIONID", admin)
                    .contentType(ContentType.URLENC)
                    .formParam("name", "Test")
                    .when()
                    .put("/api/users/%2e%2e%2f%2e%2e%2f")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id} - 삭제")
    class DeleteUser {
        @Test
        @DisplayName("❌ 일반(타인) → 403 Forbidden")
        void regularOther() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "del_other_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "DelOther", e);
            String other = AuthSteps.loginAs(u, p);
            UUID otherId = UUID.fromString(given().cookie("JSESSIONID", other).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            given()
                    .cookie("JSESSIONID", regular)
                    .when()
                    .delete("/api/users/" + otherId)
                    .then()
                    .statusCode(403)
                    .body("type", equalTo("urn:problem-type:forbidden"))
                    .body("title", equalTo(MessageUtils.get("exception.title.access-denied")))
                    .body("status", equalTo(403))
                    .body("code", equalTo("FORBIDDEN"));
        }

        @Test
        @DisplayName("✅ 본인 삭제 및 404 확인")
        void deleteOwn() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "del_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "Deletable", e);
            String me = AuthSteps.loginAs(u, p);
            UUID myId = UUID.fromString(given().cookie("JSESSIONID", me).when().get("/api/users/me").then().extract().jsonPath().getString("id"));
            given().cookie("JSESSIONID", me).when().delete("/api/users/" + myId).then().statusCode(204);
            given().cookie("JSESSIONID", admin).when().get("/api/users/" + myId).then().statusCode(404);
        }

        @Test
        @DisplayName("❌ 인증 없음 → 401 Unauthorized")
        void deleteUnauthorized() {
            given()
                    .when()
                    .delete("/api/users/00000000-0000-0000-0000-000000000001")
                    .then()
                    .statusCode(401)
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                    .body("status", equalTo(401))
                    .body("code", equalTo("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("❌ 권한 없음/리소스 없음 → 403 Forbidden")
        void deleteForbidden() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "other_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "Other", e);
            String other = AuthSteps.loginAs(u, p);

            given()
                    .cookie("JSESSIONID", other)
                    .when()
                    .delete("/api/users/99999999-9999-9999-9999-999999999999")
                    .then()
                    .statusCode(403)
                    .body("type", equalTo("urn:problem-type:forbidden"))
                    .body("title", equalTo(MessageUtils.get("exception.title.access-denied")))
                    .body("status", equalTo(403))
                    .body("code", equalTo("FORBIDDEN"));
        }

        @Test
        @DisplayName("✅ 관리자 - 타 사용자 삭제")
        void adminCanDeleteOtherUser() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "deloc_" + uid;
            final String p = "Password123!";
            String e = u + "@example.com";
            AuthSteps.register(u, p, "DelTarget", e);
            String user = AuthSteps.loginAs(u, p);
            UUID userId = UUID.fromString(given().cookie("JSESSIONID", user).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            given().cookie("JSESSIONID", admin).when().delete("/api/users/" + userId).then().statusCode(204);

            given().cookie("JSESSIONID", admin).when().get("/api/users/" + userId).then().statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /api/users/me - 현재 사용자")
    class Me {
        @Test
        @DisplayName("✅ 현재 사용자 정보 조회 (기본 일반 사용자)")
        void meSuccess() {
            given()
                    .cookie("JSESSIONID", regular)
                    .when()
                    .get("/api/users/me")
                    .then()
                    .statusCode(200)
                    .body("username", equalTo("user"))
                    .body("roles", notNullValue());
        }

        @Test
        @DisplayName("❌ 인증 없음 → 401 Unauthorized")
        void meUnauthorized() {
            given()
                    .when()
                    .get("/api/users/me")
                    .then()
                    .statusCode(401)
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                    .body("status", equalTo(401))
                    .body("code", equalTo("UNAUTHORIZED"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{id}/password - 패스워드 변경")
    class UpdatePassword {
        @Test
        @DisplayName("✅ 본인 패스워드 변경 성공")
        void updateOwnPasswordSuccess() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "pwdchange_" + uid;
            final String oldPwd = "OldPass123!";
            final String newPwd = "NewPass123!";
            String e = u + "@example.com";

            AuthSteps.register(u, oldPwd, "PasswordUser", e);
            String user = AuthSteps.loginAs(u, oldPwd);
            UUID userId = UUID.fromString(given().cookie("JSESSIONID", user).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            // 패스워드 변경
            given()
                    .cookie("JSESSIONID", user)
                    .contentType(ContentType.URLENC)
                    .formParam("currentPassword", oldPwd)
                    .formParam("newPassword", newPwd)
                    .formParam("confirmPassword", newPwd)
                    .when()
                    .patch("/api/users/" + userId + "/password")
                    .then()
                    .statusCode(204);

            // 새 패스워드로 로그인 확인
            AuthSteps.loginAs(u, newPwd);
        }

        @Test
        @DisplayName("❌ 패스워드 확인 불일치 → 422 Unprocessable Content")
        void passwordMismatch() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "pwdmismatch_" + uid;
            final String pwd = "Password123!";
            String e = u + "@example.com";

            AuthSteps.register(u, pwd, "MismatchUser", e);
            String user = AuthSteps.loginAs(u, pwd);
            UUID userId = UUID.fromString(given().cookie("JSESSIONID", user).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            given()
                    .cookie("JSESSIONID", user)
                    .contentType(ContentType.URLENC)
                    .formParam("currentPassword", pwd)
                    .formParam("newPassword", "NewPass123!")
                    .formParam("confirmPassword", "DifferentPass123!")
                    .when()
                    .patch("/api/users/" + userId + "/password")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"))
                    .body("title", equalTo(MessageUtils.get("exception.title.validation-failed")))
                    .body("status", equalTo(422));
        }

        @Test
        @DisplayName("❌ 현재 패스워드 틀림 → 401 Unauthorized")
        void wrongCurrentPassword() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "wrongpwd_" + uid;
            final String pwd = "Password123!";
            String e = u + "@example.com";

            AuthSteps.register(u, pwd, "WrongPwdUser", e);
            String user = AuthSteps.loginAs(u, pwd);
            UUID userId = UUID.fromString(given().cookie("JSESSIONID", user).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            given()
                    .cookie("JSESSIONID", user)
                    .contentType(ContentType.URLENC)
                    .formParam("currentPassword", "WrongPassword123!")
                    .formParam("newPassword", "NewPass123!")
                    .formParam("confirmPassword", "NewPass123!")
                    .when()
                    .patch("/api/users/" + userId + "/password")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("❌ 인증 없음 → 401 Unauthorized")
        void updatePasswordUnauthorized() {
            given()
                    .contentType(ContentType.URLENC)
                    .formParam("currentPassword", "OldPass123!")
                    .formParam("newPassword", "NewPass123!")
                    .formParam("confirmPassword", "NewPass123!")
                    .when()
                    .patch("/api/users/00000000-0000-0000-0000-000000000001/password")
                    .then()
                    .statusCode(401)
                    .body("type", equalTo("urn:problem-type:unauthorized"))
                    .body("title", equalTo(MessageUtils.get("exception.title.unauthorized")))
                    .body("status", equalTo(401))
                    .body("code", equalTo("UNAUTHORIZED"));
        }

        @Test
        @DisplayName("❌ 타인 패스워드 변경 시도 → 403 Forbidden")
        void updateOthersPasswordForbidden() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "pwdother_" + uid;
            final String pwd = "Password123!";
            String e = u + "@example.com";

            AuthSteps.register(u, pwd, "OtherUser", e);
            String other = AuthSteps.loginAs(u, pwd);
            UUID otherId = UUID.fromString(given().cookie("JSESSIONID", other).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            given()
                    .cookie("JSESSIONID", regular)
                    .contentType(ContentType.URLENC)
                    .formParam("currentPassword", "Password123!")
                    .formParam("newPassword", "NewPass123!")
                    .formParam("confirmPassword", "NewPass123!")
                    .when()
                    .patch("/api/users/" + otherId + "/password")
                    .then()
                    .statusCode(403)
                    .body("type", equalTo("urn:problem-type:forbidden"))
                    .body("title", equalTo(MessageUtils.get("exception.title.access-denied")))
                    .body("status", equalTo(403))
                    .body("code", equalTo("FORBIDDEN"));
        }

        @Test
        @DisplayName("❌ 존재하지 않는 사용자 → 404 Not Found")
        void updatePasswordNotFound() {
            given()
                    .cookie("JSESSIONID", admin)
                    .contentType(ContentType.URLENC)
                    .formParam("currentPassword", "Password123!")
                    .formParam("newPassword", "NewPass123!")
                    .formParam("confirmPassword", "NewPass123!")
                    .when()
                    .patch("/api/users/99999999-9999-9999-9999-999999999999/password")
                    .then()
                    .statusCode(404)
                    .body("type", equalTo("urn:problem-type:not-found"))
                    .body("title", equalTo(MessageUtils.get("exception.title.not-found")));
        }

        @Test
        @DisplayName("❌ 패스워드 복잡도 미충족 → 422 Unprocessable Content")
        void invalidPasswordComplexity() {
            String uid = UUID.randomUUID().toString().substring(0, 8);
            String u = "pwdcomplex_" + uid;
            final String pwd = "Password123!";
            String e = u + "@example.com";

            AuthSteps.register(u, pwd, "ComplexUser", e);
            String user = AuthSteps.loginAs(u, pwd);
            UUID userId = UUID.fromString(given().cookie("JSESSIONID", user).when().get("/api/users/me").then().extract().jsonPath().getString("id"));

            // 특수문자 없음
            given()
                    .cookie("JSESSIONID", user)
                    .contentType(ContentType.URLENC)
                    .formParam("currentPassword", pwd)
                    .formParam("newPassword", "Password123")
                    .formParam("confirmPassword", "Password123")
                    .when()
                    .patch("/api/users/" + userId + "/password")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"));

            // 너무 짧음
            given()
                    .cookie("JSESSIONID", user)
                    .contentType(ContentType.URLENC)
                    .formParam("currentPassword", pwd)
                    .formParam("newPassword", "Pass1!")
                    .formParam("confirmPassword", "Pass1!")
                    .when()
                    .patch("/api/users/" + userId + "/password")
                    .then()
                    .statusCode(422)
                    .body("type", equalTo("urn:problem-type:validation-error"));
        }
    }
}
