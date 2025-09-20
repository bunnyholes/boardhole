package dev.xiyo.bunnyholes.boardhole.auth.presentation.view;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.xiyo.bunnyholes.boardhole.auth.application.command.AuthCommandService;
import dev.xiyo.bunnyholes.boardhole.shared.config.ViewSecurityConfig;
import dev.xiyo.bunnyholes.boardhole.shared.config.log.RequestLoggingFilter;
import dev.xiyo.bunnyholes.boardhole.shared.exception.DuplicateEmailException;
import dev.xiyo.bunnyholes.boardhole.shared.exception.DuplicateUsernameException;
import dev.xiyo.bunnyholes.boardhole.shared.exception.GlobalExceptionHandler;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * SignupViewController Thymeleaf 템플릿 단위 테스트
 *
 * @WebMvcTest를 사용하여 빠른 뷰 레이어 테스트 수행
 * 실제 DB 연결 없이 MockBean으로 서비스 계층 모킹
 */
@WebMvcTest(
        value = SignupViewController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestLoggingFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GlobalExceptionHandler.class)
        }
)
@Import(ViewSecurityConfig.class) // 테스트용 보안 설정
@Tag("unit")
@Tag("view")
@DisplayName("SignupViewController 뷰 테스트")
class SignupViewControllerTest {

    private static final UUID TEST_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private AuthCommandService authCommandService;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PermissionEvaluator permissionEvaluator;

