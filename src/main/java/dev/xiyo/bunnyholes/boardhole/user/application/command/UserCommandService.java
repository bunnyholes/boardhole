package dev.xiyo.bunnyholes.boardhole.user.application.command;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import dev.xiyo.bunnyholes.boardhole.shared.exception.DuplicateEmailException;
import dev.xiyo.bunnyholes.boardhole.shared.exception.DuplicateUsernameException;
import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;
import dev.xiyo.bunnyholes.boardhole.shared.exception.UnauthorizedException;
import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;
import dev.xiyo.bunnyholes.boardhole.user.application.mapper.UserMapper;
import dev.xiyo.bunnyholes.boardhole.user.application.result.UserResult;
import dev.xiyo.bunnyholes.boardhole.user.domain.Role;
import dev.xiyo.bunnyholes.boardhole.user.domain.User;
import dev.xiyo.bunnyholes.boardhole.user.infrastructure.UserRepository;

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
        User saved = createUser(cmd);
        return userMapper.toResult(saved);
    }

    private User createUser(CreateUserCommand cmd) {
        if (userRepository.existsByUsername(cmd.username()))
            throw new DuplicateUsernameException(MessageUtils.get("error.user.username.already-exists"));
        if (userRepository.existsByEmail(cmd.email()))
            throw new DuplicateEmailException(MessageUtils.get("error.user.email.already-exists"));
        User user = User
                .builder()
                .username(cmd.username())
                .password(passwordEncoder.encode(cmd.password()))
                .name(cmd.name())
                .email(cmd.email())
                .roles(Set.of(Role.USER))
                .build();
        return userRepository.save(user);
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
    @PreAuthorize("hasRole('ADMIN') or #cmd.username().equalsIgnoreCase(authentication.name)")
    public UserResult update(@Valid UpdateUserCommand cmd) {
        String username = cmd.username();
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.user.not-found.username", username)));

        userMapper.updateUserFromCommand(cmd, user);

        User saved = userRepository.save(user);

        return userMapper.toResult(saved);
    }

    /**
     * 사용자 삭제
     *
     * @param username 삭제할 사용자명
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #username.equalsIgnoreCase(authentication.name)")
    public void delete(@NotBlank String username) {
        User existing = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.user.not-found.username", username)));

        userRepository.delete(existing);
    }

    /**
     * 마지막 로그인 시간 업데이트
     *
     * @param userId 로그인한 사용자 ID
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public void updateLastLogin(@NotBlank String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.user.not-found.username", username)));
        user.recordLastLogin();
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
    @PreAuthorize("hasRole('ADMIN') or #cmd.username().equalsIgnoreCase(authentication.name)")
    public void updatePassword(@Valid UpdatePasswordCommand cmd) {
        User user = userRepository
                .findByUsername(cmd.username())
                .orElseThrow(() -> new ResourceNotFoundException(MessageUtils.get("error.user.not-found.username", cmd.username())));

        // 현재 패스워드 확인
        if (!passwordEncoder.matches(cmd.currentPassword(), user.getPassword())) {
            log.warn(MessageUtils.get("log.user.password.change.failed", cmd.username()));
            throw new UnauthorizedException(MessageUtils.get("error.user.password.current.mismatch"));
        }

        // 새 패스워드 설정
        user.changePassword(passwordEncoder.encode(cmd.newPassword()));
        userRepository.save(user);
    }

}
