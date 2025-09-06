package bunny.boardhole.email.infrastructure;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import bunny.boardhole.email.domain.EmailOutbox;
import bunny.boardhole.email.domain.EmailStatus;

/**
 * EmailOutbox JPA Repository
 */
@Repository
public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {

    /**
     * 재시도 가능한 이메일 조회 상태가 PENDING이고 재시도 시간이 현재 시간 이전인 이메일
     *
     * @param status 이메일 상태
     * @param now    현재 시간
     * @return 재시도 가능한 이메일 목록
     */
    List<EmailOutbox> findByStatusAndNextRetryAtBeforeOrNextRetryAtIsNull(EmailStatus status, LocalDateTime now);

    /**
     * 특정 상태의 이메일 개수 조회
     *
     * @param status 이메일 상태
     * @return 해당 상태의 이메일 개수
     */
    long countByStatus(EmailStatus status);

    /**
     * 오래된 완료/실패 이메일 삭제
     *
     * @param statuses 삭제 대상 상태 목록
     * @param before   기준 날짜
     * @return 삭제된 레코드 수
     */
    @Modifying
    @Query("DELETE FROM EmailOutbox e WHERE e.status IN :statuses AND e.createdAt < :before")
    int deleteOldEmails(@Param("statuses") List<EmailStatus> statuses, @Param("before") LocalDateTime before);

    /**
     * 특정 수신자의 대기 중인 이메일 존재 여부 확인
     *
     * @param recipientEmail 수신자 이메일
     * @param status         이메일 상태
     * @return 존재 여부
     */
    boolean existsByRecipientEmailAndStatus(String recipientEmail, EmailStatus status);
}
