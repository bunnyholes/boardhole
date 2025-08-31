package bunny.boardhole.board.web.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "BoardResponse", description = "게시글 응답")
public class BoardResponse {
    @Schema(description = "게시글 ID", example = "1")
    private Long id;
    @Schema(description = "게시글 제목", example = "안녕하세요, 반갑습니다!")
    private String title;
    @Schema(description = "게시글 내용", example = "이것은 게시글의 내용입니다.")
    private String content;
    @Schema(description = "작성자 ID", example = "1")
    private Long authorId;
    @Schema(description = "작성자 이름", example = "홍길동")
    private String authorName;
    @Schema(description = "조회수", example = "42")
    private Integer viewCount;
    @Schema(description = "작성 일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    @Schema(description = "수정 일시", example = "2024-01-15T15:45:30")
    private LocalDateTime updatedAt;

}
