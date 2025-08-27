package bunny.boardhole.dto.board;

import lombok.Data;

@Data
public class BoardRequest {
    private String title;
    private String content;
    private Long userId;
}