package dev.xiyo.bunnyholes.boardhole.shared.presentation.view;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 전역 모델 속성을 제공하는 컨트롤러 어드바이스
 * <p>
 * 모든 뷰에서 사용할 수 있는 공통 URL들을 제공합니다.
 * 컨트롤러 메서드를 직접 참조하여 URL을 생성하므로
 * 매핑이 변경되어도 자동으로 반영됩니다.
 * </p>
 */
@ControllerAdvice
public class GlobalModelAttributeController {

    /**
     * 인증 관련 URL들을 모든 뷰에서 사용할 수 있도록 제공
     */
    @ModelAttribute("authUrls")
    public AuthUrls authUrls() {
        return new AuthUrls(
                "/auth/login",
                "/auth/signup"
        );
    }

    /**
     * 인증 관련 URL들을 담는 레코드
     */
    public record AuthUrls(
            String login,
            String signup
    ) {
    }
}