package dev.xiyo.bunnyholes.boardhole.board.application.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record BoardResult(UUID id, String title, String content, UUID authorId, String authorName, Integer viewCount, LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
}
