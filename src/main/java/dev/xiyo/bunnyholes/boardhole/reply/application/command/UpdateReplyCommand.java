package dev.xiyo.bunnyholes.boardhole.reply.application.command;

import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.optional.OptionalReplyContent;

public record UpdateReplyCommand(
    @OptionalReplyContent String content
) {
}
