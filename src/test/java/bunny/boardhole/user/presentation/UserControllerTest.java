package bunny.boardhole.user.presentation;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.user.application.command.UpdatePasswordCommand;
import bunny.boardhole.user.application.command.UpdateUserCommand;
import bunny.boardhole.user.application.command.UserCommandService;
import bunny.boardhole.user.application.query.UserQueryService;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.presentation.dto.PasswordUpdateRequest;
import bunny.boardhole.user.presentation.dto.UserResponse;
import bunny.boardhole.user.presentation.dto.UserUpdateRequest;
import bunny.boardhole.user.presentation.mapper.UserWebMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 단위 테스트")
@Tag("unit")
@Tag("user")
class UserControllerTest {

    @Mock
    private UserCommandService userCommandService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserWebMapper userWebMapper;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private AppUserPrincipal testPrincipal;
    private UserResult testUserResult;
    private UserResponse testUserResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                       .username("testuser")
                       .password("encoded_password")
                       .name("Test User")
                       .email("test@example.com")
                       .roles(Set.of(Role.USER))
                       .build();
        testPrincipal = new AppUserPrincipal(testUser);

        testUserResult = new UserResult(
                UUID.randomUUID(), "testuser", "Test User", "test@example.com",
                LocalDateTime.now(), null, null, Set.of(Role.USER)
        );

        testUserResponse = new UserResponse(
                UUID.randomUUID(), "testuser", "Test User", "test@example.com",
                LocalDateTime.now(), null, Set.of(Role.USER)
        );

