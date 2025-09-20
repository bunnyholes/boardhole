package dev.xiyo.bunnyholes.boardhole.board.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BoardResponse", description = "게시글 응답")
public record BoardResponse(@Schema(description = "게시글 ID", example = "550e8400-e29b-41d4-a716-446655440000") UUID id,
                            @Schema(description = "게시글 제목", example = "안녕하세요, 반갑습니다!") String title,
                            @Schema(description = "게시글 내용", example = "이것은 게시글의 내용입니다.") String content,
                            @Schema(description = "작성자 ID", example = "550e8400-e29b-41d4-a716-446655440001") UUID authorId,
                            @Schema(description = "작성자 이름", example = "홍길동") String authorName,
                            @Schema(description = "조회수", example = "42") Integer viewCount,
                            @Schema(description = "작성 일시", example = "2024-01-15T10:30:00") LocalDateTime createdAt,
                            @Schema(description = "수정 일시", example = "2024-01-15T15:45:30") LocalDateTime updatedAt) {
}
