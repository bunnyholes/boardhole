package dev.xiyo.bunnyholes.boardhole.testsupport.e2e;

import java.util.Map;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public final class AuthSteps {

    // Default seeded accounts (DataInitializer)
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin123!";

    public static void register(String username, String password, String name, String email) {
        given()
                .contentType(ContentType.URLENC)
                .formParams(Map.of(
                        "username", username,
                        "password", password,
                        "confirmPassword", password,  // confirmPassword 추가
                        "name", name,
                        "email", email))
                .when().post("/api/auth/signup")
                .then().statusCode(anyOf(is(204), is(409)));
    }

    public static String loginAs(String username, String password) {
        var res = given()
                .contentType(ContentType.URLENC)
                .formParams(Map.of("username", username, "password", password))
                .when().post("/api/auth/login")
                .then().statusCode(204)
                .extract().response();
        return res.getCookie("JSESSIONID");
    }

    // Semantic helpers
    public static String loginAdmin() {
        return loginAs(ADMIN_USERNAME, ADMIN_PASSWORD);
    }
}
