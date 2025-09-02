package bunny.boardhole.board.presentation;

import bunny.boardhole.board.application.command.*;
import bunny.boardhole.board.application.event.ViewedEvent;
import bunny.boardhole.board.application.query.*;
import bunny.boardhole.board.application.result.BoardResult;
import bunny.boardhole.board.presentation.dto.*;
import bunny.boardhole.board.presentation.mapper.BoardWebMapper;
import bunny.boardhole.shared.constants.ApiPaths;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.user.domain.User;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 게시판 API 컨트롤러
 * 게시글의 생성, 조회, 수정, 삭제 및 목록 조회 기능을 제공합니다.
 * 조회수 증가는 비동기 이벤트로 처리됩니다.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.BOARDS)
@Validated
@RequiredArgsConstructor
@Tag(name = "게시판 API", description = "게시판 CRUD 작업을 위한 REST API")
public class BoardController {
    
    /** HTTP 응답 코드 상수 */
    private static final String BAD_REQUEST = "400";
    private static final String UNAUTHORIZED = "401";
    private static final String NOT_FOUND = "404";
    
    /** HTTP 응답 메시지 상수 */
    private static final String BAD_REQUEST_MESSAGE = "잘못된 요청 데이터";
    private static final String UNAUTHORIZED_MESSAGE = "인증되지 않은 사용자";
    private static final String BOARD_NOT_FOUND_MESSAGE = "게시글을 찾을 수 없음";
    
    /** 게시글 명령 서비스 */
    private final BoardCommandService boardCommandService;
    
    /** 게시글 조회 서비스 */
    private final BoardQueryService boardQueryService;
    
    /** 애플리케이션 이벤트 퍼블리셔 */
    private final ApplicationEventPublisher eventPublisher;
    
