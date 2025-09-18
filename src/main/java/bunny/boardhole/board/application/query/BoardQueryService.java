package bunny.boardhole.board.application.query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bunny.boardhole.board.application.mapper.BoardMapper;
import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.domain.Board;
import bunny.boardhole.board.infrastructure.BoardRepository;
import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;

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
     * <p>
     * 주의: 본 메서드는 조회 성공 시 조회수 증가 이벤트(ViewedEvent)를 발행합니다.
     * CQRS 순수 조회 원칙과 달리, 실무 성능/경험(조회 시점에 viewCount 증가) 목적의 의도적 부수효과입니다.
     *
     * @param query 게시글 조회 쿼리
     * @return 게시글 조회 결과
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public BoardResult handle(GetBoardQuery query) {
        Board board = boardRepository
                .findById(query.id())
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

    // WebController 호환 메서드들 (기존 API 유지)

    /**
     * 게시글 목록 조회 (검색 포함)
     */
    @Transactional(readOnly = true)
    public Page<BoardResult> getBoards(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty())
            return listWithPaging(pageable, search.trim());
        return listWithPaging(pageable);
    }

    /**
     * 게시글 단일 조회
     */
    @Transactional(readOnly = true)
    public BoardResult getBoard(UUID id) {
        return handle(new GetBoardQuery(id));
    }

    // 대시보드용 메서드들

    /**
     * 최근 게시글 목록 조회
     *
     * @param limit 조회할 게시글 수
     * @return 최근 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<BoardResult> getRecentBoards(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return boardRepository.findAll(pageable).map(boardMapper::toResult).getContent();
    }

    /**
     * 전체 게시글 수 조회
     *
     * @return 전체 게시글 수
     */
    @Transactional(readOnly = true)
    public Long getTotalBoardCount() {
        return boardRepository.count();
    }

    /**
     * 오늘 작성된 게시글 수 조회
     *
     * @return 오늘 작성된 게시글 수
     */
    @Transactional(readOnly = true)
    public Long getTodayBoardCount() {
        LocalDate today = LocalDate.now();
        return boardRepository.countByCreatedAtBetween(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );
    }

    /**
     * 특정 사용자의 게시글 수 조회
     *
     * @param authorId 작성자 ID
     * @return 사용자의 게시글 수
     */
    @Transactional(readOnly = true)
    public Long getMyBoardCount(UUID authorId) {
        return boardRepository.countByAuthorId(authorId);
    }
}
