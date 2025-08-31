package bunny.boardhole.service;

import bunny.boardhole.domain.Board;
import bunny.boardhole.domain.User;
import bunny.boardhole.dto.board.BoardRequest;
import bunny.boardhole.dto.board.BoardDto;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.repository.BoardRepository;
import bunny.boardhole.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public BoardDto create(BoardRequest req) {
        Long authorId = req.getUserId() != null ? req.getUserId() : 1L;
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authorId));

        Board board = Board.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .author(author)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Board saved = boardRepository.save(board);
        return BoardDto.from(saved);
    }

    @Transactional(readOnly = true)
    public BoardDto get(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));
        return BoardDto.from(board);
    }

    @Transactional(readOnly = true)
    public List<BoardDto> list() {
        return boardRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(BoardDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BoardDto> listWithPaging(Pageable pageable, String search) {
        Page<Board> page;
        if (search == null || search.isBlank()) {
            page = boardRepository.findAll(pageable);
        } else {
            page = boardRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(search, search, pageable);
        }
        return page.map(BoardDto::from);
    }

    @Transactional
    public BoardDto update(Long id, BoardRequest req) {
        Board existing = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));
        if (req.getTitle() != null) existing.setTitle(req.getTitle());
        if (req.getContent() != null) existing.setContent(req.getContent());
        existing.setUpdatedAt(LocalDateTime.now());
        Board saved = boardRepository.save(existing);
        return BoardDto.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        // 존재 여부 확인
        if (!boardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Board not found with id: " + id);
        }
        boardRepository.deleteById(id);
    }
}
