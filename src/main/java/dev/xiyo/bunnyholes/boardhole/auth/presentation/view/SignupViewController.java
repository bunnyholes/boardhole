package dev.xiyo.bunnyholes.boardhole.auth.presentation.view;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import dev.xiyo.bunnyholes.boardhole.shared.security.AppUserPrincipal;
import dev.xiyo.bunnyholes.boardhole.user.application.command.CreateUserCommand;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;
import dev.xiyo.bunnyholes.boardhole.user.presentation.dto.UserCreateRequest;

/**
 * 회원가입 뷰 컨트롤러
 * <p>
 * 웹 브라우저에서의 회원가입 페이지 표시 및 회원가입 폼 처리를 담당합니다.
 * 회원가입 성공 시 자동 로그인 처리 및 게시판 페이지로 리디렉트합니다.
 * </p>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class SignupViewController {

    private final UserCommandService userCommandService;
    private final UserRepository userRepository;

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
            BindingResult bindingResult,
            Model model,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            RedirectAttributes redirectAttributes
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
        var userResult = userCommandService.create(command);

        // 생성된 사용자 조회 (자동 로그인을 위해)
        User user = userRepository.findByUsername(request.username())
                                  .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

        // 자동 로그인 처리
        AppUserPrincipal principal = new AppUserPrincipal(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        // SecurityContext에 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 세션에 SecurityContext 저장
        httpRequest.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        log.info("회원가입 및 자동 로그인 성공: username={}", request.username());

        // boards 페이지로 리디렉트
        return "redirect:/boards";
    }
}
