package bunny.boardhole.board.application.command;

import bunny.boardhole.board.application.mapper.BoardMapper;
import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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
    private final MessageUtils messageUtils;

    /**
     * 게시글 생성
     *
     * @param cmd 게시글 생성 명령
     * @return 생성된 게시글 결과
     * @throws ResourceNotFoundException 작성자를 찾을 수 없는 경우
     */
    @Transactional
    public BoardResult create(@Valid CreateBoardCommand cmd) {
        Long authorId = cmd.authorId();
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", authorId)));

        Board board = Board.builder()
                .title(cmd.title())
                .content(cmd.content())
                .author(author)
                .build();
        Board saved = boardRepository.save(board);

        log.info(messageUtils.getMessage("log.board.created", saved.getId(), saved.getTitle(), author.getUsername()));
        return boardMapper.toResult(saved);
    }

    // 조회 전용: 증가 없이 단순 조회 (권한 확인 등 내부 용도)

    /**
     * 게시글 수정
     *
     * @param cmd 게시글 수정 명령
     * @return 수정된 게시글 결과
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    @PreAuthorize("hasPermission(#cmd.boardId, 'BOARD', 'WRITE')")
    public BoardResult update(@Valid UpdateBoardCommand cmd) {
        Long id = cmd.boardId();
        Board existing = loadBoardOrThrow(id);
        if (cmd.title() != null) existing.changeTitle(cmd.title());
        if (cmd.content() != null) existing.changeContent(cmd.content());
        Board saved = boardRepository.save(existing);

        log.info(messageUtils.getMessage("log.board.updated", saved.getId(), saved.getTitle(), saved.getAuthor().getUsername()));
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
    public void delete(@NotNull @Positive Long id) {
        Board board = loadBoardOrThrow(id);
        String authorUsername = board.getAuthor().getUsername();

        boardRepository.deleteById(id);
        log.info(messageUtils.getMessage("log.board.deleted", id, authorUsername));
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
        Long boardId = cmd.boardId();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.board.not-found.id", boardId)));

        board.increaseViewCount();

        // flush를 사용하여 낙관적 락 충돌을 즉시 감지
        Board saved = boardRepository.saveAndFlush(board);

        log.info(messageUtils.getMessage("log.board.view-count-increased", boardId, saved.getViewCount()));
    }

    /**
     * 게시글 로드 또는 예외 발생
     *
     * @param id 게시글 ID
     * @return 게시글 엔티티
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    private Board loadBoardOrThrow(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.board.not-found.id", id)));
    }

}
