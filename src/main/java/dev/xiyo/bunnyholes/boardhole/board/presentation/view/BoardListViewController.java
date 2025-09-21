package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dev.xiyo.bunnyholes.boardhole.board.application.query.BoardQueryService;

/**
 * 게시글 목록 전용 뷰 컨트롤러
 * <p>
 * 게시글 목록 조회와 검색 기능을 담당합니다.
 * 페이지네이션을 지원하며, 제목/내용 검색이 가능합니다.
 */
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardListViewController {

    private final BoardQueryService boardQueryService;

    /**
     * 게시글 목록 페이지
     * <p>
     * 검색어가 있으면 제목/내용에서 검색하여 결과를 표시합니다.
     * 페이지네이션을 지원합니다.
     *
     * @param search   검색어 (선택사항)
     * @param pageable 페이지네이션 설정 (기본 10개씩)
     * @param model    뷰에 전달할 데이터
     * @return 게시글 목록 템플릿
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) String search,
            @PageableDefault Pageable pageable,
            Model model
    ) {
        var boards = boardQueryService.getBoards(search, pageable);
        model.addAttribute("boards", boards);
        model.addAttribute("search", search);
        return "boards";
    }
}