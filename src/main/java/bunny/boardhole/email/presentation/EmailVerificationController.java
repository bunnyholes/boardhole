package bunny.boardhole.email.presentation;

import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.shared.constants.ApiPaths;
import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;

/**
 * 이메일 인증 관련 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.USERS)
@RequiredArgsConstructor
@Tag(name = "이메일 인증", description = "이메일 인증 관련 API")
public class EmailVerificationController {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;

    @Value("${boardhole.email.verification-expiration-hours:24}")
    private int verificationExpirationHours;

    @GetMapping("/{id}/email/verify")
    @Operation(
            summary = "이메일 인증",
            description = "토큰을 통한 이메일 인증 처리 (회원가입/이메일 변경)"
    )
    @ApiResponse(responseCode = "200", description = "인증 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 토큰 또는 만료된 토큰")
    @ApiResponse(responseCode = "404", description = "토큰을 찾을 수 없음")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public String verifyEmail(
            @Parameter(description = "사용자 ID")
            @PathVariable Long id,
            @Parameter(description = "인증 토큰", example = "abc123def456")
            @RequestParam String token) {

        EmailVerification verification = emailVerificationRepository.findByCodeAndUsedFalse(token)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.get("error.email-verification.invalid-token")));

        if (verification.isExpired()) throw new IllegalArgumentException(
                MessageUtils.get("error.email-verification.expired"));

        User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.get("error.user.not-found.id", verification.getUserId())));

        // 인증 타입에 따른 처리
        if (verification.getVerificationType() == EmailVerificationType.SIGNUP) {
            // 회원가입 인증
            user.verifyEmail();
            user.changeEmail(verification.getNewEmail());
            emailService.sendWelcomeEmail(user);

        } else if (verification.getVerificationType() == EmailVerificationType.CHANGE_EMAIL) {
            // 이메일 변경 인증
            user.changeEmail(verification.getNewEmail());
            emailService.sendEmailChangedNotification(user, verification.getNewEmail());
        }

        verification.markAsUsed();
        userRepository.save(user);
        emailVerificationRepository.save(verification);

        return MessageUtils.get("success.email-verification.completed");
    }

    @PostMapping("/{id}/email/resend")
    @Operation(
            summary = "인증 이메일 재발송",
            description = "미인증 사용자의 인증 이메일 재발송"
    )
    @ApiResponse(responseCode = "200", description = "재발송 성공")
    @ApiResponse(responseCode = "400", description = "이미 인증된 사용자 또는 잘못된 요청")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public String resendVerificationEmail(
            @Parameter(description = "사용자 ID")
            @PathVariable Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.get("error.user.not-found.id", id)));

        if (user.isEmailVerified()) throw new IllegalArgumentException(
                MessageUtils.get("error.email-verification.already-verified"));

        // 기존 미사용 토큰들을 만료 처리
        List<EmailVerification> existingVerifications = emailVerificationRepository.findByUserIdAndUsedFalse(user.getId());
        existingVerifications.forEach(verification -> {
            verification.markAsUsed();
            emailVerificationRepository.save(verification);
        });

        // 새 인증 토큰 생성 및 이메일 발송
        String newToken = java.util.UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now(ZoneId.systemDefault())
                .plusHours(verificationExpirationHours);

        EmailVerification newVerification = EmailVerification.builder()
                .code(newToken)
                .userId(user.getId())
                .newEmail(user.getEmail())
                .expiresAt(expiresAt)
                .verificationType(EmailVerificationType.SIGNUP)
                .build();

        emailVerificationRepository.save(newVerification);
        emailService.sendSignupVerificationEmail(user, newToken);

        return MessageUtils.get("success.email-verification.resent");
    }
}