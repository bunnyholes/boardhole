package bunny.boardhole.web;

import bunny.boardhole.board.application.command.BoardCommandService;
import bunny.boardhole.board.application.command.CreateBoardCommand;
import bunny.boardhole.board.application.command.UpdateBoardCommand;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.user.application.command.UpdatePasswordCommand;
import bunny.boardhole.user.application.command.UpdateUserCommand;
import bunny.boardhole.user.application.command.UserCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 폼 제출 처리 컨트롤러
 * POST/Redirect/GET 패턴으로 폼 처리 및 플래시 메시지 관리
 */
@Slf4j
@Controller
@RequestMapping("/forms")
@RequiredArgsConstructor
public class FormController {

    private final BoardCommandService boardCommandService;
    private final UserCommandService userCommandService;

    /**
     * 게시글 작성 처리
     */
    @PostMapping("/boards")
    public String createBoard(
            @Valid @ModelAttribute CreateBoardRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal AppUserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "입력 정보를 확인해주세요.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.createBoardRequest", bindingResult);
            redirectAttributes.addFlashAttribute("createBoardRequest", request);
            return "redirect:/boards/write";
        }

        try {
            var command = new CreateBoardCommand(principal.user().getId(), request.title(), request.content());
            var result = boardCommandService.create(command);
            
            redirectAttributes.addFlashAttribute("success", "게시글이 성공적으로 작성되었습니다.");
            return "redirect:/boards/" + result.id();
        } catch (Exception e) {
            log.error("게시글 작성 실패", e);
            redirectAttributes.addFlashAttribute("error", "게시글 작성 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("createBoardRequest", request);
            return "redirect:/boards/write";
        }
    }

    /**
     * 게시글 수정 처리
     */
    @PostMapping("/boards/{id}")
    public String updateBoard(
            @PathVariable Long id,
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

        try {
            var command = new UpdateBoardCommand(id, principal.user().getId(), request.title(), request.content());
            boardCommandService.update(command);
            
            redirectAttributes.addFlashAttribute("success", "게시글이 성공적으로 수정되었습니다.");
            return "redirect:/boards/" + id;
        } catch (Exception e) {
            log.error("게시글 수정 실패: boardId={}", id, e);
            redirectAttributes.addFlashAttribute("error", "게시글 수정 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("updateBoardRequest", request);
            return "redirect:/boards/" + id + "/edit";
        }
    }

    /**
     * 게시글 삭제 처리
     */
    @PostMapping("/boards/{id}/delete")
    public String deleteBoard(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boardCommandService.delete(id);
            
            redirectAttributes.addFlashAttribute("success", "게시글이 성공적으로 삭제되었습니다.");
            return "redirect:/boards";
        } catch (Exception e) {
            log.error("게시글 삭제 실패: boardId={}", id, e);
            redirectAttributes.addFlashAttribute("error", "게시글 삭제 중 오류가 발생했습니다.");
            return "redirect:/boards/" + id;
        }
    }

    /**
     * 사용자 정보 수정 처리
     */
    @PostMapping("/users/profile")
    public String updateProfile(
            @Valid @ModelAttribute UpdateUserRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal AppUserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "입력 정보를 확인해주세요.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updateUserRequest", bindingResult);
            redirectAttributes.addFlashAttribute("updateUserRequest", request);
            return "redirect:/mypage";
        }

        try {
            var command = new UpdateUserCommand(principal.user().getId(), request.name());
            userCommandService.update(command);
            
            redirectAttributes.addFlashAttribute("success", "프로필이 성공적으로 수정되었습니다.");
            return "redirect:/mypage";
        } catch (Exception e) {
            log.error("프로필 수정 실패: userId={}", principal.user().getId(), e);
            redirectAttributes.addFlashAttribute("error", "프로필 수정 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("updateUserRequest", request);
            return "redirect:/mypage";
        }
    }

    /**
     * 비밀번호 변경 처리
     */
    @PostMapping("/users/password")
    public String updatePassword(
            @Valid @ModelAttribute UpdatePasswordRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal AppUserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "입력 정보를 확인해주세요.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updatePasswordRequest", bindingResult);
            return "redirect:/mypage";
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "redirect:/mypage";
        }

        try {
            var command = new UpdatePasswordCommand(principal.user().getId(), request.currentPassword(), request.newPassword());
            userCommandService.updatePassword(command);
            
            redirectAttributes.addFlashAttribute("success", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/mypage";
        } catch (Exception e) {
            log.error("비밀번호 변경 실패: userId={}", principal.user().getId(), e);
            redirectAttributes.addFlashAttribute("error", "비밀번호 변경 중 오류가 발생했습니다.");
            return "redirect:/mypage";
        }
    }

    // Form Request DTOs
    
    /**
     * 게시글 작성 요청 DTO
     */
    public record CreateBoardRequest(
            String title,
            String content
    ) {}

    /**
     * 게시글 수정 요청 DTO
     */
    public record UpdateBoardRequest(
            String title,
            String content
    ) {}

    /**
     * 사용자 정보 수정 요청 DTO
     */
    public record UpdateUserRequest(
            String name
    ) {}

    /**
     * 비밀번호 변경 요청 DTO
     */
    public record UpdatePasswordRequest(
            String currentPassword,
            String newPassword,
            String confirmPassword
    ) {}
}