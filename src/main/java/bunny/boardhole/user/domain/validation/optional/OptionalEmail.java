package bunny.boardhole.user.domain.validation.optional;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.*;

import java.lang.annotation.*;

/**
 * 사용자 이메일 선택적 검증을 위한 커스텀 어노테이션입니다.
 * null 값 허용하는 선택적 필드로, 값이 있을 경우 RFC 이메일 형식을 준수해야 합니다.
 * 
 * @see ValidationConstants#USER_EMAIL_MAX_LENGTH
 * @since 1.0
 * @author BoardHole Development Team
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Email(message = "{user.validation.email.format}")
@Size(max = ValidationConstants.USER_EMAIL_MAX_LENGTH, message = "{user.validation.email.size}")
@Constraint(validatedBy = {})
public @interface OptionalEmail {
    /**
     * 검증 실패 시 반환될 기본 메시지를 반환합니다.
     * 
     * @return 기본 검증 오류 메시지
     */
    String message() default "{user.validation.email.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OptionalEmail[] value();
    }
}