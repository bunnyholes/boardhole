package bunny.boardhole.testsupport.e2e;

import java.util.Map;
import java.util.UUID;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public final class BoardSteps {

    public static Response create(String jsessionId, String title, String content) {
        return given().cookie("JSESSIONID", jsessionId)
                      .contentType(ContentType.URLENC)
                      .formParams(Map.of("title", title, "content", content))
                      .when().post("/api/boards")
                      .then().extract().response();
    }

    public static Response get(String jsessionId, long id) {
        return given().cookie("JSESSIONID", jsessionId)
                      .when().get("/api/boards/" + id).then().extract().response();
    }

    public static Response get(String jsessionId, UUID id) {
        return given().cookie("JSESSIONID", jsessionId)
                      .when().get("/api/boards/" + id.toString()).then().extract().response();
    }

    public static Response list(String jsessionId) {
        return given().cookie("JSESSIONID", jsessionId)
                      .when().get("/api/boards").then().extract().response();
    }

    public static Response update(String jsessionId, long id, String title, String content) {
        return given().cookie("JSESSIONID", jsessionId)
                      .contentType(ContentType.URLENC)
                      .formParams(Map.of("title", title, "content", content))
                      .when().put("/api/boards/" + id)
                      .then().extract().response();
    }

    public static Response update(String jsessionId, UUID id, String title, String content) {
        return given().cookie("JSESSIONID", jsessionId)
                      .contentType(ContentType.URLENC)
                      .formParams(Map.of("title", title, "content", content))
                      .when().put("/api/boards/" + id.toString())
                      .then().extract().response();
    }

    public static Response delete(String jsessionId, long id) {
        return given().cookie("JSESSIONID", jsessionId)
                      .when().delete("/api/boards/" + id).then().extract().response();
    }

    public static Response delete(String jsessionId, UUID id) {
        return given().cookie("JSESSIONID", jsessionId)
                      .when().delete("/api/boards/" + id.toString()).then().extract().response();
    }
}
