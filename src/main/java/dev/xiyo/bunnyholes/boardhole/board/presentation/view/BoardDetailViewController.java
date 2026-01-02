package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.xiyo.bunnyholes.boardhole.board.application.query.BoardQueryService;
import dev.xiyo.bunnyholes.boardhole.reply.application.query.ReplyQueryService;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyTreeResult;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;

/**
 * 게시글 상세 조회 전용 뷰 컨트롤러
 * <p>
 * 게시글 상세 정보 조회를 담당합니다.
 * 조회 시 조회수가 자동으로 증가합니다.
 */
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardDetailViewController {

    private final BoardQueryService boardQueryService;
    private final ReplyQueryService replyQueryService;

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        var board = boardQueryService.getBoard(id);
        ReplyTreeResult replyTree = replyQueryService.getReplyTree(id);

        model.addAttribute("board", board);
        model.addAttribute("replies", replyTree.replies());
        model.addAttribute("replyCount", replyTree.totalCount());
        return "boards/detail";
    }
}
