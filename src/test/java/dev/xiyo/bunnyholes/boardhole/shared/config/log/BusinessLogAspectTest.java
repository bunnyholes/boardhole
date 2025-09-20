package dev.xiyo.bunnyholes.boardhole.shared.config.log;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.xiyo.bunnyholes.boardhole.board.application.command.BoardCommandService;
import dev.xiyo.bunnyholes.boardhole.board.application.command.CreateBoardCommand;
import dev.xiyo.bunnyholes.boardhole.board.application.mapper.BoardMapper;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.board.domain.Board;
import dev.xiyo.bunnyholes.boardhole.board.infrastructure.BoardRepository;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.application.command.UserCommandService;
import dev.xiyo.bunnyholes.boardhole.user.application.mapper.UserMapper;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Tag("unit")
class BusinessLogAspectTest {

    private BoardCommandService boardService;
    private UserCommandService userService;
    private BoardRepository boardRepository;
    private UserRepository boardUserRepository;
    private UserRepository userRepository;
    private BoardMapper boardMapper;

    @BeforeEach
    void setUp() {
        // Setup MessageUtils using setter (no reflection)
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true);
        MessageUtils.setMessageSource(ms);

        BusinessLogAspect aspect = new BusinessLogAspect();

        // Board service setup
        boardRepository = Mockito.mock(BoardRepository.class);
        boardUserRepository = Mockito.mock(UserRepository.class);
        boardMapper = Mockito.mock(BoardMapper.class);
        BoardCommandService targetBoard = new BoardCommandService(boardRepository, boardUserRepository, boardMapper);
        AspectJProxyFactory boardFactory = new AspectJProxyFactory(targetBoard);
        boardFactory.addAspect(aspect);
        boardService = boardFactory.getProxy();

        // User service setup
        userRepository = Mockito.mock(UserRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        UserCommandService targetUser = new UserCommandService(userRepository, encoder, userMapper);
        AspectJProxyFactory userFactory = new AspectJProxyFactory(targetUser);
        userFactory.addAspect(aspect);
        userService = userFactory.getProxy();
    }

    @Test
    void boardCreate_logsSuccess_withoutArguments() {
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(BusinessLogAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        User author = User.builder().username("writer").password("pw").name("Writer").email("writer@example.com").roles(Set.of(Role.USER)).build();
        UUID authorId = UUID.randomUUID();
        ReflectionTestUtils.setField(author, "id", authorId);
        given(boardUserRepository.findById(authorId)).willReturn(Optional.of(author));
        Board board = Board.builder().title("title").content("secret content").author(author).build();
        UUID boardId = UUID.randomUUID();
        ReflectionTestUtils.setField(board, "id", boardId);
        given(boardRepository.save(any(Board.class))).willReturn(board);
        // Suppress null warning: test record with null timestamps for logging test purposes
        @SuppressWarnings("DataFlowIssue") BoardResult boardResult = new BoardResult(boardId, "title", "secret content", authorId, "writer", 0, null,
                null);
        given(boardMapper.toResult(board)).willReturn(boardResult);

        boardService.create(new CreateBoardCommand(authorId, "title", "secret content"));

        List<ILoggingEvent> events = appender.list;
        String expected = MessageUtils.get("log.board.created", boardId, "title", "writer");
        assertThat(events.stream().anyMatch(e -> e.getFormattedMessage().contains(expected))).isTrue();
        assertThat(events.stream().anyMatch(e -> e.getFormattedMessage().contains("secret content"))).isFalse();
    }

    @Test
    void boardCreate_logsWarning_onError() {
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(BusinessLogAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        User author = User.builder().username("writer").password("pw").name("Writer").email("writer@example.com").roles(Set.of(Role.USER)).build();
        UUID authorId = UUID.randomUUID();
        ReflectionTestUtils.setField(author, "id", authorId);
        given(boardUserRepository.findById(authorId)).willReturn(Optional.of(author));
        given(boardRepository.save(any(Board.class))).willThrow(new RuntimeException("db error"));

        assertThrows(RuntimeException.class, () -> boardService.create(new CreateBoardCommand(authorId, "t", "c")));
        assertThat(appender.list
                .stream()
                .anyMatch(e -> e.getFormattedMessage().contains("Method failed") || e.getFormattedMessage().contains("메소드 실패"))).isTrue();
    }

    @Test
    void userDelete_logsSuccess() {
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(BusinessLogAspect.class);
        logger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        User existing = Mockito.mock(User.class);
        given(existing.getUsername()).willReturn("user");
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(existing, "id", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(existing));

        userService.delete(userId);

        // 로그에서 사용자 삭제 메시지 확인
        assertThat(appender.list
                .stream()
                .anyMatch(e -> e.getFormattedMessage().contains("User deleted") || e.getFormattedMessage().contains("사용자 삭제"))).isTrue();
    }
}
