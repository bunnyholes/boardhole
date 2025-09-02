package bunny.boardhole.auth.presentation;

import bunny.boardhole.auth.application.command.*;
import bunny.boardhole.auth.presentation.dto.LoginRequest;
import bunny.boardhole.auth.presentation.mapper.AuthWebMapper;
import bunny.boardhole.shared.constants.ApiPaths;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.user.application.command.CreateUserCommand;
import bunny.boardhole.user.application.command.UserCommandService;
import bunny.boardhole.user.presentation.dto.UserCreateRequest;
import bunny.boardhole.user.presentation.mapper.UserWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 컨트롤러
 * 회원가입, 로그인, 로그아웃, 현재 사용자 정보 조회 등 인증 관련 기능을 제공합니다.
 */
@RestController
@RequestMapping(ApiPaths.AUTH)
@Validated
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "사용자 인증 및 권한 관리 기능")
public class AuthController {
    
    /** HTTP 204 No Content 응답 코드 */
    private static final String HTTP_NO_CONTENT = "204";
    
    /** HTTP 401 Unauthorized 응답 코드 */
    private static final String HTTP_UNAUTHORIZED = "401";

    /** 사용자 명령 서비스 */
    private final UserCommandService userCommandService;
    
    /** 인증 명령 서비스 */
    private final AuthCommandService authCommandService;
    
    /** 인증 웹 매퍼 */
    private final AuthWebMapper authWebMapper;
    
    /** 사용자 웹 매퍼 */
    private final UserWebMapper userWebMapper;

    @PostMapping(value = ApiPaths.AUTH_SIGNUP, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermitAll
    @Operation(
            summary = "회원가입",
            description = "[PUBLIC] 새로운 사용자 계정을 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = UserCreateRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = HTTP_NO_CONTENT, description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 중복된 사용자명/이메일")
    })
    public void signup(@Validated @ModelAttribute final UserCreateRequest request) {
        // Map request to command; keep repository returning entities in service
        final CreateUserCommand createCommand = userWebMapper.toCreateCommand(request);
        userCommandService.create(createCommand);
    }

    @PostMapping(value = ApiPaths.AUTH_LOGIN, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermitAll
    @Operation(
            summary = "로그인",
            description = "[PUBLIC] 사용자의 인증 정보를 확인하고 세션을 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = LoginRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = HTTP_NO_CONTENT, description = "로그인 성공"),
            @ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = "잘못된 인증 정보")
    })
    public void login(@Validated @ModelAttribute final LoginRequest loginRequest, final HttpServletRequest request, final HttpServletResponse response) {
        // CQRS 패턴을 통한 로그인 처리
        final LoginCommand processedLoginCommand = authWebMapper.toLoginCommand(loginRequest);
        authCommandService.login(processedLoginCommand, request, response);

        // 마지막 로그인 시간 업데이트 (기존 로직 유지)
        try {
            final Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
            if (authenticatedUser != null && authenticatedUser.getPrincipal() instanceof AppUserPrincipal(
                    bunny.boardhole.user.domain.User user
            )) {
                userCommandService.updateLastLogin(user.getId());
            }
        } catch (final UnsupportedOperationException ignored) {
            // 일부 테스트/환경에서 보조 로직 미구현으로 인한 예외는 로그인 성공 흐름에 영향 주지 않도록 무시
        }
    }

    @PostMapping(ApiPaths.AUTH_LOGOUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "로그아웃",
            description = "[AUTH] 현재 사용자의 세션을 종료하고 로그아웃합니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(responseCode = HTTP_NO_CONTENT, description = "로그아웃 성공"),
            @ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = "인증되지 않은 사용자")
    })
    public void logout(final HttpServletRequest request, final HttpServletResponse response,
                       @AuthenticationPrincipal final AppUserPrincipal principal) {
        // CQRS 패턴을 통한 로그아웃 처리
        final Long userId = principal != null ? principal.user().getId() : null;
        if (userId != null) {
            final LogoutCommand processedLogoutCommand = LogoutCommand.create(userId);
            authCommandService.logout(processedLogoutCommand, request, response);
        } else {
            // 인증 정보가 없는 경우 기본 로그아웃 처리
            SecurityContextHolder.clearContext();
            final HttpSession currentSession = request.getSession(false);
            if (currentSession != null) {
                currentSession.invalidate();
            }
        }
    }

    @GetMapping(ApiPaths.AUTH_ADMIN_ONLY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @Operation(
            summary = "관리자 전용 엔드포인트",
            description = "[ROLE:ADMIN] 관리자 권한을 가진 사용자만 접근할 수 있는 테스트 엔드포인트입니다.",
            security = @SecurityRequirement(name = "admin-role")
    )
    @ApiResponses({
            @ApiResponse(responseCode = HTTP_NO_CONTENT, description = "관리자 접근 성공"),
            @ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public void adminOnly(@AuthenticationPrincipal final AppUserPrincipal principal) {
        // Test endpoint - no response body needed
    }

    @GetMapping(ApiPaths.AUTH_USER_ACCESS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated() and hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "일반 사용자 접근 엔드포인트",
            description = "[MULTI-ROLE:USER,ADMIN] USER 또는 ADMIN 권한을 가진 사용자가 접근할 수 있는 테스트 엔드포인트입니다.",
            security = @SecurityRequirement(name = "user-role")
    )
    @ApiResponses({
            @ApiResponse(responseCode = HTTP_NO_CONTENT, description = "사용자 접근 성공"),
            @ApiResponse(responseCode = HTTP_UNAUTHORIZED, description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "사용자 권한 없음")
    })
    public void userAccess(@AuthenticationPrincipal final AppUserPrincipal principal) {
        // Test endpoint - no response body needed
    }

    @GetMapping(ApiPaths.AUTH_PUBLIC_ACCESS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermitAll
    @Operation(
            summary = "공개 엔드포인트",
            description = "[PUBLIC] 인증 없이 모든 사용자가 접근할 수 있는 공개 테스트 엔드포인트입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = HTTP_NO_CONTENT, description = "공개 엔드포인트 접근 성공")
    })
    public void publicAccess() {
        // Test endpoint - no response body needed
    }


}
