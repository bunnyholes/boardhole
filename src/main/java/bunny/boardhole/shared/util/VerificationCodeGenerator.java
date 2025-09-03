package bunny.boardhole.shared.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 검증 코드 생성 유틸리티
 * SecurityProperties에서 설정된 문자셋과 길이를 사용하여 검증 코드 생성
 */
@Component
public class VerificationCodeGenerator {

    @Value("${boardhole.security.verification-code.charset}")
    private String charset;

    @Value("${boardhole.security.verification-code.length}")
    private int length;

    private final SecureRandom random = new SecureRandom();

    /**
     * 검증 코드 생성
     * application.yml에 설정된 문자셋과 길이를 사용
     *
     * @return 생성된 검증 코드
     */
    public String generate() {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) code.append(charset.charAt(random.nextInt(charset.length())));
        return code.toString();
    }
}