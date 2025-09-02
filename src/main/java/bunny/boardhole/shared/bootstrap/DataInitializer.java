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

/**
 * 애플리케이션 시작 시 기본 데이터를 초기화하는 컴포넌트입니다.
 * 관리자 및 일반 사용자 계정과 환영 게시글을 생성합니다.
 * 모든 환경에서 실행되며, 중복 생성을 방지하는 멱등 로직을 포함합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    /** 사용자 데이터베이스 접근을 위한 리포지토리 */
    private final UserRepository userRepository;
    
    /** 게시글 데이터베이스 접근을 위한 리포지토리 */
    private final BoardRepository boardRepository;
    
    /** 비밀번호 암호화를 위한 인코더 */
    private final PasswordEncoder passwordEncoder;
    
    /** 다국어 메시지 처리를 위한 메시지 소스 */
    private final MessageSource messageSource;
    /** 기본 관리자 계정 사용자명 */
    @Value("${boardhole.default-users.admin.username}")
    private final String adminUsername;
    
    /** 기본 관리자 계정 비밀번호 */
    @Value("${boardhole.default-users.admin.password}")
    private final String adminPassword;
    
    /** 기본 관리자 계정 이름 */
    @Value("${boardhole.default-users.admin.name}")
    private final String adminName;
    
    /** 기본 관리자 계정 이메일 */
    @Value("${boardhole.default-users.admin.email}")
    private final String adminEmail;
    
    /** 기본 일반 사용자 계정 사용자명 */
    @Value("${boardhole.default-users.regular.username}")
    private final String regularUsername;
    
    /** 기본 일반 사용자 계정 비밀번호 */
    @Value("${boardhole.default-users.regular.password}")
    private final String regularPassword;
    
    /** 기본 일반 사용자 계정 이름 */
    @Value("${boardhole.default-users.regular.name}")
    private final String regularName;
    
    /** 기본 일반 사용자 계정 이메일 */
    @Value("${boardhole.default-users.regular.email}")
    private final String regularEmail;

    /**
     * 애플리케이션 시작 시 실행되는 초기화 메소드입니다.
     * 기본 관리자/일반 사용자 계정과 환영 게시글을 생성합니다.
     * 
     * @param arguments 명령행 인자들 (사용되지 않음)
     */
    @Override
    public void run(final String... arguments) {
        // 주의: 이 초기화기는 모든 환경(프로덕션 포함)에서 항상 실행되어
        // 기본 관리자/사용자 계정과 환영 게시글을 삽입합니다.
        // 중복 삽입을 피하기 위해 존재 여부를 확인하는 멱등 로직으로 설계되어 있습니다.
        logInitializationStart();
        
        initializeUsers();
        initializeBoards();
    }
    
    /**
     * 초기화 시작 로그를 출력합니다.
     */
    private void logInitializationStart() {
        if (log.isInfoEnabled()) {
            log.info(messageSource.getMessage("log.user.init.start", null, LocaleContextHolder.getLocale()));
        }
    }
    
    /**
     * 기본 사용자들(관리자, 일반 사용자)을 초기화합니다.
     */
    private void initializeUsers() {
        initializeAdminUser();
        initializeRegularUser();
    }
    
    /**
     * 관리자 사용자를 초기화합니다.
     * 이미 존재하는 경우 생성하지 않습니다.
     */
    private void initializeAdminUser() {
        if (userRepository.existsByUsername(adminUsername)) {
            logUserAlreadyExists("log.user.admin.exists", adminUsername);
            return;
        }
        
        final User admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .name(adminName)
                .email(adminEmail)
                .userRoles(Set.of(Role.ADMIN))
                .build();
        userRepository.save(admin);
        
        logUserCreated("log.user.admin.created", adminUsername);
    }
    
    /**
     * 일반 사용자를 초기화합니다.
     * 이미 존재하는 경우 생성하지 않습니다.
     */
    private void initializeRegularUser() {
        if (userRepository.existsByUsername(regularUsername)) {
            logUserAlreadyExists("log.user.regular.exists", regularUsername);
            return;
        }
        
        final User regularUser = User.builder()
                .username(regularUsername)
                .password(passwordEncoder.encode(regularPassword))
                .name(regularName)
                .email(regularEmail)
                .userRoles(Set.of(Role.USER))
                .build();
        userRepository.save(regularUser);
        
        logUserCreated("log.user.regular.created", regularUsername);
    }
    
    /**
     * 사용자 생성 로그를 출력합니다.
     * 
     * @param messageKey 로그 메시지 키
     * @param username 사용자명
     */
    private void logUserCreated(final String messageKey, final String username) {
        if (log.isInfoEnabled()) {
            log.info(messageSource.getMessage(messageKey,
                    new Object[]{username}, LocaleContextHolder.getLocale()));
        }
    }
    
    /**
     * 사용자 존재 로그를 출력합니다.
     * 
     * @param messageKey 로그 메시지 키
     * @param username 사용자명
     */
    private void logUserAlreadyExists(final String messageKey, final String username) {
        if (log.isInfoEnabled()) {
            log.info(messageSource.getMessage(messageKey,
                    new Object[]{username}, LocaleContextHolder.getLocale()));
        }
    }
    
    /**
     * 기본 게시글들을 초기화합니다.
     */
    private void initializeBoards() {
        if (boardRepository.count() == 0) {
            createWelcomeBoard();
            logWelcomeBoardCreated();
        }
    }
    
    /**
     * 환영 게시글 생성 로그를 출력합니다.
     */
    private void logWelcomeBoardCreated() {
        if (log.isInfoEnabled()) {
            log.info(messageSource.getMessage("log.board.welcome.created", null, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * 환영 게시글을 생성하는 private 메소드입니다.
     * 관리자가 작성자로 설정된 환영 메시지 게시글을 생성합니다.
     */
    private void createWelcomeBoard() {
        final User admin = userRepository.findByUsername(adminUsername);

        final String title = messageSource.getMessage("data.welcome.board.title", null, LocaleContextHolder.getLocale());
        final String content = messageSource.getMessage("data.welcome.board.content", null, LocaleContextHolder.getLocale());

        final Board welcomeBoard = Board.builder()
                .title(title)
                .content(content)
                .author(admin)
                .build();
        boardRepository.save(welcomeBoard);
    }

}
