package bunny.boardhole.service;

import bunny.boardhole.domain.Board;
import bunny.boardhole.dto.board.BoardCreateRequest;
import bunny.boardhole.dto.board.BoardUpdateRequest;
import bunny.boardhole.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BoardService {
    private final BoardMapper boardMapper;

    public Board create(BoardCreateRequest req) {
        Board board = Board.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .authorId(req.getAuthorId())
                .build();
        boardMapper.insert(board);
        return boardMapper.findById(board.getId());
    }

    public Board get(Long id) {
        return boardMapper.findById(id);
    }

    public List<Board> list() {
        return boardMapper.findAll();
    }

    public Board update(Long id, BoardUpdateRequest req) {
        Board existing = boardMapper.findById(id);
        if (existing == null) return null;
        if (req.getTitle() != null) existing.setTitle(req.getTitle());
        if (req.getContent() != null) existing.setContent(req.getContent());
        existing.setUpdatedAt(LocalDateTime.now());
        boardMapper.update(existing);
        return boardMapper.findById(id);
    }

    public void delete(Long id) {
        boardMapper.deleteById(id);
    }
}

