package dev.xiyo.bunnyholes.boardhole.user.presentation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import dev.xiyo.bunnyholes.boardhole.shared.config.ApiSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.constants.ApiPaths;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;
import dev.xiyo.bunnyholes.boardhole.shared.exception.UnauthorizedException;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UpdatePasswordCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UpdateUserCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.application.query.UserQueryService;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.PasswordUpdateRequest;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserResponse;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserUpdateRequest;
import dev.xiyo.bunnyholes.boardhole.user.presentation.mapper.UserWebMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(ApiSecurityConfig.class)
@DisplayName("UserController MockMvc 테스트")
@Tag("unit")
@Tag("user")
class UserControllerTest {

    private static final String USERS_URL = ApiPaths.USERS;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private UserQueryService userQueryService;

    @MockitoBean
    private UserWebMapper userWebMapper;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private EntityManager entityManager;

    private UUID userId;
    private UserResult userResult;
    private UserResponse userResponse;

    private static Stream<Arguments> searchParameters() {
        return Stream.of(
                Arguments.of("검색어 없이", null),
                Arguments.of("검색어 포함", "tester"),
                Arguments.of("공백 검색어", "   ")
        );
    }

    private static MockHttpServletRequestBuilder form(MockHttpServletRequestBuilder builder) {
        return builder.contentType(MediaType.APPLICATION_FORM_URLENCODED).with(csrf());
    }

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        userResult = new UserResult(userId, "tester", "테스터", "tester@example.com",
                LocalDateTime.now(), LocalDateTime.now(), null, Set.of(Role.USER));
        userResponse = new UserResponse(userResult.id(), userResult.username(), userResult.name(), userResult.email(),
                userResult.createdAt(), userResult.lastLogin(), userResult.roles());
    }

    private Page<UserResult> singleUserPage(Pageable pageable) {
        return new PageImpl<>(List.of(userResult), pageable, 1);
    }

    private UserUpdateRequest validUpdateRequest() {
        return new UserUpdateRequest("새 이름");
    }

    private PasswordUpdateRequest validPasswordUpdateRequest() {
        return new PasswordUpdateRequest("OldPass123!", "NewPass123!", "NewPass123!");
    }

    @Nested
    @DisplayName("GET /api/users - 사용자 목록 조회")
    class ListUsers {

        @Nested
        @DisplayName("성공")
        class Success {

            @ParameterizedTest(name = "{0}")
            @MethodSource("dev.xiyo.bunnyholes.boardhole.user.presentation.UserControllerTest#searchParameters")
            @WithMockUser(roles = "ADMIN")
            @DisplayName("✅ 관리자는 검색 조건과 무관하게 목록을 조회할 수 있다")
            void shouldListUsers(String scenario, String search) throws Exception {
                Pageable pageable = PageRequest.of(0, 20);
                Page<UserResult> page = singleUserPage(pageable);

                if (search == null || search.trim().isEmpty()) {
                    given(userQueryService.listWithPaging(any(Pageable.class))).willReturn(page);
                } else {
                    given(userQueryService.listWithPaging(any(Pageable.class), eq(search.trim()))).willReturn(page);
                }
                given(userWebMapper.toResponse(userResult)).willReturn(userResponse);

                MockHttpServletRequestBuilder request = get(USERS_URL)
                        .param("page", "0")
                        .param("size", "20");
                if (search != null) {
                    request = request.param("search", search);
                }

                mockMvc.perform(request)
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content[0].username").value(userResponse.username()))
                        .andExpect(jsonPath("$.content[0].roles[0]").value("USER"));

                if (search == null || search.trim().isEmpty()) {
                    then(userQueryService).should().listWithPaging(any(Pageable.class));
                } else {
                    then(userQueryService).should().listWithPaging(any(Pageable.class), eq(search.trim()));
                }
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증되지 않은 사용자는 목록을 조회할 수 없다")
                void shouldRejectAnonymous() throws Exception {
                    mockMvc.perform(get(USERS_URL))
                            .andExpect(status().isUnauthorized());

                    then(userQueryService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(roles = "ADMIN")
                @DisplayName("❌ 내부 오류 발생 시 500 ProblemDetail을 반환한다")
                void shouldHandleUnexpectedFailure() throws Exception {
                    given(userQueryService.listWithPaging(any(Pageable.class)))
                            .willThrow(new IllegalStateException("목록 조회 실패"));

                    mockMvc.perform(get(USERS_URL))
                            .andExpect(status().isInternalServerError())
                            .andExpect(jsonPath("$.type").value("urn:problem-type:internal-error"));
                }
            }

            @Nested
            @DisplayName("엣지")
            class Edge {

                @Test
                @WithMockUser(roles = "ADMIN")
                @DisplayName("❌ 잘못된 정렬 방향이면 400 ProblemDetail을 반환한다")
                void shouldRejectInvalidSortDirection() throws Exception {
                    given(userQueryService.listWithPaging(any(Pageable.class)))
                            .willThrow(new IllegalArgumentException("Sort direction must be ASC or DESC"));

                    mockMvc.perform(get(USERS_URL).param("sort", "createdAt,upwards"))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.type").value("urn:problem-type:invalid-sort"))
                            .andExpect(jsonPath("$.sort[0]").value("createdAt,upwards"));
                }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/users/{username} - 사용자 상세 조회")
    class GetUser {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "tester", roles = "USER")
            @DisplayName("✅ 인증 사용자는 사용자 정보를 조회할 수 있다")
            void shouldGetUser() throws Exception {
                given(userQueryService.get("tester")).willReturn(userResult);
                given(userWebMapper.toResponse(userResult)).willReturn(userResponse);

                mockMvc.perform(get(USERS_URL + "/tester"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value(userResponse.username()))
                        .andExpect(jsonPath("$.name").value(userResponse.name()));

                then(userQueryService).should().get("tester");
                then(userWebMapper).should().toResponse(userResult);
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증되지 않으면 상세 조회가 거부된다")
                void shouldRejectAnonymous() throws Exception {
                    mockMvc.perform(get(USERS_URL + "/tester"))
                            .andExpect(status().isUnauthorized());

                    then(userQueryService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "tester", roles = "USER")
                @DisplayName("❌ 존재하지 않는 사용자는 404 ProblemDetail을 반환한다")
                void shouldReturnNotFound() throws Exception {
                    given(userQueryService.get("tester")).willThrow(new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

                    mockMvc.perform(get(USERS_URL + "/tester"))
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.status").value(404));
                }
            }
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{username} - 사용자 정보 수정")
    class UpdateUser {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "tester", roles = "USER")
            @DisplayName("✅ 인증 사용자는 자신의 정보를 수정할 수 있다")
            void shouldUpdateUser() throws Exception {
                UserUpdateRequest request = validUpdateRequest();
                UpdateUserCommand command = new UpdateUserCommand("tester", request.name());

                given(userWebMapper.toUpdateCommand(eq("tester"), any(UserUpdateRequest.class))).willReturn(command);
                given(userCommandService.update(command)).willReturn(userResult);
                given(userWebMapper.toResponse(userResult)).willReturn(userResponse);

                mockMvc.perform(form(put(USERS_URL + "/tester"))
                                .param("name", request.name()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value(userResponse.username()));

                then(userWebMapper).should().toUpdateCommand(eq("tester"), any(UserUpdateRequest.class));
                then(userCommandService).should().update(command);
                then(userWebMapper).should().toResponse(userResult);
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증되지 않은 사용자는 정보를 수정할 수 없다")
                void shouldRejectAnonymousUpdate() throws Exception {
                    mockMvc.perform(form(put(USERS_URL + "/tester"))
                                    .param("name", "누구"))
                            .andExpect(status().isUnauthorized());

                    then(userCommandService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "tester", roles = "USER")
                @DisplayName("❌ 존재하지 않는 사용자 수정 시 404 ProblemDetail을 반환한다")
                void shouldReturnNotFoundOnUpdate() throws Exception {
                    UserUpdateRequest request = validUpdateRequest();
                    UpdateUserCommand command = new UpdateUserCommand("tester", request.name());

                    given(userWebMapper.toUpdateCommand(eq("tester"), any(UserUpdateRequest.class))).willReturn(command);
                    willThrow(new ResourceNotFoundException("사용자를 찾을 수 없습니다."))
                            .given(userCommandService)
                            .update(command);

                    mockMvc.perform(form(put(USERS_URL + "/tester"))
                                    .param("name", request.name()))
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.status").value(404));
                }
            }

            @Nested
            @DisplayName("엣지")
            class Edge {

                @Test
                @WithMockUser(username = "tester", roles = "USER")
                @DisplayName("❌ 이름을 비우면 422 ProblemDetail을 반환한다")
                void shouldValidateBlankName() throws Exception {
                    mockMvc.perform(form(put(USERS_URL + "/tester"))
                                    .param("name", " "))
                            .andExpect(status().isUnprocessableEntity())
                            .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                            .andExpect(jsonPath("$.errors").isArray());

                    then(userCommandService).shouldHaveNoInteractions();
                }
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{username} - 사용자 삭제")
    class DeleteUser {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "tester", roles = "USER")
            @DisplayName("✅ 인증 사용자는 계정을 삭제할 수 있다")
            void shouldDeleteUser() throws Exception {
                mockMvc.perform(form(delete(USERS_URL + "/tester")))
                        .andExpect(status().isNoContent());

                then(userCommandService).should().delete("tester");
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증되지 않은 사용자는 계정을 삭제할 수 없다")
                void shouldRejectAnonymousDelete() throws Exception {
                    mockMvc.perform(form(delete(USERS_URL + "/tester")))
                            .andExpect(status().isUnauthorized());

                    then(userCommandService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "tester", roles = "USER")
                @DisplayName("❌ 존재하지 않는 사용자 삭제 시 404 ProblemDetail을 반환한다")
                void shouldReturnNotFoundOnDelete() throws Exception {
                    willThrow(new ResourceNotFoundException("사용자를 찾을 수 없습니다."))
                            .given(userCommandService)
                            .delete("tester");

                    mockMvc.perform(form(delete(USERS_URL + "/tester")))
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.status").value(404));
                }
            }
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{username}/password - 패스워드 변경")
    class UpdatePassword {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "tester", roles = "USER")
            @DisplayName("✅ 패스워드 변경 요청을 처리한다")
            void shouldUpdatePassword() throws Exception {
                PasswordUpdateRequest request = validPasswordUpdateRequest();
                UpdatePasswordCommand command = new UpdatePasswordCommand("tester", request.currentPassword(),
                        request.newPassword(), request.confirmPassword());

                given(userWebMapper.toUpdatePasswordCommand(eq("tester"), any(PasswordUpdateRequest.class))).willReturn(command);

                mockMvc.perform(form(patch(USERS_URL + "/tester/password"))
                                .param("currentPassword", request.currentPassword())
                                .param("newPassword", request.newPassword())
                                .param("confirmPassword", request.confirmPassword()))
                        .andExpect(status().isNoContent());

                ArgumentCaptor<UpdatePasswordCommand> captor = ArgumentCaptor.forClass(UpdatePasswordCommand.class);
                then(userCommandService).should().updatePassword(captor.capture());

                UpdatePasswordCommand captured = captor.getValue();
                assertThat(captured.username()).isEqualTo("tester");
                assertThat(captured.newPassword()).isEqualTo(request.newPassword());
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증되지 않은 사용자는 패스워드를 변경할 수 없다")
                void shouldRejectAnonymousPasswordUpdate() throws Exception {
                    mockMvc.perform(form(patch(USERS_URL + "/tester/password"))
                                    .param("currentPassword", "old")
                                    .param("newPassword", "new")
                                    .param("confirmPassword", "new"))
                            .andExpect(status().isUnauthorized());

                    then(userCommandService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "tester", roles = "USER")
                @DisplayName("❌ 현재 비밀번호가 틀리면 401 ProblemDetail을 반환한다")
                void shouldReturnUnauthorizedWhenCurrentPasswordMismatch() throws Exception {
                    PasswordUpdateRequest request = validPasswordUpdateRequest();
                    UpdatePasswordCommand command = new UpdatePasswordCommand("tester", request.currentPassword(),
                            request.newPassword(), request.confirmPassword());

                    given(userWebMapper.toUpdatePasswordCommand(eq("tester"), any(PasswordUpdateRequest.class))).willReturn(command);
                    willThrow(new UnauthorizedException("현재 비밀번호가 일치하지 않습니다."))
                            .given(userCommandService)
                            .updatePassword(command);

                    mockMvc.perform(form(patch(USERS_URL + "/tester/password"))
                                    .param("currentPassword", request.currentPassword())
                                    .param("newPassword", request.newPassword())
                                    .param("confirmPassword", request.confirmPassword()))
                            .andExpect(status().isUnauthorized())
                            .andExpect(jsonPath("$.type").value("urn:problem-type:unauthorized"));
                }
            }

            @Nested
            @DisplayName("엣지")
            class Edge {

                @Test
                @WithMockUser(username = "tester", roles = "USER")
                @DisplayName("❌ 새 비밀번호가 일치하지 않으면 422 ProblemDetail을 반환한다")
                void shouldValidatePasswordConfirmation() throws Exception {
                    mockMvc.perform(form(patch(USERS_URL + "/tester/password"))
                                    .param("currentPassword", "OldPass123!")
                                    .param("newPassword", "NewPass123!")
                                    .param("confirmPassword", "Mismatch123!"))
                            .andExpect(status().isUnprocessableEntity())
                            .andExpect(jsonPath("$.type").value("urn:problem-type:validation-error"))
                            .andExpect(jsonPath("$.errors").isArray());

                    then(userCommandService).shouldHaveNoInteractions();
                }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/users/me - 현재 사용자 정보")
    class CurrentUser {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(username = "tester", roles = "USER")
            @DisplayName("✅ 로그인한 사용자는 자신의 정보를 조회할 수 있다")
            void shouldReturnCurrentUser() throws Exception {
                given(userQueryService.get("tester")).willReturn(userResult);
                given(userWebMapper.toResponse(userResult)).willReturn(userResponse);

                mockMvc.perform(get(USERS_URL + ApiPaths.USERS_ME))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.username").value(userResponse.username()));

                then(userQueryService).should().get("tester");
            }
        }

        @Nested
        @DisplayName("실패")
        class Failures {

            @Nested
            @DisplayName("인증")
            class Authentication {

                @Test
                @WithAnonymousUser
                @DisplayName("❌ 인증되지 않으면 현재 사용자 정보를 조회할 수 없다")
                void shouldRejectAnonymous() throws Exception {
                    mockMvc.perform(get(USERS_URL + ApiPaths.USERS_ME))
                            .andExpect(status().isUnauthorized());

                    then(userQueryService).shouldHaveNoInteractions();
                }
            }

            @Nested
            @DisplayName("일반")
            class General {

                @Test
                @WithMockUser(username = "tester", roles = "USER")
                @DisplayName("❌ 사용자 정보가 없으면 404 ProblemDetail을 반환한다")
                void shouldReturnNotFoundWhenCurrentUserMissing() throws Exception {
                    given(userQueryService.get("tester")).willThrow(new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

                    mockMvc.perform(get(USERS_URL + ApiPaths.USERS_ME))
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.status").value(404));
                }
            }
        }
    }
}
