# Lombok 사용 가이드

## 📌 개요
이 프로젝트는 boilerplate 코드를 줄이고 일관성을 유지하기 위해 **Lombok 사용을 강제**합니다.

## ✅ 필수 규칙

### 1. Getter/Setter
```java
// ❌ 금지 - 수동 getter/setter
public class User {
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}

// ✅ 권장 - Lombok 사용
@Getter
@Setter
public class User {
    private String name;
}
```

### 2. 생성자
```java
// ❌ 금지 - 수동 생성자
public class User {
    private String name;
    private String email;
    
    public User() {}
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}

// ✅ 권장 - Lombok 사용
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String name;
    private String email;
}
```

### 3. Builder 패턴
```java
// ❌ 금지 - 수동 Builder
public class User {
    private String name;
    private String email;
    
    public static class Builder {
        // ... builder implementation
    }
}

// ✅ 권장 - Lombok Builder
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String name;
    private String email;
}
```

### 4. toString(), equals(), hashCode()
```java
// ❌ 금지 - 수동 구현
public class User {
    @Override
    public String toString() {
        return "User{name='" + name + "'}";
    }
    
    @Override
    public boolean equals(Object o) {
        // manual implementation
    }
    
    @Override
    public int hashCode() {
        // manual implementation
    }
}

// ✅ 권장 - Lombok 사용
@ToString
@EqualsAndHashCode
public class User {
    private String name;
    private String email;
}
```

## 🎯 도메인별 권장 패턴

### Entity 클래스
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"password", "roles"})
@Entity
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    private String username;
    
    @Builder
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
```

### DTO 클래스
```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String email;
}
```

### Command/Request 클래스
```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserCommand {
    @NotBlank
    private String username;
    
    @Email
    private String email;
}
```

### Value Object
```java
@Value
@Builder
public class EmailAddress {
    String value;
    
    // custom validation in builder
}
```

## 🛠️ 검증 도구

### 1. Checkstyle
- 수동 getter/setter 감지
- 빈 생성자 감지
- toString/equals/hashCode 수동 구현 감지

### 2. PMD
- Lombok 애노테이션 없는 boilerplate 코드 감지
- XPath 규칙으로 수동 구현 방지

### 3. ArchUnit
- 아키텍처 테스트로 Lombok 사용 강제
- `LombokRulesTest.java` 참조

## 🔧 IntelliJ IDEA 설정

### Lombok 플러그인 설치
1. Settings → Plugins → "Lombok" 검색 및 설치
2. Settings → Build, Execution, Deployment → Compiler → Annotation Processors
3. "Enable annotation processing" 체크

### 코드 생성 템플릿
1. Settings → Editor → File and Code Templates
2. Class 템플릿에 기본 Lombok 애노테이션 추가:
```java
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

import lombok.*;

@Getter
@NoArgsConstructor
public class ${NAME} {
}
```

## ⚠️ 예외 사항

### Lombok을 사용하지 않아도 되는 경우:
1. **테스트 클래스**: 테스트 목적의 간단한 헬퍼 클래스
2. **유틸리티 클래스**: static 메서드만 포함하는 클래스
3. **특수한 비즈니스 로직**: getter/setter에 복잡한 로직이 필요한 경우
4. **JPA Lazy Loading**: 특별한 초기화 로직이 필요한 경우

이러한 경우에는 `@SuppressWarnings("LombokRequired")` 애노테이션을 추가하고 주석으로 이유를 명시하세요.

## 📊 검증 명령어

```bash
# Checkstyle 검사
./gradlew checkstyleMain checkstyleTest

# PMD 검사
./gradlew pmdMain pmdTest

# ArchUnit 테스트
./gradlew test --tests LombokRulesTest

# 전체 품질 검사
./gradlew qualityCheck
```

## 💡 Tips

1. **@Data 사용 주의**: `@ToString`, `@EqualsAndHashCode`, `@Getter`, `@Setter`, `@RequiredArgsConstructor`를 모두 포함하므로 신중하게 사용
2. **@Value**: 불변 객체에 사용 (모든 필드가 final)
3. **@Builder.Default**: Builder 패턴에서 기본값 설정
4. **@Slf4j**: 로깅을 위한 log 필드 자동 생성

## 🔗 참고 자료
- [Lombok 공식 문서](https://projectlombok.org/features/)
- [Lombok Best Practices](https://www.baeldung.com/lombok)
- [프로젝트 ArchUnit 테스트](../src/test/java/bunny/boardhole/architecture/LombokRulesTest.java)