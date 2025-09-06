package bunny.boardhole.shared.util;

import java.util.UUID;

import lombok.experimental.UtilityClass;

/**
 * 토큰 생성 유틸리티
 * 인증 토큰, 세션 토큰 등 UUID 기반 토큰 생성을 위한 공통 유틸리티
 */
@UtilityClass
public class TokenGenerator {

    /**
     * UUID 기반 토큰 생성
     * 이메일 인증, 패스워드 재설정 등에 사용
     *
     * @return 생성된 UUID 토큰
     */
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}