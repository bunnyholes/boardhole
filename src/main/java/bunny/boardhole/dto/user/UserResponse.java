package bunny.boardhole.dto.user;

import bunny.boardhole.domain.Role;
import bunny.boardhole.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private Set<Role> roles;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .roles(user.getRoles())
                .build();
    }

    public static UserResponse from(UserDto dto) {
        return UserResponse.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .name(dto.getName())
                .email(dto.getEmail())
                .createdAt(dto.getCreatedAt())
                .lastLogin(dto.getLastLogin())
                .roles(dto.getRoles())
                .build();
    }
}
