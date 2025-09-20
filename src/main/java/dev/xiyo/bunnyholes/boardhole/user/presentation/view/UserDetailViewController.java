package dev.xiyo.bunnyholes.boardhole.user.presentation.view;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.xiyo.bunnyholes.boardhole.user.application.command.UpdateUserCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.application.query.UserQueryService;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserResponse;
import dev.xiyo.bunnyholes.boardhole.user.presentation.mapper.UserWebMapper;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserDetailViewController {

    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    private final UserWebMapper userWebMapper;

    @ModelAttribute("user")
    public UserResponse loadUser(@AuthenticationPrincipal UserDetails principal) {
        var username = principal.getUsername();
        var result = userQueryService.getUser(username);
        return userWebMapper.toResponse(result);
    }

    /**
     * 사용자 프로필 페이지 (관리자 전용)
     * <p>
     * 관리자가 특정 사용자의 프로필 정보를 조회하여 표시합니다.
     *
     * @param model 뷰에 전달할 데이터
     * @return 사용자 프로필 템플릿
     * @throws dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public String userProfile(@PathVariable String username, Model model) {
        var result = userQueryService.getUser(username);
        UserResponse userResponse = userWebMapper.toResponse(result);
        model.addAttribute("user", userResponse);
        return "user/detail";
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public String getUserDetailPage() {
        return "user/detail";
    }

    @GetMapping("/me/edit")
    @PreAuthorize("isAuthenticated()")
    public String showEditForm(Model model) {
        var user = (UserResponse) model.getAttribute("user");
        model.addAttribute("updateUserRequest", new UpdateUserRequest(user.name()));
        return "user/edit";
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(
            @Valid @ModelAttribute("updateUserRequest") UpdateUserRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors())
            return "user/edit";

        var command = new UpdateUserCommand(principal.getUsername(), request.name());
        userCommandService.update(command);

        redirectAttributes.addFlashAttribute("success", "프로필이 성공적으로 수정되었습니다.");
        return "redirect:/users/me";
    }
}
