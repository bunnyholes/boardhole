package bunny.boardhole.admin.presentation;

import bunny.boardhole.admin.application.command.*;
import bunny.boardhole.admin.application.query.AdminQueryService;
import bunny.boardhole.admin.presentation.dto.AdminStatsResponse;
import bunny.boardhole.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 API 컨트롤러
 * 시스템 통계 조회 및 관리자 전용 기능을 제공합니다.
 * 모든 엔드포인트는 ADMIN 권한이 필요합니다.
 */
@RestController
@RequestMapping(ApiPaths.ADMIN)
@Tag(name = "관리자 API", description = "시스템 관리 및 통계 기능")
@RequiredArgsConstructor
public class AdminController {

    /** 관리자 조회 서비스 */
    private final AdminQueryService adminQueryService;
    private final AdminCommandService adminCommandService;

    /**
     * 시스템 통계 조회 API
     * 전체 사용자 수, 게시글 수, 전체 조회수를 반환합니다.
     *
     * @return 시스템 통계 정보
     */
    @GetMapping(ApiPaths.ADMIN_STATS)
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
        return adminQueryService.getSystemStats();
    }

    @PostMapping("/users/manage")
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @Operation(
            summary = "사용자 관리",
            description = "[ADMIN] 사용자 차단, 권한 변경 등 사용자 관리 작업을 수행합니다. 관리자만 사용할 수 있습니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 관리 작업 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<Void> manageUser(
            @RequestBody(description = "사용자 관리 요청")
            @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid ManageUserCommand cmd
    ) {
        adminCommandService.manageUser(cmd);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/content/manage")
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @Operation(
            summary = "콘텐츠 관리",
            description = "[ADMIN] 게시글 삭제, 숨김 등 콘텐츠 관리 작업을 수행합니다. 관리자만 사용할 수 있습니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "콘텐츠 관리 작업 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<Void> manageContent(
            @RequestBody(description = "콘텐츠 관리 요청")
            @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid ManageContentCommand cmd
    ) {
        adminCommandService.manageContent(cmd);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/settings")
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @Operation(
            summary = "시스템 설정 업데이트",
            description = "[ADMIN] 시스템 설정을 업데이트합니다. 관리자만 사용할 수 있습니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시스템 설정 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public ResponseEntity<Void> updateSystemSettings(
            @RequestBody(description = "시스템 설정 업데이트 요청")
            @org.springframework.web.bind.annotation.RequestBody @jakarta.validation.Valid UpdateSystemSettingsCommand cmd
    ) {
        adminCommandService.updateSystemSettings(cmd);
        return ResponseEntity.ok().build();
    }

}
