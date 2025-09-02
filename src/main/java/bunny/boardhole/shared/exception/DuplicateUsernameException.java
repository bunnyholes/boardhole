package bunny.boardhole.shared.exception;

import lombok.experimental.StandardException;

/**
 * 중복된 사용자명 충돌 시 발생하는 예외 클래스입니다.
 * 사용자 등록이나 사용자명 변경 시 이미 존재하는 사용자명이 입력되었을 때 발생합니다.
 * 
 * @author 시스템 개발팀
 * @version 1.0
 * @since 1.0.0
 * @see ConflictException
 */
@StandardException
public class DuplicateUsernameException extends ConflictException {
    
    /** 
     * 직렬화 버전 UID입니다.
     * 클래스 구조 변경 시 반드시 업데이트해야 합니다.
     */
    private static final long serialVersionUID = 1L;
}