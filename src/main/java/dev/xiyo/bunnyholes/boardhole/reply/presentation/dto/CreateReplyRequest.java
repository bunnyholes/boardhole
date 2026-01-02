package dev.xiyo.bunnyholes.boardhole.reply.presentation.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

import org.jspecify.annotations.Nullable;

import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.ReplyValidationConstants;
import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.required.ValidReplyContent;

@Schema(name = "CreateReplyRequest", description = "댓글 작성 요청")
public record CreateReplyRequest(
    @Nullable
    @Schema(description = "상위 댓글 ID (대댓글인 경우)", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID parentId,

    @ValidReplyContent
    @Schema(
        description = "댓글 내용",
        example = "좋은 글이네요!",
        maxLength = ReplyValidationConstants.CONTENT_MAX_LENGTH,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String content
) {
}
