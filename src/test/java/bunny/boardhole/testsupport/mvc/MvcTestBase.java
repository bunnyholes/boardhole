package bunny.boardhole.testsupport.mvc;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.testsupport.config.TestEmailConfig;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestEmailConfig.class)
public abstract class MvcTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected BoardRepository boardRepository;
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
    protected String getAdminUsername() {
        return adminUsername;
    }

    protected String getAdminPassword() {
        return adminPassword;
    }

    protected String getAdminEmail() {
        return adminEmail;
    }

    protected String getRegularUsername() {
        return regularUsername;
    }

    protected String getRegularPassword() {
        return regularPassword;
    }

    protected String getRegularEmail() {
        return regularEmail;
    }

    protected Long seedBoardOwnedBy(String username, String title, String content) {
        User owner = userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("User not found: " + username));
        Board board = Board.builder().title(title).content(content).author(owner).build();
        return boardRepository.save(board).getId();
    }

    protected Long seedUser(String username, String name, String email, String rawPassword, java.util.Set<Role> roles) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent())
            return existing.get().getId();
        User user = User.builder().username(username).password(rawPassword).name(name).email(email).roles(new java.util.HashSet<>(roles)).build();
        user.verifyEmail();
        return userRepository.save(user).getId();
    }

    protected Long findUserIdByUsername(String username) {
        return userRepository.findByUsername(username).map(User::getId).orElse(null);
    }
}
