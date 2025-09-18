package bunny.boardhole.web.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 세션 생성 테스트용 컨트롤러
 */
@Controller
public class TestViewController {
    
    @GetMapping("/test-no-sec")
    public String testNoSec() {
        return "test-no-sec";
    }
}