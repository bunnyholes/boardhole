package bunny.boardhole.config;

import bunny.boardhole.domain.Board;
import bunny.boardhole.domain.Role;
import bunny.boardhole.domain.User;
import bunny.boardhole.repository.BoardRepository;
import bunny.boardhole.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    // 기본 게시글 상수
    public static final String WELCOME_BOARD_TITLE = "Board Hole에 오신 것을 환영합니다!";
    public static final String WELCOME_BOARD_CONTENT = "첫 번째 게시글입니다. 자유롭게 둘러보고 게시물을 작성해 보세요!";
    // 테스트에서 사용할 수 있도록 상수 유지 (하위 호환성)
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String TEST_USERNAME = "user";
    public static final String TEST_PASSWORD = "user123";
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    // 기본 사용자 설정
    @Value("${boardhole.default-users.admin.username}")
    private String adminUsername;
    @Value("${boardhole.default-users.admin.password}")
    private String adminPassword;
    @Value("${boardhole.default-users.admin.name}")
    private String adminName;
    @Value("${boardhole.default-users.admin.email}")
    private String adminEmail;
    @Value("${boardhole.default-users.regular.username}")
    private String regularUsername;
    @Value("${boardhole.default-users.regular.password}")
    private String regularPassword;
    @Value("${boardhole.default-users.regular.name}")
    private String regularName;
    @Value("${boardhole.default-users.regular.email}")
    private String regularEmail;

    @Override
    public void run(String... args) {
        log.info("기본 사용자 계정 확인 시작...");

        // 관리자 계정 확인 및 생성
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .name(adminName)
                    .email(adminEmail)
                    .roles(new java.util.HashSet<>(List.of(Role.ADMIN)))
                    .build();
            userRepository.save(admin);
            log.info("기본 관리자 계정 생성 완료: {}", adminUsername);
        } else {
            log.info("관리자 계정이 이미 존재합니다: {}", adminUsername);
        }

        // 일반 사용자 계정 확인 및 생성
        if (!userRepository.existsByUsername(regularUsername)) {
            User regularUser = User.builder()
                    .username(regularUsername)
                    .password(passwordEncoder.encode(regularPassword))
                    .name(regularName)
                    .email(regularEmail)
                    .roles(new java.util.HashSet<>(List.of(Role.USER)))
                    .build();
            userRepository.save(regularUser);
            log.info("기본 사용자 계정 생성 완료: {}", regularUsername);
        } else {
            log.info("사용자 계정이 이미 존재합니다: {}", regularUsername);
        }

        // 기본 환영 게시글 생성
        if (boardRepository.count() == 0) {
            createWelcomeBoard();
            log.info("기본 환영 게시글 생성 완료");
        }
    }

    private void createWelcomeBoard() {
        User admin = userRepository.findByUsername(adminUsername);

        Board welcomeBoard = Board.builder()
                .title(WELCOME_BOARD_TITLE)
                .content(WELCOME_BOARD_CONTENT)
                .author(admin)
                .build();
        boardRepository.save(welcomeBoard);
    }
}