    /** 게시글 웹 매퍼 */
    private final BoardWebMapper boardWebMapper;

    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시글 작성",
            description = "[AUTH] 새로운 게시글을 작성합니다. 인증된 사용자만 사용할 수 있습니다.",
            security = @SecurityRequirement(name = "session"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = BoardCreateRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글이 성공적으로 작성됨",
                    content = @Content(schema = @Schema(implementation = BoardResponse.class))
            ),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_MESSAGE),
            @ApiResponse(responseCode = UNAUTHORIZED, description = UNAUTHORIZED_MESSAGE)
    })
    /**
     * 새로운 게시글을 작성합니다.
     * 인증된 사용자만 사용할 수 있으며, 요청의 userId는 무시되고 현재 인증된 사용자로 설정됩니다.
     *
     * @param request 게시글 작성 요청 데이터
     * @param principal 현재 인증된 사용자 정보
     * @return 작성된 게시글 응답 데이터
     */
    public ResponseEntity<BoardResponse> create(@Validated @ModelAttribute final BoardCreateRequest request, @AuthenticationPrincipal final AppUserPrincipal principal) {
        // 요청의 userId를 무시하고 인증된 사용자로 고정
        final User currentUser = principal.user();
        final CreateBoardCommand createCommand = boardWebMapper.toCreateCommand(request, currentUser.getId());
        final BoardResult boardResult = boardCommandService.create(createCommand);
        final BoardResponse responseBody = boardWebMapper.toResponse(boardResult);
        return ResponseEntity.created(java.net.URI.create(ApiPaths.BOARDS + "/" + boardResult.id())).body(responseBody);
    }

    @GetMapping
    @PermitAll
    @Operation(
            summary = "게시글 목록 조회",
            description = "[PUBLIC] 페이지네이션을 지원하는 게시글 목록을 조회합니다. 검색어를 포함할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @Parameters({
            @Parameter(name = "page", description = "0부터 시작하는 페이지 인덱스", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "10"),
            @Parameter(name = "sort", description = "정렬 (필드,방향)", example = "id,desc")
    })
    /**
     * 게시글 목록을 페이지네이션으로 조회합니다.
     * 검색어를 포함하여 제목이나 내용에서 검색할 수 있습니다.
     *
     * @param pageable 페이지네이션 정보 (기본: 페이지 크기 10, ID 내림차순 정렬)
     * @param searchTerm 검색어 (제목 또는 내용에서 검색)
     * @return 게시글 목록 페이지
     */
    public Page<BoardResponse> list(
            @Parameter(description = "페이지네이션 정보 (기본: 페이지 크기 10, ID 내림차순 정렬)")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) @ParameterObject final Pageable pageable,
            @Parameter(description = "검색어 (제목 또는 내용에서 검색)", example = "공지")
            @RequestParam(required = false) final String searchTerm) {
        final Page<BoardResult> resultPage = searchTerm == null
                ? boardQueryService.listWithPaging(pageable)
                : boardQueryService.listWithPaging(pageable, searchTerm);
        return resultPage.map(boardWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PermitAll
    @Operation(
            summary = "게시글 상세 조회",
            description = "[PUBLIC] 특정 게시글의 상세 정보를 조회합니다. 조회수가 자동으로 증가됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 조회 성공",
                    content = @Content(schema = @Schema(implementation = BoardResponse.class))
            ),
            @ApiResponse(responseCode = NOT_FOUND, description = BOARD_NOT_FOUND_MESSAGE)
    })
    /**
     * 특정 게시글의 상세 정보를 조회합니다.
     * 조회수는 비동기 이벤트로 자동 증가됩니다.
     *
     * @param boardId 조회할 게시글 ID
     * @param principal 현재 인증된 사용자 정보 (선택적)
     * @return 게시글 상세 정보
     */
    public BoardResponse get(
            @Parameter(description = "조회할 게시글 ID")
            @PathVariable final Long boardId,
            @AuthenticationPrincipal final AppUserPrincipal principal) {
        // 1) 순수 조회 (Query)
        final BoardResult boardResult = boardQueryService.handle(GetBoardQuery.of(boardId));

        // 2) 뷰 증가 이벤트 발행 (Command는 비동기 처리)
        final Long viewerId = principal != null && principal.user() != null ? principal.user().getId() : null;
        eventPublisher.publishEvent(ViewedEvent.of(boardId, viewerId));

        // 3) 결과를 Response로 매핑
        return boardWebMapper.toResponse(boardResult);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시글 수정",
            description = "[OWNER] 기존 게시글을 수정합니다. 작성자 본인만 수정 가능합니다.",
            security = @SecurityRequirement(name = "session"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = BoardUpdateRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 수정 성공",
                    content = @Content(schema = @Schema(implementation = BoardResponse.class))
            ),
            @ApiResponse(responseCode = BAD_REQUEST, description = BAD_REQUEST_MESSAGE),
            @ApiResponse(responseCode = UNAUTHORIZED, description = UNAUTHORIZED_MESSAGE),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = NOT_FOUND, description = BOARD_NOT_FOUND_MESSAGE)
    })
    /**
     * 기존 게시글을 수정합니다.
     * 작성자 본인만 수정 가능하며, 권한 검증은 서비스 레이어에서 처리됩니다.
     *
     * @param boardId 수정할 게시글 ID
     * @param request 게시글 수정 요청 데이터
     * @param principal 현재 인증된 사용자 정보
     * @return 수정된 게시글 응답 데이터
     */
    public BoardResponse update(
            @Parameter(description = "수정할 게시글 ID")
            @PathVariable final Long boardId,
            @ModelAttribute final BoardUpdateRequest request,
            @AuthenticationPrincipal final AppUserPrincipal principal) {
        // 권한 검증은 서비스 @PreAuthorize가 처리
        final UpdateBoardCommand updateCommand = boardWebMapper.toUpdateCommand(boardId, principal.user().getId(), request);
        final BoardResult updatedBoard = boardCommandService.update(updateCommand);
        return boardWebMapper.toResponse(updatedBoard);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "게시글 삭제",
            description = "[OWNER] 기존 게시글을 삭제합니다. 작성자 본인만 삭제 가능합니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = UNAUTHORIZED, description = UNAUTHORIZED_MESSAGE),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = NOT_FOUND, description = BOARD_NOT_FOUND_MESSAGE)
    })
    /**
     * 기존 게시글을 삭제합니다.
     * 작성자 본인만 삭제 가능하며, 권한 검증은 서비스 레이어에서 처리됩니다.
     *
     * @param boardId 삭제할 게시글 ID
     */
    public void delete(
            @Parameter(description = "삭제할 게시글 ID")
            @PathVariable final Long boardId) {
        // 권한 검증은 서비스 @PreAuthorize가 처리
        boardCommandService.delete(boardId);
    }

}
