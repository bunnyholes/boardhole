package bunny.boardhole.controller;

import bunny.boardhole.domain.Board;
import bunny.boardhole.domain.User;
import bunny.boardhole.dto.board.BoardRequest;
import bunny.boardhole.dto.board.BoardResponse;
import bunny.boardhole.dto.common.PageRequest;
import bunny.boardhole.dto.common.PageResponse;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.exception.UnauthorizedException;
import bunny.boardhole.service.BoardService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    public BoardResponse create(@Validated(BoardRequest.Create.class) @RequestBody BoardRequest req, HttpSession session) {
        User currentUser = getCurrentUser(session);
        Board board = boardService.create(req, currentUser.getId());
        return BoardResponse.from(board);
    }

    @GetMapping
    public List<BoardResponse> list() {
        return boardService.list().stream()
                .map(BoardResponse::from)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/search")
    public PageResponse<BoardResponse> searchWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPage(page);
        pageRequest.setSize(size);
        pageRequest.setSearch(search);
        pageRequest.setSortBy(sortBy);
        pageRequest.setSortDirection(sortDirection);
        
        PageResponse<Board> pageResponse = boardService.listWithPaging(pageRequest);
        List<BoardResponse> content = pageResponse.getContent().stream()
                .map(BoardResponse::from)
                .collect(Collectors.toList());
                
        return PageResponse.of(content, pageResponse.getPage(), pageResponse.getSize(), pageResponse.getTotalElements());
    }

    @GetMapping("/{id}")
    public BoardResponse get(@PathVariable Long id) {
        Board board = boardService.get(id);
        return BoardResponse.from(board);
    }

    @PutMapping("/{id}")
    public BoardResponse update(@PathVariable Long id, @Validated(BoardRequest.Update.class) @RequestBody BoardRequest req, HttpSession session) {
        User currentUser = getCurrentUser(session);
        Board updated = boardService.update(id, req, currentUser.getId());
        return BoardResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        boardService.delete(id, currentUser.getId());
    }

    private User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new UnauthorizedException("not logged in");
        }
        return user;
    }
}

