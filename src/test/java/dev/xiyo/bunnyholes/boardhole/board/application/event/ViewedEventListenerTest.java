package dev.xiyo.bunnyholes.boardhole.board.application.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import dev.xiyo.bunnyholes.boardhole.board.infrastructure.BoardRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ViewedEventListener 단위 테스트")
class ViewedEventListenerTest {

    private static final UUID BOARD_ID = UUID.randomUUID();

    @Mock
    private BoardRepository boardRepository;

    private ViewedEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new ViewedEventListener(boardRepository);
    }

    @Test
    @DisplayName("✅ 조회 이벤트 수신 시 조회수를 1회 증가시킨다")
    void onViewed_IncrementsViewCountOnce() {
        when(boardRepository.incrementViewCount(BOARD_ID)).thenReturn(1);

        listener.onViewed(new ViewedEvent(BOARD_ID));

        verify(boardRepository).incrementViewCount(BOARD_ID);
    }

    @Test
    @DisplayName("✅ 예외 발생 시 최대 5회까지 재시도한다")
    void onViewed_RetriesUpToMaxAttempts() {
        when(boardRepository.incrementViewCount(BOARD_ID))
                .thenThrow(new OptimisticLockingFailureException("retry"))
                .thenThrow(new OptimisticLockingFailureException("retry"))
                .thenThrow(new OptimisticLockingFailureException("retry"))
                .thenThrow(new OptimisticLockingFailureException("retry"))
                .thenThrow(new OptimisticLockingFailureException("retry"));

        listener.onViewed(new ViewedEvent(BOARD_ID));

        verify(boardRepository, times(5)).incrementViewCount(BOARD_ID);
    }

    @Test
    @DisplayName("✅ 재시도 도중 성공하면 즉시 종료한다")
    void onViewed_RetrySucceedsEarly() {
        when(boardRepository.incrementViewCount(BOARD_ID))
                .thenThrow(new OptimisticLockingFailureException("retry"))
                .thenReturn(1);

        listener.onViewed(new ViewedEvent(BOARD_ID));

        verify(boardRepository, times(2)).incrementViewCount(BOARD_ID);
    }

    @Test
    @DisplayName("✅ 게시글 미존재 시 재시도하지 않는다")
    void onViewed_BoardMissing_DoesNotRetry() {
        when(boardRepository.incrementViewCount(BOARD_ID)).thenReturn(0);

        listener.onViewed(new ViewedEvent(BOARD_ID));

        verify(boardRepository, times(1)).incrementViewCount(BOARD_ID);
    }
}
