package bunny.boardhole.auth.presentation;

import java.util.UUID;

import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bunny.boardhole.auth.application.AuthCommandService;
import bunny.boardhole.auth.application.mapper.AuthMapper;
import bunny.boardhole.auth.presentation.dto.LoginRequest;
import bunny.boardhole.auth.presentation.mapper.AuthWebMapper;
import bunny.boardhole.shared.constants.ApiPaths;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.user.application.command.UserCommandService;
import bunny.boardhole.user.presentation.dto.UserCreateRequest;
import bunny.boardhole.user.presentation.mapper.UserWebMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.AUTH)
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증 API", description = "사용자 인증 및 권한 관리 기능")
public class AuthController {

    private final UserCommandService userCommandService;
    private final AuthCommandService authCommandService;
    private final AuthWebMapper authWebMapper;
    private final AuthMapper authMapper;
    private final UserWebMapper userWebMapper;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping(value = ApiPaths.AUTH_SIGNUP, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermitAll
    @Operation(summary = "회원가입", description = "[PUBLIC] 새로운 사용자 계정을 생성합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = UserCreateRequest.class))))
    @ApiResponse(responseCode = "204", description = "회원가입 성공")
    @ApiResponse(responseCode = "422", description = "유효성 검증 실패 (필수 필드 누락, 패스워드 패턴 불일치 등)")
    @ApiResponse(responseCode = "409", description = "중복된 사용자명 또는 이메일")
    public void signup(@Validated @ModelAttribute UserCreateRequest req) {
        // Map request to command; keep repository returning entities in service
        var cmd = userWebMapper.toCreateCommand(req);
        userCommandService.create(cmd);
    }

    @PostMapping(value = ApiPaths.AUTH_LOGIN, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermitAll
    @Operation(summary = "로그인", description = "[PUBLIC] 사용자의 인증 정보를 확인하고 세션을 생성합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = LoginRequest.class))))
    @ApiResponse(responseCode = "204", description = "로그인 성공")
    @ApiResponse(responseCode = "401", description = "잘못된 인증 정보")
    public void login(@Validated @ModelAttribute LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        // CQRS 패턴을 통한 로그인 처리
        var loginCommand = authWebMapper.toLoginCommand(req);
        authCommandService.login(loginCommand);

        // HTTP 세션 처리는 Controller에서 담당
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            SecurityContext context = SecurityContextHolder.getContext();
            securityContextRepository.saveContext(context, request, response);
        }

        // 마지막 로그인 시간 업데이트 (기존 로직 유지)
        try {
            if (authentication != null && authentication.getPrincipal() instanceof AppUserPrincipal(
                    bunny.boardhole.user.domain.User user
            ))
                userCommandService.updateLastLogin(user.getId());
        } catch (UnsupportedOperationException ignored) {
            // 일부 테스트/환경에서 보조 로직 미구현으로 인한 예외는 로그인 성공 흐름에 영향 주지 않도록 무시
        }
    }

    @PostMapping(ApiPaths.AUTH_LOGOUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "로그아웃", description = "[AUTH] 현재 사용자의 세션을 종료하고 로그아웃합니다."

    )
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    public void logout(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal AppUserPrincipal principal) {
        // CQRS 패턴을 통한 로그아웃 처리
        UUID userId = principal.user().getId();
        var logoutCommand = authMapper.toLogoutCommand(userId);
        authCommandService.logout(logoutCommand);

        // HTTP 세션 처리는 Controller에서 담당
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null)
            session.invalidate();

        // SecurityContext 저장소에서도 제거
        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);
    }

    @GetMapping(ApiPaths.AUTH_ADMIN_ONLY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated() and hasRole('ADMIN')")
    @Operation(summary = "관리자 전용 엔드포인트", description = "[ROLE:ADMIN] 관리자 권한을 가진 사용자만 접근할 수 있는 테스트 엔드포인트입니다.", security = @SecurityRequirement(name = "admin-role"))
    @ApiResponse(responseCode = "204", description = "관리자 접근 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    public void adminOnly(@AuthenticationPrincipal AppUserPrincipal principal) {
        // Validate admin access and log the request
        log.info("Admin-only endpoint accessed by user: {}", principal.user().getUsername());
    }

    @GetMapping(ApiPaths.AUTH_USER_ACCESS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated() and hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "일반 사용자 접근 엔드포인트", description = "[MULTI-ROLE:USER,ADMIN] USER 또는 ADMIN 권한을 가진 사용자가 접근할 수 있는 테스트 엔드포인트입니다.", security = @SecurityRequirement(name = "user-role"))
    @ApiResponse(responseCode = "204", description = "사용자 접근 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @ApiResponse(responseCode = "403", description = "사용자 권한 없음")
    public void userAccess(@AuthenticationPrincipal AppUserPrincipal principal) {
        // Validate user access and log the request
        log.info("User access endpoint accessed by user: {} with roles: {}", principal.user().getUsername(), principal.getAuthorities());
    }

    @GetMapping(ApiPaths.AUTH_PUBLIC_ACCESS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PermitAll
    @Operation(summary = "공개 엔드포인트", description = "[PUBLIC] 인증 없이 모든 사용자가 접근할 수 있는 공개 테스트 엔드포인트입니다.")
    @ApiResponse(responseCode = "204", description = "공개 엔드포인트 접근 성공")
    public void publicAccess() {
        // Public endpoint accessible to everyone
        log.info("Public access endpoint accessed");
    }

}
