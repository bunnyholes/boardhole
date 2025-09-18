package bunny.boardhole.board.presentation.view;

import java.util.UUID;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import bunny.boardhole.board.application.command.BoardCommandService;
import bunny.boardhole.board.application.command.UpdateBoardCommand;
import bunny.boardhole.board.application.query.BoardQueryService;
import bunny.boardhole.shared.security.AppUserPrincipal;

/**
 * 게시글 수정 뷰 컨트롤러
 * <p>
 * 게시글 수정 폼 표시와 수정 처리를 담당합니다.
 * 인증된 사용자만 접근할 수 있습니다.
 */
@Controller
@RequestMapping("/boards/{id}/edit")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BoardEditViewController {

    private final BoardQueryService boardQueryService;
    private final BoardCommandService boardCommandService;

    /**
     * 게시글 수정 폼 표시
     * <p>
     * 기존 게시글 정보를 로드하여 수정 폼에 표시합니다.
     *
     * @param id    수정할 게시글 ID
     * @param model 뷰에 전달할 데이터
     * @return 게시글 수정 폼 템플릿
     * @throws bunny.boardhole.shared.exception.ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @GetMapping
    public String showEditForm(@PathVariable UUID id, Model model) {
        var board = boardQueryService.getBoard(id);
        model.addAttribute("board", board != null ? board : new Object());
        return "board/edit";
    }

    /**
     * 게시글 수정 처리
     * <p>
     * 수정된 게시글 정보를 저장하고 상세 페이지로 리디렉트합니다.
     * 유효성 검증 실패나 예외 발생 시 ViewControllerAdvice에서 처리됩니다.
     *
     * @param id                 수정할 게시글 ID
     * @param request            게시글 수정 요청 데이터
     * @param bindingResult      유효성 검증 결과
     * @param principal          인증된 사용자 정보
     * @param redirectAttributes 리디렉트 시 전달할 메시지
     * @return 성공 시 게시글 상세 페이지로 리디렉트
     */
    @PostMapping
    public String processEdit(
            @PathVariable UUID id,
            @Valid @ModelAttribute UpdateBoardRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal AppUserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "입력 정보를 확인해주세요.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateBoardRequest", bindingResult);
            redirectAttributes.addFlashAttribute("updateBoardRequest", request);
            return "redirect:/boards/" + id + "/edit";
        }

        var command = new UpdateBoardCommand(id, request.title(), request.content());
        boardCommandService.update(command);

        redirectAttributes.addFlashAttribute("success", "게시글이 성공적으로 수정되었습니다.");
        return "redirect:/boards/" + id;
    }

    /**
     * 게시글 수정 요청 DTO
     * <p>
     * 게시글 수정 시 필요한 데이터를 담는 레코드입니다.
     *
     * @param title   게시글 제목
     * @param content 게시글 내용
     */
    public record UpdateBoardRequest(
            String title,
            String content
    ) {
    }
}