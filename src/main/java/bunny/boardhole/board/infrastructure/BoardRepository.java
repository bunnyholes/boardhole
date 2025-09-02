package bunny.boardhole.board.infrastructure;

import bunny.boardhole.board.domain.Board;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.*;

/**
 * 게시글 데이터 접근 리포지토리
 * 게시글 엔티티에 대한 CRUD 작업 및 검색, 집계 기능을 제공합니다.
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>게시글 조회 및 페이지네이션 (작성자 정보 포함)</li>
 *   <li>제목 및 내용으로 검색 (대소문자 무시)</li>
 *   <li>전체 조회수 집계 및 최적화된 인가 체크</li>
 * </ul>
 */
@Repository
@Validated
public interface BoardRepository extends JpaRepository<Board, Long> {

    /** 작성자 정보를 포함하여 게시글 조회 */
    @EntityGraph(attributePaths = {"author"})
    @Override
    Optional<Board> findById(Long boardIdentifier);

    /** 작성자 정보를 포함하여 모든 게시글 페이지 조회 */
    @EntityGraph(attributePaths = {"author"})
    @Override
    Page<Board> findAll(Pageable pageable);

    /** 작성자 정보를 포함하여 최신 순으로 모든 게시글 조회 */
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
    /** 제목 또는 내용으로 게시글 검색 (대소문자 무시) */
    @EntityGraph(attributePaths = {"author"})
    Page<Board> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(@NotNull String title, @NotNull String content, @NotNull Pageable pageable);

    /**
     * 키워드로 게시글 검색 (JPQL 사용)
     * JPQL LOWER 함수와 CONCAT을 사용하여 대소문자 무시 검색을 수행합니다.
     * 제목과 내용 중 어느 하나라도 키워드가 포함되면 결과에 포함됩니다.
     *
     * @param keyword  검색 키워드
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시글 페이지 (작성자 정보 포함)
     */
    /** 키워드로 게시글 검색 (JPQL) */
    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT b FROM Board b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Board> searchByKeyword(@Param("keyword") @NotNull String keyword, @NotNull Pageable pageable);

    /**
     * 전체 게시글 조회수 합계
     * JPQL COALESCE 함수를 사용하여 NULL 값 처리를 하고 모든 게시글의 조회수를 합산합니다.
     * 데이터가 없을 경우 0을 반환합니다.
     *
     * @return 전체 조회수 (데이터 없을 시 0)
     */
    /** 전체 게시글 조회수 합계 */
    @Query("SELECT COALESCE(SUM(b.viewCount), 0) FROM Board b")
    long sumViewCount();

    /**
     * 게시글 작성자 ID만 조회 (권한 체크용 최적화 쿼리)
     * JPQL을 사용하여 전체 엔티티 대신 작성자 ID만 선택적으로 조회합니다.
     * N+1 문제를 방지하고 권한 체크 시 성능을 최적화합니다.
     *
     * @param boardId 게시글 ID
     * @return 작성자 ID (Optional, 게시글이 존재하지 않으면 Empty)
     */
    /** 게시글 작성자 ID 조회 (권한 체크용 최적화) */
    @Query("SELECT b.author.id FROM Board b WHERE b.id = :boardIdentifier")
    Optional<Long> findAuthorIdById(@Param("boardIdentifier") Long boardIdentifier);
}
