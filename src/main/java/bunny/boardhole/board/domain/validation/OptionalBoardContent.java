package bunny.boardhole.board.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 게시글 내용 검증 애너테이션 (선택적 필드)
 * - null 허용
 * - 값이 있을 경우 최대 10,000자 제한
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = 10000, message = "{board.validation.content.size}")
@Constraint(validatedBy = {})
public @interface OptionalBoardContent {
    String message() default "{board.validation.content.invalid}";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        OptionalBoardContent[] value();
    }
}