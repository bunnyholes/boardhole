package bunny.boardhole.auth.application.query;

import bunny.boardhole.auth.application.result.AuthenticationHistoryResult;
import bunny.boardhole.user.domain.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 인증 이력 모의 데이터 제공자
 * 실제 인증 이력 테이블이 구현되기 전까지 사용하는 임시 모의 데이터 제공
 */
@Component
public class AuthHistoryMockDataProvider {

    /**
     * 사용자의 모의 인증 이력 데이터 생성
     *
     * @param user 대상 사용자
     * @return 모의 인증 이력 목록
     */
    public List<AuthenticationHistoryResult> generateMockHistory(User user) {
        return List.of(
                new AuthenticationHistoryResult(
                        1L,
                        user.getId(),
                        user.getUsername(),
                        "LOGIN",
                        LocalDateTime.now().minusHours(2),
                        "192.168.1.100",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                ),
                new AuthenticationHistoryResult(
                        2L,
                        user.getId(),
                        user.getUsername(),
                        "LOGOUT",
                        LocalDateTime.now().minusHours(1),
                        "192.168.1.100",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                )
        );
    }
}