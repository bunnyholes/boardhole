package bunny.boardhole.admin.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 콘텐츠 관리 명령
 * CQRS 패턴의 Command 객체로 관리자의 콘텐츠 관리 요청을 나타냅니다.
 */
@Schema(name = "ManageContentCommand", description = "콘텐츠 관리 명령 - CQRS 패턴의 Command 객체")
public record ManageContentCommand(
        @Schema(description = "관리할 게시글 ID", example = "1")
        @NotNull
        @Positive
        Long boardId,

        @Schema(description = "수행할 작업", example = "DELETE")
        @NotNull
        ContentManagementAction action
) {
    /**
     * 콘텐츠 관리 작업 유형
     */
    public enum ContentManagementAction {
        @Schema(description = "게시글 삭제")
        DELETE,
        
        @Schema(description = "게시글 숨김")
        HIDE,
        
        @Schema(description = "게시글 표시")
        SHOW
    }
}