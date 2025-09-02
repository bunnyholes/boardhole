package bunny.boardhole.admin.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 사용자 관리 명령
 * CQRS 패턴의 Command 객체로 관리자의 사용자 관리 요청을 나타냅니다.
 */
@Schema(name = "ManageUserCommand", description = "사용자 관리 명령 - CQRS 패턴의 Command 객체")
public record ManageUserCommand(
        @Schema(description = "관리할 사용자 ID", example = "1")
        @NotNull
        @Positive
        Long userId,

        @Schema(description = "수행할 작업", example = "BLOCK")
        @NotNull
        UserManagementAction action
) {
    /**
     * 사용자 관리 작업 유형
     */
    public enum UserManagementAction {
        @Schema(description = "사용자 차단")
        BLOCK,
        
        @Schema(description = "사용자 차단 해제")
        UNBLOCK,
        
        @Schema(description = "관리자 권한 부여")
        GRANT_ADMIN,
        
        @Schema(description = "관리자 권한 해제")
        REVOKE_ADMIN
    }
}