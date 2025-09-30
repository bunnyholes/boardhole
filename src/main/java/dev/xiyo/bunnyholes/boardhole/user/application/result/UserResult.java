package dev.xiyo.bunnyholes.boardhole.user.application.result;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import dev.xiyo.bunnyholes.boardhole.user.domain.Role;

public record UserResult(UUID id, String username, String name, String email, LocalDateTime createdAt, LocalDateTime updatedAt,
                         LocalDateTime lastLogin, Set<Role> roles, boolean hasProfileImage) {
}
