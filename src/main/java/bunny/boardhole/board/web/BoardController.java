package bunny.boardhole.board.web;

import bunny.boardhole.board.application.command.BoardCommandService;
import bunny.boardhole.board.application.command.CreateBoardCommand;
import bunny.boardhole.board.application.command.UpdateBoardCommand;
import bunny.boardhole.board.application.event.ViewedEvent;
import bunny.boardhole.board.application.query.BoardQueryService;
import bunny.boardhole.board.application.query.GetBoardQuery;
import bunny.boardhole.board.application.dto.BoardResult;
import bunny.boardhole.board.web.dto.BoardCreateRequest;
import bunny.boardhole.board.web.dto.BoardResponse;
import bunny.boardhole.board.web.dto.BoardUpdateRequest;
import bunny.boardhole.board.web.mapper.BoardWebMapper;
import bunny.boardhole.common.security.AppUserPrincipal;
import bunny.boardhole.user.domain.User;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping("/api/boards")
@Validated
@RequiredArgsConstructor
@Tag(name = "게시판 API", description = "게시판 CRUD 작업을 위한 REST API")
public class BoardController {
    private final BoardCommandService boardCommandService;
    private final BoardQueryService boardQueryService;
    private final ApplicationEventPublisher eventPublisher;
    private final BoardWebMapper boardWebMapper;

    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시글 작성",
            description = "새로운 게시글을 작성합니다. 인증된 사용자만 사용할 수 있습니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글이 성공적으로 작성됨",
                    content = @Content(schema = @Schema(implementation = BoardResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<BoardResponse> create(@Validated @ModelAttribute BoardCreateRequest req, @AuthenticationPrincipal AppUserPrincipal principal) {
        // 요청의 userId를 무시하고 인증된 사용자로 고정
        User current = principal.user();
        var cmd = boardWebMapper.toCreateCommand(req, current.getId());
        var result = boardCommandService.create(cmd);
        var body = boardWebMapper.toResponse(result);
        return ResponseEntity.created(java.net.URI.create("/api/boards/" + result.id())).body(body);
    }

    @GetMapping
    @PermitAll
    @Operation(
            summary = "게시글 목록 조회",
            description = "페이지네이션을 지원하는 게시글 목록을 조회합니다. 검색어를 포함할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    public Page<BoardResponse> list(
            @Parameter(description = "페이지네이션 정보 (기본: 페이지 크기 10, ID 내림차순 정렬)")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "검색어 (제목 또는 내용에서 검색)")
            @RequestParam(required = false) String search) {
        Page<BoardResult> page = search == null
                ? boardQueryService.listWithPaging(pageable)
                : boardQueryService.listWithPaging(pageable, search);
        return page.map(boardWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PermitAll
    @Operation(
            summary = "게시글 상세 조회",
            description = "특정 게시글의 상세 정보를 조회합니다. 조회수가 자동으로 증가됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 조회 성공",
                    content = @Content(schema = @Schema(implementation = BoardResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public BoardResponse get(
            @Parameter(description = "조회할 게시글 ID")
            @PathVariable Long id, 
            @AuthenticationPrincipal AppUserPrincipal principal) {
        // 1) 순수 조회 (Query)
        BoardResult result = boardQueryService.handle(new GetBoardQuery(id));

        // 2) 뷰 증가 이벤트 발행 (Command는 비동기 처리)
        Long viewerId = principal != null && principal.user() != null ? principal.user().getId() : null;
        eventPublisher.publishEvent(new ViewedEvent(id, viewerId));

        // 3) 결과를 Response로 매핑
        return boardWebMapper.toResponse(result);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시글 수정",
            description = "기존 게시글을 수정합니다. 작성자만 수정할 수 있습니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 수정 성공",
                    content = @Content(schema = @Schema(implementation = BoardResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public BoardResponse update(
            @Parameter(description = "수정할 게시글 ID")
            @PathVariable Long id, 
            @ModelAttribute BoardUpdateRequest req, 
            @AuthenticationPrincipal AppUserPrincipal principal) {
        // 권한 검증은 서비스 @PreAuthorize가 처리
        var cmd = boardWebMapper.toUpdateCommand(id, principal.user().getId(), req);
        var updated = boardCommandService.update(cmd);
        return boardWebMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시글 삭제",
            description = "기존 게시글을 삭제합니다. 작성자만 삭제할 수 있습니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public void delete(
            @Parameter(description = "삭제할 게시글 ID")
            @PathVariable Long id) {
        // 권한 검증은 서비스 @PreAuthorize가 처리
        boardCommandService.delete(id);
    }

}
