package bunny.boardhole.shared.web;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.config.*;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import({TestEmailConfig.class})
public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // Default users pulled directly from application.yml
    @Value("${boardhole.default-users.admin.username}")
    private String adminUsername;
    @Value("${boardhole.default-users.admin.password}")
    private String adminPassword;
    @Value("${boardhole.default-users.admin.email}")
    private String adminEmail;
    @Value("${boardhole.default-users.regular.username}")
    private String regularUsername;
    @Value("${boardhole.default-users.regular.password}")
    private String regularPassword;
    @Value("${boardhole.default-users.regular.email}")
    private String regularEmail;

    // Protected accessors for subclasses
    protected String getAdminUsername() { return adminUsername; }
    protected String getAdminPassword() { return adminPassword; }
    protected String getAdminEmail() { return adminEmail; }
    protected String getRegularUsername() { return regularUsername; }
    protected String getRegularPassword() { return regularPassword; }
    protected String getRegularEmail() { return regularEmail; }

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected BoardRepository boardRepository;


    protected Long seedBoardOwnedBy(String username, String title, String content) {
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        Board board = Board.builder()
                .title(title)
                .content(content)
                .author(owner)
                .build();
        return boardRepository.save(board).getId();
    }

    protected Long seedUser(String username, String name, String email, String rawPassword, java.util.Set<Role> roles) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) return existing.get().getId();
        User user = User.builder()
                .username(username)
                .password(rawPassword) // tests should not authenticate with this; used for data only
                .name(name)
                .email(email)
                .roles(new java.util.HashSet<>(roles))
                .build();
        user.verifyEmail(); // 테스트용 사용자는 이메일 인증 완료 상태로 생성
        return userRepository.save(user).getId();
    }

    protected Long findUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }

    // 기본 사용자 속성은 @Value 주입된 필드(regularUsername 등)를 직접 사용합니다.

}
