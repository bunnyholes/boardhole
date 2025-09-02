package bunny.boardhole.shared.web;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.config.TestUserConfig;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestUserConfig.class)
public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestUserConfig.TestUserProperties testUserProperties;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected BoardRepository boardRepository;


    protected Long seedBoardOwnedBy(String username, String title, String content) {
        User owner = userRepository.findByUsername(username);
        Board board = Board.builder()
                .title(title)
                .content(content)
                .author(owner)
                .build();
        return boardRepository.save(board).getId();
    }

    protected Long seedUser(String username, String name, String email, String rawPassword, java.util.Set<Role> roles) {
        User existing = userRepository.findByUsername(username);
        if (existing != null) return existing.getId();
        User user = User.builder()
                .username(username)
                .password(rawPassword) // tests should not authenticate with this; used for data only
                .name(name)
                .email(email)
                .userRoles(roles)
                .build();
        return userRepository.save(user).getId();
    }

    protected Long findUserIdByUsername(String username) {
        User u = userRepository.findByUsername(username);
        return u != null ? u.getId() : null;
    }

}
