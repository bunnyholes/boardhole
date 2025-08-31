package bunny.boardhole.controller;

import bunny.boardhole.domain.Role;
import bunny.boardhole.domain.User;
import bunny.boardhole.dto.board.BoardCreateRequest;
import bunny.boardhole.dto.board.BoardDto;
import bunny.boardhole.dto.board.BoardResponse;
import bunny.boardhole.dto.board.BoardUpdateRequest;
import bunny.boardhole.exception.UnauthorizedException;
import bunny.boardhole.security.AppUserPrincipal;
import bunny.boardhole.service.BoardService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/boards")
@Validated
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse create(@Validated @ModelAttribute BoardCreateRequest req, @AuthenticationPrincipal AppUserPrincipal principal) {
        // 요청의 userId를 무시하고 인증된 사용자로 고정
        User current = principal.getUser();
        BoardDto boardDto = boardService.create(current.getId(), req);
        return BoardResponse.from(boardDto);
    }

    @GetMapping
    @PermitAll
    public Page<BoardResponse> list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<BoardDto> page = search == null 
                ? boardService.listWithPaging(pageable)
                : boardService.listWithPaging(pageable, search);
        return page.map(BoardResponse::from);
    }

    @GetMapping("/{id}")
    @PermitAll
    public BoardResponse get(@PathVariable Long id) {
        // 조회 시마다 조회수 +1 (낙관적 락)
        BoardDto boardDto = boardService.getForView(id);
        return BoardResponse.from(boardDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public BoardResponse update(@PathVariable Long id, @ModelAttribute BoardUpdateRequest req, @AuthenticationPrincipal AppUserPrincipal principal) {
        User current = principal.getUser();
        BoardDto existing = boardService.get(id);
        boolean isAuthor = existing.getAuthor() != null && existing.getAuthor().getId() != null
                && existing.getAuthor().getId().equals(current.getId());
        boolean isAdmin = current.getRoles() != null && current.getRoles().contains(Role.ADMIN);
        if (!(isAuthor || isAdmin)) {
            throw new UnauthorizedException("not allowed");
        }
        Long authorId = isAuthor ? current.getId() : existing.getAuthor().getId();
        BoardDto updated = boardService.update(id, authorId, req);
        return BoardResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        User current = principal.getUser();
        BoardDto existing = boardService.get(id);
        boolean isAuthor = existing.getAuthor() != null && existing.getAuthor().getId() != null
                && existing.getAuthor().getId().equals(current.getId());
        boolean isAdmin = current.getRoles() != null && current.getRoles().contains(Role.ADMIN);
        if (!(isAuthor || isAdmin)) {
            throw new UnauthorizedException("not allowed");
        }
        boardService.delete(id);
    }

}
