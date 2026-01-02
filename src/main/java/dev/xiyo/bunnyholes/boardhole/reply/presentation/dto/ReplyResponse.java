package dev.xiyo.bunnyholes.boardhole.reply.presentation.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import org.jspecify.annotations.Nullable;

@Schema(name = "ReplyResponse", description = "댓글 응답")
public record ReplyResponse(
    @Schema(description = "댓글 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Nullable
    @Schema(description = "상위 댓글 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID parentId,

    @Schema(description = "댓글 내용", example = "좋은 글이네요!")
    String content,

    @Schema(description = "작성자 ID", example = "550e8400-e29b-41d4-a716-446655440002")
    UUID authorId,

    @Schema(description = "작성자 이름", example = "홍길동")
    String authorName,

    @Schema(description = "작성 일시", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt,

    @Nullable
    @Schema(description = "수정 일시", example = "2024-01-15T15:45:30")
    LocalDateTime updatedAt,

    @Schema(description = "삭제 여부", example = "false")
    boolean deleted,

    @Schema(description = "댓글 깊이 (0: 루트)", example = "0")
    int depth,

    @Schema(description = "자식 댓글 목록")
    List<ReplyResponse> children
) {
}
