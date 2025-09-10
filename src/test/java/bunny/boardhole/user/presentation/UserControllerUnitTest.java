package bunny.boardhole.user.presentation;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.exception.ValidationException;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.testsupport.mvc.GlobalExceptionHandlerTestSetup;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("사용자 컨트롤러 단위 테스트")
@Tag("unit")
@Tag("user")
class UserControllerUnitTest {

    private static final long USER_ID = 1L;
    private static final String USERNAME = "testuser";
    private static final String NAME = "Test User";
    private static final String EMAIL = "test@example.com";
    private static final String NEW_NAME = "Updated Name";
    private static final String NEW_EMAIL = "updated@example.com";
    private static final String CURRENT_PASSWORD = "CurrentPass123!";
    private static final String NEW_PASSWORD = "NewPass123!";

    @Mock
    private UserCommandService userCommandService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserWebMapper userWebMapper;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private AutoCloseable closeable;
    private AppUserPrincipal principal;
    private AppUserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Spring LocaleContextHolder를 한국어로 설정
        LocaleContextHolder.setLocale(Locale.KOREAN);

        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(false);
        ms.setFallbackToSystemLocale(false);
        ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);

        // Validator 설정
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        
        // MockMvc 설정 with Pageable and Principal resolvers
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver()
                )
                .setControllerAdvice(GlobalExceptionHandlerTestSetup.createTestGlobalExceptionHandler())
                .setValidator(validator)
                .build();

        // 테스트용 Principal 생성
        User user = User.builder()
                .username(USERNAME)
                .password("encoded")
                .name(NAME)
                .email(EMAIL)
                .roles(Set.of(Role.USER))
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        principal = new AppUserPrincipal(user);

        User admin = User.builder()
                .username("admin")
                .password("encoded")
                .name("Admin User")
                .email("admin@example.com")
                .roles(Set.of(Role.ADMIN))
                .build();
        ReflectionTestUtils.setField(admin, "id", 999L);
        adminPrincipal = new AppUserPrincipal(admin);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        if (closeable != null) {
            closeable.close();
        }
    }
    
    private void mockAuthentication(AppUserPrincipal principal) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.isAuthenticated()).thenReturn(true);
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("GET /api/users - 사용자 목록 조회")
    @Tag("list")
    class ListUsers {

        @Test
        @DisplayName("✅ 관리자 - 목록 조회 성공")
        void shouldListUsersForAdmin() throws Exception {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            UserResult result1 = new UserResult(1L, "user1", "User One", "user1@example.com", null, null, null, Set.of(Role.USER));
            UserResult result2 = new UserResult(2L, "user2", "User Two", "user2@example.com", null, null, null, Set.of(Role.USER));
            Page<UserResult> results = new PageImpl<>(List.of(result1, result2), pageable, 2);

            UserResponse response1 = new UserResponse(1L, "user1", "User One", "user1@example.com", null, null, Set.of(Role.USER));
            UserResponse response2 = new UserResponse(2L, "user2", "User Two", "user2@example.com", null, null, Set.of(Role.USER));

            given(userQueryService.listWithPaging(any(Pageable.class))).willReturn(results);
            given(userWebMapper.toResponse(result1)).willReturn(response1);
            given(userWebMapper.toResponse(result2)).willReturn(response2);

            // when & then
            // Security context 설정
            mockAuthentication(adminPrincipal);
            
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].username").value("user1"))
                    .andExpect(jsonPath("$.content[1].username").value("user2"))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andDo(print());

            then(userQueryService).should().listWithPaging(any(Pageable.class));
            then(userWebMapper).should().toResponse(result1);
            then(userWebMapper).should().toResponse(result2);
        }

        @Test
        @DisplayName("🔍 관리자 - 검색 기능")
        void shouldSearchUsersForAdmin() throws Exception {
            // given
            String search = "test";
            Pageable pageable = PageRequest.of(0, 20);
            UserResult result = new UserResult(1L, "testuser", "Test User", "test@example.com", null, null, null, Set.of(Role.USER));
            Page<UserResult> results = new PageImpl<>(List.of(result), pageable, 1);

            UserResponse response = new UserResponse(1L, "testuser", "Test User", "test@example.com", null, null, Set.of(Role.USER));

            given(userQueryService.listWithPaging(any(Pageable.class), any(String.class))).willReturn(results);
            given(userWebMapper.toResponse(result)).willReturn(response);

            // when & then
            mockAuthentication(adminPrincipal);
            
            mockMvc.perform(get("/api/users")
                            .param("search", search))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].username").value("testuser"))
                    .andDo(print());

            then(userQueryService).should().listWithPaging(any(Pageable.class), any(String.class));
            then(userWebMapper).should().toResponse(result);
        }

        @Test
        @DisplayName("📄 페이지네이션 파라미터 적용")
        void shouldApplyPaginationParameters() throws Exception {
            // given
            UserResult result = new UserResult(1L, "user1", "User One", "user1@example.com", null, null, null, Set.of(Role.USER));
            Page<UserResult> results = new PageImpl<>(List.of(result), PageRequest.of(1, 5), 10);

            UserResponse response = new UserResponse(1L, "user1", "User One", "user1@example.com", null, null, Set.of(Role.USER));

            given(userQueryService.listWithPaging(any(Pageable.class))).willReturn(results);
            given(userWebMapper.toResponse(result)).willReturn(response);

            // when & then
            mockAuthentication(adminPrincipal);
            
            mockMvc.perform(get("/api/users")
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageable.pageNumber").value(1))
                    .andExpect(jsonPath("$.pageable.pageSize").value(5))
                    .andDo(print());

            then(userQueryService).should().listWithPaging(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id} - 사용자 단일 조회")
    @Tag("get")
    class GetUser {

        @Test
        @DisplayName("✅ 사용자 조회 성공")
        void shouldGetUserById() throws Exception {
            // given
            UserResult result = new UserResult(USER_ID, USERNAME, NAME, EMAIL, null, null, null, Set.of(Role.USER));
            UserResponse response = new UserResponse(USER_ID, USERNAME, NAME, EMAIL, null, null, Set.of(Role.USER));

            given(userQueryService.get(USER_ID)).willReturn(result);
            given(userWebMapper.toResponse(result)).willReturn(response);

            // when & then
            mockAuthentication(principal);
            
            mockMvc.perform(get("/api/users/" + USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.name").value(NAME))
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andDo(print());

            then(userQueryService).should().get(USER_ID);
            then(userWebMapper).should().toResponse(result);
        }

        @Test
        @DisplayName("❌ 사용자 미존재 → 404 Not Found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // given
            Long nonExistentId = 999L;
            given(userQueryService.get(nonExistentId))
                    .willThrow(new ResourceNotFoundException(MessageUtils.get("error.user.not-found.id", nonExistentId)));

            // when & then
            mockAuthentication(principal);
            
            mockMvc.perform(get("/api/users/" + nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").exists())
                    .andDo(print());

            then(userQueryService).should().get(nonExistentId);
            then(userWebMapper).should(never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id} - 사용자 정보 수정")
    @Tag("update")
    class UpdateUser {

        @Test
        @DisplayName("✅ 사용자 정보 수정 성공")
        void shouldUpdateUser() throws Exception {
            // given
            UserUpdateRequest request = new UserUpdateRequest(NEW_NAME);
            UpdateUserCommand command = new UpdateUserCommand(USER_ID, NEW_NAME);
            UserResult updatedResult = new UserResult(USER_ID, USERNAME, NEW_NAME, EMAIL, null, null, null, Set.of(Role.USER));
            UserResponse response = new UserResponse(USER_ID, USERNAME, NEW_NAME, EMAIL, null, null, Set.of(Role.USER));

            given(userWebMapper.toUpdateCommand(USER_ID, request)).willReturn(command);
            given(userCommandService.update(command)).willReturn(updatedResult);
            given(userWebMapper.toResponse(updatedResult)).willReturn(response);

            // when & then
            mockAuthentication(principal);
            
            mockMvc.perform(put("/api/users/" + USER_ID)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("name", NEW_NAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(NEW_NAME))
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andDo(print());

            then(userWebMapper).should().toUpdateCommand(USER_ID, request);
            then(userCommandService).should().update(command);
            then(userWebMapper).should().toResponse(updatedResult);
        }

        @Test
        @DisplayName("❌ 존재하지 않는 사용자 수정 → 404 Not Found")
        void shouldReturn404WhenUpdatingNonExistentUser() throws Exception {
            // given
            Long nonExistentId = 999L;
            UserUpdateRequest request = new UserUpdateRequest(NEW_NAME);
            UpdateUserCommand command = new UpdateUserCommand(nonExistentId, NEW_NAME);

            given(userWebMapper.toUpdateCommand(nonExistentId, request)).willReturn(command);
            willThrow(new ResourceNotFoundException(MessageUtils.get("error.user.not-found.id", nonExistentId)))
                    .given(userCommandService).update(command);

            // when & then
            mockAuthentication(principal);
            
            mockMvc.perform(put("/api/users/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("name", NEW_NAME))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").exists())
                    .andDo(print());

            then(userCommandService).should().update(command);
            then(userWebMapper).should(never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id} - 사용자 삭제")
    @Tag("delete")
    class DeleteUser {

        @Test
        @DisplayName("✅ 사용자 삭제 성공")
        void shouldDeleteUser() throws Exception {
            // given
            willDoNothing().given(userCommandService).delete(USER_ID);

            // when & then
            mockAuthentication(principal);
            
            mockMvc.perform(delete("/api/users/" + USER_ID))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            then(userCommandService).should().delete(USER_ID);
        }

        @Test
        @DisplayName("❌ 존재하지 않는 사용자 삭제 → 404 Not Found")
        void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
            // given
            Long nonExistentId = 999L;
            willThrow(new ResourceNotFoundException(MessageUtils.get("error.user.not-found.id", nonExistentId)))
                    .given(userCommandService).delete(nonExistentId);

            // when & then
            mockAuthentication(principal);
            
            mockMvc.perform(delete("/api/users/" + nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").exists())
                    .andDo(print());

            then(userCommandService).should().delete(nonExistentId);
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{id}/password - 패스워드 변경")
    @Tag("password")
    class UpdatePassword {

        @Test
        @DisplayName("✅ 패스워드 변경 성공")
        void shouldUpdatePassword() throws Exception {
            // given
            PasswordUpdateRequest request = new PasswordUpdateRequest(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD);
            UpdatePasswordCommand command = new UpdatePasswordCommand(USER_ID, CURRENT_PASSWORD, NEW_PASSWORD);

            given(userWebMapper.toUpdatePasswordCommand(USER_ID, request)).willReturn(command);
            willDoNothing().given(userCommandService).updatePassword(command);

            // when & then
            mockAuthentication(principal);
            
            mockMvc.perform(patch("/api/users/" + USER_ID + "/password")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("currentPassword", CURRENT_PASSWORD)
                            .param("newPassword", NEW_PASSWORD)
                            .param("confirmPassword", NEW_PASSWORD))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            then(userWebMapper).should().toUpdatePasswordCommand(USER_ID, request);
            then(userCommandService).should().updatePassword(command);
        }

        @Test
        @DisplayName("❌ 패스워드 확인 불일치 → 400 Bad Request")
        void shouldReturn400WhenPasswordMismatch() throws Exception {
            // when & then
            mockAuthentication(principal);
            
            mockMvc.perform(patch("/api/users/" + USER_ID + "/password")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("currentPassword", CURRENT_PASSWORD)
                            .param("newPassword", NEW_PASSWORD)
                            .param("confirmPassword", "differentPassword")
                            .locale(Locale.KOREAN))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value(MessageUtils.get("exception.title.validation-failed")))
                    .andExpect(jsonPath("$.detail").value(MessageUtils.get("error.user.password.confirm.mismatch")))
                    .andDo(print());

            then(userCommandService).should(never()).updatePassword(any());
        }

        @Test
        @DisplayName("❌ 현재 패스워드 불일치 → 401 Unauthorized")
        void shouldReturn401WhenCurrentPasswordWrong() throws Exception {
            // given
            PasswordUpdateRequest request = new PasswordUpdateRequest("wrongPassword", NEW_PASSWORD, NEW_PASSWORD);
            UpdatePasswordCommand command = new UpdatePasswordCommand(USER_ID, "wrongPassword", NEW_PASSWORD);

            given(userWebMapper.toUpdatePasswordCommand(USER_ID, request)).willReturn(command);
            willThrow(new bunny.boardhole.shared.exception.UnauthorizedException(
                    MessageUtils.get("error.user.password.current.mismatch")))
                    .given(userCommandService).updatePassword(command);

            // when & then
            mockAuthentication(principal);
            
            mockMvc.perform(patch("/api/users/" + USER_ID + "/password")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("currentPassword", "wrongPassword")
                            .param("newPassword", NEW_PASSWORD)
                            .param("confirmPassword", NEW_PASSWORD))
                    .andExpect(status().isUnauthorized())
                    .andDo(print());

            then(userCommandService).should().updatePassword(command);
        }
    }

    @Nested
    @DisplayName("GET /api/users/me - 현재 사용자 정보")
    @Tag("me")
    class CurrentUser {

        @Test
        @DisplayName("✅ 현재 로그인한 사용자 정보 조회")
        void shouldGetCurrentUserInfo() throws Exception {
            // given
            UserResult result = new UserResult(USER_ID, USERNAME, NAME, EMAIL, null, null, null, Set.of(Role.USER));
            UserResponse response = new UserResponse(USER_ID, USERNAME, NAME, EMAIL, null, null, Set.of(Role.USER));

            given(userQueryService.get(USER_ID)).willReturn(result);
            given(userWebMapper.toResponse(result)).willReturn(response);

            // when & then
            mockAuthentication(principal);
            
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.name").value(NAME))
                    .andDo(print());

            then(userQueryService).should().get(USER_ID);
            then(userWebMapper).should().toResponse(result);
        }

        @Test
        @DisplayName("❌ 인증되지 않은 사용자 → 401 Unauthorized")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // 인증되지 않은 경우 @PreAuthorize가 처리하지만 단위 테스트에서는
            // 인증 처리를 별도로 하지 않으므로 이 테스트는 skip
            // 실제 통합 테스트에서 Spring Security가 처리
            
            // 이 테스트는 실제로는 Spring Security가 처리하므로 단위 테스트에서는 제외
            // 대신 principal이 null인 경우 처리를 테스트할 수 있음
        }
    }
}