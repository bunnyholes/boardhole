package dev.xiyo.bunnyholes.boardhole.user.presentation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

import dev.xiyo.bunnyholes.boardhole.shared.config.ApiSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.constants.ApiPaths;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
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

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        userResult = new UserResult(userId, "tester", "테스터", "tester@example.com",
                LocalDateTime.now(), LocalDateTime.now(), null, Set.of(Role.USER));
        userResponse = new UserResponse(userResult.id(), userResult.username(), userResult.name(), userResult.email(),
                userResult.createdAt(), userResult.lastLogin(), userResult.roles());
    }

    @Nested
    @DisplayName("GET /api/users - 사용자 목록 조회")
    class ListUsers {

        @Test
        @WithAnonymousUser
        @DisplayName("❌ 인증되지 않은 사용자는 목록을 조회할 수 없다")
        void shouldRejectAnonymous() throws Exception {
            mockMvc.perform(get(USERS_URL))
                    .andExpect(status().isUnauthorized());

            then(userQueryService).shouldHaveNoInteractions();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ 관리자는 전체 사용자를 조회할 수 있다")
        void shouldListUsersForAdmin() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            Page<UserResult> page = new PageImpl<>(List.of(userResult), pageable, 1);

            given(userQueryService.listWithPaging(any(Pageable.class))).willReturn(page);
            given(userWebMapper.toResponse(userResult)).willReturn(userResponse);

            mockMvc.perform(get(USERS_URL)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].username").value(userResponse.username()))
                    .andExpect(jsonPath("$.content[0].roles[0]").value("USER"));

            then(userQueryService).should().listWithPaging(any(Pageable.class));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ 검색어를 제공하면 트림된 검색어로 조회한다")
        void shouldListUsersWithSearch() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            Page<UserResult> page = new PageImpl<>(List.of(userResult), pageable, 1);
            String keyword = "tester";

            given(userQueryService.listWithPaging(any(Pageable.class), eq(keyword))).willReturn(page);
            given(userWebMapper.toResponse(userResult)).willReturn(userResponse);

            mockMvc.perform(get(USERS_URL)
                            .param("search", " " + keyword + " "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].username").value(userResponse.username()));

            then(userQueryService).should().listWithPaging(any(Pageable.class), eq(keyword));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("✅ 공백 검색어는 전체 조회로 처리한다")
        void shouldTreatBlankSearchAsListAll() throws Exception {
            Pageable pageable = PageRequest.of(0, 20);
            Page<UserResult> page = new PageImpl<>(List.of(userResult), pageable, 1);

            given(userQueryService.listWithPaging(any(Pageable.class))).willReturn(page);
            given(userWebMapper.toResponse(userResult)).willReturn(userResponse);

            mockMvc.perform(get(USERS_URL)
                            .param("search", "   "))
                    .andExpect(status().isOk());

            then(userQueryService).should().listWithPaging(any(Pageable.class));
            then(userQueryService).should(never()).listWithPaging(any(Pageable.class), anyString());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{username} - 사용자 상세 조회")
    class GetUser {

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
    @DisplayName("PUT /api/users/{username} - 사용자 정보 수정")
    class UpdateUser {

        @Test
        @WithMockUser(username = "tester", roles = "USER")
        @DisplayName("✅ 인증 사용자는 자신의 정보를 수정할 수 있다")
        void shouldUpdateUser() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest("새 이름");
            UpdateUserCommand command = new UpdateUserCommand("tester", request.name());

            given(userWebMapper.toUpdateCommand(eq("tester"), any(UserUpdateRequest.class))).willReturn(command);
            given(userCommandService.update(command)).willReturn(userResult);
            given(userWebMapper.toResponse(userResult)).willReturn(userResponse);

            mockMvc.perform(put(USERS_URL + "/tester")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("name", request.name())
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(userResponse.username()));

            then(userWebMapper).should().toUpdateCommand(eq("tester"), any(UserUpdateRequest.class));
            then(userCommandService).should().update(command);
            then(userWebMapper).should().toResponse(userResult);
        }

        @Test
        @WithAnonymousUser
        @DisplayName("❌ 인증되지 않은 사용자는 정보를 수정할 수 없다")
        void shouldRejectAnonymousUpdate() throws Exception {
            mockMvc.perform(put(USERS_URL + "/tester")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("name", "누구")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            then(userCommandService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{username} - 사용자 삭제")
    class DeleteUser {

        @Test
        @WithMockUser(username = "tester", roles = "USER")
        @DisplayName("✅ 인증 사용자는 계정을 삭제할 수 있다")
        void shouldDeleteUser() throws Exception {
            mockMvc.perform(delete(USERS_URL + "/tester").with(csrf()))
                    .andExpect(status().isNoContent());

            then(userCommandService).should().delete("tester");
        }

        @Test
        @WithAnonymousUser
        @DisplayName("❌ 인증되지 않은 사용자는 계정을 삭제할 수 없다")
        void shouldRejectAnonymousDelete() throws Exception {
            mockMvc.perform(delete(USERS_URL + "/tester").with(csrf()))
                    .andExpect(status().isUnauthorized());

            then(userCommandService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{username}/password - 패스워드 변경")
    class UpdatePassword {

        @Test
        @WithMockUser(username = "tester", roles = "USER")
        @DisplayName("✅ 패스워드 변경 요청을 처리한다")
        void shouldUpdatePassword() throws Exception {
            PasswordUpdateRequest request = new PasswordUpdateRequest("OldPass123!", "NewPass123!", "NewPass123!");
            UpdatePasswordCommand command = new UpdatePasswordCommand("tester", request.currentPassword(),
                    request.newPassword(), request.confirmPassword());

            given(userWebMapper.toUpdatePasswordCommand(eq("tester"), any(PasswordUpdateRequest.class))).willReturn(command);

            mockMvc.perform(patch(USERS_URL + "/tester/password")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("currentPassword", request.currentPassword())
                            .param("newPassword", request.newPassword())
                            .param("confirmPassword", request.confirmPassword())
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            ArgumentCaptor<UpdatePasswordCommand> captor = ArgumentCaptor.forClass(UpdatePasswordCommand.class);
            then(userCommandService).should().updatePassword(captor.capture());

            UpdatePasswordCommand captured = captor.getValue();
            assertThat(captured.username()).isEqualTo("tester");
            assertThat(captured.newPassword()).isEqualTo(request.newPassword());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("❌ 인증되지 않은 사용자는 패스워드를 변경할 수 없다")
        void shouldRejectAnonymousPasswordUpdate() throws Exception {
            mockMvc.perform(patch(USERS_URL + "/tester/password")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("currentPassword", "old")
                            .param("newPassword", "new")
                            .param("confirmPassword", "new")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            then(userCommandService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("GET /api/users/me - 현재 사용자 정보")
    class CurrentUser {

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
}
