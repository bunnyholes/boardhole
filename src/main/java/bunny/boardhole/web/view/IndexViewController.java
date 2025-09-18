package bunny.boardhole.web.view;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 인덱스(홈) 페이지 뷰 컨트롤러
 * <p>
 * 애플리케이션의 홈페이지를 담당합니다.
 */
@Controller
@RequiredArgsConstructor
public class IndexViewController {

    /**
     * 홈페이지 표시
     * <p>
     * 애플리케이션의 메인 홈페이지를 표시합니다.
     * 빌드 정보를 포함합니다.
     *
     * @param model 뷰에 전달할 데이터
     * @return 인덱스 템플릿
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 인증 필요 에러 페이지 (401)
     * <p>
     * 인증되지 않은 사용자가 보호된 페이지 접근 시 표시됩니다.
     *
     * @param redirect 로그인 후 이동할 URL
     * @param model    뷰에 전달할 데이터
     * @return 401 에러 템플릿
     */
    @GetMapping("/error/401")
    public String unauthorized(
            @RequestParam(value = "redirect", required = false) String redirect,
            Model model
    ) {
        if (redirect != null && !redirect.isEmpty())
            model.addAttribute("redirectUrl", redirect);
        return "error/401";
    }
}