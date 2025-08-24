package bunny.boardhole.dto.board;

import lombok.Data;

@Data
public class BoardUpdateRequest {
    private String title;
    private String content;
}

