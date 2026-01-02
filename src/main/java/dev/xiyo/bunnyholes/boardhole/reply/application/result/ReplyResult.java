package dev.xiyo.bunnyholes.boardhole.reply.application.result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public record ReplyResult(
    UUID id,
    UUID boardId,
    @Nullable UUID parentId,
    String content,
    UUID authorId,
    String authorName,
    LocalDateTime createdAt,
    @Nullable LocalDateTime updatedAt,
    boolean deleted,
    int depth,
    List<ReplyResult> children
) {
    public ReplyResult {
        if (children == null) {
            children = new ArrayList<>();
        }
    }

    public static ReplyResult of(
        UUID id,
        UUID boardId,
        @Nullable UUID parentId,
        String content,
        UUID authorId,
        String authorName,
        LocalDateTime createdAt,
        @Nullable LocalDateTime updatedAt,
        boolean deleted,
        int depth
    ) {
        return new ReplyResult(id, boardId, parentId, content, authorId, authorName, createdAt, updatedAt, deleted, depth, new ArrayList<>());
    }
}
