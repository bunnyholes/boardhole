package bunny.boardhole.board.application.command;

import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import bunny.boardhole.board.application.mapper.BoardMapper;
import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;

/**
 * 게시글 명령 서비스
 * CQRS 패턴의 Command 측면으로 게시글 생성, 수정, 삭제 등 데이터 변경 작업을 담당합니다.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BoardCommandService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardMapper boardMapper;

    /**
     * 게시글 생성
     *
     * @param cmd 게시글 생성 명령
     * @return 생성된 게시글 결과
     * @throws ResourceNotFoundException 작성자를 찾을 수 없는 경우
     */
    @Transactional
    public BoardResult create(@Valid CreateBoardCommand cmd) {
        UUID authorId = cmd.authorId();
        User author = userRepository
                .findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.user.not-found.id", authorId)));

        Board board = Board.builder().title(cmd.title()).content(cmd.content()).author(author).build();
        Board saved = boardRepository.save(board);

        return boardMapper.toResult(saved);
    }

    // 조회 전용: 증가 없이 단순 조회 (권한 확인 등 내부 용도)

    /**
     * 게시글 수정 - @DynamicUpdate를 활용한 선택적 업데이트
     *
     * @param cmd 게시글 수정 명령
     * @return 수정된 게시글 결과
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    @PreAuthorize("hasPermission(#cmd.boardId, 'BOARD', 'WRITE')")
    public BoardResult update(@Valid UpdateBoardCommand cmd) {
        UUID id = cmd.boardId();
        Board board = boardRepository
                .findById(id).orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.board.not-found.id", id)));

        // Optional을 사용한 선택적 필드 업데이트
        Optional.ofNullable(cmd.title()).ifPresent(board::changeTitle);
        Optional.ofNullable(cmd.content()).ifPresent(board::changeContent);

        // @DynamicUpdate가 변경된 필드만 업데이트, @PreUpdate가 updatedAt 자동 설정
        Board saved = boardRepository.save(board);

        return boardMapper.toResult(saved);
    }

    /**
     * 게시글 삭제
     *
     * @param id 삭제할 게시글 ID
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    @PreAuthorize("hasPermission(#id, 'BOARD', 'DELETE')")
    public void delete(@NotNull UUID id) {
        Board board = loadBoardOrThrow(id);
        boardRepository.delete(board);
    }

    /**
     * 조회수 증가 (비동기 이벤트 처리용)
     * <p>
     * 별도 트랜잭션(REQUIRES_NEW)에서 실행하여 낙관적 동시성 제어를 제공합니다.
     * 조회 트랜잭션과 분리되어 조회 성능에 영향을 주지 않습니다.
     *
     * @param cmd 조회수 증가 명령
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void incrementViewCount(@Valid IncrementViewCountCommand cmd) {
        UUID boardId = cmd.boardId();
        Board board = boardRepository
                .findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.board.not-found.id", boardId)));

        board.increaseViewCount();

        // 커밋 시 flush가 진행되어 낙관적 락 충돌이 감지됨
        boardRepository.save(board);
    }

    /**
     * 게시글 로드 또는 예외 발생
     *
     * @param id 게시글 ID
     * @return 게시글 엔티티
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    private Board loadBoardOrThrow(UUID id) {
        return boardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.board.not-found.id", id)));
    }

}
