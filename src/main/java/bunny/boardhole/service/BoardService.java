package bunny.boardhole.service;

import bunny.boardhole.domain.Board;
import bunny.boardhole.dto.board.BoardRequest;
import bunny.boardhole.dto.common.PageRequest;
import bunny.boardhole.dto.common.PageResponse;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.exception.UnauthorizedException;
import bunny.boardhole.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardMapper boardMapper;

    @Transactional
    public Board create(BoardRequest req) {
        Board board = Board.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .authorId(req.getUserId() != null ? req.getUserId() : 1L) // userId가 없으면 기본값 1
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        boardMapper.insert(board);
        return boardMapper.findById(board.getId());
    }

    @Transactional(readOnly = true)
    public Board get(Long id) {
        Board board = boardMapper.findById(id);
        if (board == null) {
            throw new ResourceNotFoundException("Board not found with id: " + id);
        }
        return board;
    }

    @Transactional(readOnly = true)
    public List<Board> list() {
        return boardMapper.findAll();
    }
    
    @Transactional(readOnly = true)
    public PageResponse<Board> listWithPaging(PageRequest pageRequest) {
        List<Board> boards = boardMapper.findWithPaging(pageRequest);
        long totalElements = boardMapper.countWithSearch(pageRequest.getSearch());
        return PageResponse.of(boards, pageRequest.getPage(), pageRequest.getSize(), totalElements);
    }

    @Transactional
    public Board update(Long id, BoardRequest req) {
        Board existing = get(id); // Uses get() which throws if not found
        
        // 검증 없이 누구나 수정 가능
        if (req.getTitle() != null) existing.setTitle(req.getTitle());
        if (req.getContent() != null) existing.setContent(req.getContent());
        existing.setUpdatedAt(LocalDateTime.now());
        boardMapper.update(existing);
        return boardMapper.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        // 검증 없이 누구나 삭제 가능
        Board existing = get(id); // 존재 여부만 확인
        boardMapper.deleteById(id);
    }
}

