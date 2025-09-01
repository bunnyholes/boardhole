package bunny.boardhole.user.application.result;

import bunny.boardhole.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(name = "UserResult", description = "사용자 서비스 결과 객체 - 애플리케이션 레이어에서 사용되는 사용자 정보")
public record UserResult(
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        @Schema(description = "사용자명", example = "johndoe")
        String username,
        @Schema(description = "사용자 실명", example = "홍길동")
        String name,
        @Schema(description = "이메일 주소", example = "john@example.com")
        String email,
        @Schema(description = "계정 생성 일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
        @Schema(description = "마지막 정보 수정 일시", example = "2024-01-16T14:20:15")
        LocalDateTime updatedAt,
        @Schema(description = "마지막 로그인 일시", example = "2024-01-16T14:20:15")
        LocalDateTime lastLogin,
        @Schema(description = "사용자 권한 목록", example = "[\"USER\", \"ADMIN\"]")
        Set<Role> roles
) {
}

