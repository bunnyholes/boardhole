package dev.xiyo.bunnyholes.boardhole.reply.infrastructure;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public interface ReplyTreeProjection {
    UUID getId();

    @Nullable
    UUID getParentId();

    String getContent();

    UUID getAuthorId();

    String getAuthorName();

    LocalDateTime getCreatedAt();

    @Nullable
    LocalDateTime getUpdatedAt();

    boolean isDeleted();

    int getDepth();
}
