package bunny.boardhole.service;

import bunny.boardhole.domain.Board;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.repository.BoardRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BoardViewService {

    private final BoardRepository boardRepository;
    
    private static final int MAX_VIEW_RETRY = 5;

    // 비동기로 조회수 증가 (낙관적 락, 최대 5회 재시도)
    @Async("taskExecutor")
    public CompletableFuture<Void> incrementViewCountAsync(@NotNull @Positive Long id) {
        return CompletableFuture.runAsync(() -> {
            int attempt = 1;
            while (attempt <= MAX_VIEW_RETRY) {
                try {
                    incrementViewCountOnce(id);
                    log.debug("Successfully incremented view count for board id: {} on attempt {}", id, attempt);
                    return;
                } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
                    if (attempt == MAX_VIEW_RETRY) {
                        log.warn("Failed to increment view count for board id: {} after {} attempts", id, MAX_VIEW_RETRY);
                        return; // 조회수 업데이트 실패해도 데이터는 정상 반환
                    }
                    attempt++;
                    log.debug("Retrying view count increment for board id: {}, attempt: {}", id, attempt);
                } catch (ResourceNotFoundException ex) {
                    log.warn("Board not found for view count increment: {}", id);
                    return;
                } catch (Exception ex) {
                    log.error("Unexpected error during view count increment for board id: {}", id, ex);
                    return;
                }
            }
        }).exceptionally(throwable -> {
            log.error("Async view count increment failed for board id: {}", id, throwable);
            return null; // 조회수 증가 실패는 무시하고 계속 진행
        });
    }

    // 조회수 1회 증가를 별도 트랜잭션(REQUIRES_NEW)으로 수행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Board incrementViewCountOnce(@NotNull @Positive Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));
        Integer current = board.getViewCount() == null ? 0 : board.getViewCount();
        board.setViewCount(current + 1);
        // flush 해서 낙관적 락 충돌을 즉시 감지
        return boardRepository.saveAndFlush(board);
    }
}
