# Spring Boot 보일러플레이트 코드 절감 가이드

## 📚 개요
Spring Boot 애플리케이션에서 애너테이션과 라이브러리를 활용하여 보일러플레이트 코드를 최대 90%까지 줄일 수 있는 방법을 정리한 문서입니다.

---

## 🎯 현재 프로젝트 적용 현황 및 개선 기회

### ✅ 잘 활용 중인 기능
- **Lombok 기본**: @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor
- **MapStruct**: 엔티티-DTO 매핑 자동화
- **JPA Auditing**: @CreatedDate, @LastModifiedDate
- **커스텀 검증**: @ValidEmail, @OptionalEmail 등
- **Records**: DTO에 Java Record 활용

### 🚀 즉시 적용 가능한 개선사항

#### 1. **@Slf4j 로거 자동 주입** (코드 90% 절감)
```java
// Before: 수동 로거 생성
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
}

// After: @Slf4j 사용
@Slf4j
@Service
public class UserService {
    public void process() {
        log.info("Processing...");
    }
}
```
**적용 대상**: 모든 Service, Controller 클래스

#### 2. **@UtilityClass로 유틸리티 클래스 개선** (코드 80% 절감)
```java
// Before
public final class MessageUtils {
    private MessageUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    public static String get(String key) {
        // ...
    }
}

// After
@UtilityClass
public class MessageUtils {
    public String get(String key) {  // 자동으로 static 변환
        // ...
    }
}
```
**적용 대상**: MessageUtils, MDCUtil

#### 3. **@ConfigurationProperties로 설정 바인딩** (코드 70% 절감)
```java
@ConfigurationProperties(prefix = "boardhole")
@Data
public class BoardHoleProperties {
    private Security security;
    private Email email;
    private int maxUploadSize;
    
    @Data
    public static class Security {
        private int sessionTimeout;
        private boolean csrfEnabled;
    }
    
    @Data
    public static class Email {
        private String from;
        private String smtp;
    }
}
```

---

## 📊 Lombok 완전 활용 가이드

### 1. 기본 애너테이션 (현재 사용 중)

| 애너테이션 | 생성되는 코드 | 코드 절감률 | 사용 시기 |
|-----------|-------------|-----------|----------|
| @Getter | getter 메서드 | 60-80% | 모든 필드 읽기 필요 시 |
| @Setter | setter 메서드 | 60-80% | 가변 객체 |
| @NoArgsConstructor | 기본 생성자 | 90% | JPA 엔티티 필수 |
| @AllArgsConstructor | 모든 필드 생성자 | 90% | 테스트, 빌더 보조 |
| @Builder | 빌더 패턴 | 95% | 복잡한 객체 생성 |
| @ToString | toString() | 80% | 디버깅, 로깅 |
| @EqualsAndHashCode | equals(), hashCode() | 90% | 동등성 비교 필요 시 |

### 2. 고급 애너테이션 (추천)

#### **@Data** - DTO 전용
```java
@Data  // @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor
public class UserDto {
    private String username;
    private String email;
}
```
⚠️ **주의**: 엔티티에는 사용 금지 (양방향 연관관계 시 StackOverflow)

#### **@Value** - 불변 객체
```java
@Value  // 모든 필드 final, getter만 생성, 불변성 보장
public class ImmutableConfig {
    String apiKey;
    int timeout;
}
```

#### **@With** - 불변 객체 업데이트
```java
@Value
@With
public class Settings {
    boolean darkMode;
    String language;
}
// 사용: settings.withDarkMode(true)
```

#### **@Slf4j/@Log4j2** - 로거 주입
```java
@Slf4j
@Service
public class BoardService {
    public void process() {
        log.debug("Debug message");
        log.info("Info message with param: {}", param);
        log.error("Error occurred", exception);
    }
}
```

#### **@Cleanup** - 자동 리소스 관리
```java
public void readFile(String path) {
    @Cleanup InputStream in = new FileInputStream(path);
    // 자동으로 close() 호출
}
```

#### **@SneakyThrows** - Checked Exception 처리
```java
@SneakyThrows  // throws 선언 없이 checked exception 던지기
public String readFile(String path) {
    return Files.readString(Paths.get(path));
}
```

---

## 🗃️ JPA/Hibernate 보일러플레이트 절감

### 1. 자동 Auditing (현재 부분 적용)

#### 개선안: @CreatedBy, @LastModifiedBy 추가
```java
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName);
    }
}

// BaseEntity 개선
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}
```

### 2. Named Entity Graph (N+1 문제 해결)
```java
@Entity
@NamedEntityGraph(
    name = "Board.withAuthor",
    attributeNodes = @NamedAttributeNode("author")
)
public class Board extends BaseEntity {
    // ...
}

// Repository 사용
@EntityGraph("Board.withAuthor")
Optional<Board> findWithAuthorById(Long id);
```

### 3. Soft Delete 자동화
```java
@Entity
@SQLDelete(sql = "UPDATE boards SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Board extends BaseEntity {
    
    @Column(nullable = false)
    private boolean deleted = false;
}
```

---

## 🔄 MapStruct 고급 활용

### 1. 조건부 매핑
```java
@Mapper(config = MapstructConfig.class)
public interface UserMapper {
    
    @Mapping(target = "password", ignore = true)  // 비밀번호 제외
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + ' ' + user.getLastName())")
    UserDto toDto(User user);
    
    @AfterMapping
    default void maskSensitiveData(@MappingTarget UserDto dto) {
        if (dto.getEmail() != null) {
            dto.setEmail(maskEmail(dto.getEmail()));
        }
    }
}
```

