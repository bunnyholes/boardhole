package dev.xiyo.bunnyholes.boardhole.shared.exception;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.TypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;

/**
 * 뷰 컨트롤러 전용 예외 처리 어드바이스
 * <p>
 * {domain}/presentation/view 패키지의 모든 컨트롤러에 적용되어
 * 뷰 페이지로의 리디렉트 또는 포워드를 통한 에러 처리를 담당합니다.
 * REST API용 GlobalExceptionHandler보다 높은 우선순위를 가집니다.
 */
@Slf4j
@ControllerAdvice(basePackages = {
        "dev.xiyo.bunnyholes.boardhole.auth.presentation.view",
        "dev.xiyo.bunnyholes.boardhole.board.presentation.view",
        "dev.xiyo.bunnyholes.boardhole.shared.presentation.view",
        "dev.xiyo.bunnyholes.boardhole.user.presentation.view"
})
public class ViewControllerAdvice {

    /**
     * 리소스를 찾을 수 없는 경우 처리 (404 에러)
     * <p>
     * 404 에러 페이지로 포워드하여 URL 변경 없이 에러 정보를 표시합니다.
     *
     * @param ex      발생한 예외
     * @param model   뷰에 전달할 모델
     * @param request HTTP 요청 정보
     * @return 404 에러 페이지 템플릿 경로
     */
    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public String handleNotFound(
            Exception ex,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.warn("🔍 404 error in view: path={}, message={}",
                request.getRequestURI(), ex.getMessage());

        model.addAttribute("error", ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", Instant.now());

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);

        // forward로 404 에러 페이지 표시 (URL 변경 없음)
        return "error/404";
    }

    /**
     * 접근 권한 없음 처리 (403 에러)
     * <p>
     * 로그인은 했지만 권한이 없는 리소스 접근 시 403 에러 페이지로 포워드합니다.
     * 인증 여부를 확인하여 미인증 시에는 로그인 페이지로 리디렉트합니다.
     *
     * @param ex                 발생한 예외
     * @param model              뷰에 전달할 모델
     * @param redirectAttributes 플래시 메시지 전달용
     * @param request            HTTP 요청 정보
     * @param authentication     현재 인증 정보
     * @return 403 에러 페이지 또는 로그인 페이지
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(
            AccessDeniedException ex,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            Authentication authentication
    ) {
        log.warn("🚫 403 Forbidden in view: path={}, authenticated={}, message={}",
                request.getRequestURI(),
                authentication != null && authentication.isAuthenticated(),
                ex.getMessage());

        // 인증 여부 확인
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal() instanceof String) {
            // 미인증 상태인 경우 로그인 페이지로 리디렉트 (실제로는 401 상황)
            log.debug("🔐 Not authenticated, redirecting to login");
            redirectAttributes.addFlashAttribute("error",
                    MessageUtils.get("error.auth.required"));
            return "redirect:/auth/login";
        }

        // 인증은 되었지만 권한이 없는 경우 전용 에러 페이지로 리디렉트
        log.debug("🚫 Authenticated but forbidden, redirecting to /error/403");
        redirectAttributes.addFlashAttribute("error", MessageUtils.get("error.access.denied"));
        redirectAttributes.addFlashAttribute("path", request.getRequestURI());
        redirectAttributes.addFlashAttribute("timestamp", Instant.now());
        return "redirect:/error/403";
    }

    /**
     * 타입 불일치 처리 (400 에러)
     * <p>
     * 잘못된 타입의 파라미터나 경로 변수 전달 시 400 에러 페이지로 리디렉트합니다.
     * 주로 UUID 형식 오류 등에서 발생합니다.
     *
     * @param ex                 발생한 예외
     * @param redirectAttributes 플래시 메시지 전달용
     * @param request            HTTP 요청 정보
     * @return 400 에러 페이지 리디렉트 URL
     */
    @ExceptionHandler(TypeMismatchException.class)
    public String handleTypeMismatch(
            TypeMismatchException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        log.warn("🚨 400 error in view: path={}, type={}, value={}",
                request.getRequestURI(), ex.getRequiredType(), ex.getValue());

        redirectAttributes.addFlashAttribute("error", MessageUtils.get("error.bad-request"));
        redirectAttributes.addFlashAttribute("path", request.getRequestURI());
        redirectAttributes.addFlashAttribute("timestamp", Instant.now());

        return "redirect:/error/400";
    }

    /**
     * 인증 실패 처리 (401 에러)
     * <p>
     * 인증이 필요한 페이지 접근 시 로그인 페이지로 리디렉트합니다.
     *
     * @param ex                 발생한 예외
     * @param redirectAttributes 플래시 메시지 전달용
     * @param request            HTTP 요청 정보
     * @return 로그인 페이지 리디렉트 URL
     */
    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorized(
            UnauthorizedException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        log.warn("Unauthorized access in view controller: path={}, message={}",
                request.getRequestURI(), ex.getMessage());

        redirectAttributes.addFlashAttribute("error",
                MessageUtils.get("error.auth.required"));
        return "redirect:/auth/login";
    }

    /**
     * 서버 내부 오류 처리 (500 에러)
     * <p>
     * 예상하지 못한 서버 에러 발생 시 500 에러 페이지로 포워드합니다.
     *
     * @param ex      발생한 예외
     * @param model   뷰에 전달할 모델
     * @param request HTTP 요청 정보
     * @return 500 에러 페이지 템플릿 경로
     */
    @ExceptionHandler(Exception.class)
    public String handleInternalServerError(
            Exception ex,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.error("💥 500 error in view: path={}", request.getRequestURI(), ex);

        model.addAttribute("error", MessageUtils.get("error.general"));
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", Instant.now());

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        // forward로 500 에러 페이지 표시 (URL 변경 없음)
        return "error/500";
    }

}
