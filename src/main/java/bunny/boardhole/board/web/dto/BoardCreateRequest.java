package bunny.boardhole.board.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(name = "BoardCreateRequest", description = "게시글 작성 요청")
public class BoardCreateRequest {
    @NotBlank
    @Size(max = 200)
    @Schema(description = "게시글 제목", example = "안녕하세요, 반갑습니다!", maxLength = 200, requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank
    @Size(max = 10000)
    @Schema(description = "게시글 내용", example = "이것은 새로운 게시글의 내용입니다.", maxLength = 10000, requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}

