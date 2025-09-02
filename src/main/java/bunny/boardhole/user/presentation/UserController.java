package bunny.boardhole.user.presentation;

import bunny.boardhole.auth.presentation.dto.CurrentUserResponse;
import bunny.boardhole.auth.presentation.mapper.AuthWebMapper;
import bunny.boardhole.shared.constants.ApiPaths;
import bunny.boardhole.shared.exception.*;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.application.command.UserCommandService;
import bunny.boardhole.user.application.query.UserQueryService;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.presentation.dto.*;
import bunny.boardhole.user.presentation.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(ApiPaths.USERS)
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "사용자 목록 조회",
            description = "[ROLE:ADMIN] 관리자가 사용자 목록을 페이지네이션으로 조회합니다. 검색 기능을 제공합니다.",
            security = @SecurityRequirement(name = "admin-role")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public Page<UserResponse> list(
            @Parameter(description = "검색어 (사용자명, 이름, 이메일)")
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        Page<UserResult> results = (search != null && !search.trim().isEmpty())
                ? userQueryService.listWithPaging(pageable, search.trim())
                : userQueryService.listWithPaging(pageable);

        return results.map(userWebMapper::toResponse);
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

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "패스워드 변경",
            description = "[AUTH] 사용자의 패스워드를 변경합니다. 현재 패스워드 확인이 필요합니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "패스워드 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "현재 패스워드 불일치"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public void updatePassword(
            @Parameter(description = "사용자 ID")
            @PathVariable Long id,
            @Validated @RequestBody PasswordUpdateRequest req,
            @AuthenticationPrincipal AppUserPrincipal principal) {

        // 패스워드 확인 불일치 처리
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new ValidationException(messageUtils.getMessage("error.user.password.confirm.mismatch"));
        }

        var cmd = userWebMapper.toUpdatePasswordCommand(id, req);
        userCommandService.updatePassword(cmd);
    }


    @PostMapping("/{id}/email/verification")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "이메일 변경 검증 요청",
            description = "[AUTH] 이메일 변경을 위한 검증 코드를 요청합니다. 검증 코드는 이메일로 발송됩니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검증 코드 발송 완료",
                    content = @Content(schema = @Schema(type = "object", example = "{\"message\": \"Verification code sent\"}"))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "현재 패스워드 불일치"),
            @ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    public ResponseEntity<java.util.Map<String, String>> requestEmailVerification(
            @Parameter(description = "사용자 ID")
            @PathVariable Long id,
            @Validated @RequestBody EmailVerificationRequest req) {

        var cmd = userWebMapper.toRequestEmailVerificationCommand(id, req);
        String code = userCommandService.requestEmailVerification(cmd);

        // 개발 환경에서만 코드 반환, 프로덕션에서는 메시지만
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", messageUtils.getMessage("info.user.email.verification.sent"));
        // TODO: 개발 환경 체크 후 코드 포함 여부 결정
        if (code != null) {
            response.put("code", code); // 테스트용
        }
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/email")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "이메일 변경",
            description = "[AUTH] 검증 코드를 확인하고 이메일을 변경합니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "이메일 변경 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 검증 코드"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public UserResponse updateEmail(
            @Parameter(description = "사용자 ID")
            @PathVariable Long id,
            @Validated @RequestBody EmailUpdateRequest req) {

        var cmd = userWebMapper.toUpdateEmailCommand(id, req);
        var updated = userCommandService.updateEmail(cmd);
        return userWebMapper.toResponse(updated);
    }

    @GetMapping(ApiPaths.USERS_ME)
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
