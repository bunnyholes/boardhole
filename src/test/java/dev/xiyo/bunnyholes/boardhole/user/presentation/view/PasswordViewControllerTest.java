package dev.xiyo.bunnyholes.boardhole.user.presentation.view;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.UnauthorizedException;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ViewControllerAdvice;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PasswordViewController 뷰 테스트
 *
 * @WebMvcTest를 사용하여 빠른 뷰 레이어 테스트 수행
 * 실제 DB 연결 없이 MockBean으로 서비스 계층 모킹
 */
@WebMvcTest(
        value = {PasswordViewController.class, ViewControllerAdvice.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class)
        }
)
@Import(ViewSecurityConfig.class)
@Tag("unit")
@Tag("view")
@DisplayName("PasswordViewController 뷰 테스트")
class PasswordViewControllerTest {

    private static final String USERNAME = "testuser";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PermissionEvaluator permissionEvaluator;

    @Test
    @DisplayName("비인증 사용자는 비밀번호 변경을 할 수 없다")
    @WithAnonymousUser
    void updatePassword_Anonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", "oldPassword123!")
                       .param("newPassword", "newPassword123!")
                       .param("confirmPassword", "newPassword123!"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/auth/login"))
               .andExpect(flash().attribute("error", MessageUtils.get("error.auth.required")));
    }

    @Test
    @DisplayName("인증된 사용자는 올바른 비밀번호로 변경할 수 있다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void updatePassword_ValidRequest_ShouldSucceed() throws Exception {
        // given
        final String currentPassword = "oldPassword123!";
        final String newPassword = "newPassword123!";

        // when & then
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", currentPassword)
                       .param("newPassword", newPassword)
                       .param("confirmPassword", newPassword))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/users/me"))
               .andExpect(flash().attribute("success", "비밀번호가 성공적으로 변경되었습니다."));

        verify(userCommandService).updatePassword(any());
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 변경에 실패한다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void updatePassword_WrongCurrentPassword_ShouldFail() throws Exception {
        // given
        final String wrongCurrentPassword = "wrongPassword123!";
        final String newPassword = "newPassword123!";

        doThrow(new UnauthorizedException("현재 비밀번호가 일치하지 않습니다."))
                .when(userCommandService).updatePassword(any());

        // when & then - UnauthorizedException은 ViewControllerAdvice에 의해 처리되어 로그인 페이지로 리디렉트됨
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", wrongCurrentPassword)
                       .param("newPassword", newPassword)
                       .param("confirmPassword", newPassword))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/auth/login"))
               .andExpect(flash().attributeExists("error"));

        verify(userCommandService).updatePassword(any());
    }

    @Test
    @DisplayName("약한 새 비밀번호는 변경에 실패한다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void updatePassword_WeakNewPassword_ShouldFail() throws Exception {
        // given
        final String currentPassword = "oldPassword123!";
        final String weakNewPassword = "weak"; // 패스워드 정책에 맞지 않는 약한 비밀번호

        // when & then
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", currentPassword)
                       .param("newPassword", weakNewPassword)
                       .param("confirmPassword", weakNewPassword))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/users/me"))
               .andExpect(flash().attribute("error", "입력 정보를 확인해주세요."));

        verify(userCommandService, never()).updatePassword(any());
    }

    @Test
    @DisplayName("새 비밀번호와 확인 비밀번호가 일치하지 않으면 변경에 실패한다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void updatePassword_PasswordMismatch_ShouldFail() throws Exception {
        // given
        final String currentPassword = "oldPassword123!";
        final String newPassword = "newPassword123!";
        final String confirmPassword = "differentPassword123!";

        // when & then
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", currentPassword)
                       .param("newPassword", newPassword)
                       .param("confirmPassword", confirmPassword))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/users/me"))
               .andExpect(flash().attribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."));

        verify(userCommandService, never()).updatePassword(any());
    }

    @Test
    @DisplayName("현재 비밀번호가 비어있으면 변경에 실패한다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void updatePassword_EmptyCurrentPassword_ShouldFail() throws Exception {
        // given
        final String newPassword = "newPassword123!";

        // when & then
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", "")
                       .param("newPassword", newPassword)
                       .param("confirmPassword", newPassword))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/users/me"))
               .andExpect(flash().attribute("error", "입력 정보를 확인해주세요."));

        verify(userCommandService, never()).updatePassword(any());
    }

    @Test
    @DisplayName("새 비밀번호가 비어있으면 변경에 실패한다")
    @WithMockUser(username = USERNAME, authorities = {"ROLE_USER"})
    void updatePassword_EmptyNewPassword_ShouldFail() throws Exception {
        // given
        final String currentPassword = "oldPassword123!";

        // when & then
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", currentPassword)
                       .param("newPassword", "")
                       .param("confirmPassword", ""))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/users/me"))
               .andExpect(flash().attribute("error", "입력 정보를 확인해주세요."));

        verify(userCommandService, never()).updatePassword(any());
    }

}
