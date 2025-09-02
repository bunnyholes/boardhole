package bunny.boardhole.user.application.command;

import bunny.boardhole.shared.exception.*;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.application.mapper.UserMapper;
import bunny.boardhole.user.application.result.UserResult;
import bunny.boardhole.user.domain.*;
import bunny.boardhole.user.infrastructure.UserRepository;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MessageUtils messageUtils;

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
        if (userRepository.existsByUsername(cmd.username())) {
            throw new bunny.boardhole.shared.exception.DuplicateUsernameException(messageUtils.getMessage("error.user.username.already-exists"));
        }
        if (userRepository.existsByEmail(cmd.email())) {
            throw new bunny.boardhole.shared.exception.DuplicateEmailException(messageUtils.getMessage("error.user.email.already-exists"));
        }
        User user = User.builder()
                .username(cmd.username())
                .password(passwordEncoder.encode(cmd.password()))
                .name(cmd.name())
                .email(cmd.email())
                .roles(java.util.Set.of(Role.USER))
                .build();
        User saved = userRepository.save(user);

        log.info(messageUtils.getMessage("log.user.created", saved.getUsername(), saved.getEmail()));
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
        
        // Optional을 사용한 선택적 필드 업데이트
        Optional.ofNullable(cmd.name()).ifPresent(user::changeName);
        Optional.ofNullable(cmd.email()).ifPresent(user::changeEmail);
        Optional.ofNullable(cmd.password())
                .map(passwordEncoder::encode)
                .ifPresent(user::changePassword);
        
        // @DynamicUpdate가 변경된 필드만 업데이트, @PreUpdate가 updatedAt 자동 설정
        User saved = userRepository.save(user);
        
        log.info(messageUtils.getMessage("log.user.updated", saved.getUsername()));
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
    public void delete(@NotNull @Positive Long id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", id)));

        String username = existing.getUsername();
        userRepository.delete(existing);
        log.info(messageUtils.getMessage("log.user.deleted", username));
    }

    @Deprecated
    public User login(String username, String password) {
        throw new UnsupportedOperationException(
                "Authentication should be handled by Spring Security AuthenticationManager"
        );
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
        user.recordLastLogin(LocalDateTime.now());
        User saved = userRepository.save(user);

        log.info(messageUtils.getMessage("log.user.last-login-updated", saved.getUsername()));
    }


}
