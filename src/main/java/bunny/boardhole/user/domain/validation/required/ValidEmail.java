package bunny.boardhole.user.domain.validation.required;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

/**
 * 사용자 이메일 필수 검증을 위한 커스텀 어노테이션입니다.
 * 
 * <p>다음 조건들을 만족해야 합니다:
 * <ul>
 *   <li>필수값 검증: null, 빈 문자열, 공백 문자만 포함된 문자열 허용하지 않음</li>
 *   <li>이메일 형식 검증: 표준 RFC 이메일 형식 준수</li>
 *   <li>길이 제한: 최대 255자까지 허용</li>
 * </ul>
 * 
 * @see ValidationConstants#USER_EMAIL_MAX_LENGTH
 * @since 1.0
 * @author BoardHole Development Team
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{user.validation.email.required}")
@Email(message = "{user.validation.email.format}")
@Size(max = ValidationConstants.USER_EMAIL_MAX_LENGTH, message = "{user.validation.email.size}")
@Constraint(validatedBy = {})
public @interface ValidEmail {
    String message() default "{user.validation.email.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidEmail[] value();
    }
}