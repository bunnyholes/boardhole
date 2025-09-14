package bunny.boardhole.shared.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 새 비밀번호와 확인 비밀번호 일치 검증
 * 대상 타입은 다음 두 개의 접근자 메서드를 가져야 합니다:
 * - String newPassword()
 * - String confirmPassword()
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = PasswordConfirmedValidator.class)
public @interface PasswordConfirmed {
    String message() default "{error.user.password.confirm.mismatch}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

