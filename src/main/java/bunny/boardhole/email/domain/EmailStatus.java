package bunny.boardhole.email.domain;

/** 이메일 발송 상태 */
public enum EmailStatus {
  /** 재시도 대기 중 */
  PENDING,

  /** 처리 중 */
  PROCESSING,

  /** 발송 완료 */
  SENT,

  /** 최종 실패 (최대 재시도 횟수 초과) */
  FAILED
}
