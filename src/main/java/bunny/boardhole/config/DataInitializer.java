package bunny.boardhole.config;

import bunny.boardhole.domain.Board;
import bunny.boardhole.domain.User;
import bunny.boardhole.domain.Role;
import bunny.boardhole.repository.BoardRepository;
import bunny.boardhole.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (userRepository.count() > 0) {
            log.info("Data already exists, skipping initialization");
            return;
        }

        log.info("Initializing sample data...");

        // Create admin user
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .name("Administrator")
                .email("admin@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roles(new java.util.HashSet<>(List.of(Role.ADMIN)))
                .build();
        admin = userRepository.save(admin);
        log.info("Created admin user: {}", admin.getUsername());

        // Create test user
        User testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("test123"))
                .name("Test User")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .roles(new java.util.HashSet<>(List.of(Role.USER)))
                .build();
        testUser = userRepository.save(testUser);
        log.info("Created test user: {}", testUser.getUsername());

        // Create sample boards
        Board board1 = Board.builder()
                .title("Welcome to Board Hole!")
                .content("This is the first post on Board Hole. Feel free to explore and create your own posts!")
                .author(admin)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        boardRepository.save(board1);
        log.info("Created sample board: {}", board1.getTitle());

        Board board2 = Board.builder()
                .title("Test Post")
                .content("This is a test post created by the test user. You can edit or delete this post.")
                .author(testUser)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        boardRepository.save(board2);
        log.info("Created sample board: {}", board2.getTitle());

        log.info("Sample data initialization completed!");
    }
}
