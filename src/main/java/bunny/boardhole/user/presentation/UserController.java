package bunny.boardhole.user.presentation;

import bunny.boardhole.auth.presentation.dto.CurrentUserResponse;
import bunny.boardhole.auth.presentation.mapper.AuthWebMapper;
import bunny.boardhole.shared.exception.UnauthorizedException;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.application.command.UserCommandService;
import bunny.boardhole.user.application.query.UserQueryService;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.presentation.dto.UserResponse;
import bunny.boardhole.user.presentation.dto.UserUpdateRequest;
import bunny.boardhole.user.presentation.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@Validated
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 관리 및 조회 기능")
public class UserController {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserWebMapper userWebMapper;
    private final AuthWebMapper authWebMapper;
    private final MessageUtils messageUtils;

    @GetMapping
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @Operation(
            summary = "사용자 목록 조회",
            description = "[ADMIN] 페이지네이션을 지원하는 사용자 목록을 조회합니다. 관리자만 접근 가능합니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @Parameters({
            @Parameter(name = "page", description = "0부터 시작하는 페이지 인덱스", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "10"),
            @Parameter(name = "sort", description = "정렬 (필드,방향)", example = "id,desc")
    })
    public Page<UserResponse> list(
            @Parameter(description = "페이지네이션 정보 (기본: 페이지 크기 10, ID 내림차순 정렬)")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) @ParameterObject Pageable pageable,
            @Parameter(description = "검색어 (사용자명으로 검색)", example = "admin")
            @RequestParam(required = false) String search
    ) {
        Page<UserResult> page = search == null
                ? userQueryService.listWithPaging(pageable)
                : userQueryService.listWithPaging(pageable, search);
        return page.map(userWebMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or #id == authentication.principal.user.id)")
    @Operation(
            summary = "사용자 상세 조회",
            description = "[AUTH] 특정 사용자의 상세 정보를 조회합니다. 관리자이거나 본인만 조회 가능합니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public UserResponse get(
            @Parameter(description = "조회할 사용자 ID")
            @PathVariable Long id) {
        var userResult = userQueryService.get(id);
        return userWebMapper.toResponse(userResult);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "사용자 정보 수정",
            description = "[AUTH] 사용자의 개인 정보를 수정합니다. 인증된 사용자만 사용할 수 있습니다.",
            security = @SecurityRequirement(name = "session"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = UserUpdateRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public UserResponse update(
            @Parameter(description = "수정할 사용자 ID")
            @PathVariable Long id,
            @Validated @ModelAttribute UserUpdateRequest req) {
        var cmd = userWebMapper.toUpdateCommand(id, req);
        var updated = userCommandService.update(cmd);
        return userWebMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "사용자 삭제",
            description = "[AUTH] 사용자 계정을 삭제합니다. 인증된 사용자만 사용할 수 있습니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public void delete(
            @Parameter(description = "삭제할 사용자 ID")
            @PathVariable Long id) {
        userCommandService.delete(id);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "현재 로그인한 사용자 정보 조회",
            description = "[AUTH] 현재 로그인한 사용자의 상세 정보를 조회합니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "현재 사용자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = CurrentUserResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public CurrentUserResponse me(@AuthenticationPrincipal AppUserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException(messageUtils.getMessage("error.auth.not-logged-in"));
        }
        return authWebMapper.toCurrentUser(principal.user());
    }
}
