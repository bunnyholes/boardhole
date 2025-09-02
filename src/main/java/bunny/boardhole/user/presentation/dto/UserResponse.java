package bunny.boardhole.user.presentation.dto;

import bunny.boardhole.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 사용자 정보 응답 DTO
 * 사용자 정보 조회 시 반환되는 데이터를 담고 있는 응답 객체입니다.
 * 민감한 정보(비밀번호 등)는 제외하고 필요한 정보만 포함합니다.
 */
@Schema(name = "UserResponse", description = "사용자 정보 응답")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        @Schema(description = "사용자명", example = "admin")
        String username,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "이메일 주소", example = "admin@boardhole.com")
        String email,
        @Schema(description = "계정 생성 일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
        @Schema(description = "마지막 로그인 일시", example = "2024-01-16T14:20:15")
        LocalDateTime lastLogin,
        @Schema(description = "사용자 역할 목록", example = "[\"USER\", \"ADMIN\"]")
        Set<Role> roles
) {
}
