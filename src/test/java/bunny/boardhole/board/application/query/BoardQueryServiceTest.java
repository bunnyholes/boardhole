package bunny.boardhole.board.application.query;

import bunny.boardhole.board.application.event.ViewedEvent;
import bunny.boardhole.board.application.mapper.BoardMapper;
import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Tag("unit")
class BoardQueryServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BoardQueryService service;

    private User author;
    private Board board;
    private BoardResult boardResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // MessageUtils 초기화
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);

        // 테스트 데이터 준비
        author = User.builder()
                .username("testuser")
                .password("password")
                .name("Test User")
                .email("test@example.com")
                .build();
        ReflectionTestUtils.setField(author, "id", 1L);

        board = Board.builder()
                .title("Test Board")
                .content("Test Content")
                .author(author)
                .build();
        ReflectionTestUtils.setField(board, "id", 1L);
        ReflectionTestUtils.setField(board, "viewCount", 0);
        ReflectionTestUtils.setField(board, "createdAt", LocalDateTime.now());

        boardResult = new BoardResult(
                1L,
                "Test Board",
                "Test Content",
                1L,
                "testuser",
                0,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    @Nested
    @DisplayName("게시글 단건 조회")
    class GetBoardTest {

        @Test
        @DisplayName("존재하는 게시글 조회 시 결과 반환 및 조회 이벤트 발행")
        void handle_ExistingBoard_ReturnsResultAndPublishesEvent() {
            // Given
            Long boardId = 1L;
            GetBoardQuery query = new GetBoardQuery(boardId);
            ViewedEvent viewedEvent = new ViewedEvent(boardId);

            given(boardRepository.findById(boardId)).willReturn(Optional.of(board));
            given(boardMapper.toResult(board)).willReturn(boardResult);
            given(boardMapper.toViewedEvent(boardId)).willReturn(viewedEvent);

            // When
            BoardResult result = service.handle(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(boardId);
            assertThat(result.title()).isEqualTo("Test Board");
            assertThat(result.content()).isEqualTo("Test Content");
            assertThat(result.authorName()).isEqualTo("testuser");

            verify(boardRepository).findById(boardId);
            verify(boardMapper).toResult(board);
            verify(boardMapper).toViewedEvent(boardId);
            verify(eventPublisher).publishEvent(viewedEvent);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 ResourceNotFoundException 발생")
        void handle_NonExistingBoard_ThrowsResourceNotFoundException() {
            // Given
            Long boardId = 999L;
            GetBoardQuery query = new GetBoardQuery(boardId);

            given(boardRepository.findById(boardId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.handle(query))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(boardRepository).findById(boardId);
            verify(boardMapper, never()).toResult(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("게시글 목록 조회")
    class ListBoardsTest {

        @Test
        @DisplayName("페이징 처리된 게시글 목록 조회")
        void listWithPaging_ReturnsPagedResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<Board> boards = List.of(board);
            Page<Board> boardPage = new PageImpl<>(boards, pageable, 1);

            given(boardRepository.findAll(pageable)).willReturn(boardPage);
            given(boardMapper.toResult(any(Board.class))).willReturn(boardResult);

            // When
            Page<BoardResult> result = service.listWithPaging(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);

            BoardResult firstResult = result.getContent().get(0);
            assertThat(firstResult.title()).isEqualTo("Test Board");

            verify(boardRepository).findAll(pageable);
            verify(boardMapper).toResult(board);
        }

        @Test
        @DisplayName("빈 게시글 목록 조회")
        void listWithPaging_EmptyList_ReturnsEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Board> emptyPage = Page.empty(pageable);

            given(boardRepository.findAll(pageable)).willReturn(emptyPage);

            // When
            Page<BoardResult> result = service.listWithPaging(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(boardRepository).findAll(pageable);
            verify(boardMapper, never()).toResult(any());
        }
    }

    @Nested
    @DisplayName("게시글 검색")
    class SearchBoardsTest {

        @Test
        @DisplayName("검색어로 게시글 목록 조회")
        void listWithPaging_WithSearch_ReturnsFilteredResults() {
            // Given
            String searchKeyword = "Test";
            Pageable pageable = PageRequest.of(0, 10);
            List<Board> boards = List.of(board);
            Page<Board> boardPage = new PageImpl<>(boards, pageable, 1);

            given(boardRepository.searchByKeyword(searchKeyword, pageable)).willReturn(boardPage);
            given(boardMapper.toResult(any(Board.class))).willReturn(boardResult);

            // When
            Page<BoardResult> result = service.listWithPaging(pageable, searchKeyword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).title()).contains("Test");

            verify(boardRepository).searchByKeyword(searchKeyword, pageable);
            verify(boardMapper).toResult(board);
        }

        @Test
        @DisplayName("검색 결과가 없는 경우 빈 페이지 반환")
        void listWithPaging_NoSearchResults_ReturnsEmptyPage() {
            // Given
            String searchKeyword = "NonExistent";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Board> emptyPage = Page.empty(pageable);

            given(boardRepository.searchByKeyword(searchKeyword, pageable)).willReturn(emptyPage);

            // When
            Page<BoardResult> result = service.listWithPaging(pageable, searchKeyword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(boardRepository).searchByKeyword(searchKeyword, pageable);
            verify(boardMapper, never()).toResult(any());
        }

        @Test
        @DisplayName("빈 검색어로 검색 시 전체 목록 반환")
        void listWithPaging_EmptySearch_ReturnsAllResults() {
            // Given
            String searchKeyword = "";
            Pageable pageable = PageRequest.of(0, 10);
            List<Board> boards = List.of(board);
            Page<Board> boardPage = new PageImpl<>(boards, pageable, 1);

            given(boardRepository.searchByKeyword(searchKeyword, pageable)).willReturn(boardPage);
            given(boardMapper.toResult(any(Board.class))).willReturn(boardResult);

            // When
            Page<BoardResult> result = service.listWithPaging(pageable, searchKeyword);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(boardRepository).searchByKeyword(searchKeyword, pageable);
            verify(boardMapper).toResult(board);
        }
    }

    @Nested
    @DisplayName("페이징 및 정렬")
    class PagingAndSortingTest {

        @Test
        @DisplayName("다양한 정렬 옵션으로 목록 조회")
        void listWithPaging_DifferentSortOptions_AppliesCorrectSort() {
            // Given - 조회수 내림차순 정렬
            Pageable pageableByViewCount = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "viewCount"));
            Page<Board> boardPage = new PageImpl<>(List.of(board), pageableByViewCount, 1);

            given(boardRepository.findAll(pageableByViewCount)).willReturn(boardPage);
            given(boardMapper.toResult(any(Board.class))).willReturn(boardResult);

            // When
            Page<BoardResult> result = service.listWithPaging(pageableByViewCount);

            // Then
            assertThat(result.getSort().getOrderFor("viewCount")).isNotNull();
            assertThat(result.getSort().getOrderFor("viewCount").getDirection()).isEqualTo(Sort.Direction.DESC);

            verify(boardRepository).findAll(pageableByViewCount);
        }

        @Test
        @DisplayName("페이지 크기 및 오프셋 처리")
        void listWithPaging_DifferentPageSizes_HandlesCorrectly() {
            // Given - 페이지 크기 20, 두 번째 페이지
            Pageable pageable = PageRequest.of(1, 20);
            Page<Board> boardPage = new PageImpl<>(List.of(), pageable, 50);

            given(boardRepository.findAll(pageable)).willReturn(boardPage);

            // When
            Page<BoardResult> result = service.listWithPaging(pageable);

            // Then
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(20);
            assertThat(result.getTotalElements()).isEqualTo(50);
            assertThat(result.getTotalPages()).isEqualTo(3); // 50/20 = 2.5 -> 3 pages

            verify(boardRepository).findAll(pageable);
        }
    }
}