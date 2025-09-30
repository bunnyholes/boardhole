package dev.xiyo.bunnyholes.boardhole.board.application.command;

import java.util.UUID;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import dev.xiyo.bunnyholes.boardhole.board.application.mapper.BoardMapper;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.board.domain.Board;
import dev.xiyo.bunnyholes.boardhole.board.infrastructure.BoardRepository;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

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
        String authorUsername = cmd.authorUsername();
        User author = userRepository
                .findByUsername(authorUsername)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.user.not-found.username", authorUsername)));

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

        // MapStruct를 사용한 선택적 필드 업데이트 (null 값 무시)
        boardMapper.updateBoardFromCommand(cmd, board);

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
    public void delete(UUID id) {
        Board board = loadBoardOrThrow(id);
        boardRepository.delete(board);
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
