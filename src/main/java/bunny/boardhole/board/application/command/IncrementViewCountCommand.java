package bunny.boardhole.board.application.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 조회수 증가 명령 DTO
 * CQRS 패턴의 비동기 Command 객체로 게시글 조회수 증가 요청을 표현합니다.
 */
@Schema(name = "IncrementViewCountCommand", description = "조회수 증가 명령 - CQRS 패턴의 비동기 Command 객체")
public record IncrementViewCountCommand(
        @NotNull(message = "{board.validation.boardId.required}")
        @Positive(message = "{board.validation.boardId.positive}")
        @Schema(description = "조회수를 증가시킬 게시글 ID", example = "1")
        Long boardId
) {
}

