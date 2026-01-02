package dev.xiyo.bunnyholes.boardhole.reply.application.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.xiyo.bunnyholes.boardhole.reply.application.mapper.ReplyMapper;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyResult;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyTreeResult;
import dev.xiyo.bunnyholes.boardhole.reply.domain.validation.ReplyValidationConstants;
import dev.xiyo.bunnyholes.boardhole.reply.infrastructure.ReplyRepository;
import dev.xiyo.bunnyholes.boardhole.reply.infrastructure.ReplyTreeProjection;

@Service
@RequiredArgsConstructor
public class ReplyQueryService {

    private final ReplyRepository replyRepository;
    private final ReplyMapper replyMapper;

    @Transactional(readOnly = true)
    public ReplyTreeResult getReplyTree(UUID boardId) {
        List<ReplyTreeProjection> flatList = replyRepository.findReplyTreeByBoardId(
            boardId,
            ReplyValidationConstants.MAX_DEPTH
        );

        return buildTree(flatList);
    }

    @Transactional(readOnly = true)
    public long countByBoardId(UUID boardId) {
        return replyRepository.countByBoardId(boardId);
    }

    private ReplyTreeResult buildTree(List<ReplyTreeProjection> flatList) {
        if (flatList.isEmpty()) {
            return new ReplyTreeResult(List.of(), 0);
        }

        Map<UUID, ReplyResult> replyMap = new LinkedHashMap<>();
        List<ReplyResult> roots = new ArrayList<>();

        for (ReplyTreeProjection projection : flatList) {
            ReplyResult result = replyMapper.toResult(projection);
            replyMap.put(result.id(), result);

            if (projection.getParentId() == null) {
                roots.add(result);
            } else {
                ReplyResult parent = replyMap.get(projection.getParentId());
                if (parent != null) {
                    parent.children().add(result);
                }
            }
        }

        return new ReplyTreeResult(roots, flatList.size());
    }
}
