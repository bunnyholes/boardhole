package dev.xiyo.bunnyholes.boardhole.user.presentation.view;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.xiyo.bunnyholes.boardhole.shared.security.AppUserPrincipal;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UpdateUserCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.application.query.UserQueryService;

/**
 * 마이페이지 뷰 컨트롤러
 * <p>
 * 사용자 자신의 프로필 조회, 수정을 담당합니다.
 * 경로: /users/me
 * 인증된 사용자만 접근할 수 있습니다.
 */
@Controller
@RequestMapping("/users/me")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MyPageViewController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;

    /**
     * 마이페이지 표시
     * <p>
     * 현재 로그인한 사용자의 프로필 정보를 표시합니다.
     *
     * @param principal 인증된 사용자 정보
     * @param model     뷰에 전달할 데이터
     * @return 마이페이지 템플릿
     */
    @GetMapping
    public String mypage(@AuthenticationPrincipal AppUserPrincipal principal, Model model) {
        if (principal == null)
            model.addAttribute("user", new Object());
        else {
            var user = userQueryService.getUser(principal.user().getId());
            model.addAttribute("user", user != null ? user : new Object());
        }
        return "user/mypage";
    }

    /**
     * 프로필 수정 처리
     * <p>
     * 사용자의 프로필 정보를 수정하고 마이페이지로 리디렉트합니다.
     * 유효성 검증 실패나 예외 발생 시 ViewControllerAdvice에서 처리됩니다.
     *
     * @param request            프로필 수정 요청 데이터
     * @param bindingResult      유효성 검증 결과
     * @param principal          인증된 사용자 정보
     * @param redirectAttributes 리디렉트 시 전달할 메시지
     * @return 성공 시 마이페이지로 리디렉트
     */
    @PostMapping("/profile")
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
            return "redirect:/users/me";
        }

        var command = new UpdateUserCommand(principal.user().getId(), request.name());
        userCommandService.update(command);

        redirectAttributes.addFlashAttribute("success", "프로필이 성공적으로 수정되었습니다.");
        return "redirect:/users/me";
    }

    /**
     * 사용자 정보 수정 요청 DTO
     * <p>
     * 프로필 수정 시 필요한 데이터를 담는 레코드입니다.
     *
     * @param name 사용자 이름
     */
    public record UpdateUserRequest(
            String name
    ) {
    }
}