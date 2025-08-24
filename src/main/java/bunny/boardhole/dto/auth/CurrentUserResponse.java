package bunny.boardhole.dto.auth;

import bunny.boardhole.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentUserResponse {
    private Long userId;
    private String username;
    private String name;
    private String email;

    public static CurrentUserResponse from(User user) {
        return CurrentUserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}