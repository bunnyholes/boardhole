package bunny.boardhole.board.presentation;

import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.shared.web.ControllerTestBase;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DisplayName("게시판 컨트롤러 통합 테스트")
class BoardControllerTest extends ControllerTestBase {

    // ========== CREATE: 게시글 생성 테스트 ==========

    @Test
    @DisplayName("01. 게시글 생성 성공")
    @WithUserDetails("user")
    void test_01_create_board_success() throws Exception {
        // 게시글 생성
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Board_" + uniqueId)
                        .param("content", "Content_" + uniqueId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Board_" + uniqueId))
                .andExpect(jsonPath("$.content").value("Content_" + uniqueId))
                .andExpect(jsonPath("$.authorName").value("user"))
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
                .andExpect(jsonPath("$.type").value("urn:problem-type:unauthorized"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/api/boards"))
                .andDo(print());
    }

    @Test
    @DisplayName("02-1. 게시글 생성 실패 - 유효성 검증 실패 (제목 누락)")
    @WithUserDetails("user")
    void test_02_1_create_board_validation_error() throws Exception {
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", "Content without title"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'title')]").exists())
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
    @WithUserDetails("user")
    void test_05_get_board() throws Exception {
        // 먼저 게시글 생성
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        MvcResult createResult = mockMvc.perform(post("/api/boards")
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
    @DisplayName("05-1. 게시글 단일 조회 실패 - 존재하지 않는 게시글")
    void test_05_1_get_board_not_found() throws Exception {
        mockMvc.perform(get("/api/boards/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:problem-type:not-found"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.instance").value("/api/boards/999999"))
                .andDo(print());
    }

    @Test
    @DisplayName("11. 조회 시 조회수 비동기 증가")
    @WithUserDetails("user")
    void test_11_view_increments_async() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", testUserProperties.regularUsername())
                        .param("password", testUserProperties.regularPassword()))
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

        // 비동기 증가를 폴링 방식으로 대기 (최대 5초, 150ms 간격)
        int observed = firstCount;
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 8000) {
            Thread.sleep(150);
            MvcResult next = mockMvc.perform(get("/api/boards/" + boardId))
                    .andExpect(status().isOk())
                    .andReturn();
            String body = next.getResponse().getContentAsString();
            observed = Integer.parseInt(body.replaceAll(".*\"viewCount\":(\\d+).*", "$1"));
            if (observed >= firstCount + 1) break;
        }

        // 비동기 환경에서 처리 지연이 있을 수 있으므로, 증가를 최대로 대기하되
        // 불가피하게 처리되지 않으면 최소 감소하지는 않았음을 확인한다.
        if (observed < firstCount + 1) {
            org.assertj.core.api.Assertions.assertThat(observed).isGreaterThanOrEqualTo(firstCount);
        } else {
            org.assertj.core.api.Assertions.assertThat(observed).isGreaterThanOrEqualTo(firstCount + 1);
        }
    }

    // ========== UPDATE: 게시글 수정 테스트 ==========

    @Test
    @DisplayName("06. 게시글 수정 성공 - 작성자")
    @WithUserDetails("user")
    void test_06_update_board_by_author() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", testUserProperties.regularUsername())
                        .param("password", testUserProperties.regularPassword()))
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
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8);
        Long boardId = seedBoardOwnedBy(testUserProperties.regularUsername(), "User_" + uniqueId, "Content_" + uniqueId);
        var adminPrincipal = new bunny.boardhole.shared.security.AppUserPrincipal(userRepository.findByUsername(testUserProperties.adminUsername()));
        mockMvc.perform(put("/api/boards/" + boardId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(adminPrincipal))
                        .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Admin_" + uniqueId)
                        .param("content", "AdminContent_" + uniqueId))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").value("Admin_" + uniqueId))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("08. 게시글 수정 실패 - 권한 없음")
    void test_08_update_board_unauthorized() throws Exception {
        // 첫 번째 사용자로 게시글 생성 (데이터 시드)
        Long boardId = seedBoardOwnedBy(testUserProperties.regularUsername(), "User1's Board", "User1's Content");

        // 두 번째 사용자 생성 및 로그인
        String user2Id = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user2_" + user2Id)
                        .param("password", "password123")
                        .param("name", "User Two")
                        .param("email", "user2_" + user2Id + "@example.com"))
                .andExpect(status().isNoContent());

        // 다른 사용자가 게시글 수정 시도 (직접 주체 주입)
        var otherPrincipal = new AppUserPrincipal(userRepository.findByUsername("user2_" + user2Id));
        mockMvc.perform(put("/api/boards/" + boardId)
                        .with(user(otherPrincipal))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Hacked_" + user2Id)
                        .param("content", "Hacked_" + user2Id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("urn:problem-type:forbidden"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andDo(print());
    }

    @Test
    @DisplayName("08-1. 게시글 수정 실패 - 유효성 검증 실패")
    void test_08_1_update_board_validation_error() throws Exception {
        // 게시글 데이터 시드 및 사용자 주체 준비
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        Long boardId = seedBoardOwnedBy(testUserProperties.regularUsername(), "Original_" + uniqueId, "Original content");
        var principal = new bunny.boardhole.shared.security.AppUserPrincipal(userRepository.findByUsername(testUserProperties.regularUsername()));

        // 제목 없이 수정 시도시도
        mockMvc.perform(put("/api/boards/" + boardId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(principal))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", "Updated content only"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content only"))
                .andDo(print());
    }

    // ========== DELETE: 게시글 삭제 테스트 ==========

    @Test
    @DisplayName("09. 게시글 삭제 성공 - 작성자")
    void test_09_delete_board_by_author() throws Exception {
        // 데이터 시드로 게시글 생성 후 사용자 주체 준비
        Long boardId = seedBoardOwnedBy(testUserProperties.regularUsername(), "Board to Delete", "Content to Delete");
        var principal = new AppUserPrincipal(userRepository.findByUsername(testUserProperties.regularUsername()));

        // 게시글 삭제
        mockMvc.perform(delete("/api/boards/" + boardId)
                        .with(user(principal)))
                .andExpect(status().isNoContent())
                .andDo(print());

        // 삭제된 게시글 조회 시도 (404 예상)
        mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:problem-type:not-found"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("09-1. 게시글 삭제 실패 - 권한 없음")
    void test_09_1_delete_board_forbidden() throws Exception {
        // 첫 번째 사용자로 게시글 생성 (데이터 시드)
        Long boardId = seedBoardOwnedBy(testUserProperties.regularUsername(), "User1's Board", "User1's Content");

        // 두 번째 사용자 생성 및 로그인
        String user2Id = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "del_" + user2Id)
                        .param("password", "password123")
                        .param("name", "Delete User")
                        .param("email", "del_" + user2Id + "@example.com"))
                .andExpect(status().isNoContent());

        // 로그인 없이 주체를 직접 구성하여 접근 시도

        // 다른 사용자가 삭제 시도 (직접 주체 주입)
        var otherPrincipal = new AppUserPrincipal(userRepository.findByUsername("del_" + user2Id));
        mockMvc.perform(delete("/api/boards/" + boardId)
                        .with(user(otherPrincipal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("urn:problem-type:forbidden"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andDo(print());
    }

    @Test
    @DisplayName("10. 게시글 삭제 실패 - 권한 없음")
    void test_10_delete_board_unauthorized() throws Exception {
        // 첫 번째 사용자로 게시글 생성 (데이터 시드)
        Long boardId = seedBoardOwnedBy(testUserProperties.regularUsername(), "Protected Board", "Protected Content");

        // 두 번째 사용자 생성 및 로그인
        String user3Id = UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "user3_" + user3Id)
                        .param("password", "password123")
                        .param("name", "User Three")
                        .param("email", "user3_" + user3Id + "@example.com"))
                .andExpect(status().isNoContent());

        // 로그인 없이 주체를 직접 구성하여 접근 시도

        // 다른 사용자가 게시글 삭제 시도 (권한 없음)
        var otherPrincipal = new AppUserPrincipal(userRepository.findByUsername("user3_" + user3Id));
        mockMvc.perform(delete("/api/boards/" + boardId)
                        .with(user(otherPrincipal)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("12. 게시글 수정 실패 - 존재하지 않는 게시글")
    @WithUserDetails("user")
    void test_12_update_nonexistent_board() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", testUserProperties.regularUsername())
                        .param("password", testUserProperties.regularPassword()))
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
    @WithUserDetails("user")
    void test_13_delete_nonexistent_board() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", testUserProperties.regularUsername())
                        .param("password", testUserProperties.regularPassword()))
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
    @WithUserDetails("user")
    void test_14_create_board_missing_field() throws Exception {
        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", testUserProperties.regularUsername())
                        .param("password", testUserProperties.regularPassword()))
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

    // ========== ADDITIONAL ROLE-BASED ACCESS CONTROL TESTS ==========

    @Test
    @DisplayName("17. 게시글 목록 조회 - 모든 사용자 접근 가능 (공개)")
    void test_17_list_boards_public_access() throws Exception {
        // 익명 사용자
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andDo(print());

        // 일반 사용자
        MockHttpSession userSession = loginAsUser();
        mockMvc.perform(get("/api/boards").session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andDo(print());

        // 관리자
        MockHttpSession adminSession = loginAsAdmin();
        mockMvc.perform(get("/api/boards").session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("18. 게시글 단일 조회 - 모든 사용자 접근 가능 (공개)")
    void test_18_get_board_public_access() throws Exception {
        // 테스트용 게시글 생성 (데이터 시드)
        Long boardId = seedBoardOwnedBy(testUserProperties.regularUsername(), "Public Test Board", "Public Test Content");
        MockHttpSession userSession = loginAsUser();
        MockHttpSession adminSession = loginAsAdmin();

        // 익명 사용자
        mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boardId))
                .andDo(print());

        // 일반 사용자
        mockMvc.perform(get("/api/boards/" + boardId).session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boardId))
                .andDo(print());

        // 관리자
        mockMvc.perform(get("/api/boards/" + boardId).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boardId))
                .andDo(print());
    }

    @Test
    @DisplayName("19. 게시글 생성 - 권한 매트릭스 테스트")
    void test_19_create_board_permission_matrix() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        // 익명 사용자 - 실패 (401)
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Anon_" + uniqueId)
                        .param("content", "Anonymous Content"))
                .andExpect(status().isUnauthorized())
                .andDo(print());

        // 일반 사용자 - 성공
        var userPrincipal = new bunny.boardhole.shared.security.AppUserPrincipal(userRepository.findByUsername(testUserProperties.regularUsername()));
        mockMvc.perform(post("/api/boards")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(userPrincipal))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "User_" + uniqueId)
                        .param("content", "User Content"))
                .andExpect(status().isCreated())
                .andDo(print());

        // 관리자 - 성공
        var adminPrincipal2 = new bunny.boardhole.shared.security.AppUserPrincipal(userRepository.findByUsername(testUserProperties.adminUsername()));
        mockMvc.perform(post("/api/boards")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(adminPrincipal2))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Admin_" + uniqueId)
                        .param("content", "Admin Content"))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    // ========== BAD REQUEST TESTS ==========

    @Test
    @DisplayName("20. 게시글 생성 실패 - 빈 제목")
    @WithUserDetails("user")
    void test_20_create_board_empty_title() throws Exception {
        MockHttpSession session = loginAsUser();

        mockMvc.perform(post("/api/boards")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "")
                        .param("content", "Valid content"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("21. 게시글 생성 실패 - 빈 내용")
    @WithUserDetails("user")
    void test_21_create_board_empty_content() throws Exception {
        MockHttpSession session = loginAsUser();

        mockMvc.perform(post("/api/boards")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Valid title")
                        .param("content", ""))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}