    @BeforeEach
    void setUp() {
        lenient().when(permissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        reset(permissionEvaluator, authCommandService);
    }

    @Test
    @DisplayName("회원가입 폼 페이지가 정상적으로 렌더링된다")
    @WithAnonymousUser
    void signupPage_ShouldRenderSignupForm() throws Exception {
        // when & then
        mockMvc.perform(get("/auth/signup"))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/signup"))
               .andExpect(model().attributeExists("userCreateRequest"))
               // Thymeleaf 렌더링 내용 검증
               .andExpect(content().string(containsString("회원가입")))
               .andExpect(content().string(containsString("username")))
               .andExpect(content().string(containsString("password")))
               .andExpect(content().string(containsString("confirmPassword")))
               .andExpect(content().string(containsString("name")))
               .andExpect(content().string(containsString("email")));
    }

    @Test
    @DisplayName("유효한 회원가입 정보로 성공적으로 가입 처리된다")
    @WithAnonymousUser
    void processSignup_ValidRequest_ShouldRedirectToBoards() throws Exception {
        // given
        var userResult = new UserResult(
                TEST_USER_ID,
                "testuser",
                "Test User",
                "test@example.com",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                Set.of(Role.USER)
        );

        when(userCommandService.create(any())).thenReturn(userResult);

        // when & then
        mockMvc.perform(post("/auth/signup")
                       .with(csrf())
                       .param("username", "testuser")
                       .param("password", "Password123!")
                       .param("confirmPassword", "Password123!")
                       .param("name", "Test User")
                       .param("email", "test@example.com"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/boards"));

        verify(authCommandService).login("testuser");
    }

    @Test
    @DisplayName("중복된 사용자명으로 가입 시도 시 에러가 표시된다")
    @WithAnonymousUser
    void processSignup_DuplicateUsername_ShouldShowError() throws Exception {
        // given
        String duplicateUsernameMessage = messageSource.getMessage(
                "error.user.username.already-exists",
                null,
                LocaleContextHolder.getLocale()
        );
        when(userCommandService.create(any())).thenThrow(new DuplicateUsernameException(duplicateUsernameMessage));

        // when & then
        mockMvc.perform(post("/auth/signup")
                       .with(csrf())
                       .param("username", "existinguser")
                       .param("password", "Password123!")
                       .param("confirmPassword", "Password123!")
                       .param("name", "Test User")
                       .param("email", "test@example.com"))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/signup"))
               .andExpect(model().attributeHasFieldErrors("userCreateRequest", "username"))
               .andExpect(content().string(containsString(duplicateUsernameMessage)));

        verifyNoInteractions(authCommandService);
    }

    @Test
    @DisplayName("중복된 이메일로 가입 시도 시 에러가 표시된다")
    @WithAnonymousUser
    void processSignup_DuplicateEmail_ShouldShowError() throws Exception {
        // given
        String duplicateEmailMessage = messageSource.getMessage(
                "error.user.email.already-exists",
                null,
                LocaleContextHolder.getLocale()
        );
        when(userCommandService.create(any())).thenThrow(new DuplicateEmailException(duplicateEmailMessage));

        // when & then
        mockMvc.perform(post("/auth/signup")
                       .with(csrf())
                       .param("username", "testuser")
                       .param("password", "Password123!")
                       .param("confirmPassword", "Password123!")
                       .param("name", "Test User")
                       .param("email", "existing@example.com"))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/signup"))
               .andExpect(model().attributeHasFieldErrors("userCreateRequest", "email"))
               .andExpect(content().string(containsString(duplicateEmailMessage)));

        verifyNoInteractions(authCommandService);
    }

    @Test
    @DisplayName("유효성 검증 실패 시 폼으로 돌아간다")
    @WithAnonymousUser
    void processSignup_ValidationErrors_ShouldReturnToForm() throws Exception {
        // when & then - 빈 필드들로 검증 실패 유발
        mockMvc.perform(post("/auth/signup")
                       .with(csrf())
                       .param("username", "")
                       .param("password", "")
                       .param("confirmPassword", "")
                       .param("name", "")
                       .param("email", ""))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/signup"))
               .andExpect(model().attributeExists("userCreateRequest"))
               .andExpect(model().hasErrors());

        verifyNoInteractions(authCommandService);
    }

    @Test
    @DisplayName("비밀번호와 확인 비밀번호가 일치하지 않을 때 검증 실패한다")
    @WithAnonymousUser
    void processSignup_PasswordMismatch_ShouldReturnToForm() throws Exception {
        // when & then
        mockMvc.perform(post("/auth/signup")
                       .with(csrf())
                       .param("username", "testuser")
                       .param("password", "Password123!")
                       .param("confirmPassword", "DifferentPassword!")
                       .param("name", "Test User")
                       .param("email", "test@example.com"))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/signup"))
               .andExpect(model().attributeExists("userCreateRequest"))
               .andExpect(model().hasErrors());

        verifyNoInteractions(authCommandService);
    }

    @Test
    @DisplayName("잘못된 사용자명 형식 시 검증 실패한다")
    @WithAnonymousUser
    void processSignup_InvalidUsername_ShouldReturnToForm() throws Exception {
        // when & then - 특수문자 포함 사용자명으로 검증 실패 유발
        mockMvc.perform(post("/auth/signup")
                       .with(csrf())
                       .param("username", "test@user")
                       .param("password", "Password123!")
                       .param("confirmPassword", "Password123!")
                       .param("name", "Test User")
                       .param("email", "test@example.com"))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/signup"))
               .andExpect(model().attributeExists("userCreateRequest"))
               .andExpect(model().attributeHasFieldErrors("userCreateRequest", "username"))
               .andExpect(content().string(containsString(messageSource.getMessage(
                       "validation.user.username.pattern",
                       null,
                       LocaleContextHolder.getLocale()
               ))));

        verifyNoInteractions(authCommandService);
    }

    @Test
    @DisplayName("잘못된 이메일 형식 시 검증 실패한다")
    @WithAnonymousUser
    void processSignup_InvalidEmail_ShouldReturnToForm() throws Exception {
        // when & then - 잘못된 이메일 형식으로 검증 실패 유발
        mockMvc.perform(post("/auth/signup")
                       .with(csrf())
                       .param("username", "testuser")
                       .param("password", "Password123!")
                       .param("confirmPassword", "Password123!")
                       .param("name", "Test User")
                       .param("email", "invalid-email"))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/signup"))
               .andExpect(model().attributeExists("userCreateRequest"))
               .andExpect(model().hasErrors());

        verifyNoInteractions(authCommandService);
    }

    @Test
    @DisplayName("약한 비밀번호 시 검증 실패한다")
    @WithAnonymousUser
    void processSignup_WeakPassword_ShouldReturnToForm() throws Exception {
        // when & then - 복잡성이 부족한 비밀번호로 검증 실패 유발
        mockMvc.perform(post("/auth/signup")
                       .with(csrf())
                       .param("username", "testuser")
                       .param("password", "weak")
                       .param("confirmPassword", "weak")
                       .param("name", "Test User")
                       .param("email", "test@example.com"))
               .andExpect(status().isOk())
               .andExpect(view().name("auth/signup"))
               .andExpect(model().attributeExists("userCreateRequest"))
               .andExpect(model().hasErrors());

        verifyNoInteractions(authCommandService);
    }
}
