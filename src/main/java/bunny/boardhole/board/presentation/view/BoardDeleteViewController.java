package bunny.boardhole.board.presentation.view;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import bunny.boardhole.board.application.command.BoardCommandService;

/**
 * 게시글 삭제 뷰 컨트롤러
 * <p>
 * 게시글 삭제 처리를 담당합니다.
 * 인증된 사용자만 접근할 수 있습니다.
 */
@Controller
@RequestMapping("/boards/{id}/delete")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BoardDeleteViewController {

    private final BoardCommandService boardCommandService;

    /**
     * 게시글 삭제 처리
     * <p>
     * 지정된 게시글을 삭제하고 게시글 목록으로 리디렉트합니다.
     * 예외 발생 시 ViewControllerAdvice에서 처리됩니다.
     *
     * @param id                 삭제할 게시글 ID
     * @param redirectAttributes 리디렉트 시 전달할 메시지
     * @return 성공 시 게시글 목록으로 리디렉트
     * @throws bunny.boardhole.shared.exception.ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @PostMapping
    public String processDelete(
            @PathVariable UUID id,
            RedirectAttributes redirectAttributes
    ) {
        boardCommandService.delete(id);

        redirectAttributes.addFlashAttribute("success", "게시글이 성공적으로 삭제되었습니다.");
        return "redirect:/boards";
    }
}