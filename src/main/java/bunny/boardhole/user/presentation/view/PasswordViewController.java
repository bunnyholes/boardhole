package bunny.boardhole.user.presentation.view;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.user.application.command.UpdatePasswordCommand;
import bunny.boardhole.user.application.command.UserCommandService;

/**
 * 비밀번호 변경 뷰 컨트롤러
 * <p>
 * 사용자 자신의 비밀번호 변경 처리를 담당합니다.
 * 경로: /users/me/password
 * 인증된 사용자만 접근할 수 있습니다.
 */
@Controller
@RequestMapping("/users/me/password")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PasswordViewController {

    private final UserCommandService userCommandService;

    /**
     * 비밀번호 변경 처리
     * <p>
     * 사용자의 비밀번호를 변경하고 마이페이지로 리디렉트합니다.
     * 유효성 검증 실패나 예외 발생 시 ViewControllerAdvice에서 처리됩니다.
     *
     * @param request            비밀번호 변경 요청 데이터
     * @param bindingResult      유효성 검증 결과
     * @param principal          인증된 사용자 정보
     * @param redirectAttributes 리디렉트 시 전달할 메시지
     * @return 성공 시 마이페이지로 리디렉트
     */
    @PostMapping
    public String updatePassword(
            @Valid @ModelAttribute UpdatePasswordRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal AppUserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "입력 정보를 확인해주세요.");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.updatePasswordRequest", bindingResult);
            return "redirect:/users/me";
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            redirectAttributes.addFlashAttribute("error", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return "redirect:/users/me";
        }

        var command = new UpdatePasswordCommand(
                principal.user().getId(), 
                request.currentPassword(), 
                request.newPassword(), 
                request.confirmPassword()
        );
        userCommandService.updatePassword(command);
        
        redirectAttributes.addFlashAttribute("success", "비밀번호가 성공적으로 변경되었습니다.");
        return "redirect:/users/me";
    }

    /**
     * 비밀번호 변경 요청 DTO
     * <p>
     * 비밀번호 변경 시 필요한 데이터를 담는 레코드입니다.
     *
     * @param currentPassword 현재 비밀번호
     * @param newPassword     새 비밀번호
     * @param confirmPassword 새 비밀번호 확인
     */
    public record UpdatePasswordRequest(
            String currentPassword,
            String newPassword,
            String confirmPassword
    ) {}
}