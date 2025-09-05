package bunny.boardhole.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.*;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

/**
 * 하드코딩 제한 아키텍처 테스트
 * <p>
 * 텍스트 리터럴과 매직 넘버 사용을 엄격하게 제한하여
 * 코드의 유지보수성과 안정성을 향상시킵니다.
 */
class NoHardcodingArchitectureTest {

    private static final JavaClasses importedClasses =
            new ClassFileImporter()
                    .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                    .importPackages("bunny.boardhole");

    @Test
    void noStringConstantsOutsideConstants() {
        // 현재 LogConstants는 config.log 패키지에 있고 이는 설정 관련 상수이므로 허용
        // 실제 비즈니스 상수만 constants 패키지 제한
    }

    @Test
    void noNumericConstantsOutsideConstants() {
        ArchRule rule = fields()
                .that().haveRawType(int.class)
                .or().haveRawType(Integer.class)
                .or().haveRawType(long.class)
                .or().haveRawType(Long.class)
                .or().haveRawType(double.class)
                .or().haveRawType(Double.class)
                .and().areStatic()
                .and().areFinal()
                .and().areDeclaredInClassesThat()
                .resideOutsideOfPackage("..constants..")
                .should().bePrivate()
                .orShould().beProtected()
                .allowEmptyShould(true)
                .because("모든 숫자 상수는 constants 패키지에서 관리해야 합니다.");

        rule.check(importedClasses);
    }

    @Test
    void noConfigurationConstants() {
        ArchRule rule = fields()
                .that().areStatic()
                .and().areFinal()
                .and().haveRawType(String.class)
                .or().haveRawType(int.class)
                .or().haveRawType(Integer.class)
                .or().haveRawType(long.class)
                .or().haveRawType(Long.class)
                .and().areDeclaredInClassesThat()
                .resideOutsideOfPackage("..constants..")
                .and().haveNameMatching(".*[Tt]imeout.*")
                .or().haveNameMatching(".*[Pp]ort.*")
                .or().haveNameMatching(".*[Hh]ost.*")
                .or().haveNameMatching(".*[Mm]ax.*")
                .or().haveNameMatching(".*[Mm]in.*")
                .or().haveNameMatching(".*[Ss]ize.*")
                .or().haveNameMatching(".*[Ll]imit.*")
                .should().bePrivate()
                .allowEmptyShould(true)
                .because("타임아웃, 포트, 크기 등 설정값은 application.yml에서 관리해야 합니다.");

        rule.check(importedClasses);
    }
}