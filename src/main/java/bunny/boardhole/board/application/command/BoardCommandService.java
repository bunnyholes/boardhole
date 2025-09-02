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
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * 게시글 명령 서비스
 * CQRS 패턴의 Command 측면으로 게시글 생성, 수정, 삭제 등 데이터 변경 작업을 담당합니다.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BoardCommandService {

    // 로그 출력용 상수
    private static final String NULL_STRING = "null";
    private static final String NEWLINE_REPLACEMENT = "_";
    private static final String NEWLINE_PATTERN = "[\r\n]";
    /** 게시글 레포지토리 */
    private final BoardRepository boardRepository;
    
    /** 사용자 레포지토리 */
    private final UserRepository userRepository;
    
    /** 게시글 매퍼 */
    private final BoardMapper boardMapper;
    
    /** 메시지 유틸리티 */
    private final MessageUtils messageUtils;

    /**
     * 게시글 생성
     *
     * @param cmd 게시글 생성 명령
     * @return 생성된 게시글 결과
     * @throws ResourceNotFoundException 작성자를 찾을 수 없는 경우
     */
    @Transactional
    public BoardResult create(@Valid final CreateBoardCommand cmd) {
        final Long authorIdentifier = cmd.authorId();
        final User author = userRepository.findById(authorIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", authorIdentifier)));

        final Board board = Board.builder()
                .title(cmd.title())
                .content(cmd.content())
                .author(author)
                .build();
        final Board saved = boardRepository.save(board);

        if (log.isInfoEnabled()) {
            final String sanitizedTitle = sanitizeForLog(saved.getTitle());
            final String sanitizedUsername = sanitizeForLog(author.getUsername());
            log.info(messageUtils.getMessage("log.board.created", saved.getId(), sanitizedTitle, sanitizedUsername));
        }
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
    public BoardResult update(@Valid @NonNull final UpdateBoardCommand cmd) {
        final Long boardIdentifier = cmd.boardId();
        final Board board = boardRepository.findById(boardIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.board.not-found.id", boardIdentifier)));

        // Optional을 사용한 선택적 필드 업데이트
        Optional.ofNullable(cmd.title()).ifPresent(board::changeTitle);
        Optional.ofNullable(cmd.content()).ifPresent(board::changeContent);

        // @DynamicUpdate가 변경된 필드만 업데이트, @PreUpdate가 updatedAt 자동 설정
        final Board saved = boardRepository.save(board);

        if (log.isInfoEnabled()) {
            final String sanitizedTitle = sanitizeForLog(saved.getTitle());
            final String sanitizedUsername = sanitizeForLog(saved.getAuthor().getUsername());
            log.info(messageUtils.getMessage("log.board.updated", saved.getId(), sanitizedTitle, sanitizedUsername));
        }
        return boardMapper.toResult(saved);
    }

    /**
     * 게시글 삭제
     *
     * @param id 삭제할 게시글 ID
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    @PreAuthorize("hasPermission(#boardIdentifier, 'BOARD', 'DELETE')")
    public void delete(@NotNull @Positive final Long boardIdentifier) {
        final Board board = loadBoardOrThrow(boardIdentifier);
        final String authorUsername = board.getAuthor().getUsername();

        boardRepository.deleteById(boardIdentifier);
        if (log.isInfoEnabled()) {
            final String sanitizedUsername = sanitizeForLog(authorUsername);
            log.info(messageUtils.getMessage("log.board.deleted", boardIdentifier, sanitizedUsername));
        }
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
    public void incrementViewCount(@Valid final IncrementViewCountCommand cmd) {
        final Long boardIdentifier = cmd.boardId();
        final Board board = boardRepository.findById(boardIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.board.not-found.id", boardIdentifier)));

        board.increaseViewCount();

        // flush를 사용하여 낙관적 락 충돌을 즉시 감지
        final Board saved = boardRepository.saveAndFlush(board);

        if (log.isInfoEnabled()) {
            log.info(messageUtils.getMessage("log.board.view-count-increased", boardIdentifier, saved.getViewCount()));
        }
    }

    /**
     * 게시글 로드 또는 예외 발생
     *
     * @param id 게시글 ID
     * @return 게시글 엔티티
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @NonNull
    private Board loadBoardOrThrow(@NonNull final Long boardIdentifier) {
        return boardRepository.findById(boardIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.board.not-found.id", boardIdentifier)));
    }
    
    /**
     * 로그 출력용 문자열 새니타이징
     * CRLF 인젝션 공격을 방지하기 위해 개행 문자를 제거합니다.
     *
     * @param input 새니타이징할 입력 문자열
     * @return 새니타이징된 문자열
     */
    private String sanitizeForLog(final String input) {
        return (input == null) ? NULL_STRING : input.replaceAll(NEWLINE_PATTERN, NEWLINE_REPLACEMENT);
    }

}
