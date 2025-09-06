package bunny.boardhole.testsupport.e2e;

import java.util.Map;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public final class BoardSteps {

    private BoardSteps() {
    }

    public static Response create(SessionCookie session, String title, String content) {
        return RestSpecs.auth(session).contentType(ContentType.URLENC).formParams(Map.of("title", title, "content", content)).when().post("boards").then().extract().response();
    }

    public static Response get(SessionCookie session, long id) {
        return RestSpecs.auth(session).when().get("boards/" + id).then().extract().response();
    }

    public static Response list(SessionCookie session) {
        return RestSpecs.auth(session).when().get("boards").then().extract().response();
    }

    public static Response update(SessionCookie session, long id, String title, String content) {
        return RestSpecs.auth(session).contentType(ContentType.URLENC).formParams(Map.of("title", title, "content", content)).when().put("boards/" + id).then().extract().response();
    }

    public static Response delete(SessionCookie session, long id) {
        return RestSpecs.auth(session).when().delete("boards/" + id).then().extract().response();
    }
}
