package bunny.boardhole.repository;

import bunny.boardhole.domain.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Repository
@Validated
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(@NotNull String username);
    boolean existsByEmail(@NotNull String email);

    @EntityGraph(attributePaths = {"roles"})
    User findByUsername(@NotNull String username);
    
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findById(Long id);

    Page<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            @NotNull String username, @NotNull String name, @NotNull String email, @NotNull Pageable pageable);
}
