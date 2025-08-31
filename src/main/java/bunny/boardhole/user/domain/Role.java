package bunny.boardhole.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Role", description = "사용자 권한 열거형 - 시스템 접근 권한을 정의")
public enum Role {
    @Schema(description = "관리자 권한 - 모든 기능에 접근 가능")
    ADMIN,
    @Schema(description = "일반 사용자 권한 - 기본 기능에 접근 가능")
    USER
}

