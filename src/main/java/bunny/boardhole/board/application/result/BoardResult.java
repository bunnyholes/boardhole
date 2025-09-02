package bunny.boardhole.board.application.result;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 게시글 서비스 결과 DTO
 * CQRS 패턴에서 애플리케이션 레이어의 쿼리/명령 결과를 표현하는 데이터 전송 객체입니다.
 */
@Schema(name = "BoardResult", description = "게시글 서비스 결과 객체 - 애플리케이션 레이어에서 사용되는 게시글 정보")
public record BoardResult(
        @Schema(description = "게시글 ID", example = "1")
        Long id,
        @Schema(description = "게시글 제목", example = "안녕하세요, 반갑습니다!")
        String title,
        @Schema(description = "게시글 내용", example = "이것은 게시글의 내용입니다.")
        String content,
        @Schema(description = "작성자 ID", example = "1")
        Long authorId,
        @Schema(description = "작성자 이름", example = "홍길동")
        String authorName,
        @Schema(description = "조회수", example = "42")
        Integer viewCount,
        @Schema(description = "작성 일시", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,
        @Schema(description = "마지막 수정 일시", example = "2024-01-15T15:45:30")
        LocalDateTime updatedAt
) {
}

