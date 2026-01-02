package dev.xiyo.bunnyholes.boardhole.reply.presentation.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ReplyTreeResponse", description = "댓글 트리 응답")
public record ReplyTreeResponse(
    @Schema(description = "댓글 목록")
    List<ReplyResponse> replies,

    @Schema(description = "전체 댓글 수", example = "10")
    long totalCount
) {
}
