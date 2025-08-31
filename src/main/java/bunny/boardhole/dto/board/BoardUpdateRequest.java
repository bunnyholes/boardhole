package bunny.boardhole.dto.board;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BoardUpdateRequest {
    @Size(max = 200)
    private String title;

    @Size(max = 10000)
    private String content;
}

