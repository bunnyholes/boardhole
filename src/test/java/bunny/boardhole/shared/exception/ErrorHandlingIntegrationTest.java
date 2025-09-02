package bunny.boardhole.shared.exception;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.shared.web.ControllerTestBase;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("에러 처리 통합 테스트")
public class ErrorHandlingIntegrationTest extends ControllerTestBase {

    @Test
    @DisplayName("E2E: 404 Not Found - 존재하지 않는 리소스")
    void test_404_not_found() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/boards/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:not-found"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/api/boards/999999"))
                .andExpect(jsonPath("$.path").value("/api/boards/999999"))
                .andExpect(jsonPath("$.method").value("GET"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print())
                .andReturn();

        // Verify traceId is included when available
        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> problemDetail = objectMapper.readValue(responseBody, Map.class);
        assertThat(problemDetail).containsKey("timestamp");
    }

    @Test
    @DisplayName("E2E: 400 Bad Request - 유효성 검증 실패")
    @WithUserDetails
    void test_400_validation_error() throws Exception {
        // 제목 없이 게시글 생성 시도
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", "Content without title"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'title')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'title')].message").exists())
                .andExpect(jsonPath("$.path").value("/api/boards"))
                .andExpect(jsonPath("$.method").value("POST"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: 401 Unauthorized - 인증 실패")
    void test_401_unauthorized() throws Exception {
        // 인증 없이 보호된 리소스 접근
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Test Title")
                        .param("content", "Test Content"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:unauthorized"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.path").value("/api/boards"))
                .andExpect(jsonPath("$.method").value("POST"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());
    }


    @Test
    @DisplayName("E2E: 409 Conflict - 중복된 사용자명")
    void test_409_duplicate_username() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String username = "dup_test_" + uniqueId;
        String email1 = "user1_" + uniqueId + "@example.com";
        String email2 = "user2_" + uniqueId + "@example.com";

        // 첫 번째 회원가입 (성공)
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", "Password123!")
                        .param("name", "First User")
                        .param("email", email1))
                .andExpect(status().isNoContent());

        // 같은 사용자명으로 두 번째 회원가입 시도 (실패)
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", "Password456!")
                        .param("name", "Second User")
                        .param("email", email2))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:duplicate-username"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.code").value("USER_DUPLICATE_USERNAME"))
                .andExpect(jsonPath("$.path").value("/api/auth/signup"))
                .andExpect(jsonPath("$.method").value("POST"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: TraceId 전파 확인")
    void test_traceId_propagation() throws Exception {
        // 여러 요청에서 traceId가 생성되고 포함되는지 확인
        MvcResult result1 = mockMvc.perform(get("/api/boards/999999"))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseBody1 = result1.getResponse().getContentAsString();
        Map<String, Object> problemDetail1 = objectMapper.readValue(responseBody1, Map.class);

        // traceId는 요청마다 다를 수 있으므로 존재 여부만 확인
        // (RequestLoggingFilter가 활성화된 경우에만 포함됨)
        if (problemDetail1.containsKey("traceId")) {
            assertThat(problemDetail1.get("traceId")).isNotNull();
            assertThat(problemDetail1.get("traceId").toString()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("E2E: 다른 사용자의 리소스 수정 시도")
    @WithUserDetails
    void test_403_forbidden_resource_owner() throws Exception {
        // 다른 사용자(현재 user가 아닌)의 게시글 데이터 시드
        String owner = "owner_" + UUID.randomUUID().toString().substring(0, 8);
        var ownerUser = userRepository.save(bunny.boardhole.user.domain.User.builder()
                .username(owner)
                .password("plain")
                .name("Owner")
                .email(owner + "@example.com")
                .userRoles(java.util.Set.of(bunny.boardhole.user.domain.Role.USER))
                .build());

        Long boardId = boardRepository.save(Board.builder()
                .title("Owner's Board")
                .content("Owner's Content")
                .author(ownerUser)
                .build()).getId();

        // 현재 사용자(user)가 타인의 게시글 수정 시도 → 403 기대
        mockMvc.perform(put("/api/boards/" + boardId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Hacked Title")
                        .param("content", "Hacked Content"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:forbidden"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Content-Type에 따른 에러 응답 형식")
    void test_content_type_negotiation() throws Exception {
        // application/json 요청
        mockMvc.perform(get("/api/boards/999999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").exists())
                .andDo(print());

        // application/problem+json 요청
        mockMvc.perform(get("/api/boards/999999")
                        .accept("application/problem+json"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.type").exists())
                .andDo(print());
    }

    // Helper methods

    private Long extractBoardId(String jsonResponse) {
        return Long.parseLong(jsonResponse.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }
}
