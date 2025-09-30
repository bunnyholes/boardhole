package dev.xiyo.bunnyholes.boardhole.board.application.event;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import dev.xiyo.bunnyholes.boardhole.board.infrastructure.BoardRepository;

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

    private static final int MAX_RETRY_ATTEMPTS = 5;

    private final BoardRepository boardRepository;

    /**
     * 게시글 조회 이벤트 처리
     * 비동기로 ViewCount 증가를 처리하여 조회 성능을 보장합니다.
     * 트랜잭션은 @Modifying 리포지토리 메서드에서 자체적으로 관리됩니다.
     */
    @Async
    @EventListener
    public void onViewed(ViewedEvent event) {
        UUID boardId = event.boardId();

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                int updatedRows = boardRepository.incrementViewCount(boardId);

                if (updatedRows == 0) {
                    log.debug("조회수 증가 스킵 - 게시글 미존재. boardId={}", boardId);
                }
                return;
            } catch (DataAccessException ex) {
                if (attempt >= MAX_RETRY_ATTEMPTS) {
                    log.warn("조회수 증가 실패 - 최대 재시도 초과. boardId={}, attempts={}", boardId, MAX_RETRY_ATTEMPTS, ex);
                    return;
                }

                log.debug("조회수 증가 재시도. boardId={}, attempt={} / {}", boardId, attempt, MAX_RETRY_ATTEMPTS, ex);
            }
        }
    }
}
