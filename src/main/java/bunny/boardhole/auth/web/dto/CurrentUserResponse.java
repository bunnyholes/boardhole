package bunny.boardhole.auth.web.dto;

import bunny.boardhole.user.domain.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(name = "CurrentUserResponse", description = "현재 인증된 사용자 정보 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CurrentUserResponse(
        @Schema(description = "사용자 ID", example = "1") Long userId,
        @Schema(description = "사용자명", example = "johndoe") String username,
        @Schema(description = "이름", example = "John Doe") String name,
        @Schema(description = "이메일", example = "john@example.com") String email,
        @Schema(description = "역할 목록", example = "[\"USER\", \"ADMIN\"]") Set<Role> roles,
        @Schema(description = "가입일(생성일시)", example = "2025-09-01T01:13:00") LocalDateTime createdAt,
        @Schema(description = "마지막 로그인 일시", example = "2025-09-01T08:30:15") LocalDateTime lastLogin
) {
}
