package bunny.boardhole.board.application.command;

import bunny.boardhole.board.domain.validation.required.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 게시글 생성 명령 DTO
 * CQRS 패턴의 Command 객체로 새로운 게시글 생성 요청을 표현합니다.
 */
@Schema(name = "CreateBoardCommand", description = "게시글 생성 명령 - CQRS 패턴의 Command 객체")
public record CreateBoardCommand(
        @NotNull(message = "{board.validation.authorId.required}")
        @Positive(message = "{board.validation.authorId.positive}")
        @Schema(description = "작성자 ID", example = "1")
        Long authorId,

        @ValidBoardTitle
        @Schema(description = "게시글 제목", example = "안녕하세요, 반갑습니다!")
        String title,

        @ValidBoardContent
        @Schema(description = "게시글 내용", example = "이것은 새로운 게시글의 내용입니다.")
        String content
) {
}

