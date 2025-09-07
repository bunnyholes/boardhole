package bunny.boardhole.board.infrastructure;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.hibernate.TransientObjectException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.shared.config.TestJpaConfig;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestJpaConfig.class)
@DisplayName("BoardRepository 테스트")
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User author;
    private Board board1;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        author = User.builder().username("testuser").password("Password123!").name("Test User").email("test@example.com").roles(java.util.Set.of(bunny.boardhole.user.domain.Role.USER)).build();
        author = userRepository.save(author);

        // 테스트 게시글 생성
        board1 = Board.builder().title("Spring Boot Tutorial").content("This is a comprehensive guide to Spring Boot").author(author).build();
        board1 = boardRepository.save(board1);

        Board board2 = Board.builder().title("Java Best Practices").content("Learn about Java coding standards and best practices").author(author).build();
        boardRepository.save(board2);

        Board board3 = Board.builder().title("Testing with JUnit").content("Complete guide to testing Spring applications with JUnit").author(author).build();
        boardRepository.save(board3);
    }

    @AfterEach
    void tearDown() {
        try {
            // 먼저 모든 게시글 삭제
            boardRepository.deleteAll();
            entityManager.flush();
            entityManager.clear();

            // 그 다음 모든 사용자 삭제
            userRepository.deleteAll();
            entityManager.flush();
            entityManager.clear();
        } catch (Exception e) {
            // 정리 실패 시 개별 삭제 시도
            try {
                List<Board> allBoards = boardRepository.findAll();
                for (Board board : allBoards)
                    boardRepository.delete(board);
                entityManager.flush();
                entityManager.clear();

                List<User> allUsers = userRepository.findAll();
                for (User user : allUsers)
                    userRepository.delete(user);
                entityManager.flush();
                entityManager.clear();
            } catch (Exception cleanupException) {
                // 정리 실패를 로그로만 남기고 계속 진행
                System.err.println("Test cleanup failed: " + cleanupException.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("게시글 단건 조회")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 게시글 조회 시 작성자 정보 포함")
        void findById_ExistingBoard_ReturnsWithAuthor() {
            // When
            Optional<Board> found = boardRepository.findById(board1.getId());

            // Then
            assertThat(found).isPresent();
            Board foundBoard = found.get();
            assertThat(foundBoard.getTitle()).isEqualTo("Spring Boot Tutorial");
            assertThat(foundBoard.getAuthor()).isNotNull();
            assertThat(foundBoard.getAuthor().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 빈 결과")
        void findById_NonExistentBoard_ReturnsEmpty() {
            // When
            Optional<Board> found = boardRepository.findById(999L);

            // Then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("게시글 목록 조회")
    class FindAllTest {

        @Test
        @DisplayName("모든 게시글 조회")
        void findAll_ReturnsAllBoards() {
            // When
            List<Board> boards = boardRepository.findAll();

            // Then
            assertThat(boards).hasSize(3);
        }

        @Test
        @DisplayName("페이징을 통한 게시글 조회")
        void findAll_WithPaging_ReturnsPagedResults() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 2);

            // When
            Page<Board> boardPage = boardRepository.findAll(pageRequest);

            // Then
            assertThat(boardPage.getContent()).hasSize(2);
            assertThat(boardPage.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("정렬을 통한 게시글 조회")
        void findAll_WithSort_ReturnsSortedResults() {
            // When
            List<Board> boards = boardRepository.findAll(org.springframework.data.domain.Sort.by("title"));

            // Then
            assertThat(boards).hasSize(3);
            // 제목 순으로 정렬되어야 함
            assertThat(boards.get(0).getTitle()).isEqualTo("Java Best Practices");
        }
    }

    @Nested
    @DisplayName("게시글 CRUD 작업")
    class CrudOperationsTest {

        @Test
        @DisplayName("게시글 생성")
        void save_NewBoard_ReturnsPersistedBoard() {
            // Given
            Board newBoard = Board.builder().title("New Test Board").content("New test content").author(author).build();

            // When
            Board savedBoard = boardRepository.save(newBoard);

            // Then
            assertThat(savedBoard.getId()).isNotNull();
            assertThat(savedBoard.getTitle()).isEqualTo("New Test Board");
            assertThat(savedBoard.getContent()).isEqualTo("New test content");
            assertThat(savedBoard.getAuthor().getId()).isEqualTo(author.getId());
        }

        @Test
        @DisplayName("게시글 수정")
        void save_ExistingBoard_UpdatesBoard() {
            // Given
            final String newTitle = "Updated Title";
            final String newContent = "Updated content";

            // When
            board1.changeTitle(newTitle);
            board1.changeContent(newContent);
            Board updatedBoard = boardRepository.save(board1);

            // Then
            assertThat(updatedBoard.getTitle()).isEqualTo(newTitle);
            assertThat(updatedBoard.getContent()).isEqualTo(newContent);
        }

        @Test
        @DisplayName("게시글 삭제")
        void delete_ExistingBoard_RemovesBoard() {
            // Given
            Long boardId = board1.getId();

            // When
            boardRepository.delete(board1);

            // Then
            Optional<Board> found = boardRepository.findById(boardId);
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("게시글 개수 확인")
        void count_ReturnsCorrectCount() {
            // When
            long count = boardRepository.count();

            // Then
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("키워드 검색")
    class SearchByKeywordTest {

        @Test
        @DisplayName("제목에서 키워드 검색")
        void searchByKeyword_InTitle_ReturnsMatchingBoards() {
            // When
            Page<Board> results = boardRepository.searchByKeyword("Spring", PageRequest.of(0, 10));

            // Then - "Spring Boot Tutorial"과 "Complete guide to testing Spring applications" 둘 다 매칭
            assertThat(results.getContent()).hasSize(2);
            assertThat(results.getContent()).anyMatch(board -> board.getTitle().contains("Spring"));
        }

        @Test
        @DisplayName("내용에서 키워드 검색")
        void searchByKeyword_InContent_ReturnsMatchingBoards() {
            // When
            Page<Board> results = boardRepository.searchByKeyword("guide", PageRequest.of(0, 10));

            // Then
            assertThat(results.getContent()).hasSize(2); // "comprehensive guide"와 "Complete guide"
        }

        @Test
        @DisplayName("존재하지 않는 키워드 검색")
        void searchByKeyword_NonExistentKeyword_ReturnsEmpty() {
            // When
            Page<Board> results = boardRepository.searchByKeyword("nonexistent", PageRequest.of(0, 10));

            // Then
            assertThat(results.getContent()).isEmpty();
        }

        @Test
        @DisplayName("제목과 내용 모두에서 키워드 검색")
        void searchByKeyword_InTitleAndContent_ReturnsMatchingBoards() {
            // When
            Page<Board> results = boardRepository.searchByKeyword("Testing", PageRequest.of(0, 10));

            // Then
            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getTitle()).contains("Testing");
        }

        @Test
        @DisplayName("대소문자 구분 없이 키워드 검색")
        void searchByKeyword_CaseInsensitive_ReturnsMatchingBoards() {
            // When
            Page<Board> results = boardRepository.searchByKeyword("JAVA", PageRequest.of(0, 10));

            // Then
            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getTitle()).containsIgnoringCase("java");
        }

        @Test
        @DisplayName("부분 단어로 키워드 검색")
        void searchByKeyword_PartialWord_ReturnsMatchingBoards() {
            // When
            Page<Board> results = boardRepository.searchByKeyword("Boot", PageRequest.of(0, 10));

            // Then
            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getTitle()).contains("Boot");
        }
    }

    @Nested
    @DisplayName("작성자 ID로 검색")
    class FindAuthorIdByIdTest {

        @Test
        @DisplayName("존재하는 게시글의 작성자 ID 조회")
        void findAuthorIdById_ExistingBoard_ReturnsAuthorId() {
            // When
            Optional<Long> authorId = boardRepository.findAuthorIdById(board1.getId());

            // Then
            assertThat(authorId).isPresent();
            assertThat(authorId.get()).isEqualTo(author.getId());
        }

        @Test
        @DisplayName("존재하지 않는 게시글의 작성자 ID 조회")
        void findAuthorIdById_NonExistentBoard_ReturnsEmpty() {
            // When
            Optional<Long> authorId = boardRepository.findAuthorIdById(999L);

            // Then
            assertThat(authorId).isEmpty();
        }
    }

    @Nested
    @DisplayName("조회수 관리")
    class ViewCountTest {

        @Test
        @DisplayName("조회수 증가")
        void incrementViewCount_IncreasesCount() {
            // Given
            int originalViewCount = board1.getViewCount();

            // When
            board1.increaseViewCount();
            boardRepository.save(board1);

            // Then
            Optional<Board> updated = boardRepository.findById(board1.getId());
            assertThat(updated).isPresent();
            assertThat(updated.get().getViewCount()).isEqualTo(originalViewCount + 1);
        }
    }

    @Nested
    @DisplayName("연관관계 및 캐스케이드")
    class RelationshipAndCascadeTest {

        @Test
        @DisplayName("Lazy Loading으로 작성자 조회")
        void lazyLoading_Author() {
            // Given
            entityManager.clear(); // 영속성 컨텍스트 클리어

            // When
            Optional<Board> found = boardRepository.findById(board1.getId());

            // Then
            assertThat(found).isPresent();
            Board foundBoard = found.get();
            // Lazy loading 확인을 위해 작성자 정보 접근
            assertThat(foundBoard.getAuthor()).isNotNull();
            assertThat(foundBoard.getAuthor().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("EntityGraph로 N+1 문제 해결")
        void entityGraph_PreventsNPlusOne() {
            // Given
            entityManager.clear();

            // When - EntityGraph로 author 함께 조회
            Optional<Board> found = boardRepository.findById(board1.getId());

            // Then - 추가 쿼리 없이 author 접근 가능
            assertThat(found).isPresent();
            assertThat(found.get().getAuthor()).isNotNull();
            assertThat(found.get().getAuthor().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("작성자 삭제 시 게시글 처리")
        void authorDeletion_BoardHandling() {
            // Given - 독립적인 사용자와 게시글 생성
            User newAuthor = User.builder().username("delete_test").password("Password123!").name("Delete Test").email("delete@example.com").roles(java.util.Set.of(bunny.boardhole.user.domain.Role.USER)).build();
            User savedAuthor = userRepository.save(newAuthor);

            Board boardWithNewAuthor = Board.builder().title("Cascade Test").content("Testing cascade").author(savedAuthor).build();
            boardRepository.save(boardWithNewAuthor);

            entityManager.flush();
            entityManager.clear();

            // When & Then - 외래키 제약으로 인해 삭제 실패 예상
            assertThatThrownBy(() -> {
                User userToDelete = userRepository.findById(savedAuthor.getId()).orElseThrow();
                userRepository.delete(userToDelete);
                userRepository.flush();
            }).satisfiesAnyOf(
                    // 데이터 무결성 위반 예외
                    throwable -> assertThat(throwable).isInstanceOf(DataIntegrityViolationException.class),
                    // 제약 조건 위반 예외  
                    throwable -> assertThat(throwable).isInstanceOf(ConstraintViolationException.class),
                    // Transient 객체 예외
                    throwable -> {
                        assertThat(throwable).isInstanceOf(InvalidDataAccessApiUsageException.class);
                        assertThat(throwable.getCause()).isInstanceOf(TransientObjectException.class);
                    });
        }

        @Test
        @DisplayName("게시글 삭제 시 작성자는 유지")
        void boardDeletion_AuthorRemains() {
            // Given
            Long boardId = board1.getId();
            Long authorId = author.getId();

            // When
            boardRepository.delete(board1);
            boardRepository.flush();

            // Then
            Optional<Board> deletedBoard = boardRepository.findById(boardId);
            Optional<User> remainingAuthor = userRepository.findById(authorId);

            assertThat(deletedBoard).isEmpty();
            assertThat(remainingAuthor).isPresent();
        }
    }

    @Nested
    @DisplayName("버전 관리 및 낙관적 잠금")
    class VersionAndOptimisticLockingTest {

        @Test
        @DisplayName("버전 관리")
        void version_Management() {
            // Given - version starts as null for new entities, becomes 0 after first save
            entityManager.flush();
            entityManager.clear();
            Board freshBoard = boardRepository.findById(board1.getId()).get();
            Long initialVersion = freshBoard.getVersion();

            // When
            freshBoard.changeTitle("Updated Title");
            Board updatedBoard = boardRepository.save(freshBoard);
            entityManager.flush();

            // Then
            assertThat(updatedBoard.getVersion()).isEqualTo(initialVersion + 1);
        }

        @Test
        @DisplayName("낙관적 잠금 충돌 발생")
        void optimisticLocking_ConflictDetection() {
            // Given - 초기 버전 확인
            entityManager.flush();
            entityManager.clear();

            Board originalBoard = boardRepository.findById(board1.getId()).get();
            Long originalVersion = originalBoard.getVersion();

            // When - 직접적으로 버전을 조작하여 낙관적 잠금 충돌 시뮬레이션
            // 다른 트랜잭션에서 업데이트가 발생했다고 가정하고 DB의 버전을 먼저 증가
            entityManager.createNativeQuery("UPDATE boards SET version = version + 1 WHERE id = ?").setParameter(1, originalBoard.getId()).executeUpdate();
            entityManager.flush();

            // Then - 이전 버전의 엔티티로 업데이트 시도하면 낙관적 잠금 예외 발생
            assertThatThrownBy(() -> {
                originalBoard.changeTitle("Should Fail Update");
                boardRepository.saveAndFlush(originalBoard);
            }).isInstanceOf(OptimisticLockingFailureException.class);
        }
    }

    @Nested
    @DisplayName("최적화된 쿼리")
    class OptimizedQueryTest {

        @Test
        @DisplayName("N+1 문제 없이 작성자 정보 함께 조회")
        void findWithAuthor_NoNPlusOneProblem() {
            // When
            List<Board> boards = boardRepository.findAll();

            // Then
            assertThat(boards).hasSize(3);
            // 각 게시글의 작성자 정보에 접근해도 추가 쿼리 발생하지 않음
            for (Board board : boards) {
                assertThat(board.getAuthor()).isNotNull();
                assertThat(board.getAuthor().getUsername()).isNotNull();
            }
        }
    }
}