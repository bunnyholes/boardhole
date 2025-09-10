package bunny.boardhole.testsupport.mvc;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.properties.DefaultUsersProperties;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class MvcTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired
    protected UserRepository userRepository;
    
    @Autowired
    protected BoardRepository boardRepository;
    
    @Autowired
    protected DefaultUsersProperties defaultUsers;

    // Protected accessors for subclasses
    protected String getAdminUsername() {
        return defaultUsers.admin().username();
    }

    protected String getAdminPassword() {
        return defaultUsers.admin().password();
    }

    protected String getAdminEmail() {
        return defaultUsers.admin().email();
    }

    protected String getRegularUsername() {
        return defaultUsers.regular().username();
    }

    protected String getRegularPassword() {
        return defaultUsers.regular().password();
    }

    protected String getRegularEmail() {
        return defaultUsers.regular().email();
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
        User user = User.builder().username(username).password(rawPassword).name(name).email(email).roles(roles).build();
        user.verifyEmail();
        return userRepository.save(user).getId();
    }

    protected Long findUserIdByUsername(String username) {
        return userRepository.findByUsername(username).map(User::getId).orElse(null);
    }
}
