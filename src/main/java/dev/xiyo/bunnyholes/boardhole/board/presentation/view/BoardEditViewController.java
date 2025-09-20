package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import java.util.UUID;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.xiyo.bunnyholes.boardhole.board.application.command.BoardCommandService;
import dev.xiyo.bunnyholes.boardhole.board.application.query.BoardQueryService;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardFormRequest;
import dev.xiyo.bunnyholes.boardhole.board.presentation.mapper.BoardWebMapper;

/**
 * 게시글 수정 뷰 컨트롤러
 * <p>
 * 게시글 수정 폼 표시와 수정 처리를 담당합니다.
 * 게시글 작성자 본인 또는 관리자만 접근할 수 있습니다.
 */
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardEditViewController {

    private final BoardQueryService boardQueryService;
    private final BoardCommandService boardCommandService;
    private final BoardWebMapper boardWebMapper;

    /**
     * 게시글 수정 폼 표시
     * <p>
     * 기존 게시글 정보를 로드하여 수정 폼에 표시합니다.
     * 작성자 본인 또는 관리자만 접근 가능합니다.
     *
     * @param id    수정할 게시글 ID
     * @param model 뷰에 전달할 데이터
     * @return 게시글 수정 폼 템플릿
     * @throws dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException 게시글을 찾을 수 없는 경우
     * @throws org.springframework.security.access.AccessDeniedException                수정 권한이 없는 경우
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasPermission(#id, 'BOARD', 'WRITE')")
    public String showEditForm(@PathVariable UUID id, Model model) {
        var board = boardQueryService.getBoard(id);
        model.addAttribute("board", boardWebMapper.toFormRequest(board));
        return "board/edit";
    }

    /**
     * 게시글 수정 처리
     * <p>
     * HTML 폼의 _method=put을 통해 PUT 요청을 처리합니다.
     * 수정된 게시글 정보를 저장하고 상세 페이지로 리디렉트합니다.
     * 유효성 검증 실패 시 수정 폼으로 돌아갑니다.
     * 작성자 본인 또는 관리자만 수정 가능합니다.
     *
     * @param id                 수정할 게시글 ID
     * @param formRequest        게시글 수정 요청 데이터
     * @param bindingResult      유효성 검증 결과
     * @param redirectAttributes 리디렉트 시 전달할 메시지
     * @return 성공 시 게시글 상세 페이지로 리디렉트, 실패 시 수정 폼
     * @throws org.springframework.security.access.AccessDeniedException 수정 권한이 없는 경우
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'BOARD', 'WRITE')")
    public String processEdit(
            @PathVariable UUID id,
            @Valid @ModelAttribute("board") BoardFormRequest formRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors())
            return "board/edit";

        var command = boardWebMapper.toUpdateCommand(id, formRequest);
        boardCommandService.update(command);

        redirectAttributes.addFlashAttribute("success", "게시글이 성공적으로 수정되었습니다.");
        return "redirect:/boards/" + id;
    }
}