package bunny.boardhole.dto.board;

import bunny.boardhole.domain.Board;
import bunny.boardhole.dto.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {
    private Long id;
    private String title;
    private String content;
    private UserDto author;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static BoardDto from(Board board) {
        return BoardDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .author(board.getAuthor() != null ? UserDto.from(board.getAuthor()) : null)
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
    
    public Board toEntity() {
        Board board = Board.builder()
                .title(this.title)
                .content(this.content)
                .author(this.author != null ? this.author.toEntity() : null)
                .build();
        board.setId(this.id);
        board.setViewCount(this.viewCount);
        return board;
    }
}