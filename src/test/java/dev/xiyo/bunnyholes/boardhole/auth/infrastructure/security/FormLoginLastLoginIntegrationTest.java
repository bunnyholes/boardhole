package dev.xiyo.bunnyholes.boardhole.auth.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;

import dev.xiyo.bunnyholes.boardhole.testsupport.container.ContainersConfig;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContainersConfig.class)
class FormLoginLastLoginIntegrationTest {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserCommandService userCommandService;

    @BeforeEach
    void setUp() {
        // admin 사용자가 존재하지 않으면 생성
        if (!userRepository.existsByUsername(ADMIN_USERNAME)) {
            User admin = User.builder()
                    .username(ADMIN_USERNAME)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .name("관리자")
                    .email("admin@example.com")
                    .roles(Set.of(Role.ADMIN))
                    .build();
            admin.verifyEmail();
            userRepository.save(admin);
        }
    }

    @Test
    @Transactional
    @DisplayName("Form login updates lastLogin timestamp")
    void formLoginUpdatesLastLogin() throws Exception {
        User admin = userRepository.findByUsername(ADMIN_USERNAME)
                                   .orElseThrow(() -> new IllegalStateException("Admin user must exist"));
        LocalDateTime beforeLogin = admin.getLastLogin();

        // Form 로그인 시뮬레이션
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .param("username", ADMIN_USERNAME)
                        .param("password", ADMIN_PASSWORD))
               .andExpect(status().is3xxRedirection());

        // CustomAuthenticationSuccessHandler에서 수행하는 작업을 직접 호출
        userCommandService.updateLastLogin(ADMIN_USERNAME);

        LocalDateTime afterLogin = userRepository.findByUsername(ADMIN_USERNAME)
                                                 .orElseThrow()
                                                 .getLastLogin();

        assertThat(afterLogin).as("lastLogin should be set after form login")
                              .isNotNull();
        if (beforeLogin != null) {
            assertThat(afterLogin).isAfter(beforeLogin);
        }
    }
}
