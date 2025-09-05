package bunny.boardhole.architecture;

import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Lombok 사용 완전 강제 규칙 검증 테스트
 *
 * <p>주의: ArchUnit은 컴파일된 바이트코드를 검사하므로 Lombok이 생성한 메서드를 수동으로 작성한 메서드와 구분할 수 없습니다. 따라서 이 테스트는 비활성화되고,
 * 대신 Checkstyle과 PMD로 소스 레벨에서 검사합니다.
 *
 * <p>Lombok 강제는 다음과 같이 적용됩니다: - Checkstyle: 소스 코드 레벨에서 수동 getter/setter/toString/equals/hashCode 감지
 * - PMD: 정적 분석으로 boilerplate 코드 감지 - 빌드 시점에 즉시 위반 사항 차단
 */
@AnalyzeClasses(
        packages = "bunny.boardhole",
        importOptions = {
                com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class,
                com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeJars.class
        })
@org.junit.jupiter.api.Disabled("Lombok이 생성한 바이트코드와 수동 코드를 구분할 수 없음. @Builder 생성자는 비즈니스 로직용으로 허용")
class LombokRulesTest {

    /**
     * 도메인 엔티티는 반드시 Lombok @Getter를 사용해야 함
     */
    // @ArchTest - Lombok 바이트코드 구분 불가로 비활성화
    static final ArchRule domainEntitiesMustUseLombokGetter =
            classes()
                    .that()
                    .resideInAnyPackage("..domain..")
                    .and()
                    .areNotInterfaces()
                    .and()
                    .areNotEnums()
                    .and()
                    .areNotRecords()
                    .and()
                    .doNotHaveSimpleName("package-info")
                    .and()
                    .areNotInnerClasses()
                    .and()
                    .areNotAnnotatedWith("org.springframework.stereotype.Service")
                    .and()
                    .areNotAnnotatedWith("org.springframework.stereotype.Component")
                    .and()
                    .areNotAnnotatedWith("org.springframework.context.annotation.Configuration")
                    .should(haveLombokGetterOrNoGetterMethods())
                    .because("도메인 엔티티는 반드시 Lombok @Getter를 사용하거나 수동 getter가 없어야 합니다");

    /**
     * 모든 수동 getter 메서드 금지 (비즈니스 로직이 아닌 단순 getter)
     */
    // @ArchTest - Lombok 바이트코드 구분 불가로 비활성화
    static final ArchRule noManualGetters =
            methods()
                    .that()
                    .areDeclaredInClassesThat()
                    .resideInAnyPackage("bunny.boardhole..")
                    .and()
                    .arePublic()
                    .and()
                    .haveName(matchesGetterPattern())
                    .and()
                    .haveRawParameterTypes(new Class[0])
                    .and()
                    .areNotStatic()
                    .should(notBeSimpleGetter())
                    .because("단순 getter는 Lombok @Getter를 사용해야 합니다");

    /**
     * 모든 수동 setter 메서드 금지 (비즈니스 로직이 아닌 단순 setter)
     */
    // @ArchTest - Lombok 바이트코드 구분 불가로 비활성화
    static final ArchRule noManualSetters =
            methods()
                    .that()
                    .areDeclaredInClassesThat()
                    .resideInAnyPackage("bunny.boardhole..")
                    .and()
                    .arePublic()
                    .and()
                    .haveName(matchesSetterPattern())
                    .and()
                    .haveRawReturnType(void.class)
                    .and()
                    .haveRawParameterTypes(new Class[]{Object.class})
                    .and()
                    .areNotStatic()
                    .should(notBeSimpleSetter())
                    .because("단순 setter는 Lombok @Setter를 사용해야 합니다");

    /**
     * 수동 toString 메서드 금지
     */
    // @ArchTest - Lombok 바이트코드 구분 불가로 비활성화
    static final ArchRule noManualToString =
            methods()
                    .that()
                    .areDeclaredInClassesThat()
                    .resideInAnyPackage("bunny.boardhole..")
                    .and()
                    .haveName("toString")
                    .and()
                    .haveRawParameterTypes(new Class[0])
                    .and()
                    .haveRawReturnType(String.class)
                    .should(beInClassWithLombokToString())
                    .because("toString()은 Lombok @ToString을 사용해야 합니다");

    /**
     * 수동 equals/hashCode 메서드 금지
     */
    // @ArchTest - Lombok 바이트코드 구분 불가로 비활성화
    static final ArchRule noManualEqualsHashCode =
            methods()
                    .that()
                    .areDeclaredInClassesThat()
                    .resideInAnyPackage("bunny.boardhole..")
                    .and()
                    .haveName("equals")
                    .or()
                    .haveName("hashCode")
                    .should(beInClassWithLombokEqualsAndHashCode())
                    .because("equals/hashCode는 Lombok @EqualsAndHashCode를 사용해야 합니다");

