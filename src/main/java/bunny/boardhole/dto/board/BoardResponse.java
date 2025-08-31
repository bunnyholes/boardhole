package bunny.boardhole.dto.board;

import bunny.boardhole.domain.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponse {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BoardResponse from(Board board) {
        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorId(board.getAuthor() != null ? board.getAuthor().getId() : null)
                .authorName(board.getAuthor() != null ? board.getAuthor().getUsername() : null)
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    public static BoardResponse from(BoardDto dto) {
        return BoardResponse.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .authorId(dto.getAuthor() != null ? dto.getAuthor().getId() : null)
                .authorName(dto.getAuthor() != null ? dto.getAuthor().getUsername() : null)
                .viewCount(dto.getViewCount())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
