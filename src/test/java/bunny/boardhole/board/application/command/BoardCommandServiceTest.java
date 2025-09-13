package bunny.boardhole.board.application.command;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import bunny.boardhole.board.application.mapper.BoardMapper;
import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.Role;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
class BoardCommandServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardMapper boardMapper;

    private BoardCommandService service;

    @BeforeEach
    void setUp() {
        try (var ignored = MockitoAnnotations.openMocks(this)) {
            // Spring LocaleContextHolder를 한국어로 설정
            LocaleContextHolder.setLocale(Locale.KOREAN);

            ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
            ms.setBasename("messages");
            ms.setDefaultEncoding("UTF-8");
            ms.setUseCodeAsDefaultMessage(true);
            ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);
            service = new BoardCommandService(boardRepository, userRepository, boardMapper);
        } catch (Exception e) {
            throw new RuntimeException("Mock setup failed", e);
        }
    }

    @Nested
    @DisplayName("게시글 생성")
    class CreateBoard {

        @Test
        @DisplayName("❌ 작성자 미존재 → ResourceNotFoundException with 국제화 메시지")
        void shouldThrowWhenAuthorNotFound() {
            // given
            final Long authorId = 99L;
            CreateBoardCommand cmd = new CreateBoardCommand(authorId, "title", "content");
            when(userRepository.findById(authorId)).thenReturn(Optional.empty());

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.user.not-found.id", authorId);

            // when & then
            assertThatThrownBy(() -> service.create(cmd))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용과 파라미터 치환 확인
            assertThat(expectedMessage)
                    .isEqualTo("사용자를 찾을 수 없습니다. ID: 99")
                    .contains("99");

            verify(userRepository).findById(authorId);
        }

        @Test
        @DisplayName("✅ 게시글 생성 성공")
        void shouldCreateBoard() {
            // given
            final Long authorId = 1L;
            User author = User.builder()
                              .username("writer")
                              .password("Password123!")
                              .name("Writer")
                              .email("writer@example.com")
                              .roles(Set.of(Role.USER))
                              .build();
            ReflectionTestUtils.setField(author, "id", authorId);

            CreateBoardCommand cmd = new CreateBoardCommand(authorId, "title", "content");
            when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

            Board board = Board.builder()
                               .title(cmd.title())
                               .content(cmd.content())
                               .author(author)
                               .build();
            ReflectionTestUtils.setField(board, "id", 1L);

            when(boardRepository.save(any(Board.class))).thenReturn(board);

            BoardResult expectedResult = new BoardResult(
                    1L, "title", "content", authorId, "writer", 0, null, null
            );
            when(boardMapper.toResult(board)).thenReturn(expectedResult);

            // when
            BoardResult result = service.create(cmd);

            // then
            assertThat(result).isEqualTo(expectedResult);
            verify(userRepository).findById(authorId);
            verify(boardRepository).save(any(Board.class));
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class UpdateBoard {

        @Test
        @DisplayName("❌ 게시글 미존재 → ResourceNotFoundException with 국제화 메시지")
        void shouldThrowWhenBoardNotFound() {
            // given
            final Long boardId = 123L;
            UpdateBoardCommand cmd = new UpdateBoardCommand(boardId, 1L, "new title", "new content");
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.board.not-found.id", boardId);

            // when & then
            assertThatThrownBy(() -> service.update(cmd))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용과 파라미터 치환 확인
            assertThat(expectedMessage)
                    .isEqualTo("게시글을 찾을 수 없습니다. ID: 123")
                    .contains("123");

            verify(boardRepository).findById(boardId);
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class DeleteBoard {

        @Test
        @DisplayName("❌ 게시글 미존재 → ResourceNotFoundException with 국제화 메시지")
        void shouldThrowWhenBoardNotFoundForDelete() {
            // given
            final Long boardId = 456L;
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.board.not-found.id", boardId);

            // when & then
            assertThatThrownBy(() -> service.delete(boardId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용과 파라미터 치환 확인
            assertThat(expectedMessage)
                    .isEqualTo("게시글을 찾을 수 없습니다. ID: 456")
                    .contains("456");

            verify(boardRepository).findById(boardId);
        }
    }

    @Nested
    @DisplayName("조회수 증가")
    class IncrementViewCount {

        @Test
        @DisplayName("✅ 조회수 증가 - save 사용 (saveAndFlush 아님)")
        void incrementViewCount_usesSaveWithoutImmediateFlush() {
            // given
            User author = User.builder()
                              .username("writer")
                              .password("Password123!")
                              .name("Writer")
                              .email("writer@example.com")
                              .roles(Set.of(Role.USER))
                              .build();
            ReflectionTestUtils.setField(author, "id", 1L);

            Board board = Board.builder()
                               .title("title")
                               .content("content")
                               .author(author)
                               .build();
            ReflectionTestUtils.setField(board, "id", 1L);

            given(boardRepository.findById(1L)).willReturn(Optional.of(board));
            given(boardRepository.save(board)).willReturn(board);

            // when
            service.incrementViewCount(new IncrementViewCountCommand(1L));

            // then
            verify(boardRepository).save(board);
            verify(boardRepository, never()).saveAndFlush(any());
            assertThat(board.getViewCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("❌ 게시글 미존재 → ResourceNotFoundException with 국제화 메시지")
        void shouldThrowWhenBoardNotFoundForViewCount() {
            // given
            final Long boardId = 789L;
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            // 실제 메시지 로드
            String expectedMessage = MessageUtils.get("error.board.not-found.id", boardId);

            // when & then
            assertThatThrownBy(() -> service.incrementViewCount(new IncrementViewCountCommand(boardId)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(expectedMessage);

            // 메시지 내용과 파라미터 치환 확인
            assertThat(expectedMessage)
                    .isEqualTo("게시글을 찾을 수 없습니다. ID: 789")
                    .contains("789");

            verify(boardRepository).findById(boardId);
        }
    }
}
