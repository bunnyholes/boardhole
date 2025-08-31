package bunny.boardhole.user.web;

import bunny.boardhole.common.bootstrap.DataInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DisplayName("사용자 컨트롤러 통합 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== READ: 사용자 조회 테스트 ==========

    @Test
    @DisplayName("01. 사용자 목록 조회")
    void test_01_list_users() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.pageable").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("02. 사용자 검색")
    void test_02_search_users() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("03. 사용자 단일 조회")
    void test_03_get_user() throws Exception {
        // 새로운 사용자 생성
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user_" + uniqueId)
                        .param("password", "password123")
                        .param("name", "Test User")
                        .param("email", "user_" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent());

        // 사용자 목록에서 해당 사용자 찾기
        MvcResult listResult = mockMvc.perform(get("/api/users")
                        .param("search", "user_" + uniqueId))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = listResult.getResponse().getContentAsString();
        // ID 추출 (첫 번째 사용자의 ID를 가져옴)
        Long userId = Long.parseLong(responseContent.replaceAll(".*\"content\":\\[\\{\"id\":(\\d+).*", "$1"));

        // 사용자 조회
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("user_" + uniqueId))
                .andDo(print());
    }

    @Test
    @DisplayName("04. 존재하지 않는 사용자 조회")
    void test_04_get_nonexistent_user() throws Exception {
        mockMvc.perform(get("/api/users/999999"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // ========== UPDATE: 사용자 수정 테스트 ==========

    @Test
    @DisplayName("05. 사용자 정보 수정 성공")
    void test_05_update_user_success() throws Exception {
        // 새로운 사용자 생성 및 로그인
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "update_" + uniqueId)
                        .param("password", "password123")
                        .param("name", "Original Name")
                        .param("email", "update_" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "update_" + uniqueId)
                        .param("password", "password123"))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 사용자 ID 찾기
        MvcResult listResult = mockMvc.perform(get("/api/users")
                        .param("search", "update_" + uniqueId))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = listResult.getResponse().getContentAsString();
        Long userId = Long.parseLong(responseContent.replaceAll(".*\"content\":\\[\\{\"id\":(\\d+).*", "$1"));

        // 사용자 정보 수정
        mockMvc.perform(put("/api/users/" + userId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Updated Name")
                        .param("email", "updated_" + uniqueId + "@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated_" + uniqueId + "@example.com"))
                .andDo(print());
    }

    @Test
    @DisplayName("06. 사용자 정보 수정 실패 - 인증되지 않은 사용자")
    void test_06_update_user_unauthorized() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Hacked Name")
                        .param("email", "hacked@example.com"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("07. 존재하지 않는 사용자 수정")
    void test_07_update_nonexistent_user() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.TEST_USERNAME)
                        .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        mockMvc.perform(put("/api/users/999999")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "New Name")
                        .param("email", "new@example.com"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    // ========== DELETE: 사용자 삭제 테스트 ==========

    @Test
    @DisplayName("08. 사용자 삭제 성공")
    void test_08_delete_user_success() throws Exception {
        // 새로운 사용자 생성 및 로그인
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "delete_" + uniqueId)
                        .param("password", "password123")
                        .param("name", "To Delete")
                        .param("email", "delete_" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "delete_" + uniqueId)
                        .param("password", "password123"))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 사용자 ID 찾기
        MvcResult listResult = mockMvc.perform(get("/api/users")
                        .param("search", "delete_" + uniqueId))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = listResult.getResponse().getContentAsString();
        Long userId = Long.parseLong(responseContent.replaceAll(".*\"content\":\\[\\{\"id\":(\\d+).*", "$1"));

        // 사용자 삭제
        mockMvc.perform(delete("/api/users/" + userId)
                        .session(session))
                .andExpect(status().isNoContent())
                .andDo(print());

        // 삭제된 사용자 조회 시도 (404 예상)
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("09. 사용자 삭제 실패 - 인증되지 않은 사용자")
    void test_09_delete_user_unauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("10. 존재하지 않는 사용자 삭제")
    void test_10_delete_nonexistent_user() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.TEST_USERNAME)
                        .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        mockMvc.perform(delete("/api/users/999999")
                        .session(session))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    // ========== SPECIAL: 현재 사용자 정보 테스트 ==========

    @Test
    @DisplayName("11. 현재 로그인한 사용자 정보 조회")
    void test_11_get_current_user() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.TEST_USERNAME)
                        .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 현재 사용자 정보 조회
        mockMvc.perform(get("/api/users/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(DataInitializer.TEST_USERNAME))
                .andExpect(jsonPath("$.roles").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("12. 현재 사용자 정보 조회 실패 - 인증되지 않은 사용자")
    void test_12_get_current_user_unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("13. 사용자 목록 조회 - 페이지네이션 테스트")
    void test_13_list_users_pagination() throws Exception {
        // 첫 번째 페이지 조회 (크기 5)
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.pageable.pageSize").value(5))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("14. 다른 사용자 정보 수정 실패 - 일반 사용자")
    void test_14_update_other_user_forbidden() throws Exception {
        // 첫 번째 사용자 생성
        String user1Id = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user1_" + user1Id)
                        .param("password", "password123")
                        .param("name", "User One")
                        .param("email", "user1_" + user1Id + "@example.com"))
                .andExpect(status().isNoContent());

        // 두 번째 사용자 생성 및 로그인
        String user2Id = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user2_" + user2Id)
                        .param("password", "password123")
                        .param("name", "User Two")
                        .param("email", "user2_" + user2Id + "@example.com"))
                .andExpect(status().isNoContent());

        MvcResult user2LoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user2_" + user2Id)
                        .param("password", "password123"))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession user2Session = (MockHttpSession) user2LoginResult.getRequest().getSession();

        // 첫 번째 사용자 ID 찾기
        MvcResult listResult = mockMvc.perform(get("/api/users")
                        .param("search", "user1_" + user1Id))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = listResult.getResponse().getContentAsString();
        Long user1IdLong = Long.parseLong(responseContent.replaceAll(".*\"content\":\\[\\{\"id\":(\\d+).*", "$1"));

        // 두 번째 사용자가 첫 번째 사용자 수정 시도 (권한 없음)
        mockMvc.perform(put("/api/users/" + user1IdLong)
                        .session(user2Session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Hacked Name")
                        .param("email", "hacked@example.com"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("15. 관리자가 다른 사용자 정보 수정 성공")
    void test_15_admin_update_other_user_success() throws Exception {
        // 새로운 사용자 생성
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "target_" + uniqueId)
                        .param("password", "password123")
                        .param("name", "Target User")
                        .param("email", "target_" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent());

        // 관리자로 로그인
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.ADMIN_USERNAME)
                        .param("password", DataInitializer.ADMIN_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession adminSession = (MockHttpSession) adminLoginResult.getRequest().getSession();

        // 대상 사용자 ID 찾기
        MvcResult listResult = mockMvc.perform(get("/api/users")
                        .param("search", "target_" + uniqueId))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = listResult.getResponse().getContentAsString();
        Long targetUserId = Long.parseLong(responseContent.replaceAll(".*\"content\":\\[\\{\"id\":(\\d+).*", "$1"));

        // 관리자가 다른 사용자 정보 수정
        mockMvc.perform(put("/api/users/" + targetUserId)
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Admin Updated Name")
                        .param("email", "admin_updated_" + uniqueId + "@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin Updated Name"))
                .andExpect(jsonPath("$.email").value("admin_updated_" + uniqueId + "@example.com"))
                .andDo(print());
    }
}
