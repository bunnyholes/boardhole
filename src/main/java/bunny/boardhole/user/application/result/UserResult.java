package bunny.boardhole.user.application.result;

import bunny.boardhole.user.domain.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResult(
        Long id,
        String username,
        String name,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLogin,
        Set<Role> roles
) {
}

