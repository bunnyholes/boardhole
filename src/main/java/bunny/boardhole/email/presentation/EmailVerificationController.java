package bunny.boardhole.email.presentation;

import bunny.boardhole.email.application.EmailService;
import bunny.boardhole.shared.constants.ApiPaths;
import bunny.boardhole.shared.exception.ResourceNotFoundException;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 이메일 인증 관련 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
@Tag(name = "이메일 인증", description = "이메일 인증 관련 API")
public class EmailVerificationController {

    /**
     * 인증 토큰 만료 시간 (시간)
     */
    @Value("${boardhole.email.verification-expiration-hours:24}")
    private int verificationExpirationHours;

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    private final MessageUtils messageUtils;

    /**
     * 이메일 인증 처리
     *
     * @param token 인증 토큰
     * @return 인증 결과
     */
    @GetMapping("/verify-email")
    @Operation(
        summary = "이메일 인증",
        description = "토큰을 통한 이메일 인증 처리 (회원가입/이메일 변경)"
    )
    @ApiResponse(responseCode = "200", description = "인증 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 토큰 또는 만료된 토큰")
    @ApiResponse(responseCode = "404", description = "토큰을 찾을 수 없음")
    @Transactional
    public ResponseEntity<String> verifyEmail(
            @Parameter(description = "인증 토큰", example = "abc123def456")
            @RequestParam final String verificationToken) {
        
        final EmailVerification verification = emailVerificationRepository.findByCodeAndUsedFalse(verificationToken)
                .orElseThrow(() -> new ResourceNotFoundException(
                    messageUtils.getMessage("error.email-verification.invalid-token")));

        if (verification.isExpired()) {
            throw new IllegalArgumentException(
                messageUtils.getMessage("error.email-verification.expired"));
        }

        final User user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    messageUtils.getMessage("error.user.not-found.id", verification.getUserId())));

        // 인증 타입에 따른 처리
        if (verification.getVerificationType() == EmailVerificationType.SIGNUP) {
            // 회원가입 인증
            user.verifyEmail();
            user.changeEmail(verification.getNewEmail());
            emailService.sendWelcomeEmail(user);
            
            log.info("회원가입 이메일 인증 완료: userId={}, email={}", 
                    user.getId(), user.getEmail());
            
        } else if (verification.getVerificationType() == EmailVerificationType.CHANGE_EMAIL) {
            // 이메일 변경 인증
            final String oldEmail = user.getEmail();
            user.changeEmail(verification.getNewEmail());
            emailService.sendEmailChangedNotification(user, verification.getNewEmail());
            
            log.info("이메일 변경 인증 완료: userId={}, oldEmail={}, newEmail={}", 
                    user.getId(), oldEmail, verification.getNewEmail());
        }

        verification.markAsUsed();
        userRepository.save(user);
        emailVerificationRepository.save(verification);

        return ResponseEntity.ok(messageUtils.getMessage("success.email-verification.completed"));
    }

    /**
     * 인증 이메일 재발송
     *
     * @param email 이메일 주소
     * @return 재발송 결과
     */
    @PostMapping("/resend-verification")
    @Operation(
        summary = "인증 이메일 재발송",
        description = "미인증 사용자의 인증 이메일 재발송"
    )
    @ApiResponse(responseCode = "200", description = "재발송 성공")
    @ApiResponse(responseCode = "400", description = "이미 인증된 사용자 또는 잘못된 요청")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    @Transactional
    public ResponseEntity<String> resendVerificationEmail(
            @Parameter(description = "재발송할 이메일 주소", example = "user@example.com")
            @RequestParam final String emailAddress) {
        
        final User user = userRepository.findByEmail(emailAddress)
                .orElseThrow(() -> new ResourceNotFoundException(
                    messageUtils.getMessage("error.user.not-found.email", emailAddress)));

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException(
                messageUtils.getMessage("error.email-verification.already-verified"));
        }

        // 기존 미사용 토큰들을 만료 처리
        emailVerificationRepository.findByUserIdAndUsedFalse(user.getId())
                .forEach(verification -> {
                    verification.markAsUsed();
                    emailVerificationRepository.save(verification);
                });

        // 새 인증 토큰 생성 및 이메일 발송
        final String newToken = java.util.UUID.randomUUID().toString();
        final LocalDateTime expiresAt = LocalDateTime.now().plusHours(verificationExpirationHours);
        
        final EmailVerification newVerification = EmailVerification.builder()
                .code(newToken)
                .userId(user.getId())
                .newEmail(user.getEmail())
                .expiresAt(expiresAt)
                .verificationType(EmailVerificationType.SIGNUP)
                .build();
        
        emailVerificationRepository.save(newVerification);
        emailService.sendSignupVerificationEmail(user, newToken);

        log.info("인증 이메일 재발송: userId={}, email={}", user.getId(), emailAddress);
        return ResponseEntity.ok(messageUtils.getMessage("success.email-verification.resent"));
    }
}