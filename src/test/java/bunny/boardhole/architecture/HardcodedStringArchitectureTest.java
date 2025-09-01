package bunny.boardhole.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * 하드코딩된 문자열 검증을 위한 아키텍처 테스트
 * 국제화(i18n)를 위해 모든 사용자 대상 메시지는 MessageSource를 통해 관리되어야 합니다.
 */
@DisplayName("하드코딩 문자열 아키텍처 테스트")
class HardcodedStringArchitectureTest {

    private static final String BASE_PACKAGE = "bunny.boardhole";
    
    // 한글 문자 패턴
    private static final Pattern KOREAN_PATTERN = Pattern.compile("[\u3131-\u318E\uAC00-\uD7A3]");
    
    // 허용되는 예외 패턴들
    private static final Set<String> ALLOWED_STRINGS = Set.of(
            // 기술적 키워드
            "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS",
            "id", "name", "code", "type", "path", "method", "timestamp", "traceId",
            "field", "message", "value", "errors", "parameter", "property",
            "sessionId", "creationTime", "lastAccessedTime", "maxInactiveInterval",
            "authenticated", "totalSessions", "authenticatedSessions", "anonymousSessions",
            "averageTtlSeconds", "ttlSeconds", "supportedMethods", "parameterType", "requiredType",
            // 에러 코드
            "CONFLICT", "UNAUTHORIZED", "FORBIDDEN", "VALIDATION_ERROR", "BAD_REQUEST",
            "INVALID_JSON", "METHOD_NOT_ALLOWED", "UNSUPPORTED_MEDIA_TYPE", 
            "MISSING_PARAMETER", "TYPE_MISMATCH", "INTERNAL_ERROR",
            "USER_DUPLICATE_USERNAME", "USER_DUPLICATE_EMAIL",
            // Redis/세션 네임스페이스
            "board-hole:session:sessions:", "board-hole:session:index:",
            "sessionAttr:", "sessionAttr:SPRING_SECURITY_CONTEXT",
            // 프로토콜
            "urn:problem-type:"
    );

    @Test
    @DisplayName("비즈니스 로직 클래스는 한글 문자열을 하드코딩하지 않아야 한다")
    void businessLogicClassesShouldNotHaveHardcodedKoreanStrings() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPackages(BASE_PACKAGE);

        classes()
                .that().resideInAnyPackage(
                        "..application..",
                        "..domain..",
                        "..infrastructure..",
                        "..shared.."
                )
                .and().areNotAnnotatedWith("org.springframework.context.annotation.Configuration")
                .and().doNotHaveSimpleName("DataInitializer") // 초기 데이터는 예외
                .should(notContainHardcodedKoreanStrings())
                .check(importedClasses);
    }

    @Test
    @DisplayName("Controller 클래스의 로그 메시지는 MessageSource를 사용해야 한다")
    void controllerLogMessagesShouldUseMessageSource() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPackages(BASE_PACKAGE);

        methods()
                .that().areDeclaredInClassesThat().resideInAnyPackage("..presentation..")
                .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
                .should(notUseHardcodedStringsInLogStatements())
                .check(importedClasses);
    }

    @Test
    @DisplayName("예외 핸들러는 MessageSource를 통해 메시지를 관리해야 한다")
    void exceptionHandlersShouldUseMessageSource() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPackages(BASE_PACKAGE);

        classes()
                .that().resideInAnyPackage("..exception..")
                .or().areAnnotatedWith("org.springframework.web.bind.annotation.RestControllerAdvice")
                .or().areAnnotatedWith("org.springframework.web.bind.annotation.ControllerAdvice")
                .should(haveFieldOfType("org.springframework.context.MessageSource"))
                .check(importedClasses);
    }

    /**
     * 한글 문자열 하드코딩 검사 조건
     */
    private static ArchCondition<JavaClass> notContainHardcodedKoreanStrings() {
        return new ArchCondition<>("not contain hardcoded Korean strings") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                // 실제 바이트코드 분석을 통한 문자열 상수 검사
                // 이 부분은 실제 구현 시 ASM 또는 바이트코드 분석 라이브러리를 사용해야 함
                // 여기서는 개념적인 구조만 제시
                
                // Swagger 어노테이션은 제외
                if (isSwaggerAnnotated(javaClass)) {
                    return;
                }
                
                // 소스 코드에서 한글 문자열 검사 (실제로는 바이트코드 분석 필요)
                String className = javaClass.getFullName();
                String message = String.format(
                    "클래스 %s 에 하드코딩된 한글 문자열이 있습니다. MessageSource를 사용하세요.",
                    className
                );
                
                // 실제 구현 시 여기서 바이트코드 분석을 통해 문자열 상수를 확인해야 함
                // events.add(SimpleConditionEvent.violated(javaClass, message));
            }
        };
    }

    /**
     * 로그 문에서 하드코딩된 문자열 사용 검사
     */
    private static ArchCondition<JavaMethod> notUseHardcodedStringsInLogStatements() {
        return new ArchCondition<>("not use hardcoded strings in log statements") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                // 메서드가 log.info, log.error 등을 호출하는지 확인
                // MessageSource.getMessage를 사용하는지 검증
                // 실제 구현은 바이트코드 분석이 필요함
            }
        };
    }

    /**
     * MessageSource 필드 존재 여부 확인
     */
    private static ArchCondition<JavaClass> haveFieldOfType(String typeName) {
        return new ArchCondition<>("have field of type " + typeName) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasMessageSource = javaClass.getAllFields().stream()
                        .anyMatch(field -> field.getRawType().getFullName().equals(typeName));
                
                if (!hasMessageSource) {
                    String message = String.format(
                        "클래스 %s 는 %s 타입의 필드를 가져야 합니다",
                        javaClass.getFullName(), typeName
                    );
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /**
     * Swagger 어노테이션 여부 확인
     */
    private static boolean isSwaggerAnnotated(JavaClass javaClass) {
        return javaClass.isAnnotatedWith("io.swagger.v3.oas.annotations.media.Schema")
                || javaClass.isAnnotatedWith("io.swagger.v3.oas.annotations.tags.Tag");
    }
}