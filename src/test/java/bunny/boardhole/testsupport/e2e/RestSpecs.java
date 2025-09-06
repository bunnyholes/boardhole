package bunny.boardhole.testsupport.e2e;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * RestAssured 테스트를 위한 공통 RequestSpecification 팩토리
 * 인증 상태별로 사전 구성된 요청 스펙을 제공합니다.
 */
final class RestSpecs {
    private RestSpecs() {
    }

    /**
     * 익명 사용자용 JSON 요청 스펙
     */
    public static RequestSpecification anon() {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    /**
     * 인증된 사용자용 JSON 요청 스펙
     */
    public static RequestSpecification auth(SessionCookie session) {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .cookie(session.name(), session.value());
    }

    /**
     * 폼 데이터 전송용 익명 요청 스펙
     */
    public static RequestSpecification form() {
        return given()
                .contentType(ContentType.URLENC)
                .accept(ContentType.JSON);
    }

    /**
     * 폼 데이터 전송용 인증된 요청 스펙
     */
    public static RequestSpecification formAuth(SessionCookie session) {
        return given()
                .contentType(ContentType.URLENC)
                .accept(ContentType.JSON)
                .cookie(session.name(), session.value());
    }
}

