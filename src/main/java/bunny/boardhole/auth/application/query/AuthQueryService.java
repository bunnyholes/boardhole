package bunny.boardhole.auth.application.query;

import bunny.boardhole.auth.application.result.*;
import bunny.boardhole.shared.exception.*;
import bunny.boardhole.shared.security.AppUserPrincipal;
import bunny.boardhole.shared.util.MessageUtils;
import bunny.boardhole.user.domain.User;
import bunny.boardhole.user.infrastructure.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 인증 조회 서비스
 * CQRS 패턴의 Query 측면으로 인증 관련 조회 전용 비지니스 로직을 담당합니다.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class AuthQueryService {

    private final UserRepository userRepository;
    private final MessageUtils messageUtils;
    private final AuthHistoryMockDataProvider mockDataProvider;

    @Value("${boardhole.auth.default-role}")
    private String defaultRole;

    /**
     * 현재 인증 정보 조회
     *
     * @param query 현재 인증 정보 조회 쿼리
     * @return 현재 인증 정보 결과
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws UnauthorizedException     인증되지 않은 경우
     */
    @Transactional(readOnly = true)
    @NonNull
    public AuthenticationResult getCurrentAuthentication(@Valid @NonNull GetCurrentAuthenticationQuery query) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(messageUtils.getMessage("error.auth.not-authenticated"));
        }

        // 사용자 정보 조회
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", query.userId())));

        // 세션 ID 추출 (가능한 경우)
        String sessionId = authentication.getDetails() != null ?
                authentication.getDetails().toString() : "N/A";

        log.info(messageUtils.getMessage("log.auth.query-current", user.getUsername(), user.getId()));

        // 안전한 Role 접근
        String roleName = user.getRoles().isEmpty() ?
                defaultRole : user.getRoles().iterator().next().name();

        return new AuthenticationResult(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                roleName,
                true,
                sessionId
        );
    }

    /**
     * 토큰 검증
     *
     * @param query 토큰 검증 쿼리
     * @return 토큰 검증 결과
     */
    @Transactional(readOnly = true)
    @NonNull
    public TokenValidationResult validateToken(@Valid @NonNull ValidateTokenQuery query) {
        try {
            // 현재 Spring Security 컨텍스트에서 인증 정보 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return new TokenValidationResult(false, null, null, "Not authenticated");
            }

            if (authentication.getPrincipal() instanceof AppUserPrincipal(User user)) {

                log.info(messageUtils.getMessage("log.auth.token-validated", user.getUsername()));

                return new TokenValidationResult(
                        true,
                        user.getId(),
                        user.getUsername(),
                        null
                );
            }

            return new TokenValidationResult(false, null, null, "Invalid principal type");

        } catch (Exception e) {
            log.warn(messageUtils.getMessage("log.auth.token-validation-failed"), e);
            return new TokenValidationResult(false, null, null, e.getMessage());
        }
    }

    /**
     * 인증 이력 조회 (선택적 기능)
     * <p>
     * 현재 구현에서는 실제 이력 테이블이 없으므로 모의 데이터를 반환합니다.
     * 실제 운영 환경에서는 별도의 이력 테이블과 Repository가 필요합니다.
     *
     * @param query 인증 이력 조회 쿼리
     * @return 인증 이력 페이지
     */
    @Transactional(readOnly = true)
    @NonNull
    public Page<AuthenticationHistoryResult> getAuthenticationHistory(@Valid @NonNull GetAuthenticationHistoryQuery query) {
        // 사용자 존재 확인
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new ResourceNotFoundException(messageUtils.getMessage("error.user.not-found.id", query.userId())));

        // 현재 구현에서는 모의 이력 데이터 반환
        // 실제 환경에서는 AuthenticationHistory 엔티티와 Repository가 필요
        List<AuthenticationHistoryResult> mockHistory = mockDataProvider.generateMockHistory(user);

        log.info(messageUtils.getMessage("log.auth.history-queried", user.getUsername(), mockHistory.size()));

        return new PageImpl<>(mockHistory, query.pageable(), mockHistory.size());
    }
}