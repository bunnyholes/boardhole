package bunny.boardhole.board.application.query;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bunny.boardhole.board.application.mapper.BoardMapper;
import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 게시글 조회 서비스
 * CQRS 패턴의 Query 측면으로 게시글 조회 전용 비지니스 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardQueryService {

    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * 게시글 단일 조회 쿼리 처리
     *
     * @param query 게시글 조회 쿼리
     * @return 게시글 조회 결과
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public BoardResult handle(GetBoardQuery query) {
        Board board = boardRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.board.not-found.id", query.id())));
        eventPublisher.publishEvent(boardMapper.toViewedEvent(query.id()));
        return boardMapper.toResult(board);
    }

    /**
     * 게시글 목록 페이지네이션 조회
     *
     * @param pageable 페이지네이션 정보
     * @return 게시글 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<BoardResult> listWithPaging(Pageable pageable) {
        return boardRepository.findAll(pageable).map(boardMapper::toResult);
    }

    /**
     * 검색어로 게시글 목록 페이지네이션 조회
     *
     * @param pageable 페이지네이션 정보
     * @param search   검색어 (제목, 내용에서 검색)
     * @return 검색된 게시글 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<BoardResult> listWithPaging(Pageable pageable, String search) {
        return boardRepository.searchByKeyword(search, pageable).map(boardMapper::toResult);
    }
}
