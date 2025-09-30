package dev.xiyo.bunnyholes.boardhole.board.infrastructure;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import dev.xiyo.bunnyholes.boardhole.board.domain.Board;
import dev.xiyo.bunnyholes.boardhole.testsupport.jpa.EntityTestBase;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Tag("unit")
@Tag("repository")
class BoardRepositoryTest extends EntityTestBase {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private Board testBoard;

    @BeforeEach
    void setUp() {
        author = userRepository.save(User
                .builder()
                .username("test_author")
                .password(EntityTestBase.passwordEncoder.encode("Password123!"))
                .name("Test Author")
                .email("author@example.com")
                .roles(Set.of(Role.USER))
                .build());

        testBoard = boardRepository.save(Board.builder().title("Test Board").content("Test Content").author(author).build());
    }

    // =====================================
    // CREATE 테스트
    // =====================================
    @Nested
    @DisplayName("CREATE - 게시글 생성")
    class CreateTest {

        @Test
        @DisplayName("새 게시글 생성 성공")
        void save_NewBoard_CreatesSuccessfully() {
            // Given
            Board newBoard = Board.builder().title("New Board").content("New Content").author(author).build();

            // When
            Board saved = boardRepository.save(newBoard);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTitle()).isEqualTo("New Board");
            assertThat(saved.getContent()).isEqualTo("New Content");
            assertThat(saved.getAuthor().getId()).isEqualTo(author.getId());
        }