        pageable = PageRequest.of(0, 20);
    }

    @Nested
    @DisplayName("GET /api/users - 사용자 목록 조회")
    class ListUsers {

        @Test
        @DisplayName("✅ 검색어 없이 전체 사용자 목록 조회")
        void shouldListAllUsers() {
            // given
            Page<UserResult> resultPage = new PageImpl<>(Collections.singletonList(testUserResult),
                    pageable, 1);
            Page<UserResponse> responsePage = new PageImpl<>(Collections.singletonList(testUserResponse),
                    pageable, 1);

            given(userQueryService.listWithPaging(pageable)).willReturn(resultPage);
            given(userWebMapper.toResponse(testUserResult)).willReturn(
                    testUserResponse);

            // when
            Page<UserResponse> result = userController.list(null, pageable);

            // then
            assertThat(result).isEqualTo(responsePage);
            then(userQueryService).should().listWithPaging(pageable);
            then(userWebMapper).should().toResponse(testUserResult);
        }

        @Test
        @DisplayName("✅ 검색어로 사용자 검색")
        void shouldSearchUsers() {
            // given
            final String searchTerm = "test";
            Page<UserResult> resultPage = new PageImpl<>(Collections.singletonList(testUserResult),
                    pageable, 1);
            Page<UserResponse> responsePage = new PageImpl<>(Collections.singletonList(testUserResponse),
                    pageable, 1);

            given(userQueryService.listWithPaging(pageable, searchTerm)).willReturn(resultPage);
            given(userWebMapper.toResponse(testUserResult)).willReturn(
                    testUserResponse);

            // when
            Page<UserResponse> result = userController.list(searchTerm, pageable);

            // then
            assertThat(result).isEqualTo(responsePage);
            then(userQueryService).should().listWithPaging(pageable, searchTerm);
            then(userWebMapper).should().toResponse(testUserResult);
        }

        @Test
        @DisplayName("✅ 빈 검색어로 전체 목록 조회")
        void shouldListAllUsersWhenSearchIsEmpty() {
            // given
            final String emptySearch = "   ";
            Page<UserResult> resultPage = new PageImpl<>(Collections.singletonList(testUserResult),
                    pageable, 1);

            given(userQueryService.listWithPaging(pageable)).willReturn(resultPage);
            given(userWebMapper.toResponse(testUserResult)).willReturn(
                    testUserResponse);

            // when
            userController.list(emptySearch, pageable);

            // then
            then(userQueryService).should().listWithPaging(pageable);
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id} - 사용자 단일 조회")
    class GetUser {

        @Test
        @DisplayName("✅ 사용자 ID로 조회 성공")
        void shouldGetUserById() {
            // given
            UUID userId = UUID.randomUUID();
            given(userQueryService.get(userId)).willReturn(testUserResult);
            given(userWebMapper.toResponse(testUserResult)).willReturn(
                    testUserResponse);

            // when
            UserResponse result = userController.get(userId);

            // then
            assertThat(result).isEqualTo(testUserResponse);
            then(userQueryService).should().get(userId);
            then(userWebMapper).should().toResponse(testUserResult);
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id} - 사용자 정보 수정")
    class UpdateUser {

        @Test
        @DisplayName("✅ 사용자 정보 수정 성공")
        void shouldUpdateUserSuccessfully() {
            // given
            UUID userId = UUID.randomUUID();
            UserUpdateRequest request = new UserUpdateRequest("Updated Name");
            UpdateUserCommand command = new UpdateUserCommand(userId, "Updated Name");
            UserResult updatedResult = new UserResult(
                    userId, "testuser", "Updated Name", "updated@example.com",
                    LocalDateTime.now(), null, null, Set.of(Role.USER)
            );
            UserResponse updatedResponse = new UserResponse(
                    userId, "testuser", "Updated Name", "updated@example.com",
                    LocalDateTime.now(), null, Set.of(Role.USER)
            );

            given(userWebMapper.toUpdateCommand(userId, request)).willReturn(command);
            given(userCommandService.update(command)).willReturn(updatedResult);
            given(userWebMapper.toResponse(updatedResult)).willReturn(updatedResponse);

            // when
            UserResponse result = userController.update(userId, request);

            // then
            assertThat(result).isEqualTo(updatedResponse);
            then(userWebMapper).should().toUpdateCommand(userId, request);
            then(userCommandService).should().update(command);
            then(userWebMapper).should().toResponse(updatedResult);
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id} - 사용자 삭제")
    class DeleteUser {

        @Test
        @DisplayName("✅ 사용자 삭제 성공")
        void shouldDeleteUserSuccessfully() {
            // given
            UUID userId = UUID.randomUUID();
            willDoNothing().given(userCommandService).delete(userId);

            // when
            userController.delete(userId);

            // then
            then(userCommandService).should().delete(userId);
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{id}/password - 패스워드 변경")
    class UpdatePassword {

        @Test
        @DisplayName("✅ 패스워드 변경 성공")
        void shouldUpdatePasswordSuccessfully() {
            // given
            UUID userId = UUID.randomUUID();
            PasswordUpdateRequest request = new PasswordUpdateRequest(
                    "currentPassword", "newPassword123!", "newPassword123!"
            );
            UpdatePasswordCommand command = new UpdatePasswordCommand(userId, "currentPassword", "newPassword123!", "newPassword123!");

            given(userWebMapper.toUpdatePasswordCommand(userId, request)).willReturn(command);
            willDoNothing().given(userCommandService).updatePassword(command);

            // when
            userController.updatePassword(userId, request);

            // then
            then(userWebMapper).should().toUpdatePasswordCommand(userId, request);
            then(userCommandService).should().updatePassword(command);
        }

        @Test
        @DisplayName("❌ 패스워드 확인 불일치 시 예외 발생")
        void shouldFailBeanValidationWhenConfirmationMismatch() {
            // given
            PasswordUpdateRequest request = new PasswordUpdateRequest(
                    "currentPassword", "newPassword123!", "differentPassword"
            );

            // when
            var factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
            var validator = factory.getValidator();
            var violations = validator.validate(request);

            // then
            assertThat(violations).isNotEmpty();
            then(userWebMapper).shouldHaveNoInteractions();
            then(userCommandService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("GET /api/users/me - 현재 사용자 정보")
    class CurrentUser {

        @Test
        @DisplayName("✅ 현재 로그인한 사용자 정보 조회")
        void shouldGetCurrentUserInfo() {
            // given
            given(userQueryService.get(testUser.getId())).willReturn(
                    testUserResult);
            given(userWebMapper.toResponse(testUserResult)).willReturn(
                    testUserResponse);

            // when
            UserResponse result = userController.me(testPrincipal);

            // then
            assertThat(result).isEqualTo(testUserResponse);
            then(userQueryService).should().get(testUser.getId());
            then(userWebMapper).should().toResponse(testUserResult);
        }
    }
}
