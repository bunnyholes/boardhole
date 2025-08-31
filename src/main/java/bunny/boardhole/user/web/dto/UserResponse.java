package bunny.boardhole.user.web.dto;

import bunny.boardhole.user.domain.Role;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "UserResponse", description = "사용자 정보 응답")
public class UserResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long id;
    @Schema(description = "사용자명", example = "johndoe")
    private String username;
    @Schema(description = "이름", example = "홍길동")
    private String name;
    @Schema(description = "이메일 주소", example = "john@example.com")
    private String email;
    @Schema(description = "계정 생성 일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    @Schema(description = "마지막 로그인 일시", example = "2024-01-16T14:20:15")
    private LocalDateTime lastLogin;
    @Schema(description = "사용자 역할 목록", example = "[\"USER\", \"ADMIN\"]")
    private Set<Role> roles;

}
