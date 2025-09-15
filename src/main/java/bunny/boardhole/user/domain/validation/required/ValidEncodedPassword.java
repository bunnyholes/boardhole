package bunny.boardhole.user.domain.validation.required;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import bunny.boardhole.user.domain.validation.UserValidationConstants;

/**
 * 인코딩된 비밀번호 검증 애너테이션
 * BCrypt로 인코딩된 비밀번호 형식을 검증
 * - 필수값 검증
 * - BCrypt 해시 패턴 검증 ($2a$, $2b$, $2y$로 시작하는 60자)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "{validation.user.password.required}")
@Pattern(regexp = UserValidationConstants.ENCODED_PASSWORD_BCRYPT_PATTERN, message = "{validation.user.password.encoded}")
@Constraint(validatedBy = {})
public @interface ValidEncodedPassword {
    String message() default "{validation.user.password.invalid.encoded}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        ValidEncodedPassword[] value();
    }
}
