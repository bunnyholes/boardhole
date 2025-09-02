package bunny.boardhole.board.application.mapper;

import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.domain.Board;
import bunny.boardhole.shared.mapstruct.MapstructConfig;
import org.mapstruct.*;

/**
 * 게시글 애플리케이션 계층 매퍼
 * 게시글 도메인 엔티티와 애플리케이션 Result 간 매핑을 담당합니다.
 */
@Mapper(config = MapstructConfig.class)
public interface BoardMapper {

    /**
     * Board entity를 BoardResult로 변환
     * 게시글 도메인 엔티티의 정보를 애플리케이션 계층 결과 객체로 변환합니다.
     *
     * @param board 게시글 도메인 엔티티
     * @return 게시글 결과 DTO
     */
    @Mapping(target = "authorId", source = "author.id") // 작성자 ID 매핑
    @Mapping(target = "authorName", source = "author.username") // 작성자 이름 매핑
    BoardResult toResult(Board board);
}
