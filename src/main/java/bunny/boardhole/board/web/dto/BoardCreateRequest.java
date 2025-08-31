package bunny.boardhole.board.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "BoardCreateRequest", description = "게시글 작성 요청")
public class BoardCreateRequest {
    @NotBlank(message = "{validation.board.title.required}")
    @Size(max = 200, message = "{validation.board.title.size}")
    @Schema(description = "게시글 제목", example = "안녕하세요, 반갑습니다!", maxLength = 200, requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "{validation.board.content.required}")
    @Size(max = 10000, message = "{validation.board.content.size}")
    @Schema(description = "게시글 내용", example = "이것은 새로운 게시글의 내용입니다.", maxLength = 10000, requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
