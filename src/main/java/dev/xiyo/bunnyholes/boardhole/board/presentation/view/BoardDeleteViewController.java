package dev.xiyo.bunnyholes.boardhole.board.presentation.view;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.xiyo.bunnyholes.boardhole.board.application.command.BoardCommandService;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;

/**
 * 게시글 삭제 전용 뷰 컨트롤러
 * <p>
 * 게시글 삭제 처리를 담당합니다.
 * 작성자 본인 또는 관리자만 삭제 가능합니다.
 */
@Controller
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardDeleteViewController {

    private final BoardCommandService boardCommandService;

    /**
     * 게시글 삭제 처리
     * <p>
     * HTML 폼의 _method=delete를 통해 DELETE 요청을 처리합니다.
     * 지정된 게시글을 삭제하고 게시글 목록으로 리디렉트합니다.
     * 작성자 본인 또는 관리자만 삭제 가능합니다.
     *
     * @param id                 삭제할 게시글 ID
     * @param redirectAttributes 리디렉트 시 전달할 메시지
     * @return 성공 시 게시글 목록으로 리디렉트
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     * @throws org.springframework.security.access.AccessDeniedException 삭제 권한이 없는 경우
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'BOARD', 'DELETE')")
    public String delete(
            @PathVariable UUID id,
            RedirectAttributes redirectAttributes
    ) {
        boardCommandService.delete(id);

        redirectAttributes.addFlashAttribute("success", "게시글이 성공적으로 삭제되었습니다.");
        return "redirect:/boards";
    }
}