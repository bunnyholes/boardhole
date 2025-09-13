package bunny.boardhole.testsupport.e2e;

import java.util.Map;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public final class BoardSteps {

    public static Response create(String jsessionId, String title, String content) {
        return given().cookie("JSESSIONID", jsessionId)
                      .contentType(ContentType.URLENC)
                      .formParams(Map.of("title", title, "content", content))
                      .when().post("boards")
                      .then().extract().response();
    }

    public static Response get(String jsessionId, long id) {
        return given().cookie("JSESSIONID", jsessionId)
                      .when().get("boards/" + id).then().extract().response();
    }

    public static Response list(String jsessionId) {
        return given().cookie("JSESSIONID", jsessionId)
                      .when().get("boards").then().extract().response();
    }

    public static Response update(String jsessionId, long id, String title, String content) {
        return given().cookie("JSESSIONID", jsessionId)
                      .contentType(ContentType.URLENC)
                      .formParams(Map.of("title", title, "content", content))
                      .when().put("boards/" + id)
                      .then().extract().response();
    }

    public static Response delete(String jsessionId, long id) {
        return given().cookie("JSESSIONID", jsessionId)
                      .when().delete("boards/" + id).then().extract().response();
    }
}
