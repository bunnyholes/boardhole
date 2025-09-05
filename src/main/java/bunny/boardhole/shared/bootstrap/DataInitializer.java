package bunny.boardhole.shared.bootstrap;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;
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
        // 주의: 이 초기화기는 모든 환경(프로덕션 포함)에서 항상 실행되어
        // 기본 관리자/사용자 계정과 환영 게시글을 삽입합니다.
        // 중복 삽입을 피하기 위해 존재 여부를 확인하는 멱등 로직으로 설계되어 있습니다.
        log.info(messageSource.getMessage("log.user.init.start", null, LocaleContextHolder.getLocale()));

        // 관리자 계정 확인 및 생성
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .name(adminName)
                    .email(adminEmail)
                    .roles(Set.of(Role.ADMIN))
                    .build();
            admin.verifyEmail(); // 기본 사용자는 이메일 인증 완료 상태로 생성
            userRepository.save(admin);
            log.info(messageSource.getMessage("log.user.admin.created",
                    new Object[]{adminUsername}, LocaleContextHolder.getLocale()));
        } else log.info(messageSource.getMessage("log.user.admin.exists",
                new Object[]{adminUsername}, LocaleContextHolder.getLocale()));

        // 일반 사용자 계정 확인 및 생성
        if (!userRepository.existsByUsername(regularUsername)) {
            User regularUser = User.builder()
                    .username(regularUsername)
                    .password(passwordEncoder.encode(regularPassword))
                    .name(regularName)
                    .email(regularEmail)
                    .roles(Set.of(Role.USER))
                    .build();
            regularUser.verifyEmail(); // 기본 사용자는 이메일 인증 완료 상태로 생성
            userRepository.save(regularUser);
            log.info(messageSource.getMessage("log.user.regular.created",
                    new Object[]{regularUsername}, LocaleContextHolder.getLocale()));
        } else log.info(messageSource.getMessage("log.user.regular.exists",
                new Object[]{regularUsername}, LocaleContextHolder.getLocale()));

        // 기본 환영 게시글 생성
        if (boardRepository.count() == 0) {
            createWelcomeBoard();
            log.info(messageSource.getMessage("log.board.welcome.created", null, LocaleContextHolder.getLocale()));
        }
    }

    private void createWelcomeBoard() {
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalStateException("Admin user not found: " + adminUsername));

        String title = messageSource.getMessage("data.welcome.board.title", null, LocaleContextHolder.getLocale());
        String content = messageSource.getMessage("data.welcome.board.content", null, LocaleContextHolder.getLocale());

        Board welcomeBoard = Board.builder()
                .title(title)
                .content(content)
                .author(admin)
                .build();
        boardRepository.save(welcomeBoard);
    }

}