    /**
     * 빈 생성자와 전체 필드 생성자만 금지 (비즈니스 로직이 있는 생성자는 허용)
     */
    // @ArchTest - Lombok 바이트코드 구분 불가로 비활성화
    static final ArchRule noManualConstructors =
            constructors()
                    .that()
                    .areDeclaredInClassesThat()
                    .resideInAnyPackage("..domain..", "..dto..", "..result..")
                    .and()
                    .arePublic()
                    .should(beAllowedConstructor())
                    .because("빈 생성자와 전체 필드 생성자는 Lombok을 사용하고, 비즈니스 로직이 있는 생성자는 허용됩니다");

    // Helper methods
    private static String matchesGetterPattern() {
        return "get[A-Z].*";
    }

    private static String matchesSetterPattern() {
        return "set[A-Z].*";
    }

    /**
     * Lombok @Getter가 있거나 수동 getter가 없어야 함
     */
    private static ArchCondition<JavaClass> haveLombokGetterOrNoGetterMethods() {
        return new ArchCondition<>("have Lombok @Getter or no getter methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasLombokGetter =
                        javaClass.isAnnotatedWith("lombok.Getter") || javaClass.isAnnotatedWith("lombok.Data");

                boolean hasManualGetter =
                        javaClass.getMethods().stream()
                                .anyMatch(
                                        m ->
                                                m.getName().matches("get[A-Z].*")
                                                        && m.getRawParameterTypes().isEmpty()
                                                        && !m.getModifiers().contains(JavaModifier.STATIC));

                if (!hasLombokGetter && hasManualGetter) {
                    String message =
                            String.format(
                                    "클래스 %s에 수동 getter가 있지만 @Getter 애노테이션이 없습니다", javaClass.getSimpleName());
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    /**
     * 단순 getter가 아니어야 함 (비즈니스 로직 포함 getter는 허용)
     */
    private static ArchCondition<JavaMethod> notBeSimpleGetter() {
        return new ArchCondition<>("not be simple getter") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                JavaClass owner = method.getOwner();

                // record, enum, Service, Configuration 등은 제외
                if (owner.isRecord()
                        || owner.isEnum()
                        || owner.isAnnotatedWith("org.springframework.stereotype.Service")
                        || owner.isAnnotatedWith("org.springframework.stereotype.Component")
                        || owner.isAnnotatedWith("org.springframework.context.annotation.Configuration")
                        || owner.isAnnotatedWith("org.springframework.stereotype.Controller")
                        || owner.isAnnotatedWith("org.springframework.web.bind.annotation.RestController"))
                    return;

                // Lombok 애노테이션이 있으면 OK
                if (owner.isAnnotatedWith("lombok.Getter")
                        || owner.isAnnotatedWith("lombok.Data")
                        || owner.isAnnotatedWith("lombok.Value")) return;

                // 특별한 getter 메서드는 허용 (비즈니스 로직)
                String methodName = method.getName();
                if (methodName.startsWith("is")
                        || methodName.startsWith("has")
                        || methodName.startsWith("can")
                        || methodName.equals("getClass")) return;

                String message =
                        String.format(
                                "%s.%s()는 단순 getter입니다. @Getter를 사용하세요", owner.getSimpleName(), methodName);
                events.add(SimpleConditionEvent.violated(method, message));
            }
        };
    }

    /**
     * 단순 setter가 아니어야 함 (비즈니스 로직 포함 setter는 허용)
     */
    private static ArchCondition<JavaMethod> notBeSimpleSetter() {
        return new ArchCondition<>("not be simple setter") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                JavaClass owner = method.getOwner();

                // record, enum, Service 등은 제외
                if (owner.isRecord()
                        || owner.isEnum()
                        || owner.isAnnotatedWith("org.springframework.stereotype.Service")
                        || owner.isAnnotatedWith("org.springframework.stereotype.Component")
                        || owner.isAnnotatedWith("org.springframework.context.annotation.Configuration"))
                    return;

                // Lombok 애노테이션이 있으면 OK
                if (owner.isAnnotatedWith("lombok.Setter") || owner.isAnnotatedWith("lombok.Data")) return;

                String message =
                        String.format(
                                "%s.%s()는 단순 setter입니다. @Setter를 사용하세요", owner.getSimpleName(), method.getName());
                events.add(SimpleConditionEvent.violated(method, message));
            }
        };
    }

    /**
     * @ToString이나 @Data가 있는 클래스에서만 toString 허용
     */
    private static ArchCondition<JavaMethod> beInClassWithLombokToString() {
        return new ArchCondition<>("be in class with Lombok @ToString") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                JavaClass owner = method.getOwner();

                // record는 자동 생성이므로 제외
                if (owner.isRecord()) return;

                boolean hasLombok =
                        owner.isAnnotatedWith("lombok.ToString")
                                || owner.isAnnotatedWith("lombok.Data")
                                || owner.isAnnotatedWith("lombok.Value");

                if (!hasLombok) {
                    String message =
                            String.format("%s.toString()은 수동 구현입니다. @ToString을 사용하세요", owner.getSimpleName());
                    events.add(SimpleConditionEvent.violated(method, message));
                }
            }
        };
    }

    /**
     * @EqualsAndHashCode나 @Data가 있는 클래스에서만 equals/hashCode 허용
     */
    private static ArchCondition<JavaMethod> beInClassWithLombokEqualsAndHashCode() {
        return new ArchCondition<>("be in class with Lombok @EqualsAndHashCode") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                JavaClass owner = method.getOwner();

                // record는 자동 생성이므로 제외
                if (owner.isRecord()) return;

                boolean hasLombok =
                        owner.isAnnotatedWith("lombok.EqualsAndHashCode")
                                || owner.isAnnotatedWith("lombok.Data")
                                || owner.isAnnotatedWith("lombok.Value");

                if (!hasLombok) {
                    String message =
                            String.format(
                                    "%s.%s()은 수동 구현입니다. @EqualsAndHashCode를 사용하세요",
                                    owner.getSimpleName(), method.getName());
                    events.add(SimpleConditionEvent.violated(method, message));
                }
            }
        };
    }

    /**
     * 허용된 생성자 패턴: Lombok 애노테이션 또는 @Builder가 붙은 비즈니스 생성자
     */
    private static ArchCondition<JavaConstructor> beAllowedConstructor() {
        return new ArchCondition<>("be allowed constructor pattern") {
            @Override
            public void check(JavaConstructor constructor, ConditionEvents events) {
                JavaClass owner = constructor.getOwner();

                // record, Builder 내부 클래스 제외
                if (owner.isRecord() || owner.getSimpleName().endsWith("Builder")) return;

                // Lombok 애노테이션이 있으면 OK
                boolean hasLombokOnClass =
                        owner.isAnnotatedWith("lombok.NoArgsConstructor")
                                || owner.isAnnotatedWith("lombok.AllArgsConstructor")
                                || owner.isAnnotatedWith("lombok.RequiredArgsConstructor")
                                || owner.isAnnotatedWith("lombok.Builder")
                                || owner.isAnnotatedWith("lombok.Data")
                                || owner.isAnnotatedWith("lombok.Value");

                if (hasLombokOnClass) return;

                // @Builder가 붙은 생성자는 허용 (비즈니스 로직 포함)
                boolean hasBuilderAnnotation = constructor.isAnnotatedWith("lombok.Builder");
                if (hasBuilderAnnotation) return;

                // 빈 생성자 검사 (매개변수 없음)
                if (constructor.getRawParameterTypes().isEmpty()) {
                    String message =
                            String.format("%s의 빈 생성자는 @NoArgsConstructor를 사용하세요", owner.getSimpleName());
                    events.add(SimpleConditionEvent.violated(constructor, message));
                    return;
                }

                // 전체 필드 생성자 검사 (모든 필드를 받는 생성자)
                int fieldCount = (int) owner.getFields().stream()
                        .filter(f -> !f.getModifiers().contains(JavaModifier.STATIC))
                        .filter(f -> !f.getModifiers().contains(JavaModifier.FINAL))
                        .count();

                int paramCount = constructor.getRawParameterTypes().size();

                // 전체 필드를 받는 생성자는 금지 (비즈니스 로직이 없는 경우)
                if (paramCount >= fieldCount && fieldCount > 0) {
                    // 단, 일부 필드만 받거나 검증 로직이 있는 생성자는 허용
                    // 여기서는 간단히 파라미터 수가 필드 수보다 적으면 비즈니스 생성자로 간주
                    String message =
                            String.format("%s의 전체 필드 생성자는 @AllArgsConstructor를 사용하세요", owner.getSimpleName());
                    events.add(SimpleConditionEvent.violated(constructor, message));
                }
                // 일부 필드만 받는 생성자는 비즈니스 로직이 있다고 간주하여 허용
            }
        };
    }
}
