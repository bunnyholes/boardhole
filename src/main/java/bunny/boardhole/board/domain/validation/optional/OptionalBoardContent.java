package bunny.boardhole.board.domain.validation.optional;

import bunny.boardhole.shared.constants.ValidationConstants;
import jakarta.validation.*;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 게시글 내용 선택적 검증을 위한 커스텀 어노테이션입니다.
 * 
 * <p>다음 조건들을 만족해야 합니다:
 * <ul>
 *   <li>null 값 허용: 선택적 필드로 빈 값 가능</li>
 *   <li>길이 제한: 값이 있을 경우 최대 10,000자까지 허용</li>
 * </ul>
 * 
 * @see ValidationConstants#BOARD_CONTENT_MAX_LENGTH
 * @since 1.0
 * @author BoardHole Development Team
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = ValidationConstants.BOARD_CONTENT_MAX_LENGTH, message = "{board.validation.content.size}")
@Constraint(validatedBy = {})
public @interface OptionalBoardContent {
    /**
     * 검증 실패 시 반환될 메시지를 정의합니다.
     * @return 검증 오류 메시지 (기본값: "{board.validation.content.invalid}")
     */
    String message() default "{board.validation.content.invalid}";

    /**
     * 검증 그룹을 정의합니다.
     * @return 검증 그룹 배열 (기본값: 빈 배열)
     */
    Class<?>[] groups() default {};

    /**
     * 검증 페이로드를 정의합니다.
     * @return 페이로드 타입 배열 (기본값: 빈 배열)
     */
    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        /**
         * 동일한 요소에 여러 OptionalBoardContent 어노테이션을 적용할 때 사용됩니다.
         * @return OptionalBoardContent 어노테이션 배열
         */
        OptionalBoardContent[] value();
    }
}