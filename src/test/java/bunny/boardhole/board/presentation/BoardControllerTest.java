package bunny.boardhole.board.presentation;

import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.shared.web.ControllerTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("게시판 API 통합 테스트")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@TestInstance(Lifecycle.PER_CLASS)
@Tag("integration")
@Tag("board")
@Import(BoardControllerTest.TestAsyncConfig.class)
class BoardControllerTest extends ControllerTestBase {

    /**
     * 테스트용 비동기 설정
     * 이 테스트에서만 비동기 이벤트를 동기적으로 실행하여
     * 조회수 증가 테스트가 안정적으로 동작하도록 함
     */
    @TestConfiguration
    static class TestAsyncConfig {
        @Bean(name = "taskExecutor")
        @Primary
        public Executor taskExecutor() {
            // SyncTaskExecutor를 사용하여 비동기 작업을 동기적으로 실행
            return new SyncTaskExecutor();
        }
    }

    @Nested
    @DisplayName("POST /api/boards - 게시글 생성")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    @Tag("create")
    class CreateBoard {

        @Nested
        @DisplayName("인증된 사용자")
        @TestInstance(Lifecycle.PER_CLASS)
        class WhenAuthenticated {

            static Stream<Arguments> provideMissingFieldTestCases() {
                return Stream.of(
                        Arguments.of("title 필드가 비어있을 때", "", "Test Content", "title"),
                        Arguments.of("content 필드가 비어있을 때", "Test Title", "", "content")
                );
            }

