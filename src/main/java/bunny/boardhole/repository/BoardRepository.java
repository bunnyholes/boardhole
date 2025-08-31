package bunny.boardhole.repository;

import bunny.boardhole.domain.Board;
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

@Repository
@Validated
public interface BoardRepository extends JpaRepository<Board, Long> {
    
    @EntityGraph(attributePaths = {"author"})
    Optional<Board> findById(Long id);

    @EntityGraph(attributePaths = {"author"})
    List<Board> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"author"})
    Page<Board> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(@NotNull String title, @NotNull String content, @NotNull Pageable pageable);
    
    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT b FROM Board b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Board> searchByKeyword(@Param("keyword") @NotNull String keyword, @NotNull Pageable pageable);
}
