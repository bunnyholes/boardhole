package dev.xiyo.bunnyholes.boardhole.user.presentation.view;

import java.util.Collections;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.xiyo.bunnyholes.boardhole.user.application.query.UserQueryService;

/**
 * 사용자 조회 전용 뷰 컨트롤러 (관리자 전용)
 * <p>
 * 관리자가 사용자 목록 조회와 특정 사용자 프로필 조회를 담당합니다.
 * 읽기 전용 작업만 처리하며, 관리자 권한이 필요합니다.
 */
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserViewController {

    private final UserQueryService userQueryService;

    /**
     * 사용자 목록 페이지 (관리자 전용)
     * <p>
     * 관리자가 전체 사용자 목록을 페이지네이션으로 조회할 수 있습니다.
     *
     * @param pageable 페이지네이션 설정 (기본 10개씩)
     * @param model    뷰에 전달할 데이터
     * @return 사용자 목록 템플릿
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String userList(
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        var users = userQueryService.getUsers(pageable);
        model.addAttribute("users", users != null ? users : Collections.emptyList());
        return "user/users";
    }

    /**
     * 사용자 프로필 페이지 (관리자 전용)
     * <p>
     * 관리자가 특정 사용자의 프로필 정보를 조회하여 표시합니다.
     *
     * @param id    사용자 ID
     * @param model 뷰에 전달할 데이터
     * @return 사용자 프로필 템플릿
     * @throws dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String userProfile(@PathVariable UUID id, Model model) {
        var user = userQueryService.getUser(id);
        model.addAttribute("user", user != null ? user : new Object());
        return "user/detail";
    }
}