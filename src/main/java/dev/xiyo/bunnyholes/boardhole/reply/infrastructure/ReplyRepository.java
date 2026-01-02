package dev.xiyo.bunnyholes.boardhole.reply.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dev.xiyo.bunnyholes.boardhole.reply.domain.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, UUID> {

    @Query(value = """
        WITH RECURSIVE reply_tree AS (
            SELECT r.id, r.parent_id, r.content, r.author_id, r.created_at,
                   r.updated_at, r.deleted, 0 AS depth, ARRAY[r.id] AS path
            FROM replies r
            WHERE r.board_id = :boardId AND r.parent_id IS NULL

            UNION ALL

            SELECT r.id, r.parent_id, r.content, r.author_id, r.created_at,
                   r.updated_at, r.deleted, rt.depth + 1, rt.path || r.id
            FROM replies r
            INNER JOIN reply_tree rt ON r.parent_id = rt.id
            WHERE rt.depth < :maxDepth
        )
        SELECT rt.id, rt.parent_id AS parentId, rt.content, rt.author_id AS authorId,
               u.username AS authorName, rt.created_at AS createdAt,
               rt.updated_at AS updatedAt, rt.deleted, rt.depth
        FROM reply_tree rt
        JOIN users u ON rt.author_id = u.id
        ORDER BY rt.path
        """, nativeQuery = true)
    List<ReplyTreeProjection> findReplyTreeByBoardId(
        @Param("boardId") UUID boardId,
        @Param("maxDepth") int maxDepth
    );

    @Query("SELECT COUNT(r) FROM Reply r WHERE r.board.id = :boardId AND r.deleted = false")
    long countByBoardId(@Param("boardId") UUID boardId);

    @Query("SELECT r FROM Reply r JOIN FETCH r.author WHERE r.id = :id")
    Optional<Reply> findByIdWithAuthor(@Param("id") UUID id);

    List<Reply> findByParentId(UUID parentId);
}
