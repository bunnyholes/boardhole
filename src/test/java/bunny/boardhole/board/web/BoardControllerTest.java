package bunny.boardhole.board.web;

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
@DisplayName("게시판 컨트롤러 통합 테스트")
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ========== CREATE: 게시글 생성 테스트 ==========

    @Test
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
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/boards")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Board_" + uniqueId)
                        .param("content", "Content_" + uniqueId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Board_" + uniqueId))
                .andExpect(jsonPath("$.content").value("Content_" + uniqueId))
                .andExpect(jsonPath("$.authorName").value(DataInitializer.TEST_USERNAME))
                .andDo(print());
    }

    @Test
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
    @DisplayName("03. 게시글 목록 조회")
    void test_03_list_boards() throws Exception {
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.pageable").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("04. 게시글 검색")
    void test_04_search_boards() throws Exception {
        mockMvc.perform(get("/api/boards")
                        .param("search", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andDo(print());
    }

    @Test
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

        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        MvcResult createResult = mockMvc.perform(post("/api/boards")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Board_" + uniqueId)
                        .param("content", "Content_" + uniqueId))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        Long boardId = Long.parseLong(responseContent.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 게시글 조회
        mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boardId))
                .andExpect(jsonPath("$.title").value("Board_" + uniqueId))
                .andDo(print());
    }

    @Test
    @DisplayName("11. 조회 시 조회수 비동기 증가")
    void test_11_view_increments_async() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.TEST_USERNAME)
                        .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 게시글 생성
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        MvcResult createResult = mockMvc.perform(post("/api/boards")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Count_" + uniqueId)
                        .param("content", "Content_" + uniqueId))
                .andExpect(status().isCreated())
                .andReturn();

        String created = createResult.getResponse().getContentAsString();
        Long boardId = Long.parseLong(created.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 첫 조회 (이 시점의 viewCount를 기록)
        MvcResult firstGet = mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isOk())
                .andReturn();
        String firstBody = firstGet.getResponse().getContentAsString();
        int firstCount = Integer.parseInt(firstBody.replaceAll(".*\"viewCount\":(\\d+).*", "$1"));

        // 비동기 증가를 기다렸다가 다시 조회
        Thread.sleep(800);

        MvcResult secondGet = mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isOk())
                .andReturn();
        String secondBody = secondGet.getResponse().getContentAsString();
        int secondCount = Integer.parseInt(secondBody.replaceAll(".*\"viewCount\":(\\d+).*", "$1"));

        org.assertj.core.api.Assertions.assertThat(secondCount).isEqualTo(firstCount + 1);
    }

    // ========== UPDATE: 게시글 수정 테스트 ==========

    @Test
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
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        MvcResult createResult = mockMvc.perform(post("/api/boards")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Original_" + uniqueId)
                        .param("content", "Content_" + uniqueId))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = createResult.getResponse().getContentAsString();
        Long boardId = Long.parseLong(responseContent.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 게시글 수정
        mockMvc.perform(put("/api/boards/" + boardId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated_" + uniqueId)
                        .param("content", "Updated_" + uniqueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated_" + uniqueId))
                .andExpect(jsonPath("$.content").value("Updated_" + uniqueId))
                .andDo(print());
    }

    @Test
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

        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        MvcResult createResult = mockMvc.perform(post("/api/boards")
                        .session(userSession)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "User_" + uniqueId)
                        .param("content", "Content_" + uniqueId))
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
                        .param("title", "Admin_" + uniqueId)
                        .param("content", "AdminContent_" + uniqueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin_" + uniqueId))
                .andDo(print());
    }

    @Test
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

        // 다른 사용자가 게시글 수정 시도
        mockMvc.perform(put("/api/boards/" + boardId)
                        .session(user2Session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Hacked_" + user2Id)
                        .param("content", "Hacked_" + user2Id))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    // ========== DELETE: 게시글 삭제 테스트 ==========

    @Test
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
        String user3Id = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user3_" + user3Id)
                        .param("password", "password123")
                        .param("name", "User Three")
                        .param("email", "user3_" + user3Id + "@example.com"))
                .andExpect(status().isNoContent());

        MvcResult user3LoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user3_" + user3Id)
                        .param("password", "password123"))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession user3Session = (MockHttpSession) user3LoginResult.getRequest().getSession();

        // 다른 사용자가 게시글 삭제 시도 (권한 없음)
        mockMvc.perform(delete("/api/boards/" + boardId)
                        .session(user3Session))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("12. 게시글 수정 실패 - 존재하지 않는 게시글")
    void test_12_update_nonexistent_board() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.TEST_USERNAME)
                        .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 존재하지 않는 게시글 수정 시도
        mockMvc.perform(put("/api/boards/999999")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated Title")
                        .param("content", "Updated Content"))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("13. 게시글 삭제 실패 - 존재하지 않는 게시글")
    void test_13_delete_nonexistent_board() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.TEST_USERNAME)
                        .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 존재하지 않는 게시글 삭제 시도
        mockMvc.perform(delete("/api/boards/999999")
                        .session(session))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("14. 게시글 생성 실패 - 필수 필드 누락")
    void test_14_create_board_missing_field() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", DataInitializer.TEST_USERNAME)
                        .param("password", DataInitializer.TEST_PASSWORD))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // 제목 없이 게시글 생성 시도
        mockMvc.perform(post("/api/boards")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", "Content without title"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("15. 게시글 목록 조회 - 페이지네이션 테스트")
    void test_15_list_boards_pagination() throws Exception {
        // 첫 번째 페이지 조회 (크기 5)
        mockMvc.perform(get("/api/boards")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.pageable.pageSize").value(5))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andDo(print());
    }

    @Test
    @DisplayName("16. 게시글 검색 - 빈 결과")
    void test_16_search_boards_empty_result() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        
        mockMvc.perform(get("/api/boards")
                        .param("search", "nonexistent_search_" + uniqueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andDo(print());
    }
}
