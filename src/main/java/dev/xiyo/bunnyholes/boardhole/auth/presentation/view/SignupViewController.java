package dev.xiyo.bunnyholes.boardhole.auth.presentation.view;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import dev.xiyo.bunnyholes.boardhole.auth.application.command.AuthCommandService;
import dev.xiyo.bunnyholes.boardhole.shared.exception.DuplicateEmailException;
import dev.xiyo.bunnyholes.boardhole.shared.exception.DuplicateUsernameException;
import dev.xiyo.bunnyholes.boardhole.user.application.command.CreateUserCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserCreateRequest;

/**
 * 회원가입 뷰 컨트롤러
 * <p>
 * 웹 브라우저에서의 회원가입 페이지 표시 및 회원가입 폼 처리를 담당합니다.
 * 회원가입 성공 시 자동 로그인 처리 및 게시판 페이지로 리디렉트합니다.
 * </p>
 */
@Controller
@RequiredArgsConstructor
public class SignupViewController {

    private final UserCommandService userCommandService;
    private final AuthCommandService authCommandService;

    /**
     * 회원가입 페이지 표시
     */
    @GetMapping("/auth/signup")
    public String signupPage(Model model) {
        model.addAttribute("userCreateRequest", new UserCreateRequest("", "", "", "", ""));
        return "auth/signup";
    }

    /**
     * 회원가입 처리 및 자동 로그인
     */
    @PostMapping("/auth/signup")
    public String processSignup(
            @Valid @ModelAttribute UserCreateRequest request,
            BindingResult bindingResult
    ) {
        // 검증 실패 시 폼으로 리턴
        if (bindingResult.hasErrors())
            return "auth/signup";

        // 사용자 생성
        CreateUserCommand command = new CreateUserCommand(
                request.username(),
                request.password(),
                request.name(),
                request.email()
        );

        try {
            UserResult signupResult = userCommandService.create(command);
            authCommandService.login(signupResult.id());

            // boards 페이지로 리디렉트
            return "redirect:/boards";
        } catch (DuplicateUsernameException ex) {
            bindingResult.rejectValue("username", "duplicate", ex.getMessage());
            return "auth/signup";
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            return "auth/signup";
        }
    }
}
