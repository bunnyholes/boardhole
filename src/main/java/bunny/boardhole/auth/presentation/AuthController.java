package bunny.boardhole.auth.presentation;

import bunny.boardhole.auth.application.command.*;
import bunny.boardhole.auth.presentation.dto.LoginRequest;
import bunny.boardhole.auth.presentation.mapper.AuthWebMapper;
import bunny.boardhole.shared.security.AppUserPrincipal;
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

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "사용자 인증 및 권한 관리 기능")
public class AuthController {

    private final UserCommandService userCommandService;
    private final AuthCommandService authCommandService;
    private final AuthWebMapper authWebMapper;
    private final UserWebMapper userWebMapper;

    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
            @ApiResponse(responseCode = "204", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 중복된 사용자명/이메일")
    })
    public void signup(@Validated @ModelAttribute UserCreateRequest req) {
        // Map request to command; keep repository returning entities in service
        var cmd = userWebMapper.toCreateCommand(req);
        userCommandService.create(cmd);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
            @ApiResponse(responseCode = "204", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "잘못된 인증 정보")
    })
    public void login(@Validated @ModelAttribute LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        // CQRS 패턴을 통한 로그인 처리
        var loginCommand = authWebMapper.toLoginCommand(req);
        authCommandService.login(loginCommand, request, response);

        // 마지막 로그인 시간 업데이트 (기존 로직 유지)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUserPrincipal(
                bunny.boardhole.user.domain.User user
        )) {
            userCommandService.updateLastLogin(user.getId());
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "로그아웃",
            description = "[AUTH] 현재 사용자의 세션을 종료하고 로그아웃합니다.",
            security = @SecurityRequirement(name = "session")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       @AuthenticationPrincipal AppUserPrincipal principal) {
        // CQRS 패턴을 통한 로그아웃 처리
        Long userId = principal != null ? principal.user().getId() : null;
        if (userId != null) {
            var logoutCommand = new LogoutCommand(userId);
            authCommandService.logout(logoutCommand, request, response);
        } else {
            // 인증 정보가 없는 경우 기본 로그아웃 처리
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
    }

    @GetMapping("/admin-only")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @Operation(
            summary = "관리자 전용 엔드포인트",
            description = "[ROLE:ADMIN] 관리자 권한을 가진 사용자만 접근할 수 있는 테스트 엔드포인트입니다.",
            security = @SecurityRequirement(name = "admin-role")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "관리자 접근 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    public void adminOnly(@AuthenticationPrincipal AppUserPrincipal principal) {
        // Test endpoint - no response body needed
    }

    @GetMapping("/user-access")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated() and hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "일반 사용자 접근 엔드포인트",
            description = "[MULTI-ROLE:USER,ADMIN] USER 또는 ADMIN 권한을 가진 사용자가 접근할 수 있는 테스트 엔드포인트입니다.",
            security = @SecurityRequirement(name = "user-role")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "사용자 접근 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "사용자 권한 없음")
    })
    public void userAccess(@AuthenticationPrincipal AppUserPrincipal principal) {
        // Test endpoint - no response body needed
    }

    @GetMapping("/public-access")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermitAll
    @Operation(
            summary = "공개 엔드포인트",
            description = "[PUBLIC] 인증 없이 모든 사용자가 접근할 수 있는 공개 테스트 엔드포인트입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "공개 엔드포인트 접근 성공")
    })
    public void publicAccess() {
        // Test endpoint - no response body needed
    }


}
