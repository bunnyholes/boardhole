package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dev.xiyo.bunnyholes.boardhole.board.application.query.BoardQueryService;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;

/**
 * 게시판 조회 전용 뷰 컨트롤러
 * <p>
 * 게시글 목록 조회와 상세 조회를 담당합니다.
 * 읽기 전용 작업만 처리하며, 작성/수정/삭제는 별도 컨트롤러에서 처리합니다.
 */
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardViewController {

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

    /**
     * 게시글 상세 페이지
     * <p>
     * 게시글 상세 정보를 조회하여 표시합니다.
     * 조회 시 조회수가 자동으로 증가합니다.
     *
     * @param id    게시글 ID
     * @param model 뷰에 전달할 데이터
     * @return 게시글 상세 템플릿
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        var board = boardQueryService.getBoard(id);
        model.addAttribute("board", board);
        return "board/detail";
    }
}