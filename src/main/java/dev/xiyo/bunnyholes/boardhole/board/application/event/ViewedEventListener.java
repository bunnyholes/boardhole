package dev.xiyo.bunnyholes.boardhole.board.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import dev.xiyo.bunnyholes.boardhole.board.application.command.BoardCommandService;
import dev.xiyo.bunnyholes.boardhole.board.application.command.IncrementViewCountCommand;
import dev.xiyo.bunnyholes.boardhole.board.application.mapper.BoardCommandMapper;

/**
 * 게시글 조회 이벤트 처리기
 * <p>
 * ViewCount 증가를 비동기 이벤트로 처리하는 이유:
 * 1. 낙관적 동시성 제어: 조회 시 즉시 응답, ViewCount는 백그라운드 처리
 * 2. 성능 최적화: 동시 조회 시 블로킹 방지
 * 3. 장애 격리: ViewCount 업데이트 실패가 조회에 영향 없음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewedEventListener {

    private final BoardCommandService boardCommandService;
    private final BoardCommandMapper boardCommandMapper;

    /**
     * 게시글 조회 이벤트 처리
     * 비동기로 ViewCount 증가를 처리하여 조회 성능을 보장합니다.
     * 트랜잭션은 BoardCommandService.incrementViewCount에서 관리됩니다.
     */
    @Async
    @EventListener
    public void onViewed(ViewedEvent event) {
        IncrementViewCountCommand command = boardCommandMapper.toIncrementViewCountCommand(event.boardId());
        boardCommandService.incrementViewCount(command);
    }
}
