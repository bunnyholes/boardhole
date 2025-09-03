package bunny.boardhole.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * 컨트롤러 아키텍처 규칙 테스트
 * <p>
 * 컨트롤러에서 ResponseEntity 사용을 금지하고,
 *
 * @ResponseStatus 애노테이션과 ResponseBodyAdvice 패턴을 강제합니다.
 */
class ControllerArchitectureTest {

    private static final JavaClasses importedClasses =
            new ClassFileImporter().importPackages("bunny.boardhole");

    @Test
    void controllersCannotReturnResponseEntity() {
        ArchRule rule = methods()
                .that().areDeclaredInClassesThat()
                .areAnnotatedWith(Controller.class)
                .or().areAnnotatedWith(RestController.class)
                .should().notHaveRawReturnType(ResponseEntity.class)
                .because("컨트롤러에서는 @ResponseStatus 애노테이션과 ResponseBodyAdvice를 사용해야 합니다. " +
                        "ResponseEntity를 직접 반환하면 응답 처리 로직이 분산되어 일관성이 떨어집니다.")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    void controllersCannotDependOnResponseEntity() {
        ArchRule rule = methods()
                .that().areDeclaredInClassesThat()
                .areAnnotatedWith(Controller.class)
                .or().areAnnotatedWith(RestController.class)
                .should().notHaveRawReturnType(ResponseEntity.class)
                .allowEmptyShould(true)
                .because("컨트롤러에서는 ResponseEntity 대신 @ResponseStatus와 ResponseBodyAdvice 패턴을 사용해야 합니다.");

        rule.check(importedClasses);
    }
}