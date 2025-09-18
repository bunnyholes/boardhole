package bunny.boardhole.shared.validation;

import java.lang.reflect.Field;
import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.extern.slf4j.Slf4j;

/**
 * 비밀번호와 비밀번호 확인 필드가 일치하는지 검증합니다.
 * 클래스 레벨 검증이지만 confirmPassword 필드의 에러로 등록하여
 * aria-invalid 속성이 올바르게 적용되도록 합니다.
 */
@Slf4j
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        // 초기화할 내용 없음
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null)
            return true;

        try {
            Object passwordValue = getFieldValue(value, "password");
            Object confirmPasswordValue = getFieldValue(value, "confirmPassword");

            // 비밀번호가 일치하지 않으면 confirmPassword 필드 에러로 등록
            if (!Objects.equals(passwordValue, confirmPasswordValue)) {
                // 기본 에러 메시지 비활성화
                context.disableDefaultConstraintViolation();

                // confirmPassword 필드의 에러로 등록
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                       .addPropertyNode("confirmPassword")
                       .addConstraintViolation();

                return false;
            }

            return true;

        } catch (Exception e) {
            log.warn("PasswordMatch 검증 중 오류 발생: {}", e.getMessage());
            return true; // 오류 시 검증 통과 (다른 검증이 담당)
        }
    }

    private static Object getFieldValue(Object bean, String fieldName) throws ReflectiveOperationException {
        Field field = bean.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(bean);
    }
}