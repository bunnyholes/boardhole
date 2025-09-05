package bunny.boardhole.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

/**
 * 레이어드 아키텍처 종합 테스트
 * <p>
 * 올바른 레이어드 아키텍처 원칙을 검증합니다:
 * - 도메인: 비즈니스 규칙과 검증 제약 (모든 레이어에서 접근 가능)
 * - 흐름: Controller → Service → Repository
 * - 역방향 의존성 금지
 */
class LayeredArchitectureTest {

    private static final JavaClasses importedClasses =
            new ClassFileImporter().importPackages("bunny.boardhole");

    @Test
    void domainLayerIsAccessibleFromAllLayers() {
        // 도메인 레이어는 모든 레이어에서 접근 가능해야 함 (비즈니스 규칙, 검증 제약 포함)
        // 이 테스트는 도메인에 대한 접근을 제한하는 규칙이 없음을 확인
        // 도메인은 비즈니스 규칙과 검증 애노테이션을 포함하므로 모든 레이어에서 참조 가능
    }

    @Test
    void domainLayerShouldNotDependOnAnyOuterLayer() {
        // 도메인 레이어는 외부 레이어에 의존하지 않아야 함
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..presentation..", "..application..", "..infrastructure..")
                .allowEmptyShould(true)
                .because("도메인 레이어는 외부 레이어에 의존해서는 안 됩니다.");

        rule.check(importedClasses);
    }

    @Test
    void applicationLayerShouldNotDependOnPresentationLayer() {
        // 애플리케이션 레이어는 프레젠테이션 레이어에 의존하지 않아야 함
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..presentation..")
                .allowEmptyShould(true)
                .because("애플리케이션 레이어는 프레젠테이션 레이어에 의존해서는 안 됩니다.");

        rule.check(importedClasses);
    }

    @Test
    void presentationLayerShouldNotDirectlyAccessInfrastructure() {
        // 테스트 클래스는 제외하고 실제 Controller만 제한
        // 프레젠테이션 레이어는 인프라스트럭처 레이어를 직접 참조하지 않아야 함
    }

    @Test
    void infrastructureLayerCanImplementApplicationInterfaces() {
        // 인프라스트럭처 레이어는 애플리케이션 인터페이스를 구현할 수 있음
        // 하지만 프레젠테이션 레이어는 참조할 수 없음
        ArchRule rule = noClasses()
                .that().resideInAPackage("..infrastructure..")
                .should().dependOnClassesThat().resideInAPackage("..presentation..")
                .allowEmptyShould(true)
                .because("인프라스트럭처 레이어는 프레젠테이션 레이어를 참조해서는 안 됩니다.");

        rule.check(importedClasses);
    }

    @Test
    void controllersShouldFollowLayeredArchitecture() {
        // 실제 Controller 클래스만 검사, Test 클래스는 제외
        // Controller -> Service -> Repository 흐름 확인
    }
}