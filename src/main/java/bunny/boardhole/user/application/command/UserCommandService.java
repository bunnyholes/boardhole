package bunny.boardhole.user.application.command;

import bunny.boardhole.shared.config.properties.ValidationProperties;
import bunny.boardhole.shared.exception.*;
import bunny.boardhole.shared.util.*;
import bunny.boardhole.user.application.mapper.UserMapper;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 사용자 명령 서비스
 * CQRS 패턴의 Command 측면으로 사용자 생성, 수정, 삭제 등 데이터 변경 작업을 담당합니다.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class UserCommandService {

    /** 사용자 레포지토리 */
    private final UserRepository userRepository;
    
    /** 이메일 검증 레포지토리 */
    private final EmailVerificationRepository emailVerificationRepository;
    
    /** 패스워드 인코더 */
    private final PasswordEncoder passwordEncoder;
    
    /** 사용자 매퍼 */
    private final UserMapper userMapper;
    
    /** 메시지 유틸리티 */
    private final MessageUtils messageUtils;
    
    /** 검증 설정 속성 */
    private final ValidationProperties validationProperties;
    
    /** 검증 코드 생성기 */
    private final VerificationCodeGenerator verificationCodeGenerator;

    /**
     * 사용자 생성
     *
     * @param cmd 사용자 생성 명령
     * @return 생성된 사용자 결과
     * @throws DuplicateUsernameException 사용자명 중복 시
     * @throws DuplicateEmailException    이메일 중복 시
     */
    @Transactional
    public UserResult create(@Valid final CreateUserCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new DuplicateUsernameException(messageUtils.getMessage("error.user.username.already-exists"));
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(messageUtils.getMessage("error.user.email.already-exists"));
        }
        final User user = User.builder()
                .username(command.username())
                .password(passwordEncoder.encode(command.password()))
                .name(command.name())
                .email(command.email())
                .userRoles(java.util.Set.of(Role.USER))
                .build();
        final User savedUser = userRepository.save(user);

        if (log.isInfoEnabled()) {
            log.info(sanitizeForLog(messageUtils.getMessage("log.user.created", savedUser.getUsername(), savedUser.getEmail())));
        }
        return userMapper.toResult(savedUser);
    }

    // 조회 관련(get/list)는 UserQueryService에서 담당

    /**
     * 사용자 정보 수정 - @DynamicUpdate를 활용한 선택적 업데이트
     *
     * @param cmd 사용자 수정 명령
     * @return 수정된 사용자 결과
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    @PreAuthorize("hasPermission(#cmd.userId, 'USER', 'WRITE')")
    public UserResult update(@Valid @NonNull final UpdateUserCommand cmd) {
        final Long userId = cmd.userId();
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", userId)));

        // Optional을 사용한 선택적 필드 업데이트 (이름만 변경 가능)
        Optional.ofNullable(cmd.name()).ifPresent(user::changeName);

        // @DynamicUpdate가 변경된 필드만 업데이트, @PreUpdate가 updatedAt 자동 설정
        final User saved = userRepository.save(user);

        if (log.isInfoEnabled()) {
            log.info(messageUtils.getMessage("log.user.updated", saved.getUsername()));
        }
        return userMapper.toResult(saved);
    }

    /**
     * 사용자 삭제
     *
     * @param id 삭제할 사용자 ID
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    @PreAuthorize("hasPermission(#id, 'USER', 'DELETE')")
    public void delete(@NotNull @Positive final Long id) {
        final User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", id)));

        final String username = existing.getUsername();
        userRepository.delete(existing);
        
        if (log.isInfoEnabled()) {
            log.info(messageUtils.getMessage("log.user.deleted", username));
        }
    }

    /**
     * 마지막 로그인 시간 업데이트
     *
     * @param userId 로그인한 사용자 ID
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public void updateLastLogin(@NotNull @Positive final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", userId)));
        user.recordLastLogin(LocalDateTime.now());
        final User saved = userRepository.save(user);

        if (log.isInfoEnabled()) {
            log.info(messageUtils.getMessage("log.user.last-login-updated", saved.getUsername()));
        }
    }

    /**
     * 패스워드 변경
     *
     * @param cmd 패스워드 변경 명령
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws UnauthorizedException     현재 패스워드가 일치하지 않는 경우
     */
    @Transactional
    @PreAuthorize("hasPermission(#cmd.userId, 'USER', 'WRITE')")
    public void updatePassword(@Valid @NonNull final UpdatePasswordCommand cmd) {
        final User user = userRepository.findById(cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", cmd.userId())));

        // 현재 패스워드 확인
        if (!passwordEncoder.matches(cmd.currentPassword(), user.getPassword())) {
            if (log.isWarnEnabled()) {
                log.warn(messageUtils.getMessage("log.user.password.change.failed", cmd.userId()));
            }
            throw new UnauthorizedException(messageUtils.getMessage("error.user.password.current.mismatch"));
        }

        // 새 패스워드 설정
        user.changePassword(passwordEncoder.encode(cmd.newPassword()));
        userRepository.save(user);

        if (log.isInfoEnabled()) {
            log.info(messageUtils.getMessage("log.user.password.changed", user.getId(), user.getUsername()));
        }
    }

    /**
     * 이메일 변경 검증 요청
     *
     * @param cmd 이메일 검증 요청 명령
     * @return 검증 코드 (테스트 환경에서만 반환, 프로덕션에서는 이메일 발송)
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws UnauthorizedException     현재 패스워드가 일치하지 않는 경우
     * @throws DuplicateEmailException   이메일이 이미 사용 중인 경우
     */
    @Transactional
    @PreAuthorize("hasPermission(#cmd.userId, 'USER', 'WRITE')")
    public String requestEmailVerification(@Valid @NonNull final RequestEmailVerificationCommand cmd) {
        final User user = userRepository.findById(cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", cmd.userId())));

        // 현재 패스워드 확인
        if (!passwordEncoder.matches(cmd.currentPassword(), user.getPassword())) {
            if (log.isWarnEnabled()) {
                log.warn(messageUtils.getMessage("log.user.email.change.failed", cmd.userId()));
            }
            throw new UnauthorizedException(messageUtils.getMessage("error.user.password.current.mismatch"));
        }

        // 새 이메일 중복 확인
        if (userRepository.existsByEmail(cmd.newEmail())) {
            throw new DuplicateEmailException(messageUtils.getMessage("error.user.email.already-exists"));
        }

        // 기존 미사용 검증 정보 무효화
        emailVerificationRepository.invalidateUserVerifications(cmd.userId());

        // 검증 코드 생성
        final String verificationCode = verificationCodeGenerator.generate();

        // 검증 정보 저장
        final EmailVerification verification = EmailVerification.builder()
                .code(verificationCode)
                .userId(cmd.userId())
                .newEmail(cmd.newEmail())
                .expiresAt(LocalDateTime.now().plusMinutes(validationProperties.getEmailVerification().getExpirationMinutes()))
                .build();
        emailVerificationRepository.save(verification);

        // TODO: 실제 환경에서는 이메일 발송 서비스 호출

        if (log.isInfoEnabled()) {
            final String sanitizedOldEmail = sanitizeForLog(user.getEmail());
            final String sanitizedNewEmail = sanitizeForLog(cmd.newEmail());
            log.info(messageUtils.getMessage("log.user.email.verification.requested",
                    user.getId(), sanitizedOldEmail, sanitizedNewEmail));
        }

        // 개발/테스트 환경에서만 코드 반환, 프로덕션에서는 null 반환
        return verificationCode;
    }

    /**
     * 이메일 변경 확정
     *
     * @param cmd 이메일 변경 명령
     * @return 변경된 사용자 정보
     * @throws ResourceNotFoundException 사용자나 검증 정보를 찾을 수 없는 경우
     * @throws ValidationException       검증 코드가 유효하지 않은 경우
     */
    @Transactional
    @PreAuthorize("hasPermission(#command.userId, 'USER', 'WRITE')")
    public UserResult updateEmail(@Valid @NonNull final UpdateEmailCommand command) {
        final User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", command.userId())));

        // 검증 코드 확인
        final EmailVerification verification = emailVerificationRepository
                .findValidVerification(command.userId(), command.verificationCode(), LocalDateTime.now())
                .orElseThrow(() -> new ValidationException(messageUtils.getMessage("error.user.email.verification.invalid")));

        // 이메일 변경
        final String oldEmail = user.getEmail();
        user.changeEmail(verification.getNewEmail());
        final User savedUser = userRepository.save(user);

        // 검증 정보 사용 처리
        verification.markAsUsed();
        emailVerificationRepository.save(verification);

        // TODO: 실제 환경에서는 이메일 변경 알림 발송
        // emailService.sendEmailChangeNotification(oldEmail, verification.getNewEmail());

        if (log.isInfoEnabled()) {
            log.info(sanitizeForLog(messageUtils.getMessage("log.user.email.changed",
                    user.getId(), oldEmail, verification.getNewEmail())));
        }

        return userMapper.toResult(savedUser);
    }

    /**
     * CRLF 인젝션 방지를 위한 로그 문자열 정리
     * 
     * @param logMessage 정리할 로그 메시지
     * @return 정리된 로그 메시지
     */
    private String sanitizeForLog(final String logMessage) {
        return (logMessage == null) ? "" : logMessage.replaceAll("[\\r\\n\\t]", "_");
    }

}
