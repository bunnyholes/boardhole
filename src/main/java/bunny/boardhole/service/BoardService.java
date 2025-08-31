package bunny.boardhole.service;

import bunny.boardhole.domain.Board;
import bunny.boardhole.domain.User;
import bunny.boardhole.dto.board.BoardCreateRequest;
import bunny.boardhole.dto.board.BoardDto;
import bunny.boardhole.dto.board.BoardUpdateRequest;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.repository.BoardRepository;
import bunny.boardhole.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardViewService boardViewService;

    @Transactional
    public BoardDto create(@NotNull @Positive Long authorId, @Valid BoardCreateRequest req) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authorId));

        Board board = Board.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .author(author)
                .build();
        Board saved = boardRepository.save(board);
        return BoardDto.from(saved);
    }

    // 조회 전용: 증가 없이 단순 조회 (권한 확인 등 내부 용도)
    @Transactional(readOnly = true)
    public BoardDto get(@NotNull @Positive Long id) {
        Board board = loadBoardOrThrow(id);
        return BoardDto.from(board);
    }

    // 화면 조회용: 조회수 증가는 비동기로 처리
    @Transactional(readOnly = true)
    public BoardDto getForView(@NotNull @Positive Long id) {
        // 먼저 데이터를 조회하여 반환
        Board board = loadBoardOrThrow(id);
        
        // 조회수 증가는 비동기로 처리 (실패해도 데이터는 정상 반환)
        boardViewService.incrementViewCountAsync(id);
        
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
    public Page<BoardDto> listWithPaging(@NotNull Pageable pageable) {
        return boardRepository.findAll(pageable).map(BoardDto::from);
    }

    @Transactional(readOnly = true)
    public Page<BoardDto> listWithPaging(@NotNull Pageable pageable, @NotBlank String search) {
        var page = boardRepository.searchByKeyword(search, pageable);
        return page.map(BoardDto::from);
    }

    @Transactional
    public BoardDto update(@NotNull @Positive Long id, @NotNull @Positive Long authorId, @Valid BoardUpdateRequest req) {
        Board existing = loadBoardOrThrow(id);
        if (req.getTitle() != null) existing.setTitle(req.getTitle());
        if (req.getContent() != null) existing.setContent(req.getContent());
        Board saved = boardRepository.save(existing);
        return BoardDto.from(saved);
    }

    @Transactional
    public void delete(@NotNull @Positive Long id) {
        if (!boardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Board not found with id: " + id);
        }
        boardRepository.deleteById(id);
    }

    // 내부 유틸
    private Board loadBoardOrThrow(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));
    }
}
