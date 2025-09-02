package bunny.boardhole.user.infrastructure;

import bunny.boardhole.user.domain.EmailVerification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 이메일 인증 데이터 접근 리포지토리
 * 이메일 인증 엔티티에 대한 CRUD 작업 및 인증 검증, 만료된 데이터 정리 기능을 제공합니다.
 * 
 * <p>주요 기능:</p>
 * <ul>
 *   <li>유효한 인증 정보 조회 및 검증</li>
 *   <li>만료된 인증 정보 자동 정리</li>
 *   <li>사용자별 인증 정보 무효화</li>
 * </ul>
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {

    /**
     * 사용자 ID와 코드로 유효한 검증 정보 조회
     * JPQL을 사용하여 사용되지 않았고 만료되지 않은 인증 정보만 조회합니다.
     *
     * @param userIdentifier 사용자 ID
     * @param verificationCode 검증 코드
     * @param currentTime 현재 시간 (만료 체크용)
     * @return 유효한 검증 정보 (Optional)
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.userId = :userId AND ev.code = :code AND ev.used = false AND ev.expiresAt > :now")
    Optional<EmailVerification> findValidVerification(@Param("userId") final Long userIdentifier, @Param("code") final String verificationCode, @Param("now") final LocalDateTime currentTime);

    /**
     * 만료된 검증 정보 삭제
     * JPQL DELETE 쿼리를 사용하여 만료되었거나 이미 사용된 인증 정보를 일괄 삭제합니다.
     * 정기적인 데이터 정리를 위해 사용됩니다.
     *
     * @param currentTime 현재 시간 (만료 기준)
     * @return 삭제된 행 수
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now OR ev.used = true")
    int deleteExpiredVerifications(@Param("now") final LocalDateTime currentTime);

    /**
     * 특정 사용자의 미사용 검증 정보 무효화
     * JPQL UPDATE 쿼리를 사용하여 특정 사용자의 모든 미사용 인증 정보를 무효화합니다.
     * 새로운 인증 코드 발급 시 기존 코드들을 무효화하는 용도로 사용됩니다.
     *
     * @param userIdentifier 사용자 ID
     * @return 업데이트된 행 수
     */
    @Modifying
    @Transactional
    @Query("UPDATE EmailVerification ev SET ev.used = true WHERE ev.userId = :userId AND ev.used = false")
    int invalidateUserVerifications(@Param("userId") final Long userIdentifier);
}