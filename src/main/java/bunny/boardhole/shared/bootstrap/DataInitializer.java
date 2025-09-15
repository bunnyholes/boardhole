package bunny.boardhole.shared.bootstrap;

import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.properties.DefaultUsersProperties;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /*
     * IMPORTANT: Intentional behavior across ALL profiles (including prod)
     * ---------------------------------------------------------------------------------
     * - This initializer is intentionally enabled in every environment and will ensure
     *   that the following default users exist (idempotently):
     *     1) Admin (ROLE_ADMIN)
     *     2) Anon convenience user (ROLE_USER)
     * - The responsibility to rotate or change the default passwords belongs to the
     *   operator/consumer of this software. Default credentials are provided via
     *   configuration (see application*.yml) and SHOULD be overridden per environment.
     * - Rationale: ease of onboarding, demo/E2E repeatability, and consistent startup
     *   experience without external provisioning steps.
     *
     * If your deployment policy requires disabling this behavior, you can:
     * - Remove this component or guard it with a profile in your fork; or
     * - Provide alternative bootstrap logic that meets your operational standards.
     */

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultUsersProperties defaultUsersProperties;

    @Override
    public void run(String... args) {
        // NOTE: Runs in ALL environments (including production) by design.
        // 기본 관리자/사용자 계정과 환영 게시글을 삽입합니다 (멱등 보장).
        // 비밀번호 변경/회전 책임은 운영자/소비자에게 있습니다.
        log.info(MessageUtils.get("log.user.init.start"));

        DefaultUsersProperties.UserInfo adminInfo = defaultUsersProperties.admin();

        // 관리자 계정 확인 및 생성/보정
        if (!userRepository.existsByUsername(adminInfo.username())) {
            User admin = User.builder()
                             .username(adminInfo.username())
                             .password(passwordEncoder.encode(adminInfo.password()))
                             .name(adminInfo.name())
                             .email(adminInfo.email())
                             .roles(Set.of(Role.ADMIN))
                             .build();
            admin.verifyEmail(); // 기본 사용자는 이메일 인증 완료 상태로 생성
            userRepository.save(admin);
            log.info(MessageUtils.get("log.user.admin.created", adminInfo.username()));
        } else
            log.info(MessageUtils.get("log.user.admin.exists", adminInfo.username()));

        // 기본 환영 게시글 생성
        if (boardRepository.count() == 0) {
            createWelcomeBoard();
            log.info(MessageUtils.get("log.board.welcome.created"));
        }

        // Ensure anonymous default user exists for E2E tests
        ensureAnonymousUser();
    }

    private void createWelcomeBoard() {
        String adminUsername = defaultUsersProperties.admin().username();
        User admin = userRepository
                .findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalStateException("Admin user not found: " + adminUsername));

        String title = MessageUtils.get("data.welcome.board.title");
        String content = MessageUtils.get("data.welcome.board.content");

        Board welcomeBoard = Board.builder().title(title).content(content).author(admin).build();
        boardRepository.save(welcomeBoard);
    }

    // Create an additional anonymous default user for E2E convenience
    // 개발 편의성 목적의 사용자이지만, 본 프로젝트에서는 의도적으로 모든 프로필에서 보장합니다.
    private void ensureAnonymousUser() {
        final String anonUsername = "anon";
        if (!userRepository.existsByUsername(anonUsername)) {
            User anon = User.builder()
                            .username(anonUsername)
                            .password(passwordEncoder.encode("Anon123!"))
                            .name("Anonymous User")
                            .email("anon@boardhole.com")
                            .roles(Set.of(Role.USER))
                            .build();
            anon.verifyEmail();
            userRepository.save(anon);
        }
    }

}
