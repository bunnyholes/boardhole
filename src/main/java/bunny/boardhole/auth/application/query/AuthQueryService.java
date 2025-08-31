package bunny.boardhole.auth.application.query;

import bunny.boardhole.auth.application.dto.AuthResult;
import bunny.boardhole.common.exception.ResourceNotFoundException;
import bunny.boardhole.common.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * 인증 쿼리 서비스
 * CQRS 패턴의 Query 측면으로 현재 사용자 정보 조회 등 읽기 작업을 담당합니다.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AuthQueryService {

    private final UserRepository userRepository;
    private final MessageUtils messageUtils;

    /**
     * 현재 인증된 사용자 정보 조회
     *
     * @param query 현재 사용자 조회 쿼리
     * @return 사용자 인증 정보
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public AuthResult getCurrentUser(@Valid GetCurrentUserQuery query) {
        Long userId = query.userId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageUtils.getMessage("error.user.not-found.id", userId)
                ));

        log.debug("Retrieved current user info for userId: {}", userId);

        return new AuthResult(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getRoles().iterator().next().name(), // 첫 번째 Role 사용
                true // 조회되었다면 인증된 상태
        );
    }
}