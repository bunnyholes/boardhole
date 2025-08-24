package bunny.boardhole.controller;

import bunny.boardhole.domain.Board;
import bunny.boardhole.dto.board.BoardCreateRequest;
import bunny.boardhole.dto.board.BoardResponse;
import bunny.boardhole.dto.board.BoardUpdateRequest;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse create(@Valid @RequestBody BoardCreateRequest req) {
        Board board = boardService.create(req);
        return BoardResponse.from(board);
    }

    @GetMapping
    public List<BoardResponse> list() {
        return boardService.list().stream()
                .map(BoardResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public BoardResponse get(@PathVariable Long id) {
        Board board = boardService.get(id);
        if (board == null) {
            throw new ResourceNotFoundException("Board not found with id: " + id);
        }
        return BoardResponse.from(board);
    }

    @PutMapping("/{id}")
    public BoardResponse update(@PathVariable Long id, @Valid @RequestBody BoardUpdateRequest req) {
        Board updated = boardService.update(id, req);
        if (updated == null) {
            throw new ResourceNotFoundException("Board not found with id: " + id);
        }
        return BoardResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        boardService.delete(id);
    }
}

