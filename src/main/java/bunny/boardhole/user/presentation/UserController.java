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

/**
 * 사용자 API 컨트롤러
 * 사용자의 생성, 조회, 수정, 삭제 및 관련 기능을 제공합니다.
 * 관리자 권한이나 본인 권한에 따른 접근 제어가 적용됩니다.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.USERS)
@Validated
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 관리 및 조회 기능")
public class UserController {
    /** User command service for data modification operations */
    private final UserCommandService userCommandService;
    
    /** User query service for data retrieval operations */
    private final UserQueryService userQueryService;
    
    /** User web mapper for web DTO conversions */
    private final UserWebMapper userWebMapper;
    
    /** Authentication web mapper for auth DTO conversions */
    private final AuthWebMapper authWebMapper;
    
    /** Message utilities for internationalization */
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
    /**
     * 사용자 목록을 페이지네이션으로 조회합니다.
     * 관리자만 접근 가능하며, 검색어로 사용자명을 필터링할 수 있습니다.
     *
     * @param pageable 페이지네이션 정보 (기본: 페이지 크기 10, ID 내림차순 정렬)
     * @param searchTerm 검색어 (사용자명으로 검색)
     * @return 사용자 목록 페이지
     */
    public Page<UserResponse> list(
            @Parameter(description = "페이지네이션 정보 (기본: 페이지 크기 10, ID 내림차순 정렬)")
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) @ParameterObject final Pageable pageable,
            @Parameter(description = "검색어 (사용자명으로 검색)", example = "admin")
            @RequestParam(required = false) final String searchTerm
    ) {
        final Page<UserResult> resultPage = searchTerm == null
                ? userQueryService.listWithPaging(pageable)
                : userQueryService.listWithPaging(pageable, searchTerm);
        return resultPage.map(userWebMapper::toResponse);
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
    /**
     * 특정 사용자의 상세 정보를 조회합니다.
     * 관리자이거나 본인만 조회 가능합니다.
     *
     * @param identifier 조회할 사용자 ID
     * @return 사용자 상세 정보
     */
    public UserResponse get(
            @Parameter(description = "조회할 사용자 ID")
            @PathVariable final Long identifier) {
        final UserResult userResult = userQueryService.get(identifier);
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
    /**
     * 사용자의 개인 정보를 수정합니다.
     * 인증된 사용자만 사용할 수 있습니다.
     *
     * @param identifier 수정할 사용자 ID
     * @param request 사용자 수정 요청 데이터
     * @return 수정된 사용자 정보
     */
    public UserResponse update(
            @Parameter(description = "수정할 사용자 ID")
            @PathVariable final Long identifier,
            @Validated @ModelAttribute final UserUpdateRequest request) {
        final var command = userWebMapper.toUpdateCommand(identifier, request);
        final var updatedUser = userCommandService.update(command);
        return userWebMapper.toResponse(updatedUser);
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
    /**
     * 사용자 계정을 삭제합니다.
     * 인증된 사용자만 사용할 수 있습니다.
     *
     * @param identifier 삭제할 사용자 ID
     */
    public void delete(
            @Parameter(description = "삭제할 사용자 ID")
            @PathVariable final Long identifier) {
        userCommandService.delete(identifier);
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
    /**
     * 사용자의 패스워드를 변경합니다.
     * 현재 패스워드 확인이 필요하며, 새 패스워드와 확인 패스워드가 일치해야 합니다.
     *
     * @param identifier 사용자 ID
     * @param request 패스워드 변경 요청 데이터
     * @param principal 현재 인증된 사용자 정보
     */
    public void updatePassword(
            @Parameter(description = "사용자 ID")
            @PathVariable final Long identifier,
            @Validated @RequestBody final PasswordUpdateRequest request,
            @AuthenticationPrincipal final AppUserPrincipal principal) {

        // 패스워드 확인 불일치 처리
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ValidationException(messageUtils.getMessage("error.user.password.confirm.mismatch"));
        }

        final var command = userWebMapper.toUpdatePasswordCommand(identifier, request);
        userCommandService.updatePassword(command);
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
    /**
     * 이메일 변경을 위한 검증 코드를 요청합니다.
     * 검증 코드는 새로운 이메일 주소로 발송됩니다.
     *
     * @param identifier 사용자 ID
     * @param request 이메일 검증 요청 데이터
     * @return 검증 코드 발송 완료 메시지
     */
    public ResponseEntity<java.util.Map<String, String>> requestEmailVerification(
            @Parameter(description = "사용자 ID")
            @PathVariable final Long identifier,
            @Validated @RequestBody final EmailVerificationRequest request) {

        final var command = userWebMapper.toRequestEmailVerificationCommand(identifier, request);
        final String verificationCode = userCommandService.requestEmailVerification(command);

        // 개발 환경에서만 코드 반환, 프로덕션에서는 메시지만
        final java.util.Map<String, String> responseMap = new java.util.HashMap<>();
        responseMap.put("message", messageUtils.getMessage("info.user.email.verification.sent"));
        // TODO: 개발 환경 체크 후 코드 포함 여부 결정
        if (verificationCode != null) {
            responseMap.put("code", verificationCode); // 테스트용
        }
        return ResponseEntity.ok(responseMap);
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
    /**
     * 검증 코드를 확인하고 이메일을 변경합니다.
     * 유효한 검증 코드가 제공되어야 합니다.
     *
     * @param identifier 사용자 ID
     * @param request 이메일 변경 요청 데이터 (검증 코드 포함)
     * @return 변경된 사용자 정보
     */
    public UserResponse updateEmail(
            @Parameter(description = "사용자 ID")
            @PathVariable final Long identifier,
            @Validated @RequestBody final EmailUpdateRequest request) {

        final var command = userWebMapper.toUpdateEmailCommand(identifier, request);
        final var updatedUser = userCommandService.updateEmail(command);
        return userWebMapper.toResponse(updatedUser);
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
    /**
     * 현재 로그인한 사용자의 상세 정보를 조회합니다.
     * 인증된 사용자만 접근 가능합니다.
     *
     * @param principal 현재 인증된 사용자 정보
     * @return 현재 사용자 정보
     * @throws UnauthorizedException 인증되지 않은 경우
     */
    public CurrentUserResponse me(@AuthenticationPrincipal final AppUserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException(messageUtils.getMessage("error.auth.not-logged-in"));
        }
        return authWebMapper.toCurrentUser(principal.user());
    }
}
