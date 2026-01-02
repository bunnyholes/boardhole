package dev.xiyo.bunnyholes.boardhole.reply.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.ReplyValidationConstants;
import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.optional.OptionalReplyContent;

@Schema(name = "UpdateReplyRequest", description = "댓글 수정 요청")
public record UpdateReplyRequest(
    @OptionalReplyContent
    @Schema(
        description = "수정할 댓글 내용",
        example = "수정된 댓글 내용입니다.",
        maxLength = ReplyValidationConstants.CONTENT_MAX_LENGTH
    )
    String content
) {
}
