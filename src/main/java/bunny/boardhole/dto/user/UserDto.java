package bunny.boardhole.dto.user;

import bunny.boardhole.domain.Role;
import bunny.boardhole.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private Set<Role> roles;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
    public User toEntity() {
        User user = User.builder()
                .username(this.username)
                .password(this.password)
                .name(this.name)
                .email(this.email)
                .roles(this.roles)
                .build();
        user.setId(this.id);
        user.setLastLogin(this.lastLogin);
        return user;
    }
}