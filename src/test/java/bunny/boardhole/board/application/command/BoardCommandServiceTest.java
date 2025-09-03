package bunny.boardhole.board.application.command;

import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class BoardCommandServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;


    private BoardCommandService service;

    @BeforeEach
    void setUp() {
        try (var mocks = MockitoAnnotations.openMocks(this)) {
            ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
            ms.setBasename("messages");
            ms.setDefaultEncoding("UTF-8");
            ms.setUseCodeAsDefaultMessage(true);
            ReflectionTestUtils.setField(MessageUtils.class, "messageSource", ms);
            service = new BoardCommandService(boardRepository, userRepository, null);
        } catch (Exception e) {
            throw new RuntimeException("Mock setup failed", e);
        }
    }

    @Test
    void incrementViewCount_usesSaveWithoutImmediateFlush() {
        User author = User.builder().username("writer").password("pw").name("Writer").email("writer@example.com").build();
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

