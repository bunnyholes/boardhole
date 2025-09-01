package bunny.boardhole.admin.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdminStatsResponse", description = "관리자 시스템 통계 응답")
public record AdminStatsResponse(
        @Schema(description = "전체 사용자 수", example = "150")
        long totalUsers,
        @Schema(description = "전체 게시글 수", example = "1250")
        long totalBoards,
        @Schema(description = "전체 조회수", example = "25000")
        long totalViews
) {
}

