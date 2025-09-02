package bunny.boardhole.shared.util;

import bunny.boardhole.shared.config.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 검증 코드 생성 유틸리티 클래스입니다.
 * SecurityProperties에서 설정된 문자셋과 길이를 사용하여 보안 검증 코드를 생성합니다.
 * SecureRandom을 사용하여 암호학적으로 안전한 난수를 생성합니다.
 * 
 * <p>이 클래스는 다음과 같은 보안 기능을 제공합니다:</p>
 * <ul>
 *   <li>암호학적으로 안전한 난수 생성 (SecureRandom 사용)</li>
 *   <li>설정 가능한 문자셋과 코드 길이</li>
 *   <li>이메일 인증, 비밀번호 재설정 등에 사용</li>
 * </ul>
 * 
 * <p>주의사항:</p>
 * <ul>
 *   <li>생성된 코드는 일회성으로 사용해야 합니다.</li>
 *   <li>코드는 적절한 만료 시간을 설정해야 합니다.</li>
 *   <li>보안상 중요한 용도로만 사용해야 합니다.</li>
 * </ul>
 *
 * @author BoardHole Development Team
 * @version 1.0
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class VerificationCodeGenerator {
    
    /** 보안 관련 설정 정보 */
    private final SecurityProperties securityProperties;
    
    /** 암호학적으로 안전한 난수 생성기 */
    private final SecureRandom random = new SecureRandom();

    /**
     * 보안 검증 코드를 생성합니다.
     * application.yml에 설정된 문자셋과 길이를 사용하여 SecureRandom으로 안전하게 생성합니다.
     * 이메일 인증, 비밀번호 재설정 등에서 사용됩니다.
     * 
     * <p>생성 과정:</p>
     * <ol>
     *   <li>SecurityProperties에서 문자셋과 길이 정보를 조회</li>
     *   <li>SecureRandom을 사용하여 각 자리수별로 문자를 무작위 선택</li>
     *   <li>설정된 길이만큼 문자를 조합하여 최종 코드 생성</li>
     * </ol>
     * 
     * <p>보안 특징:</p>
     * <ul>
     *   <li>암호학적으로 안전한 SecureRandom 사용</li>
     *   <li>예측 불가능한 난수 시드</li>
     *   <li>설정 가능한 문자셋으로 복잡도 조절</li>
     * </ul>
     *
     * @return 생성된 검증 코드 문자열 (설정된 길이와 문자셋 기준)
     * @throws IllegalStateException SecurityProperties 설정이 올바르지 않을 경우
     */
    public String generate() {
        final var config = securityProperties.getVerificationCode();
        final String charset = config.getCharset();
        final int length = config.getLength();

        final StringBuilder code = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            code.append(charset.charAt(random.nextInt(charset.length())));
        }
        return code.toString();
    }
}