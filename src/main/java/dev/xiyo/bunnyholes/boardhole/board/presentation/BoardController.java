package dev.xiyo.bunnyholes.boardhole.board.presentation;

import java.util.UUID;

import jakarta.annotation.security.PermitAll;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.xiyo.bunnyholes.boardhole.board.application.command.BoardCommandService;
import dev.xiyo.bunnyholes.boardhole.board.application.query.BoardQueryService;
import dev.xiyo.bunnyholes.boardhole.board.application.result.BoardResult;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardCreateRequest;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardResponse;
import dev.xiyo.bunnyholes.boardhole.board.presentation.dto.BoardUpdateRequest;
import dev.xiyo.bunnyholes.boardhole.board.presentation.mapper.BoardWebMapper;
import dev.xiyo.bunnyholes.boardhole.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping(ApiPaths.BOARDS)
@Validated
@RequiredArgsConstructor
@Tag(name = "게시판 API", description = "게시판 CRUD 작업을 위한 REST API")
public class BoardController {
    private final BoardCommandService boardCommandService;
    private final BoardQueryService boardQueryService;
    private final BoardWebMapper boardWebMapper;

    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "게시글 작성", description = "[AUTH] 새로운 게시글을 작성합니다. 인증된 사용자만 사용할 수 있습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = BoardCreateRequest.class))))
    @ApiResponse(responseCode = "201", description = "게시글이 성공적으로 작성됨", content = @Content(schema = @Schema(implementation = BoardResponse.class)))
    @ApiResponse(responseCode = "422", description = "유효성 검증 실패")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse create(@Validated @ModelAttribute BoardCreateRequest req, @AuthenticationPrincipal UserDetails principal) {
        var cmd = boardWebMapper.toCreateCommand(req, principal.getUsername());
        var result = boardCommandService.create(cmd);
        return boardWebMapper.toResponse(result);
    }

    @GetMapping
    @PermitAll
    @Operation(summary = "게시글 목록 조회", description = "[PUBLIC] 게시글 목록을 페이지네이션으로 조회합니다. 제목과 내용에서 검색이 가능합니다.")
    @Parameter(name = "page", description = "0부터 시작하는 페이지 인덱스", example = "0")
    @Parameter(name = "size", description = "페이지 크기", example = "10")
    @Parameter(name = "sort", description = "정렬 (필드,방향)", example = "id,desc")
    @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공", content = @Content(schema = @Schema(implementation = Page.class)))
    public Page<BoardResponse> list(@Parameter(description = "페이지네이션 정보 (기본: 페이지 크기 10, ID 내림차순 정렬)") @PageableDefault(sort = "id", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable, @Parameter(description = "검색어 (제목 또는 내용에서 검색)", example = "공지") @RequestParam(required = false) @Nullable String search) {
        Page<BoardResult> page = search == null ? boardQueryService.listWithPaging(pageable) : boardQueryService.listWithPaging(pageable, search);
        return page.map(boardWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PermitAll
    @Operation(summary = "게시글 상세 조회", description = "[PUBLIC] 특정 게시글의 상세 정보를 조회합니다. 조회수가 자동으로 증가됩니다.")
    @ApiResponse(responseCode = "200", description = "게시글 조회 성공", content = @Content(schema = @Schema(implementation = BoardResponse.class)))
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    public BoardResponse get(@Parameter(description = "조회할 게시글 ID") @PathVariable UUID id) {
        BoardResult result = boardQueryService.handle(boardWebMapper.toGetBoardQuery(id));
        return boardWebMapper.toResponse(result);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "게시글 수정", description = "[OWNER] 기존 게시글을 수정합니다. 작성자 본인만 수정 가능합니다.",

            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = BoardUpdateRequest.class))))
    @ApiResponse(responseCode = "200", description = "게시글 수정 성공", content = @Content(schema = @Schema(implementation = BoardResponse.class)))
    @ApiResponse(responseCode = "422", description = "유효성 검증 실패")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "403", description = "수정 권한 없음 (작성자가 아님)")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    public BoardResponse update(@Parameter(description = "수정할 게시글 ID") @PathVariable UUID id, @Validated @ModelAttribute BoardUpdateRequest req) {
        // 권한 검증은 서비스 @PreAuthorize가 처리, owner 확인은 PermissionEvaluator가 수행
        var cmd = boardWebMapper.toUpdateCommand(id, req);
        var updated = boardCommandService.update(cmd);
        return boardWebMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "게시글 삭제", description = "[OWNER] 기존 게시글을 삭제합니다. 작성자 본인만 삭제 가능합니다."

    )
    @ApiResponse(responseCode = "204", description = "게시글 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자가 아님)")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    public void delete(@Parameter(description = "삭제할 게시글 ID") @PathVariable UUID id) {
        // 권한 검증은 서비스 @PreAuthorize가 처리
        boardCommandService.delete(id);
    }

}