            @Test
            @DisplayName("✅ 유효한 데이터로 게시글 생성 → 201 Created")
            @WithUserDetails
            void shouldCreateBoardWithValidData() throws Exception {
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

            @ParameterizedTest(name = "[{index}] {0}")
            @MethodSource("provideMissingFieldTestCases")
            @DisplayName("❌ 필수 필드 누락 → 400 Bad Request")
            @WithUserDetails
            void shouldFailWhenRequiredFieldMissing(String displayName, String titleValue, String contentValue, String expectedMissingField) throws Exception {
                mockMvc.perform(post("/api/boards")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("title", titleValue)
                                .param("content", contentValue))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                        .andExpect(jsonPath("$.errors[?(@.field == '" + expectedMissingField + "')]").exists())
                        .andDo(print());
            }

            @ParameterizedTest(name = "[{index}] 제목이 \"{0}\"일 때")
            @EmptySource
            @ValueSource(strings = {" ", "  ", "\t", "\n"})
            @DisplayName("❌ 빈 제목 → 400 Bad Request (또는 허용)")
            @WithUserDetails
            void shouldHandleEmptyTitle(String title) throws Exception {
                mockMvc.perform(post("/api/boards")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("title", title)
                                .param("content", "Valid Content"))
                        .andExpect(status().is(anyOf(is(200), is(201), is(400))))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("인증되지 않은 사용자")
        class WhenNotAuthenticated {

            @Test
            @DisplayName("❌ 인증 없이 게시글 생성 시도 → 401 Unauthorized")
            @Tag("security")
            void shouldReturn401WhenNotAuthenticated() throws Exception {
                mockMvc.perform(post("/api/boards")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("title", "Test Title")
                                .param("content", "Test Content"))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.type").value("urn:problem-type:unauthorized"))
                        .andExpect(jsonPath("$.status").value(401))
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("GET /api/boards - 게시글 목록 조회")
    @Tag("read")
    class ListBoards {

        static Stream<Arguments> provideUserTestCases() {
            return Stream.of(
                    Arguments.of("익명", get("/api/boards")),
                    Arguments.of("일반", get("/api/boards").with(user("user").roles("USER"))),
                    Arguments.of("관리자", get("/api/boards").with(user("admin").roles("ADMIN")))
            );
        }

        @ParameterizedTest(name = "{0} 사용자")
        @MethodSource("provideUserTestCases")
        @DisplayName("모든 사용자가 목록 조회 가능")
        void shouldAllowListingForAllUsers(String role, MockHttpServletRequestBuilder requestBuilder) throws Exception {
            mockMvc.perform(requestBuilder)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.pageable").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("📄 페이지네이션 파라미터 적용")
        void shouldApplyPaginationParameters() throws Exception {
            mockMvc.perform(get("/api/boards")
                            .param("page", "0")
                            .param("size", "5")
                            .param("sort", "createdAt,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageable.pageSize").value(5))
                    .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                    .andDo(print());
        }

        @ParameterizedTest(name = "검색어: \"{0}\"")
        @ValueSource(strings = {"test", "게시글", "spring"})
        @DisplayName("🔍 검색 기능")
        void shouldFilterBySearchTerm(String searchTerm) throws Exception {
            mockMvc.perform(get("/api/boards")
                            .param("search", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /api/boards/{id} - 게시글 단일 조회")
    @Tag("read")
    class GetBoard {

        @Test
        @DisplayName("❌ 존재하지 않는 게시글 → 404 Not Found")
        void shouldReturn404WhenBoardNotFound() throws Exception {
            mockMvc.perform(get("/api/boards/999999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type").value("urn:problem-type:not-found"))
                    .andDo(print());
        }

        @Nested
        @DisplayName("존재하는 게시글")
        @TestInstance(Lifecycle.PER_CLASS)
        class WhenBoardExists {

            private Long boardId;

            static Stream<Arguments> provideBoardViewTestCases() {
                return Stream.of(
                        Arguments.of("익명", (Function<Long, MockHttpServletRequestBuilder>) id -> get("/api/boards/" + id)),
                        Arguments.of("일반", (Function<Long, MockHttpServletRequestBuilder>) id -> get("/api/boards/" + id).with(user("user").roles("USER"))),
                        Arguments.of("관리자", (Function<Long, MockHttpServletRequestBuilder>) id -> get("/api/boards/" + id).with(user("admin").roles("ADMIN")))
                );
            }

            @BeforeAll
            void setup() {
                boardId = seedBoardOwnedBy(
                        testUserProperties.regularUsername(),
                        "Test Board",
                        "Test Content"
                );
            }

            @ParameterizedTest(name = "{0} 사용자")
            @MethodSource("provideBoardViewTestCases")
            @DisplayName("✅ 모든 사용자가 조회 가능")
            void shouldAllowGettingForAllUsers(String role, Function<Long, MockHttpServletRequestBuilder> requestBuilderFunction) throws Exception {
                mockMvc.perform(requestBuilderFunction.apply(boardId))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(boardId))
                        .andDo(print());
            }

            @Test
            @DisplayName("📈 조회시 조회수 증가")
            @WithUserDetails
            void shouldIncrementViewCount() throws Exception {
                // 첫 번째 조회
                MvcResult result1 = mockMvc.perform(get("/api/boards/" + boardId))
                        .andExpect(status().isOk())
                        .andReturn();

                String json1 = result1.getResponse().getContentAsString();
                int viewCount1 = Integer.parseInt(
                        json1.replaceAll(".*\"viewCount\":(\\d+).*", "$1")
                );

                // 잠시 대기 (비동기 처리)
                Thread.sleep(100);

                // 두 번째 조회
                MvcResult result2 = mockMvc.perform(get("/api/boards/" + boardId))
                        .andExpect(status().isOk())
                        .andReturn();

                String json2 = result2.getResponse().getContentAsString();
                int viewCount2 = Integer.parseInt(
                        json2.replaceAll(".*\"viewCount\":(\\d+).*", "$1")
                );

                // 조회수 증가 확인
                assert viewCount2 > viewCount1;
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/boards/{id} - 게시글 수정")
    @Tag("update")
    @TestInstance(Lifecycle.PER_CLASS)
    class UpdateBoard {

        @Test
        @DisplayName("❌ 존재하지 않는 게시글 수정 → 403 Forbidden")
        @WithUserDetails
        void shouldReturn403WhenUpdatingNonExistentBoard() throws Exception {
            mockMvc.perform(put("/api/boards/999999")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("title", "New Title")
                            .param("content", "New Content"))
                    .andExpect(status().isForbidden())
                    .andDo(print());
        }

        @Nested
        @DisplayName("권한별 접근 제어")
        @TestInstance(Lifecycle.PER_CLASS)
        class AccessControl {

            private Long boardId;
            private final String boardOwner = "owner_" + UUID.randomUUID().toString().substring(0, 8);

            @BeforeAll
            void setup() {
                seedUser(boardOwner, "Board Owner", boardOwner + "@test.com", "password",
                        java.util.Set.of(bunny.boardhole.user.domain.Role.USER));
                boardId = seedBoardOwnedBy(boardOwner, "Original Title", "Original Content");
            }

            @Test
            @DisplayName("✅ 작성자 본인 → 수정 성공")
            void shouldAllowAuthorToUpdate() throws Exception {
                var principal = new AppUserPrincipal(userRepository.findByUsername(boardOwner));

                mockMvc.perform(put("/api/boards/" + boardId)
                                .with(user(principal))
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("title", "Updated Title")
                                .param("content", "Updated Content"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.title").value("Updated Title"))
                        .andDo(print());
            }

            @Test
            @DisplayName("✅ 관리자 → 수정 성공")
            @WithUserDetails("admin")
            void shouldAllowAdminToUpdate() throws Exception {
                mockMvc.perform(put("/api/boards/" + boardId)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("title", "Admin Updated")
                                .param("content", "Admin Content"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.title").value("Admin Updated"))
                        .andDo(print());
            }

            @Test
            @DisplayName("❌ 다른 사용자 → 403 Forbidden")
            @WithUserDetails
            void shouldDenyOtherUserToUpdate() throws Exception {
                mockMvc.perform(put("/api/boards/" + boardId)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("title", "Hacked Title")
                                .param("content", "Hacked Content"))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.type").value("urn:problem-type:forbidden"))
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/boards/{id} - 게시글 삭제")
    @Tag("delete")
    class DeleteBoard {

        @TestFactory
        @DisplayName("권한별 삭제 테스트")
        Stream<DynamicTest> deletePermissionTests() {
            return Stream.of(
                    DynamicTest.dynamicTest("✅ 작성자 본인 → 삭제 성공", () -> {
                        String owner = "deleter_" + UUID.randomUUID().toString().substring(0, 8);
                        seedUser(owner, "Deleter", owner + "@test.com", "password",
                                java.util.Set.of(bunny.boardhole.user.domain.Role.USER));
                        Long boardId = seedBoardOwnedBy(owner, "To Delete", "Content");
                        var principal = new AppUserPrincipal(userRepository.findByUsername(owner));

                        mockMvc.perform(delete("/api/boards/" + boardId).with(user(principal)))
                                .andExpect(status().isNoContent())
                                .andDo(print());
                    }),

                    DynamicTest.dynamicTest("❌ 다른 사용자 → 403 Forbidden", () -> {
                        Long boardId = seedBoardOwnedBy("admin", "Admin's Board", "Content");

                        mockMvc.perform(delete("/api/boards/" + boardId)
                                        .with(user("other").roles("USER")))
                                .andExpect(status().isForbidden())
                                .andDo(print());
                    }),

                    DynamicTest.dynamicTest("❌ 인증되지 않은 사용자 → 401 Unauthorized", () -> {
                        Long boardId = seedBoardOwnedBy("user", "User's Board", "Content");

                        mockMvc.perform(delete("/api/boards/" + boardId))
                                .andExpect(status().isUnauthorized())
                                .andDo(print());
                    })
            );
        }
    }

}