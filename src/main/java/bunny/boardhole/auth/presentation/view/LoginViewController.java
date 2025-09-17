package bunny.boardhole.auth.presentation.view;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import bunny.boardhole.auth.application.AuthCommandService;
import bunny.boardhole.auth.application.command.LoginCommand;
import bunny.boardhole.auth.presentation.dto.LoginRequest;
import bunny.boardhole.auth.presentation.mapper.AuthWebMapper;
import bunny.boardhole.shared.exception.UnauthorizedException;
import bunny.boardhole.shared.util.MessageUtils;

/**
 * 로그인 뷰 컨트롤러
 * <p>
 * 웹 브라우저에서의 로그인 페이지 표시 및 로그인 폼 처리를 담당합니다.
 * 로그인 성공 시 원래 요청한 페이지 또는 게시판 페이지로 리디렉트합니다.
 * </p>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginViewController {

    private final AuthCommandService authCommandService;
    private final AuthWebMapper authWebMapper;
    private final SecurityContextRepository securityContextRepository;

    /**
     * 로그인 페이지 표시
     */
    @GetMapping("/auth/login")
    public String loginPage(
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            Model model
    ) {
        model.addAttribute("loginRequest", new LoginRequest("", ""));

        if (returnUrl != null) {
            model.addAttribute("returnUrl", returnUrl);
        }

        return "auth/login";
    }

    /**
     * 로그인 처리
     */
    @PostMapping("/auth/login")
    public String processLogin(
            @Valid @ModelAttribute LoginRequest request,
            BindingResult bindingResult,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            Model model
    ) {
        // 검증 실패 시 에러와 함께 login 페이지 재렌더링
        if (bindingResult.hasErrors()) {
            model.addAttribute("loginRequest", request);
            if (returnUrl != null)
                model.addAttribute("returnUrl", returnUrl);
            return "auth/login";
        }

        try {
            // 로그인 처리 (SessionAuthCommandService에서 인증 및 SecurityContext 저장 완료)
            LoginCommand command = authWebMapper.toLoginCommand(request);
            authCommandService.login(command);

            // SecurityContextRepository를 통해 세션에 저장
            securityContextRepository.saveContext(
                    SecurityContextHolder.getContext(),
                    httpRequest,
                    httpResponse
            );

            log.info("로그인 성공: username={}", request.username());

            // 원래 요청한 페이지로 리디렉트하거나 기본 페이지(/boards)로 이동
            String redirectUrl = (returnUrl != null && !returnUrl.isEmpty()) ? returnUrl : "/boards";
            return "redirect:" + redirectUrl;

        } catch (UnauthorizedException ex) {
            // 글로벌 에러
            bindingResult.reject("auth.invalid", MessageUtils.get("error.auth.invalid-credentials"));
            // 필드별 에러
            bindingResult.rejectValue("username", "auth.invalid", MessageUtils.get("error.auth.invalid-username"));
            bindingResult.rejectValue("password", "auth.invalid", MessageUtils.get("error.auth.invalid-password"));
            model.addAttribute("loginRequest", request);
            if (returnUrl != null)
                model.addAttribute("returnUrl", returnUrl);
            return "auth/login";
        } catch (ConstraintViolationException ex) {
            // 서비스 계층의 메서드 검증 실패 (예: 비밀번호 정책 위반 등)
            bindingResult.reject("auth.invalid", MessageUtils.get("error.auth.invalid-credentials"));
            // UX 일관성을 위해 자격 증명 오류로 처리: 두 필드 모두 에러 표시
            bindingResult.rejectValue("username", "auth.invalid", MessageUtils.get("error.auth.invalid-username"));
            bindingResult.rejectValue("password", "auth.invalid", MessageUtils.get("error.auth.invalid-password"));

            model.addAttribute("loginRequest", request);
            if (returnUrl != null)
                model.addAttribute("returnUrl", returnUrl);
            return "auth/login";
        }
    }
}
