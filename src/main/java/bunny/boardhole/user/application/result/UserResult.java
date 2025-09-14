package bunny.boardhole.user.application.result;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import bunny.boardhole.user.domain.Role;

public record UserResult(UUID id, String username, String name, String email, LocalDateTime createdAt, LocalDateTime updatedAt,
                         LocalDateTime lastLogin, Set<Role> roles) {
}
