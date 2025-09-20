package dev.xiyo.bunnyholes.boardhole.board.application.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import dev.xiyo.bunnyholes.boardhole.board.application.event.ViewedEvent;
import dev.xiyo.bunnyholes.boardhole.board.application.mapper.BoardMapper;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.board.domain.Board;
import dev.xiyo.bunnyholes.boardhole.board.infrastructure.BoardRepository;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;
import dev.xiyo.bunnyholes.boardhole.shared.test.FixedKoreanLocaleExtension;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Tag("unit")
@org.junit.jupiter.api.extension.ExtendWith(FixedKoreanLocaleExtension.class)
class BoardQueryServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BoardQueryService service;

    private Board board;
    private BoardResult boardResult;

    @BeforeEach
    void setUp() {
        try (var ignored = MockitoAnnotations.openMocks(this)) {
            // Mocks will be cleaned up automatically
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup mocks", e);
        }

        // Spring LocaleContextHolder를 한국어로 설정
        LocaleContextHolder.setLocale(Locale.KOREAN);

        // MessageUtils 초기화 (setter 사용)
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        MessageUtils.setMessageSource(ms);

        // 테스트 데이터 준비
        User author = User
                .builder()
                .username("testuser")
                .password("Password123!")
                .name("Test User")
                .email("test@example.com")
                .roles(Set.of(dev.xiyo.bunnyholes.boardhole.user.domain.Role.USER))
                .build();
        ReflectionTestUtils.setField(author, "id", UUID.randomUUID());

        board = Board.builder().title("Test Board").content("Test Content").author(author).build();
        UUID boardId = UUID.randomUUID();
        ReflectionTestUtils.setField(board, "id", boardId);
        ReflectionTestUtils.setField(board, "viewCount", 0);
        ReflectionTestUtils.setField(board, "createdAt", LocalDateTime.now());

        boardResult = new BoardResult(boardId, "Test Board", "Test Content", UUID.randomUUID(), "testuser", 0, LocalDateTime.now(),
                LocalDateTime.now());
    }

    @Nested
    @DisplayName("게시글 단건 조회")
    class GetBoardTest {

        @Test
        @DisplayName("존재하는 게시글 조회 시 결과 반환 및 조회 이벤트 발행")
        void handle_ExistingBoard_ReturnsResultAndPublishesEvent() {
            // Given
            UUID boardId = UUID.randomUUID();
            GetBoardQuery query = new GetBoardQuery(boardId);
            ViewedEvent viewedEvent = new ViewedEvent(boardId);

            // Set the board ID to match
            ReflectionTestUtils.setField(board, "id", boardId);

            // Create boardResult with matching boardId
            BoardResult localBoardResult = new BoardResult(boardId, "Test Board", "Test Content", UUID.randomUUID(), "testuser", 0,
                    LocalDateTime.now(), LocalDateTime.now());

            given(boardRepository.findById(boardId)).willReturn(Optional.of(board));
            given(boardMapper.toResult(board)).willReturn(
                    localBoardResult);
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
        @DisplayName("❌ 존재하지 않는 게시글 조회 → ResourceNotFoundException with 국제화 메시지")
        void handle_NonExistingBoard_ThrowsResourceNotFoundException() {
            // Given
            UUID boardId = UUID.randomUUID();
            GetBoardQuery query = new GetBoardQuery(boardId);

            given(boardRepository.findById(boardId)).willReturn(Optional.empty());

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.board.not-found.id", boardId);

            // When & Then
            assertThatThrownBy(() -> service.handle(query))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용과 파라미터 치환 확인
            assertThat(expectedMessage)
                    .contains("게시글을 찾을 수 없습니다. ID:")
                    .contains(boardId.toString());

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

            BoardResult firstResult = result.getContent().getFirst();
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
            final String searchKeyword = "Test";
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
            assertThat(result.getContent().getFirst().title()).contains("Test");

            verify(boardRepository).searchByKeyword(searchKeyword, pageable);
            verify(boardMapper).toResult(board);
        }

        @Test
        @DisplayName("검색 결과가 없는 경우 빈 페이지 반환")
        void listWithPaging_NoSearchResults_ReturnsEmptyPage() {
            // Given
            final String searchKeyword = "NonExistent";
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
            final String searchKeyword = "";
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
            var order = result.getSort().getOrderFor("viewCount");
            assertThat(order).isNotNull();
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);

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
