package bunny.boardhole.email.presentation;

import bunny.boardhole.email.application.EmailVerificationService;
import bunny.boardhole.shared.constants.ApiPaths;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 이메일 인증 관련 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.USERS)
@RequiredArgsConstructor
@Tag(name = "이메일 인증", description = "이메일 인증 관련 API")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/{id}/email/verify")
    @Operation(
            summary = "이메일 인증",
            description = "토큰을 통한 이메일 인증 처리 (회원가입/이메일 변경)"
    )
    @ApiResponse(responseCode = "200", description = "인증 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 토큰 또는 만료된 토큰")
    @ApiResponse(responseCode = "404", description = "토큰을 찾을 수 없음")
    @ResponseStatus(HttpStatus.OK)
    public String verifyEmail(
            @Parameter(description = "사용자 ID") @PathVariable Long id,
            @Parameter(description = "인증 토큰", example = "abc123def456") @RequestParam String token) {

        return emailVerificationService.verifyEmail(id, token);
    }

    @PostMapping("/{id}/email/resend")
    @Operation(
            summary = "인증 이메일 재발송",
            description = "미인증 사용자의 인증 이메일 재발송"
    )
    @ApiResponse(responseCode = "200", description = "재발송 성공")
    @ApiResponse(responseCode = "400", description = "이미 인증된 사용자 또는 잘못된 요청")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    @ResponseStatus(HttpStatus.OK)
    public String resendVerificationEmail(
            @Parameter(description = "사용자 ID") @PathVariable Long id) {

        return emailVerificationService.resendVerificationEmail(id);
    }
}