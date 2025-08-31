package bunny.boardhole.controller;

import bunny.boardhole.domain.User;
import bunny.boardhole.domain.Role;
import bunny.boardhole.dto.board.BoardDto;
import bunny.boardhole.dto.board.BoardRequest;
import bunny.boardhole.dto.board.BoardResponse;
import bunny.boardhole.exception.ResourceNotFoundException;
import bunny.boardhole.exception.UnauthorizedException;
import bunny.boardhole.service.BoardService;
import bunny.boardhole.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@Slf4j
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse create(@RequestBody BoardRequest req, @AuthenticationPrincipal AppUserPrincipal principal) {
        // 요청의 userId를 무시하고 인증된 사용자로 고정
        User current = principal.getUser();
        req.setUserId(current.getId());
        BoardDto boardDto = boardService.create(req);
        return BoardResponse.from(boardDto.toEntity());
    }

    @GetMapping
    public Page<BoardResponse> list(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search) {
        Page<BoardDto> page = boardService.listWithPaging(pageable, search);
        return page.map(dto -> BoardResponse.from(dto.toEntity()));
    }

    @GetMapping("/{id}")
    public BoardResponse get(@PathVariable Long id) {
        BoardDto boardDto = boardService.get(id);
        return BoardResponse.from(boardDto.toEntity());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public BoardResponse update(@PathVariable Long id, @RequestBody BoardRequest req, @AuthenticationPrincipal AppUserPrincipal principal) {
        User current = principal.getUser();
        BoardDto existing = boardService.get(id);
        boolean isAuthor = existing.getAuthor() != null && existing.getAuthor().getId() != null
                && existing.getAuthor().getId().equals(current.getId());
        boolean isAdmin = current.getRoles() != null && current.getRoles().contains(Role.ADMIN);
        if (!(isAuthor || isAdmin)) {
            throw new UnauthorizedException("not allowed");
        }
        req.setUserId(isAuthor ? current.getId() : existing.getAuthor().getId());
        BoardDto updated = boardService.update(id, req);
        return BoardResponse.from(updated.toEntity());
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

    // 로그인 검증은 LoginRequiredInterceptor에서 처리됨
}
