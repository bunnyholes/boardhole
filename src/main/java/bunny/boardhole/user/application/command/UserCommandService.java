package bunny.boardhole.user.application.command;

import bunny.boardhole.email.application.EmailService;
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

import java.time.*;
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

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MessageUtils messageUtils;
    private final ValidationProperties validationProperties;
    private final VerificationCodeGenerator verificationCodeGenerator;
    private final EmailService emailService;

    /**
     * 사용자 생성
     *
     * @param cmd 사용자 생성 명령
     * @return 생성된 사용자 결과
     * @throws DuplicateUsernameException 사용자명 중복 시
     * @throws DuplicateEmailException    이메일 중복 시
     */
    @Transactional
    public UserResult create(@Valid CreateUserCommand cmd) {
        if (userRepository.existsByUsername(cmd.username()))
            throw new DuplicateUsernameException(messageUtils.getMessage("error.user.username.already-exists"));
        if (userRepository.existsByEmail(cmd.email()))
            throw new DuplicateEmailException(messageUtils.getMessage("error.user.email.already-exists"));
        User user = User.builder()
                .username(cmd.username())
                .password(passwordEncoder.encode(cmd.password()))
                .name(cmd.name())
                .email(cmd.email())
                .roles(java.util.Set.of(Role.USER))
                .build();
        User saved = userRepository.save(user);

        // 회원가입 이메일 인증 토큰 생성 및 발송
        String verificationToken = java.util.UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now(ZoneId.systemDefault())
                .plusHours(validationProperties.getEmailVerification().getSignupExpirationHours());

        EmailVerification verification = EmailVerification.builder()
                .code(verificationToken)
                .userId(saved.getId())
                .newEmail(saved.getEmail())
                .expiresAt(expiresAt)
                .verificationType(EmailVerificationType.SIGNUP)
                .build();

        emailVerificationRepository.save(verification);
        emailService.sendSignupVerificationEmail(saved, verificationToken);

        return userMapper.toResult(saved);
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
    public UserResult update(@Valid @NonNull UpdateUserCommand cmd) {
        Long id = cmd.userId();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", id)));

        // Optional을 사용한 선택적 필드 업데이트 (이름만 변경 가능)
        Optional.ofNullable(cmd.name()).ifPresent(user::changeName);

        // @DynamicUpdate가 변경된 필드만 업데이트, @PreUpdate가 updatedAt 자동 설정
        User saved = userRepository.save(user);

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
    public UserResult delete(@NotNull @Positive Long id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", id)));

        userRepository.delete(existing);
        return userMapper.toResult(existing);
    }

    /**
     * 마지막 로그인 시간 업데이트
     *
     * @param userId 로그인한 사용자 ID
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public void updateLastLogin(@NotNull @Positive Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", userId)));
        user.recordLastLogin(LocalDateTime.now(ZoneId.systemDefault()));
        userRepository.save(user);
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
    public void updatePassword(@Valid @NonNull UpdatePasswordCommand cmd) {
        User user = userRepository.findById(cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", cmd.userId())));

        // 현재 패스워드 확인
        if (!passwordEncoder.matches(cmd.currentPassword(), user.getPassword())) {
            log.warn(messageUtils.getMessage("log.user.password.change.failed", cmd.userId()));
            throw new UnauthorizedException(messageUtils.getMessage("error.user.password.current.mismatch"));
        }

        // 새 패스워드 설정
        user.changePassword(passwordEncoder.encode(cmd.newPassword()));
        userRepository.save(user);
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
    public String requestEmailVerification(@Valid @NonNull RequestEmailVerificationCommand cmd) {
        User user = userRepository.findById(cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", cmd.userId())));

        // 현재 패스워드 확인
        if (!passwordEncoder.matches(cmd.currentPassword(), user.getPassword())) {
            log.warn(messageUtils.getMessage("log.user.email.change.failed", cmd.userId()));
            throw new UnauthorizedException(messageUtils.getMessage("error.user.password.current.mismatch"));
        }

        // 새 이메일 중복 확인
        if (userRepository.existsByEmail(cmd.newEmail()))
            throw new DuplicateEmailException(messageUtils.getMessage("error.user.email.already-exists"));

        // 기존 미사용 검증 정보 무효화
        emailVerificationRepository.invalidateUserVerifications(cmd.userId());

        // 검증 코드 생성
        String verificationCode = verificationCodeGenerator.generate();

        // 검증 정보 저장
        EmailVerification verification = EmailVerification.builder()
                .code(verificationCode)
                .userId(cmd.userId())
                .newEmail(cmd.newEmail())
                .expiresAt(LocalDateTime.now(ZoneId.systemDefault())
                        .plusMinutes(validationProperties.getEmailVerification().getExpirationMinutes()))
                .build();
        emailVerificationRepository.save(verification);

        // TODO: 실제 환경에서는 이메일 발송 서비스 호출
        // emailService.sendVerificationEmail(user.getEmail(), cmd.newEmail(), verificationCode);

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
    @PreAuthorize("hasPermission(#cmd.userId, 'USER', 'WRITE')")
    public UserResult updateEmail(@Valid @NonNull UpdateEmailCommand cmd) {
        User user = userRepository.findById(cmd.userId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", cmd.userId())));

        // 검증 코드 확인
        EmailVerification verification = emailVerificationRepository
                .findValidVerification(cmd.userId(), cmd.verificationCode(), LocalDateTime.now(ZoneId.systemDefault()))
                .orElseThrow(() -> new ValidationException(messageUtils.getMessage("error.user.email.verification.invalid")));

        // 이메일 변경
        user.changeEmail(verification.getNewEmail());
        User saved = userRepository.save(user);

        // 검증 정보 사용 처리
        verification.markAsUsed();
        emailVerificationRepository.save(verification);

        return userMapper.toResult(saved);
    }

}
