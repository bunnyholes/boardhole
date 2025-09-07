package bunny.boardhole.board.application.command;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Tag("unit")
class BoardCommandServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private bunny.boardhole.board.application.mapper.BoardMapper boardMapper;

    private BoardCommandService service;

    @BeforeEach
    void setUp() {
        try (var ignored = MockitoAnnotations.openMocks(this)) {
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

    @Test
    void incrementViewCount_usesSaveWithoutImmediateFlush() {
        User author = User.builder().username("writer").password("Password123!").name("Writer").email("writer@example.com").roles(java.util.Set.of(bunny.boardhole.user.domain.Role.USER)).build();
        ReflectionTestUtils.setField(author, "id", 1L);
        Board board = Board.builder().title("title").content("content").author(author).build();
        ReflectionTestUtils.setField(board, "id", 1L);
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(boardRepository.save(board)).willReturn(board);

        service.incrementViewCount(new IncrementViewCountCommand(1L));

        verify(boardRepository).save(board);
        verify(boardRepository, never()).saveAndFlush(any());
        assertThat(board.getViewCount()).isEqualTo(1);
    }
}
