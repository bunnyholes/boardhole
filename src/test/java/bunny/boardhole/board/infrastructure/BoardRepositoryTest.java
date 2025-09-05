package bunny.boardhole.board.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.shared.config.TestJpaConfig;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
@Tag("integration")
class BoardRepositoryTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    private User author;
    private Board board1;
    private Board board2;
    private Board board3;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        author = User.builder()
                .username("testuser")
                .password("password123")
                .name("Test User")
                .email("test@example.com")
                .build();
        // User는 기본적으로 USER 권한을 가짐
        author = userRepository.save(author);

        // 테스트 게시글 생성
        board1 = Board.builder()
                .title("Spring Boot Tutorial")
                .content("This is a comprehensive guide to Spring Boot")
                .author(author)
                .build();
        board1 = boardRepository.save(board1);

        board2 = Board.builder()
                .title("Java Best Practices")
                .content("Learn about Java coding standards and best practices")
                .author(author)
                .build();
        board2 = boardRepository.save(board2);

        board3 = Board.builder()
                .title("Testing with JUnit")
                .content("Complete guide to testing Spring applications with JUnit")
                .author(author)
                .build();
        board3 = boardRepository.save(board3);
    }

    @AfterEach
    void tearDown() {
        boardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("게시글 단건 조회")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 게시글 조회 시 작성자 정보 포함")
        void findById_ExistingBoard_IncludesAuthor() {
            // When
            Optional<Board> found = boardRepository.findById(board1.getId());

            // Then
            assertThat(found).isPresent();
            Board board = found.get();
            assertThat(board.getTitle()).isEqualTo("Spring Boot Tutorial");
            assertThat(board.getContent()).contains("comprehensive guide");
            assertThat(board.getAuthor()).isNotNull();
            assertThat(board.getAuthor().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 빈 결과")
        void findById_NonExistingBoard_ReturnsEmpty() {
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
        @DisplayName("페이징 처리된 전체 목록 조회")
        void findAll_WithPaging_ReturnsPagedResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));

            // When
            Page<Board> page = boardRepository.findAll(pageable);

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getTotalPages()).isEqualTo(2);
            assertThat(page.getNumber()).isEqualTo(0);
            
            // 작성자 정보 포함 확인
            page.getContent().forEach(board -> {
                assertThat(board.getAuthor()).isNotNull();
                assertThat(board.getAuthor().getUsername()).isEqualTo("testuser");
            });
        }

        @Test
        @DisplayName("제목으로 정렬된 목록 조회")
        void findAll_SortedByTitle_ReturnsInCorrectOrder() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));

            // When
            Page<Board> page = boardRepository.findAll(pageable);

            // Then
            List<Board> boards = page.getContent();
            assertThat(boards).hasSize(3);
            assertThat(boards.get(0).getTitle()).isEqualTo("Java Best Practices");
            assertThat(boards.get(1).getTitle()).isEqualTo("Spring Boot Tutorial");
            assertThat(boards.get(2).getTitle()).isEqualTo("Testing with JUnit");
        }

        @Test
        @DisplayName("두 번째 페이지 조회")
        void findAll_SecondPage_ReturnsCorrectResults() {
            // Given
            Pageable pageable = PageRequest.of(1, 2);

            // When
            Page<Board> page = boardRepository.findAll(pageable);

            // Then
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getNumber()).isEqualTo(1);
            assertThat(page.isLast()).isTrue();
        }
    }

    @Nested
    @DisplayName("게시글 검색")
    class SearchByKeywordTest {

        @Test
        @DisplayName("제목에 키워드가 포함된 게시글 검색")
        void searchByKeyword_TitleMatch_ReturnsMatchingBoards() {
            // Given
            String keyword = "Spring";
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Board> page = boardRepository.searchByKeyword(keyword, pageable);

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent())
                    .extracting(Board::getTitle)
                    .containsExactlyInAnyOrder("Spring Boot Tutorial", "Testing with JUnit");
        }

        @Test
        @DisplayName("내용에 키워드가 포함된 게시글 검색")
        void searchByKeyword_ContentMatch_ReturnsMatchingBoards() {
            // Given
            String keyword = "guide";
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Board> page = boardRepository.searchByKeyword(keyword, pageable);

            // Then
            assertThat(page.getContent()).hasSize(2);
            page.getContent().forEach(board -> {
                assertThat(board.getContent().toLowerCase()).contains("guide");
            });
        }

        @Test
        @DisplayName("대소문자 구분 없이 검색")
        void searchByKeyword_CaseInsensitive_ReturnsMatchingBoards() {
            // Given
            String keyword = "JAVA";
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Board> page = boardRepository.searchByKeyword(keyword, pageable);

            // Then
            assertThat(page.getContent()).hasSize(1);
            assertThat(page.getContent().get(0).getTitle()).isEqualTo("Java Best Practices");
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void searchByKeyword_NoMatch_ReturnsEmptyPage() {
            // Given
            String keyword = "Python";
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Board> page = boardRepository.searchByKeyword(keyword, pageable);

            // Then
            assertThat(page.getContent()).isEmpty();
            assertThat(page.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("빈 키워드로 검색 시 전체 목록 반환")
        void searchByKeyword_EmptyKeyword_ReturnsAllBoards() {
            // Given
            String keyword = "";
            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Board> page = boardRepository.searchByKeyword(keyword, pageable);

            // Then
            assertThat(page.getContent()).hasSize(3);
            assertThat(page.getTotalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("검색 결과 페이징 처리")
        void searchByKeyword_WithPaging_ReturnsPagedResults() {
            // Given - 더 많은 테스트 데이터 추가
            for (int i = 0; i < 5; i++) {
                Board extraBoard = Board.builder()
                        .title("Spring Framework Part " + i)
                        .content("Spring content " + i)
                        .author(author)
                        .build();
                boardRepository.save(extraBoard);
            }

            String keyword = "Spring";
            Pageable pageable = PageRequest.of(0, 3);

            // When
            Page<Board> page = boardRepository.searchByKeyword(keyword, pageable);

            // Then
            assertThat(page.getContent()).hasSize(3);
            assertThat(page.getTotalElements()).isEqualTo(7); // 기존 2개 + 추가 5개
            assertThat(page.getTotalPages()).isEqualTo(3);
            assertThat(page.hasNext()).isTrue();
        }
    }

    @Nested
    @DisplayName("작성자 ID 조회")
    class FindAuthorIdByIdTest {

        @Test
        @DisplayName("게시글의 작성자 ID만 조회")
        void findAuthorIdById_ExistingBoard_ReturnsAuthorId() {
            // When
            Optional<Long> authorId = boardRepository.findAuthorIdById(board1.getId());

            // Then
            assertThat(authorId).isPresent();
            assertThat(authorId.get()).isEqualTo(author.getId());
        }

        @Test
        @DisplayName("존재하지 않는 게시글의 작성자 ID 조회")
        void findAuthorIdById_NonExistingBoard_ReturnsEmpty() {
            // When
            Optional<Long> authorId = boardRepository.findAuthorIdById(999L);

            // Then
            assertThat(authorId).isEmpty();
        }
    }

    @Nested
    @DisplayName("게시글 CRUD 작업")
    class CrudOperationsTest {

        @Test
        @DisplayName("게시글 생성")
        void save_NewBoard_CreatesSuccessfully() {
            // Given
            Board newBoard = Board.builder()
                    .title("New Post")
                    .content("New content")
                    .author(author)
                    .build();

            // When
            Board saved = boardRepository.save(newBoard);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTitle()).isEqualTo("New Post");
            assertThat(saved.getContent()).isEqualTo("New content");
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
            assertThat(saved.getViewCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("게시글 수정")
        void save_ExistingBoard_UpdatesSuccessfully() {
            // Given
            board1.changeTitle("Updated Title");
            board1.changeContent("Updated content");

            // When
            Board updated = boardRepository.save(board1);

            // Then
            assertThat(updated.getTitle()).isEqualTo("Updated Title");
            assertThat(updated.getContent()).isEqualTo("Updated content");
            assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());
        }

        @Test
        @DisplayName("게시글 삭제")
        void delete_ExistingBoard_RemovesSuccessfully() {
            // Given
            Long boardId = board1.getId();

            // When
            boardRepository.delete(board1);

            // Then
            Optional<Board> found = boardRepository.findById(boardId);
            assertThat(found).isEmpty();
            assertThat(boardRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("게시글 존재 여부 확인")
        void existsById_CheckExistence() {
            // When & Then
            assertThat(boardRepository.existsById(board1.getId())).isTrue();
            assertThat(boardRepository.existsById(999L)).isFalse();
        }
    }

    @Nested
    @DisplayName("조회수 증가")
    class ViewCountTest {

        @Test
        @DisplayName("조회수 증가 처리")
        void incrementViewCount_UpdatesSuccessfully() {
            // Given
            int initialViewCount = board1.getViewCount();

            // When
            board1.increaseViewCount();
            Board updated = boardRepository.save(board1);

            // Then
            assertThat(updated.getViewCount()).isEqualTo(initialViewCount + 1);
        }
    }
}