package dev.xiyo.bunnyholes.boardhole.reply.application.result;

import java.util.List;

public record ReplyTreeResult(
    List<ReplyResult> replies,
    long totalCount
) {
}
