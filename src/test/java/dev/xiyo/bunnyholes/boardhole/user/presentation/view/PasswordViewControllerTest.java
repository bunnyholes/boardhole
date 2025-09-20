package dev.xiyo.bunnyholes.boardhole.user.presentation.view;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;
import dev.xiyo.bunnyholes.boardhole.shared.exception.UnauthorizedException;
import dev.xiyo.bunnyholes.boardhole.shared.security.AppUserPrincipal;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
        value = {PasswordViewController.class, dev.xiyo.bunnyholes.boardhole.shared.exception.ViewControllerAdvice.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class)
        }
)
@Import({PasswordViewControllerTest.TestConfig.class, dev.xiyo.bunnyholes.boardhole.shared.exception.ViewControllerAdvice.class})
@Tag("unit")
@Tag("view")
@DisplayName("PasswordViewController 뷰 테스트")
class PasswordViewControllerTest {

    private static final String USER_ID_STRING = "11111111-1111-1111-1111-111111111111";
    private static final UUID USER_ID = UUID.fromString(USER_ID_STRING);

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

    @MockitoBean
    private MethodSecurityExpressionHandler methodSecurityExpressionHandler;

    @MockitoBean
    private MessageUtils messageUtils;

    /**
     * 테스트용 User 엔티티 생성
     */
    private User createTestUser() {
        return User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("테스트사용자")
                .email("test@example.com")
                .roles(Set.of(Role.USER))
                .build();
    }

    /**
     * 테스트용 Authentication 생성
     */
    private Authentication createTestAuthentication() {
        User user = createTestUser();
        AppUserPrincipal principal = new AppUserPrincipal(user);
        
        // 3-argument constructor는 이미 authenticated = true로 설정됨
        return new UsernamePasswordAuthenticationToken(
                principal,
                "password", // credentials
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("비인증 사용자는 비밀번호 변경을 할 수 없다")
    @WithAnonymousUser
    void updatePassword_Anonymous_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", "oldPassword123!")
                       .param("newPassword", "newPassword123!")
                       .param("confirmPassword", "newPassword123!"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증된 사용자는 올바른 비밀번호로 변경할 수 있다")
    @WithMockUser(username = USER_ID_STRING, authorities = {"ROLE_USER"})
    void updatePassword_ValidRequest_ShouldSucceed() throws Exception {
        // given
        String currentPassword = "oldPassword123!";
        String newPassword = "newPassword123!";

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
    @WithMockUser(username = USER_ID_STRING, authorities = {"ROLE_USER"})
    void updatePassword_WrongCurrentPassword_ShouldFail() throws Exception {
        // given
        String wrongCurrentPassword = "wrongPassword123!";
        String newPassword = "newPassword123!";

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
    @WithMockUser(username = USER_ID_STRING, authorities = {"ROLE_USER"})
    void updatePassword_WeakNewPassword_ShouldFail() throws Exception {
        // given
        String currentPassword = "oldPassword123!";
        String weakNewPassword = "weak"; // 패스워드 정책에 맞지 않는 약한 비밀번호

        // when & then
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", currentPassword)
                       .param("newPassword", weakNewPassword)
                       .param("confirmPassword", weakNewPassword))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/users/me"))
               .andExpect(flash().attribute("error", "입력 정보를 확인해주세요."));
    }

    @Test
    @DisplayName("새 비밀번호와 확인 비밀번호가 일치하지 않으면 변경에 실패한다")
    @WithMockUser(username = USER_ID_STRING, authorities = {"ROLE_USER"})
    void updatePassword_PasswordMismatch_ShouldFail() throws Exception {
        // given
        String currentPassword = "oldPassword123!";
        String newPassword = "newPassword123!";
        String confirmPassword = "differentPassword123!";

        // when & then
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", currentPassword)
                       .param("newPassword", newPassword)
                       .param("confirmPassword", confirmPassword))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/users/me"))
               .andExpect(flash().attribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("현재 비밀번호가 비어있으면 변경에 실패한다")
    @WithMockUser(username = USER_ID_STRING, authorities = {"ROLE_USER"})
    void updatePassword_EmptyCurrentPassword_ShouldFail() throws Exception {
        // given
        String newPassword = "newPassword123!";

        // when & then
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", "")
                       .param("newPassword", newPassword)
                       .param("confirmPassword", newPassword))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/users/me"))
               .andExpect(flash().attribute("error", "입력 정보를 확인해주세요."));
    }

    @Test
    @DisplayName("새 비밀번호가 비어있으면 변경에 실패한다")
    @WithMockUser(username = USER_ID_STRING, authorities = {"ROLE_USER"})
    void updatePassword_EmptyNewPassword_ShouldFail() throws Exception {
        // given
        String currentPassword = "oldPassword123!";

        // when & then
        mockMvc.perform(post("/users/me/password")
                       .with(csrf())
                       .param("currentPassword", currentPassword)
                       .param("newPassword", "")
                       .param("confirmPassword", ""))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/users/me"))
               .andExpect(flash().attribute("error", "입력 정보를 확인해주세요."));
    }

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        
        @Override
        public void addArgumentResolvers(java.util.List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.getParameterType().equals(AppUserPrincipal.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                    User testUser = createTestUser();
                    return new AppUserPrincipal(testUser);
                }
            });
        }
        
        @Bean
        public PermissionEvaluator permissionEvaluator() {
            return new PermissionEvaluator() {
                @Override
                public boolean hasPermission(org.springframework.security.core.Authentication authentication, Object targetDomainObject, Object permission) {
                    return true; // Allow all for testing
                }

                @Override
                public boolean hasPermission(org.springframework.security.core.Authentication authentication, java.io.Serializable targetId, String targetType, Object permission) {
                    return true; // Allow all for testing
                }
            };
        }
        
        private User createTestUser() {
            return User.builder()
                    .username("testuser")
                    .password("encodedPassword")
                    .name("테스트사용자")
                    .email("test@example.com")
                    .roles(Set.of(Role.USER))
                    .build();
        }
    }
}