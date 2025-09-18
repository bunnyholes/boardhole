package bunny.boardhole.board.presentation.view;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import bunny.boardhole.board.application.command.BoardCommandService;
import bunny.boardhole.board.presentation.dto.BoardCreateRequest;
import bunny.boardhole.board.presentation.mapper.BoardWebMapper;
import bunny.boardhole.shared.security.AppUserPrincipal;

/**
 * 게시글 작성 뷰 컨트롤러
 * <p>
 * 게시글 작성 폼 표시와 작성 처리를 담당합니다.
 * 인증된 사용자만 접근할 수 있습니다.
 */
@Controller
@RequestMapping("/boards/write")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class BoardWriteViewController {

    private final BoardCommandService boardCommandService;
    private final BoardWebMapper boardWebMapper;

    /**
     * 게시글 작성 폼 표시
     * <p>
     * 게시글을 작성할 수 있는 폼 페이지를 표시합니다.
     *
     * @param boardCreateRequest 빈 게시글 객체 (자동 생성)
     * @return 게시글 작성 폼 템플릿
     */
    @GetMapping
    public String showWriteForm(@ModelAttribute BoardCreateRequest boardCreateRequest) {
        return "board/write";
    }

    /**
     * 게시글 작성 처리
     * <p>
     * 작성된 게시글을 저장하고 상세 페이지로 리디렉트합니다.
     * 유효성 검증 실패 시 작성 폼으로 돌아갑니다.
     *
     * @param boardCreateRequest 게시글 작성 요청 데이터
     * @param bindingResult      유효성 검증 결과
     * @param principal          인증된 사용자 정보
     * @param redirectAttributes 리디렉트 시 전달할 메시지
     * @return 성공 시 게시글 상세 페이지로 리디렉트, 실패 시 작성 폼
     */
    @PostMapping
    public String processWrite(
            @Valid @ModelAttribute BoardCreateRequest boardCreateRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal AppUserPrincipal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors())
            return "board/write";

        var command = boardWebMapper.toCreateCommand(boardCreateRequest, principal.user().getId());
        var result = boardCommandService.create(command);

        redirectAttributes.addFlashAttribute("success", "게시글이 성공적으로 작성되었습니다.");
        return "redirect:/boards/" + result.id();
    }
}