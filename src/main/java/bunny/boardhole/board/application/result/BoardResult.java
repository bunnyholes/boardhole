package bunny.boardhole.board.application.result;

import java.time.LocalDateTime;

public record BoardResult(Long id, String title, String content, Long authorId, String authorName, Integer viewCount, LocalDateTime createdAt,
                          LocalDateTime updatedAt) {
}
