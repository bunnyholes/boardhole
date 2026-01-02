package dev.xiyo.bunnyholes.boardhole.reply.application.command;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.required.ValidReplyContent;

public record CreateReplyCommand(
    @NotNull(message = "{validation.reply.board.required}") UUID boardId,

    @Nullable UUID parentId,

    @NotBlank(message = "{validation.reply.author.required}") String authorUsername,

    @ValidReplyContent String content
) {
}
