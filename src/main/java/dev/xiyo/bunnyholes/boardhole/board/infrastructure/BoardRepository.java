package dev.xiyo.bunnyholes.boardhole.board.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.xiyo.bunnyholes.boardhole.board.domain.Board;

/**
 * 게시글 데이터 접근 리포지토리
 * 게시글 엔티티에 대한 CRUD 작업 및 검색, 집계 기능을 제공합니다.
 */
public interface BoardRepository extends JpaRepository<Board, UUID> {

    @Override
    @EntityGraph(attributePaths = "author")
    Optional<Board> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = "author")
    Page<Board> findAll(Pageable pageable);

    /**
     * 키워드로 게시글 검색 (JPQL 사용)
     *
     * @param keyword  검색 키워드
     * @param pageable 페이지네이션 정보
     * @return 검색된 게시글 페이지
     */
    @EntityGraph(attributePaths = "author")
    @Query("SELECT b FROM Board b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Board> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 게시글 작성자 ID만 조회 (권한 체크용 최적화 쿼리)
     * N+1 문제 해결: 전체 엔티티 대신 작성자 ID만 조회하여 성능 최적화
     *
     * @param boardId 게시글 ID
     * @return 작성자 ID (Optional)
     */
    @Query("SELECT b.author.id FROM Board b WHERE b.id = :boardId")
    Optional<UUID> findAuthorIdById(@Param("boardId") UUID boardId);

    /**
     * 삭제된 게시글 포함 전체 조회 (Native Query)
     *
     * @return 삭제 여부 상관없이 모든 게시글 목록
     */
    @Query(value = "SELECT * FROM boards", nativeQuery = true)
    List<Board> findAllIncludingDeleted();

    /**
     * 삭제된 게시글만 조회 (Native Query)
     *
     * @return 삭제된 게시글 목록
     */
    @Query(value = "SELECT * FROM boards WHERE deleted = true", nativeQuery = true)
    List<Board> findAllDeleted();

    /**
     * ID로 삭제 여부 상관없이 게시글 조회 (Native Query)
     *
     * @param id 게시글 ID
     * @return 게시글 (삭제 여부 상관없이)
     */
    @Query(value = "SELECT * FROM boards WHERE id = ?1", nativeQuery = true)
    Optional<Board> findByIdIncludingDeleted(UUID id);

    /**
     * 특정 기간 내 생성된 게시글 수 조회
     *
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 게시글 수
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 특정 작성자의 게시글 수 조회
     *
     * @param authorId 작성자 ID
     * @return 게시글 수
     */
    long countByAuthorId(UUID authorId);
}
