package bunny.boardhole.user.infrastructure;

import bunny.boardhole.user.domain.EmailVerification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, String> {

    /**
     * 사용자 ID와 코드로 유효한 검증 정보 조회
     *
     * @param userId 사용자 ID
     * @param code   검증 코드
     * @return 검증 정보
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.userId = :userId AND ev.code = :code AND ev.used = false AND ev.expiresAt > :now")
    Optional<EmailVerification> findValidVerification(@Param("userId") Long userId, @Param("code") String code, @Param("now") LocalDateTime now);

    /**
     * 만료된 검증 정보 삭제
     *
     * @param now 현재 시간
     * @return 삭제된 행 수
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now OR ev.used = true")
    int deleteExpiredVerifications(@Param("now") LocalDateTime now);

    /**
     * 특정 사용자의 미사용 검증 정보 무효화
     *
     * @param userId 사용자 ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE EmailVerification ev SET ev.used = true WHERE ev.userId = :userId AND ev.used = false")
    void invalidateUserVerifications(@Param("userId") Long userId);

    /**
     * 코드로 미사용 검증 정보 조회
     *
     * @param code 검증 코드
     * @return 검증 정보
     */
    Optional<EmailVerification> findByCodeAndUsedFalse(String code);

    /**
     * 사용자 ID로 미사용 검증 정보 목록 조회
     *
     * @param userId 사용자 ID
     * @return 검증 정보 목록
     */
    List<EmailVerification> findByUserIdAndUsedFalse(Long userId);
}