package bunny.boardhole.shared.config.cors;

import bunny.boardhole.shared.web.ControllerTestBase;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CORS (Cross-Origin Resource Sharing) 통합 테스트
 * <p>
 * 다양한 CORS 시나리오를 테스트하여 보안 정책이 올바르게 적용되는지 검증합니다.
 * - 허용된 Origin에서의 요청
 * - 허용되지 않은 Origin에서의 요청
 * - Preflight 요청 처리
 * - 크레덴셜 포함 요청
 * - 커스텀 헤더 처리
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("CORS 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CorsIntegrationTest extends ControllerTestBase {

    private static final String ALLOWED_ORIGIN = "http://localhost:8080";
    private static final String DISALLOWED_ORIGIN = "http://evil.com";
    private static final String API_ENDPOINT = "/api/boards";

    // ==================== 허용된 CORS 테스트 케이스 ====================

    @Nested
    @DisplayName("허용된 Origin 테스트")
    class AllowedOriginTests {

        @Test
        @Order(1)
        @DisplayName("허용된 Origin에서 GET 요청 시 CORS 헤더가 포함되어야 함")
        void allowedOrigin_GET_shouldReturnCorsHeaders() throws Exception {
            mockMvc.perform(get(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }

        @Test
        @Order(2)
        @DisplayName("허용된 Origin에서 POST 요청 시 CORS 헤더가 포함되어야 함")
        void allowedOrigin_POST_shouldReturnCorsHeaders() throws Exception {
            String requestBody = """
                    {
                        "title": "Test Board",
                        "content": "Test Content"
                    }
                    """;

            mockMvc.perform(post(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .contentType("application/json")
                            .content(requestBody))
                    .andExpect(status().isUnauthorized()) // 인증 필요
                    .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }

        @Test
        @Order(3)
        @DisplayName("허용된 Origin에서 PUT 요청 시 CORS 헤더가 포함되어야 함")
        void allowedOrigin_PUT_shouldReturnCorsHeaders() throws Exception {
            mockMvc.perform(put(API_ENDPOINT + "/1")
                            .header("Origin", ALLOWED_ORIGIN)
                            .contentType("application/json")
                            .content("{}"))
                    .andExpect(status().isUnauthorized()) // 인증 필요
                    .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }

        @Test
        @Order(4)
        @DisplayName("허용된 Origin에서 DELETE 요청 시 CORS 헤더가 포함되어야 함")
        void allowedOrigin_DELETE_shouldReturnCorsHeaders() throws Exception {
            mockMvc.perform(delete(API_ENDPOINT + "/1")
                            .header("Origin", ALLOWED_ORIGIN))
                    .andExpect(status().isUnauthorized()) // 인증 필요
                    .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }
    }

    // ==================== Preflight 요청 테스트 ====================

    @Nested
    @DisplayName("Preflight 요청 테스트")
    class PreflightRequestTests {

        @Test
        @Order(5)
        @DisplayName("허용된 메서드와 헤더로 Preflight 요청 시 성공해야 함")
        void preflightRequest_withAllowedMethodAndHeaders_shouldSucceed() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "Content-Type, Authorization"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                    .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                    .andExpect(header().string("Access-Control-Allow-Headers",
                            containsString("Content-Type")))
                    .andExpect(header().string("Access-Control-Allow-Headers",
                            containsString("Authorization")))
                    .andExpect(header().string("Access-Control-Max-Age", "3600"));
        }

        @Test
        @Order(6)
        @DisplayName("GET 메서드 Preflight 요청이 성공해야 함")
        void preflightRequest_GET_shouldSucceed() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")));
        }

        @Test
        @Order(7)
        @DisplayName("DELETE 메서드 Preflight 요청이 성공해야 함")
        void preflightRequest_DELETE_shouldSucceed() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "DELETE"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Methods", containsString("DELETE")));
        }

        @Test
        @Order(8)
        @DisplayName("X-Requested-With 헤더를 포함한 Preflight 요청이 성공해야 함")
        void preflightRequest_withXRequestedWith_shouldSucceed() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "X-Requested-With"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Headers",
                            containsString("X-Requested-With")));
        }
    }

    // ==================== 비허용 CORS 테스트 케이스 ====================

    @Nested
    @DisplayName("비허용 Origin 테스트")
    class DisallowedOriginTests {

        @Test
        @Order(9)
        @DisplayName("허용되지 않은 Origin에서 OPTIONS 요청 시 403 반환")
        void disallowedOrigin_shouldReturn403ForPreflight() throws Exception {
            // Spring Security는 허용되지 않은 Origin에서의 preflight 요청을 차단
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", DISALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(10)
        @DisplayName("다른 포트의 localhost에서 OPTIONS 요청 시 403 반환")
        void differentPort_shouldReturn403ForPreflight() throws Exception {
            // 테스트 환경에서 localhost:3000은 허용되지 않음
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", "http://localhost:3000")
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(11)
        @DisplayName("https 프로토콜에서 OPTIONS 요청 시 403 반환")
        void httpsProtocol_shouldReturn403ForPreflight() throws Exception {
            // HTTPS는 별도로 설정되지 않은 경우 차단
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", "https://localhost:8080")
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(12)
        @DisplayName("외부 도메인에서 OPTIONS 요청 시 403 반환")
        void externalDomain_shouldReturn403ForPreflight() throws Exception {
            // 외부 도메인은 차단
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", "http://example.com")
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("비허용 메서드 테스트")
    class DisallowedMethodTests {

        @Test
        @Order(13)
        @DisplayName("PATCH 메서드 Preflight 요청은 실패해야 함")
        void preflightRequest_PATCH_shouldFail() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "PATCH"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(14)
        @DisplayName("TRACE 메서드 Preflight 요청은 실패해야 함")
        void preflightRequest_TRACE_shouldFail() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "TRACE"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(15)
        @DisplayName("커스텀 메서드 Preflight 요청은 실패해야 함")
        void preflightRequest_customMethod_shouldFail() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "CUSTOM"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("비허용 헤더 테스트")
    class DisallowedHeaderTests {

        @Test
        @Order(16)
        @DisplayName("허용되지 않은 커스텀 헤더로 Preflight 요청 시 실패해야 함")
        void preflightRequest_withDisallowedHeader_shouldFail() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "X-Custom-Header"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(17)
        @DisplayName("X-Forwarded-For 헤더로 Preflight 요청 시 실패해야 함")
        void preflightRequest_withXForwardedFor_shouldFail() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "X-Forwarded-For"))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 엣지 케이스 테스트 ====================

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @Order(18)
        @DisplayName("Origin 헤더가 없는 요청도 처리되어야 함")
        void noOriginHeader_shouldBeProcessed() throws Exception {
            mockMvc.perform(get(API_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        }

        @Test
        @Order(19)
        @DisplayName("null Origin에서 OPTIONS 요청 시 403 반환")
        void nullOrigin_shouldReturn403ForPreflight() throws Exception {
            // null Origin은 보안상 차단되어야 함
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", "null")
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(20)
        @DisplayName("크레덴셜 포함 설정 확인")
        void credentialsIncluded_shouldBeAllowed() throws Exception {
            mockMvc.perform(get(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Cookie", "sessionId=test123"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        }

        @Test
        @Order(21)
        @DisplayName("Max-Age 헤더가 올바르게 설정되어야 함")
        void maxAge_shouldBeSet() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Max-Age", "3600"));
        }

        @Test
        @Order(22)
        @DisplayName("여러 헤더를 동시에 요청할 때 처리")
        void multipleHeaders_shouldBeHandled() throws Exception {
            mockMvc.perform(options(API_ENDPOINT)
                            .header("Origin", ALLOWED_ORIGIN)
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers",
                                    "Content-Type, Authorization, X-Requested-With, Accept"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Headers",
                            allOf(
                                    containsString("Content-Type"),
                                    containsString("Authorization"),
                                    containsString("X-Requested-With"),
                                    containsString("Accept")
                            )));
        }
    }

    // ==================== 특정 엔드포인트 테스트 ====================

    @Nested
    @DisplayName("특정 엔드포인트 CORS 테스트")
    class SpecificEndpointTests {

        @Test
        @Order(23)
        @DisplayName("인증 엔드포인트는 CORS를 허용해야 함")
        void authEndpoint_shouldAllowCors() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .header("Origin", ALLOWED_ORIGIN)
                            .contentType("application/json")
                            .content("{\"username\":\"test\",\"password\":\"test\"}"))
                    .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN));
        }

        @Test
        @Order(24)
        @DisplayName("Swagger UI 엔드포인트는 CORS 제한이 없어야 함")
        void swaggerEndpoint_shouldBeAccessible() throws Exception {
            mockMvc.perform(get("/v3/api-docs")
                            .header("Origin", DISALLOWED_ORIGIN))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(25)
        @DisplayName("정적 리소스는 CORS와 관계없이 접근 가능해야 함")
        void staticResources_shouldBeAccessible() throws Exception {
            // 정적 리소스는 CORS와 관계없이 접근 가능
            // 파일이 없으면 404, 있으면 200 반환
            var result = mockMvc.perform(get("/css/style.css")
                            .header("Origin", DISALLOWED_ORIGIN))
                    .andReturn();

            // 정적 리소스는 404(파일 없음) 또는 200(파일 있음) 반환
            // 403 Forbidden이 아니면 성공
            int status = result.getResponse().getStatus();
            assert status != 403 : "Static resources should not be blocked by CORS";
        }
    }
}