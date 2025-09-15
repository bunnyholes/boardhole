package bunny.boardhole.shared.validation;

import java.lang.reflect.Method;
import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {
    private String[] fields;

    @Override
    public void initialize(FieldsMatch constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (fields == null || fields.length < 2) return true; // 잘못된 설정은 통과 처리

        try {
            Object first = read(value, fields[0]);
            for (int i = 1; i < fields.length; i++) {
                Object next = read(value, fields[i]);
                if (!Objects.equals(first, next)) return false;
            }
            return true;
        } catch (ReflectiveOperationException e) {
            // 대상이 접근자를 제공하지 않으면 해당 검증은 건너뜀
            return true;
        }
    }

    private Object read(Object bean, String accessor) throws ReflectiveOperationException {
        Method m = bean.getClass().getMethod(accessor);
        return m.invoke(bean);
    }
}

