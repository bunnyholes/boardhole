package bunny.boardhole.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoardCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    @NotNull
    private Long authorId;
}

