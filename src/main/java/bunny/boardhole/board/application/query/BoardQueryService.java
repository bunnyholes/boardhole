package bunny.boardhole.board.application.query;

import bunny.boardhole.board.application.mapper.BoardMapper;
import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 조회 서비스
 * CQRS 패턴의 Query 측면으로 게시글 조회 전용 비지니스 로직을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardQueryService {

    /** 게시글 레포지토리 */
    private final BoardRepository boardRepository;
    
    /** 게시글 매퍼 */
    private final BoardMapper boardMapper;
    
    /** 메시지 유틸리티 */
    private final MessageUtils messageUtils;

    /**
     * 게시글 단일 조회 쿼리 처리
     *
     * @param query 게시글 조회 쿼리
     * @return 게시글 조회 결과
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public BoardResult handle(final GetBoardQuery query) {
        final Board board = boardRepository.findById(query.id())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.board.not-found.id", query.id())));
        
        if (log.isDebugEnabled()) {
            log.debug(messageUtils.getMessage("log.board.fetched", query.id()));
        }
        
        return boardMapper.toResult(board);
    }

    /**
     * 게시글 목록 페이지네이션 조회
     *
     * @param pageable 페이지네이션 정보
     * @return 게시글 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<BoardResult> listWithPaging(final Pageable pageable) {
        final Page<BoardResult> results = boardRepository.findAll(pageable).map(boardMapper::toResult);
        
        if (log.isDebugEnabled()) {
            log.debug(messageUtils.getMessage("log.board.list.fetched", results.getTotalElements(), pageable.getPageNumber()));
        }
        
        return results;
    }

    /**
     * 검색어로 게시글 목록 페이지네이션 조회
     *
     * @param pageable 페이지네이션 정보
     * @param search   검색어 (제목, 내용에서 검색)
     * @return 검색된 게시글 목록 페이지
     */
    @Transactional(readOnly = true)
    public Page<BoardResult> listWithPaging(final Pageable pageable, final String search) {
        final Page<BoardResult> results = boardRepository.searchByKeyword(search, pageable).map(boardMapper::toResult);
        
        if (log.isDebugEnabled()) {
            final String sanitizedSearch = sanitizeForLog(search);
            log.debug(messageUtils.getMessage("log.board.search.fetched", results.getTotalElements(), sanitizedSearch));
        }
        
        return results;
    }

    /**
     * 로그 출력용 문자열 새니타이징
     * CRLF 인젝션 공격을 방지하기 위해 개행 문자를 제거합니다.
     *
     * @param input 새니타이징할 입력 문자열
     * @return 새니타이징된 문자열
     */
    private String sanitizeForLog(final String input) {
        return (input == null) ? "null" : input.replaceAll("[\r\n]", "_");
    }
}
