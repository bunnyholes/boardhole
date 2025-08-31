package bunny.boardhole.board.infrastructure;

import bunny.boardhole.board.domain.Board;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * 게시글 데이터 접근 리포지토리
 * 게시글 엔티티에 대한 CRUD 작업 및 검색, 집계 기능을 제공합니다.
 */
@Repository
@Validated
public interface BoardRepository extends JpaRepository<Board, Long> {

    @EntityGraph(attributePaths = {"author"})
    Optional<Board> findById(Long id);

    @EntityGraph(attributePaths = {"author"})
    Page<Board> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    List<Board> findAllByOrderByCreatedAtDesc();

    /**
     * 제목 또는 내용으로 대소문자 구분 없이 검색
     *
     * @param title    검색할 제목 키워드
     * @param content  검색할 내용 키워드
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시글 페이지
     */
    @EntityGraph(attributePaths = {"author"})
    Page<Board> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(@NotNull String title, @NotNull String content, @NotNull Pageable pageable);

    /**
     * 키워드로 게시글 검색 (JPQL 사용)
     *
     * @param keyword  검색 키워드
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시글 페이지
     */
    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT b FROM Board b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Board> searchByKeyword(@Param("keyword") @NotNull String keyword, @NotNull Pageable pageable);

    /**
     * 전체 게시글 조회수 합계
     *
     * @return 전체 조회수
     */
    @Query("SELECT COALESCE(SUM(b.viewCount), 0) FROM Board b")
    long sumViewCount();

    /**
     * 게시글 작성자 ID만 조회 (권한 체크용 최적화 쿼리)
     * N+1 문제 해결: 전체 엔티티 대신 작성자 ID만 조회하여 성능 최적화
     *
     * @param boardId 게시글 ID
     * @return 작성자 ID (Optional)
     */
    @Query("SELECT b.author.id FROM Board b WHERE b.id = :boardId")
    Optional<Long> findAuthorIdById(@Param("boardId") Long boardId);
}
