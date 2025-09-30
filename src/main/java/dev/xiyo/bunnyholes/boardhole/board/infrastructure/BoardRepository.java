package dev.xiyo.bunnyholes.boardhole.board.infrastructure;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.transaction.annotation.Transactional;

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
     * 게시글 작성자 사용자명만 조회 (권한 체크용 최적화 쿼리)
     * 전체 엔티티 대신 작성자 username만 조회하여 성능 최적화
     *
     * @param boardId 게시글 ID
     * @return 작성자 username (Optional)
     */
    @Query("SELECT b.author.username FROM Board b WHERE b.id = :boardId")
    Optional<String> findAuthorUsernameById(@Param("boardId") UUID boardId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT b FROM Board b WHERE b.id = :boardId")
    Optional<Board> findByIdForUpdate(@Param("boardId") UUID boardId);

    /**
     * 게시글 조회수 증가 (감사 필드 미갱신용 직접 업데이트)
     *
     * @param boardId 게시글 ID
     * @return 업데이트된 행 수 (0이면 게시글 미존재)
     */
    @Modifying(clearAutomatically = false, flushAutomatically = false)
    @Transactional
    @Query("UPDATE Board b SET b.viewCount = b.viewCount + 1 WHERE b.id = :boardId")
    int incrementViewCount(@Param("boardId") UUID boardId);

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
