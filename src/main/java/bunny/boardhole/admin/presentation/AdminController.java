package bunny.boardhole.admin.presentation;

import bunny.boardhole.admin.presentation.dto.AdminStatsResponse;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.user.infrastructure.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "관리자 API", description = "시스템 관리 및 통계 기능")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @Operation(
            summary = "시스템 통계 조회",
            description = "[ADMIN] 전체 사용자 수, 게시글 수, 전체 조회수 등 시스템 통계를 조회합니다. 관리자만 사용할 수 있습니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "시스템 통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = AdminStatsResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public AdminStatsResponse stats() {
        long users = userRepository.count();
        long boards = boardRepository.count();
        long totalViews = boardRepository.sumViewCount();
        return new AdminStatsResponse(users, boards, totalViews);
    }

}