        @Test
        @DisplayName("작성자 없이 게시글 생성 실패")
        void save_WithoutAuthor_ThrowsException() {
            // Given
            Board boardWithoutAuthor = Board.builder().title("No Author Board").content("No Author Content").author(null).build();

            // When & Then
            assertThatThrownBy(() -> boardRepository.saveAndFlush(boardWithoutAuthor)).isInstanceOf(
                    jakarta.validation.ConstraintViolationException.class);
        }
    }

    // =====================================
    // READ 테스트
    // =====================================
    @Nested
    @DisplayName("READ - 게시글 조회")
    class ReadTest {

        @Test
        @DisplayName("ID로 게시글 조회")
        void findById_ExistingBoard_ReturnsBoard() {
            // When
            Optional<Board> found = boardRepository.findById(testBoard.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("Test Board");
            assertThat(found.get().getContent()).isEqualTo("Test Content");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 결과")
        void findById_NonExistingBoard_ReturnsEmpty() {
            // When
            Optional<Board> found = boardRepository.findById(UUID.randomUUID());

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("전체 게시글 조회")
        void findAll_ReturnsAllBoards() {
            // Given
            boardRepository.save(Board.builder().title("Another Board").content("Another Content").author(
                    author).build());

            // When
            var boards = boardRepository.findAll();

            // Then
            assertThat(boards).hasSize(2);
        }

        @Test
        @DisplayName("작성자 username 조회")
        void findAuthorUsernameById_ExistingBoard_ReturnsAuthorUsername() {
            // When
            Optional<String> authorUsername = boardRepository.findAuthorUsernameById(
                    testBoard.getId());

            // Then
            assertThat(authorUsername).isPresent();
            assertThat(authorUsername.get()).isEqualTo(author.getUsername());
        }

        @Test
        @DisplayName("작성자 정보와 함께 조회")
        void findById_WithAuthor_LoadsAuthorData() {
            // When
            Optional<Board> found = boardRepository.findById(testBoard.getId());

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getAuthor()).isNotNull();
            assertThat(found.get().getAuthor().getUsername()).isEqualTo("test_author");
        }
    }

    // =====================================
    // UPDATE 테스트
    // =====================================
    @Nested
    @DisplayName("UPDATE - 게시글 수정")
    class UpdateTest {

        @Test
        @DisplayName("게시글 제목 및 내용 수정")
        void save_ExistingBoard_UpdatesSuccessfully() {
            // Given
            testBoard.setTitle("Updated Title");
            testBoard.setContent("Updated Content");

            // When
            Board updated = boardRepository.save(testBoard);

            // Then
            assertThat(updated.getTitle()).isEqualTo("Updated Title");
            assertThat(updated.getContent()).isEqualTo("Updated Content");
        }

        @Test
        @DisplayName("조회수 증가 시 버전과 조회수가 함께 증가")
        void increaseViewCount_UpdatesViewCountAndVersion() {
            // Given
            entityManager.flush();
            entityManager.clear();

            Board board = boardRepository.findByIdForUpdate(testBoard.getId()).orElseThrow();
            int originalViewCount = board.getViewCount();
            long previousVersion = board.getVersion() == null ? 0L : board.getVersion();

            // When
            board.increaseViewCount();
            entityManager.flush();
            entityManager.clear();
            Board found = boardRepository.findById(testBoard.getId()).orElseThrow();

            // Then
            assertThat(found.getViewCount()).isEqualTo(originalViewCount + 1);
            assertThat(found.getVersion()).isEqualTo(previousVersion + 1);
        }

        @Test
        @DisplayName("낙관적 잠금을 위한 전용 조회 메서드 사용")
        void findByIdForUpdate_ReturnsManagedEntity() {
            // Given
            entityManager.flush();
            entityManager.clear();

            // When
            Optional<Board> locked = boardRepository.findByIdForUpdate(testBoard.getId());

            // Then
            assertThat(locked).isPresent();
            assertThat(locked.get().getId()).isEqualTo(testBoard.getId());
        }

    }

    // =====================================
    // DELETE 테스트
    // =====================================
    @Nested
    @DisplayName("DELETE - 게시글 삭제")
    class DeleteTest {

        @Test
        @DisplayName("게시글 삭제 - 완전 삭제")
        void delete_ExistingBoard_RemovesRecord() {
            // Given
            UUID boardId = testBoard.getId();
            long countBefore = boardRepository.count();

            // When
            boardRepository.delete(testBoard);

            // Then
            assertThat(boardRepository.findById(boardId)).isEmpty();
            assertThat(boardRepository.count()).isEqualTo(countBefore - 1);
        }

        @Test
        @DisplayName("ID로 게시글 삭제 - 완전 삭제")
        void deleteById_ExistingBoard_RemovesRecord() {
            // Given
            UUID boardId = testBoard.getId();
            long countBefore = boardRepository.count();

            // When
            boardRepository.deleteById(boardId);

            // Then
            assertThat(boardRepository.findById(boardId)).isEmpty();
            assertThat(boardRepository.count()).isEqualTo(countBefore - 1);
        }

        @Test
        @DisplayName("전체 게시글 삭제 - 완전 삭제")
        void deleteAll_RemovesAllBoards() {
            // Given
            boardRepository.save(Board.builder().title("Board to Delete").content("Will be deleted").author(
                    author).build());

            // When
            boardRepository.deleteAll();

            // Then
            assertThat(boardRepository.count()).isEqualTo(0);
            assertThat(boardRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("게시글 삭제 시 작성자는 유지")
        void delete_Board_AuthorRemains() {
            // Given
            UUID boardId = testBoard.getId();
            UUID authorId = author.getId();

            // When
            boardRepository.delete(testBoard);

            // Then
            assertThat(boardRepository.findById(boardId)).isEmpty();
            assertThat(userRepository.findById(authorId)).isPresent(); // Author remains active
        }
    }

    // =====================================
    // 페이징 및 정렬 테스트
    // =====================================
    @Nested
    @DisplayName("페이징 및 정렬")
    class PagingAndSortingTest {

        @BeforeEach
        void setUpAdditionalBoards() {
            for (int i = 1; i <= 5; i++)
                boardRepository.save(Board.builder().title("Board " + i).content("Content " + i).author(
                        author).build());
        }

        @Test
        @DisplayName("페이징 처리된 게시글 목록 조회")
        void findAll_WithPaging_ReturnsPagedResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 3);

            // When
            Page<Board> page = boardRepository.findAll(pageable);

            // Then
            assertThat(page.getContent()).hasSize(3);
            assertThat(page.getTotalElements()).isEqualTo(6);
            assertThat(page.getTotalPages()).isEqualTo(2);
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("제목으로 정렬된 게시글 조회")
        void findAll_WithSort_ReturnsSortedResults() {
            // When
            var boards = boardRepository.findAll(Sort.by("title"));

            // Then
            assertThat(boards).hasSize(6);
            assertThat(boards.get(0).getTitle()).isEqualTo("Board 1");
        }
    }

    // =====================================
    // 검색 테스트
    // =====================================
    @Nested
    @DisplayName("키워드 검색")
    class SearchTest {

        @BeforeEach
        void setUpSearchableBoards() {
            boardRepository.save(Board.builder().title("Spring Boot Tutorial").content("Learn Spring Boot basics").author(
                    author).build());

            boardRepository.save(Board.builder().title("Java Best Practices").content("Essential Java coding standards").author(
                    author).build());
        }

        @Test
        @DisplayName("제목에서 키워드 검색")
        void searchByKeyword_InTitle_ReturnsMatchingBoards() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Board> result = boardRepository.searchByKeyword("Spring", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).contains("Spring");
        }

        @Test
        @DisplayName("내용에서 키워드 검색")
        void searchByKeyword_InContent_ReturnsMatchingBoards() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Board> result = boardRepository.searchByKeyword("basics", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getContent()).contains("basics");
        }

        @Test
        @DisplayName("대소문자 구분 없는 검색")
        void searchByKeyword_CaseInsensitive_ReturnsMatchingBoards() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Board> result = boardRepository.searchByKeyword("JAVA", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).containsIgnoringCase("java");
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void searchByKeyword_NoMatch_ReturnsEmpty() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Board> result = boardRepository.searchByKeyword("nonexistent", pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
        }
    }

    // =====================================
    // Auditing 테스트
    // =====================================
    @Nested
    @DisplayName("Auditing 기능")
    class AuditingTest {

        @Test
        @DisplayName("생성 시 createdAt, updatedAt 자동 설정")
        void save_NewEntity_SetsAuditFields() {
            // Given
            Board newBoard = Board.builder().title("Audit Test").content("Audit Content").author(author).build();

            // When
            Board saved = boardRepository.save(newBoard);

            // Then
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
            assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
        }

        @Test
        @DisplayName("수정 시 updatedAt 변경")
        void update_ExistingEntity_UpdatesAuditFields() {
            // Given
            var createdAt = testBoard.getCreatedAt();
            testBoard.setTitle("Modified");

            // When
            Board updated = boardRepository.save(testBoard);

            // Then
            assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
            assertThat(updated.getUpdatedAt()).isNotNull();
            assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(createdAt);
        }
    }
}
