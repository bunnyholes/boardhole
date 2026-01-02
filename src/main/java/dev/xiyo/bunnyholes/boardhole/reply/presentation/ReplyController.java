package dev.xiyo.bunnyholes.boardhole.reply.presentation;

import java.util.UUID;

import jakarta.annotation.security.PermitAll;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.xiyo.bunnyholes.boardhole.reply.application.command.ReplyCommandService;
import dev.xiyo.bunnyholes.boardhole.reply.application.query.ReplyQueryService;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyResult;
import dev.xiyo.bunnyholes.boardhole.reply.application.result.ReplyTreeResult;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.dto.CreateReplyRequest;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.dto.ReplyResponse;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.dto.ReplyTreeResponse;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.dto.UpdateReplyRequest;
import dev.xiyo.bunnyholes.boardhole.reply.presentation.mapper.ReplyWebMapper;
import dev.xiyo.bunnyholes.boardhole.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "댓글 API", description = "댓글 CRUD 작업을 위한 REST API")
public class ReplyController {

    private final ReplyCommandService replyCommandService;
    private final ReplyQueryService replyQueryService;
    private final ReplyWebMapper replyWebMapper;

    @GetMapping(ApiPaths.BOARD_REPLIES)
    @PermitAll
    @Operation(summary = "댓글 트리 조회", description = "[PUBLIC] 게시글의 전체 댓글을 계층형 트리 구조로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공",
        content = @Content(schema = @Schema(implementation = ReplyTreeResponse.class)))
    public ReplyTreeResponse getReplyTree(
        @Parameter(description = "게시글 ID") @PathVariable UUID boardId
    ) {
        ReplyTreeResult result = replyQueryService.getReplyTree(boardId);
        return replyWebMapper.toResponse(result);
    }

    @PostMapping(
        value = ApiPaths.BOARD_REPLIES,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 작성", description = "[AUTH] 새로운 댓글을 작성합니다. 인증된 사용자만 사용할 수 있습니다.",
        security = @SecurityRequirement(name = "basicAuth"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                schema = @Schema(implementation = CreateReplyRequest.class)
            )
        )
    )
    @ApiResponse(responseCode = "201", description = "댓글이 성공적으로 작성됨",
        content = @Content(schema = @Schema(implementation = ReplyResponse.class)))
    @ApiResponse(responseCode = "422", description = "유효성 검증 실패")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "404", description = "게시글 또는 상위 댓글을 찾을 수 없음")
    public ReplyResponse create(
        @Parameter(description = "게시글 ID") @PathVariable UUID boardId,
        @Validated @org.springframework.web.bind.annotation.RequestBody CreateReplyRequest request,
        @AuthenticationPrincipal UserDetails principal
    ) {
        var cmd = replyWebMapper.toCommand(request, boardId, principal.getUsername());
        ReplyResult result = replyCommandService.create(cmd);
        return replyWebMapper.toResponse(result);
    }

    @PutMapping(
        value = ApiPaths.REPLIES + "/{replyId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "댓글 수정", description = "[OWNER] 기존 댓글을 수정합니다. 작성자 본인만 수정 가능합니다.",
        security = @SecurityRequirement(name = "basicAuth"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                schema = @Schema(implementation = UpdateReplyRequest.class)
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
        content = @Content(schema = @Schema(implementation = ReplyResponse.class)))
    @ApiResponse(responseCode = "422", description = "유효성 검증 실패")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "403", description = "수정 권한 없음 (작성자가 아님)")
    @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    public ReplyResponse update(
        @Parameter(description = "수정할 댓글 ID") @PathVariable UUID replyId,
        @Validated @org.springframework.web.bind.annotation.RequestBody UpdateReplyRequest request
    ) {
        var cmd = replyWebMapper.toCommand(request);
        ReplyResult result = replyCommandService.update(replyId, cmd);
        return replyWebMapper.toResponse(result);
    }

    @DeleteMapping(ApiPaths.REPLIES + "/{replyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "댓글 삭제", description = "[OWNER] 기존 댓글을 삭제합니다. 자식 댓글이 있으면 soft delete 처리됩니다.",
        security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "204", description = "댓글 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자가 아님)")
    @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    public void delete(
        @Parameter(description = "삭제할 댓글 ID") @PathVariable UUID replyId
    ) {
        replyCommandService.delete(replyId);
    }
}