### 2. 컬렉션 매핑
```java
@Mapper(config = MapstructConfig.class)
public interface BoardMapper {
    List<BoardDto> toDtoList(List<Board> boards);  // 자동 생성
    
    @IterableMapping(qualifiedByName = "summary")  // 커스텀 매핑
    List<BoardSummary> toSummaryList(List<Board> boards);
    
    @Named("summary")
    @Mapping(target = "content", ignore = true)
    BoardSummary toSummary(Board board);
}
```

---

## 🛡️ Validation 고급 활용

### 1. 그룹 검증
```java
public class UserDto {
    
    public interface Create {}
    public interface Update {}
    
    @NotNull(groups = Create.class)
    @Null(groups = Update.class)
    private Long id;
    
    @NotBlank(groups = {Create.class, Update.class})
    @Size(min = 3, max = 20)
    private String username;
}

// Controller
@PostMapping
public ResponseEntity<?> create(@Validated(UserDto.Create.class) @RequestBody UserDto dto) {
    // ...
}
```

### 2. 크로스 필드 검증
```java
@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
public @interface PasswordMatches {
    String message() default "Passwords don't match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@PasswordMatches
public class PasswordResetDto {
    private String password;
    private String confirmPassword;
}
```

---

## 📦 Spring Boot 자동 설정 활용

### 1. 조건부 Bean 생성
```java
@Configuration
public class FeatureConfig {
    
    @Bean
    @ConditionalOnProperty(name = "feature.email.enabled", havingValue = "true", matchIfMissing = true)
    public EmailService emailService() {
        return new EmailServiceImpl();
    }
    
    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailService mockEmailService() {
        return new MockEmailService();
    }
    
    @Bean
    @Profile("production")
    @ConditionalOnClass(RedisOperations.class)
    public CacheManager cacheManager() {
        return RedisCacheManager.create();
    }
}
```

### 2. 커스텀 자동 설정
```java
@Configuration
@EnableConfigurationProperties(BoardHoleProperties.class)
@ConditionalOnWebApplication
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class BoardHoleAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public SecurityConfig securityConfig(BoardHoleProperties properties) {
        return new SecurityConfig(properties.getSecurity());
    }
}
```

---

## 🎨 Jackson JSON 처리 최적화

### 1. 전역 설정
```java
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {
    private String status;
    private T data;
    private LocalDateTime timestamp;
}
```

### 2. 커스텀 직렬화
```java
@JsonSerialize(using = MaskSerializer.class)
private String email;

@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime createdAt;

@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private String password;
```

---

## 🧪 테스트 보일러플레이트 절감

### 1. 테스트 슬라이스 애너테이션
```java
@DataJpaTest  // JPA 레이어만 테스트
@WebMvcTest(UserController.class)  // 웹 레이어만 테스트
@JsonTest  // JSON 직렬화만 테스트
@RestClientTest  // REST 클라이언트만 테스트
```

### 2. 커스텀 메타 애너테이션
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql")
public @interface IntegrationTest {
}

// 사용
@IntegrationTest
class UserIntegrationTest {
    // 모든 설정 자동 적용
}
```

---

## 📈 코드 절감 효과 분석

### 현재 상태
- **Lombok 기본 사용**: 엔티티/DTO 보일러플레이트 70% 절감
- **MapStruct**: 매핑 코드 90% 절감
- **Custom Validation**: 검증 로직 60% 절감

### 개선 후 예상 효과

| 영역 | 현재 | 개선 후 | 절감률 |
|-----|------|--------|-------|
| 로깅 | 수동 Logger 생성 | @Slf4j | 90% |
| 유틸리티 클래스 | 수동 static/private 생성자 | @UtilityClass | 80% |
| 설정 바인딩 | @Value 개별 주입 | @ConfigurationProperties | 70% |
| Auditing | CreatedDate/UpdatedDate만 | + CreatedBy/UpdatedBy | 추가 20% |
| **전체** | **70%** | **85-90%** | **+15-20%** |

---

## ⚠️ 주의사항

### 1. Lombok 사용 시
- **@Data**: 엔티티에 사용 금지 (순환 참조 위험)
- **@EqualsAndHashCode**: exclude 속성으로 순환 참조 필드 제외
- **@ToString**: 연관관계 필드는 exclude
- IDE에 Lombok 플러그인 필수 설치

### 2. MapStruct 사용 시
- 컴파일 타임 코드 생성 → 빌드 시간 증가
- IntelliJ IDEA: "Enable annotation processing" 필수
- 디버깅 시 생성된 코드 확인 필요

### 3. Spring Boot 자동 설정
- 너무 많은 조건부 로직은 시작 시간 증가
- @ConditionalOn* 남용 주의
- 명시적 설정이 더 나은 경우도 있음

---

## 🚀 단계별 적용 로드맵

### Phase 1 (즉시 적용) - 1주
1. ✅ @Slf4j 전체 Service/Controller 적용
2. ✅ @UtilityClass 유틸리티 클래스 적용
3. ✅ @ConfigurationProperties 설정 바인딩

### Phase 2 (단기) - 2주
1. ⏳ @CreatedBy/@LastModifiedBy Auditing 확장
2. ⏳ @Value 불변 DTO 적용
3. ⏳ @NamedEntityGraph N+1 최적화

### Phase 3 (중기) - 1개월
1. ⏳ 조건부 Bean 생성으로 Feature Toggle
2. ⏳ 커스텀 메타 애너테이션 생성
3. ⏳ Advanced MapStruct 패턴 적용

---

## 📚 참고 자료

- [Project Lombok 공식 문서](https://projectlombok.org/features/all)
- [MapStruct Reference Guide](https://mapstruct.org/documentation/stable/reference/html/)
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [JPA Best Practices](https://vladmihalcea.com/tutorials/hibernate/)
- [Effective Java 3rd Edition](https://www.oreilly.com/library/view/effective-java-3rd/9780134686097/)