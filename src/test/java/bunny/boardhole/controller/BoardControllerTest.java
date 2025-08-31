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
@DisplayName("게시판 컨트롤러 통합 테스트")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== CREATE: 게시글 생성 테스트 ==========
    
    @Test
    @Order(1)
    @DisplayName("01. 게시글 생성 성공")
    void test_01_create_board_success() throws Exception {
        // 먼저 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", DataInitializer.TEST_USERNAME)
                .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 게시글 생성
        mockMvc.perform(post("/api/boards")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Board Title")
                .param("content", "Test Board Content"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Board Title"))
                .andExpect(jsonPath("$.content").value("Test Board Content"))
                .andExpect(jsonPath("$.authorName").value(DataInitializer.TEST_USERNAME))
                .andDo(print());
    }

    @Test
    @Order(2)
    @DisplayName("02. 게시글 생성 실패 - 인증되지 않은 사용자")
    void test_02_create_board_unauthorized() throws Exception {
        mockMvc.perform(post("/api/boards")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Board Title")
                .param("content", "Test Board Content"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    // ========== READ: 게시글 조회 테스트 ==========

    @Test
    @Order(3)
    @DisplayName("03. 게시글 목록 조회")
    void test_03_list_boards() throws Exception {
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.pageable").exists())
                .andDo(print());
    }

    @Test
    @Order(4)
    @DisplayName("04. 게시글 검색")
    void test_04_search_boards() throws Exception {
        mockMvc.perform(get("/api/boards")
                .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andDo(print());
    }

    @Test
    @Order(5)
    @DisplayName("05. 게시글 단일 조회")
    void test_05_get_board() throws Exception {
        // 먼저 게시글 생성
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", DataInitializer.TEST_USERNAME)
                .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        MvcResult createResult = mockMvc.perform(post("/api/boards")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Board to Get")
                .param("content", "Content to Get"))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        Long boardId = Long.parseLong(responseContent.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 게시글 조회
        mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boardId))
                .andExpect(jsonPath("$.title").value("Board to Get"))
                .andDo(print());
    }

    // ========== UPDATE: 게시글 수정 테스트 ==========

    @Test
    @Order(6)
    @DisplayName("06. 게시글 수정 성공 - 작성자")
    void test_06_update_board_by_author() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", DataInitializer.TEST_USERNAME)
                .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 게시글 생성
        MvcResult createResult = mockMvc.perform(post("/api/boards")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Original Title")
                .param("content", "Original Content"))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        Long boardId = Long.parseLong(responseContent.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 게시글 수정
        mockMvc.perform(put("/api/boards/" + boardId)
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Updated Title")
                .param("content", "Updated Content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"))
                .andDo(print());
    }

    @Test
    @Order(7)
    @DisplayName("07. 게시글 수정 성공 - 관리자")
    void test_07_update_board_by_admin() throws Exception {
        // 일반 사용자로 로그인하여 게시글 생성
        MvcResult userLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", DataInitializer.TEST_USERNAME)
                .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession userSession = (MockHttpSession) userLoginResult.getRequest().getSession();

        MvcResult createResult = mockMvc.perform(post("/api/boards")
                .session(userSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "User's Board")
                .param("content", "User's Content"))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        Long boardId = Long.parseLong(responseContent.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 관리자로 로그인
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", DataInitializer.ADMIN_USERNAME)
                .param("password", DataInitializer.ADMIN_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession adminSession = (MockHttpSession) adminLoginResult.getRequest().getSession();

        // 관리자가 게시글 수정
        mockMvc.perform(put("/api/boards/" + boardId)
                .session(adminSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Admin Updated Title")
                .param("content", "Admin Updated Content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin Updated Title"))
                .andDo(print());
    }

    @Test
    @Order(8)
    @DisplayName("08. 게시글 수정 실패 - 권한 없음")
    void test_08_update_board_unauthorized() throws Exception {
        // 첫 번째 사용자로 게시글 생성
        MvcResult user1LoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", DataInitializer.TEST_USERNAME)
                .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession user1Session = (MockHttpSession) user1LoginResult.getRequest().getSession();

        MvcResult createResult = mockMvc.perform(post("/api/boards")
                .session(user1Session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "User1's Board")
                .param("content", "User1's Content"))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        Long boardId = Long.parseLong(responseContent.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 두 번째 사용자 생성 및 로그인
        String uniqueId = String.valueOf(System.currentTimeMillis() % 10000);
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user2_" + uniqueId)
                .param("password", "password123")
                .param("name", "User Two")
                .param("email", "user2_" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent());

        MvcResult user2LoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user2_" + uniqueId)
                .param("password", "password123"))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession user2Session = (MockHttpSession) user2LoginResult.getRequest().getSession();

        // 다른 사용자가 게시글 수정 시도
        mockMvc.perform(put("/api/boards/" + boardId)
                .session(user2Session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Hacked Title")
                .param("content", "Hacked Content"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    // ========== DELETE: 게시글 삭제 테스트 ==========

    @Test
    @Order(9)
    @DisplayName("09. 게시글 삭제 성공 - 작성자")
    void test_09_delete_board_by_author() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", DataInitializer.TEST_USERNAME)
                .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 게시글 생성
        MvcResult createResult = mockMvc.perform(post("/api/boards")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Board to Delete")
                .param("content", "Content to Delete"))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        Long boardId = Long.parseLong(responseContent.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 게시글 삭제
        mockMvc.perform(delete("/api/boards/" + boardId)
                .session(session))
                .andExpect(status().isNoContent())
                .andDo(print());

        // 삭제된 게시글 조회 시도 (404 예상)
        mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @Order(10)
    @DisplayName("10. 게시글 삭제 실패 - 권한 없음")
    void test_10_delete_board_unauthorized() throws Exception {
        // 첫 번째 사용자로 게시글 생성
        MvcResult user1LoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", DataInitializer.TEST_USERNAME)
                .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession user1Session = (MockHttpSession) user1LoginResult.getRequest().getSession();

        MvcResult createResult = mockMvc.perform(post("/api/boards")
                .session(user1Session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Protected Board")
                .param("content", "Protected Content"))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        Long boardId = Long.parseLong(responseContent.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 두 번째 사용자 생성 및 로그인
        String uniqueId = String.valueOf(System.currentTimeMillis() % 10000);
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user3_" + uniqueId)
                .param("password", "password123")
                .param("name", "User Three")
                .param("email", "user3_" + uniqueId + "@example.com"))
                .andExpect(status().isNoContent());

        MvcResult user3LoginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "user3_" + uniqueId)
                .param("password", "password123"))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession user3Session = (MockHttpSession) user3LoginResult.getRequest().getSession();

        // 다른 사용자가 게시글 삭제 시도
        mockMvc.perform(delete("/api/boards/" + boardId)
                .session(user3Session))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
