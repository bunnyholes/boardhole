package bunny.boardhole.controller;

import bunny.boardhole.config.DataInitializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("사용자 컨트롤러 통합 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== READ: 사용자 조회 테스트 ==========
    
    @Test
    @Order(1)
    @DisplayName("01. 사용자 목록 조회")
    void test_01_list_users() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.pageable").exists())
                .andDo(print());
    }

    @Test
    @Order(2)
    @DisplayName("02. 사용자 검색")
    void test_02_search_users() throws Exception {
        mockMvc.perform(get("/api/users")
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andDo(print());
    }

    @Test
    @Order(3)
    @DisplayName("03. 사용자 단일 조회")
    void test_03_get_user() throws Exception {
        // 새로운 사용자 생성
        String uniqueId = String.valueOf(System.currentTimeMillis() % 10000);
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
    @Order(4)
    @DisplayName("04. 존재하지 않는 사용자 조회")
    void test_04_get_nonexistent_user() throws Exception {
        mockMvc.perform(get("/api/users/999999"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // ========== UPDATE: 사용자 수정 테스트 ==========

    @Test
    @Order(5)
    @DisplayName("05. 사용자 정보 수정 성공")
    void test_05_update_user_success() throws Exception {
        // 새로운 사용자 생성 및 로그인
        String uniqueId = String.valueOf(System.currentTimeMillis() % 10000);
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
    @Order(6)
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
    @Order(7)
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
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // ========== DELETE: 사용자 삭제 테스트 ==========

    @Test
    @Order(8)
    @DisplayName("08. 사용자 삭제 성공")
    void test_08_delete_user_success() throws Exception {
        // 새로운 사용자 생성 및 로그인
        String uniqueId = String.valueOf(System.currentTimeMillis() % 10000);
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
    @Order(9)
    @DisplayName("09. 사용자 삭제 실패 - 인증되지 않은 사용자")
    void test_09_delete_user_unauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @Order(10)
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
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // ========== SPECIAL: 현재 사용자 정보 테스트 ==========

    @Test
    @Order(11)
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
    @Order(12)
    @DisplayName("12. 현재 사용자 정보 조회 실패 - 인증되지 않은 사용자")
    void test_12_get_current_user_unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }
}
