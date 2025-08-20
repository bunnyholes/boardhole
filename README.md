# Board-Hole

Spring Boot MVC 패턴 학습을 위한 기초 프로젝트

이 프로젝트는 **Spring Boot의 MVC 패턴**과 **Controller-Service-Repository 레이어드 아키텍처**를 학습하기 위한 교육용 프로젝트입니다.

## 현재 상태

**학습 단계**: MVC 패턴 기초 구현
- ✅ Controller-Service 레이어 구조
- ✅ REST API 기본 엔드포인트
- ✅ 의존성 주입(DI) 구현
- 🔄 Repository 레이어 (구현 예정)
- ⏳ 데이터베이스 연동 (구현 예정)

## 학습 목표

- MVC 패턴 이해 및 실습
- 레이어드 아키텍처 구조 학습
- RESTful API 개발 기초
- Spring Boot 기본 설정 및 사용법
- 의존성 주입(DI) 개념 이해

## 기술 스택

| 기술 | 버전 | 용도 |
|------|------|------|
| **Java** | 21 | 기본 언어 |
| **Spring Boot** | 3.5.4 | 프레임워크 |
| **Spring Web** | - | MVC 패턴 |
| **H2 Database** | - | 인메모리 데이터베이스 |
| **Lombok** | - | 코드 간소화 |
| **SpringDoc OpenAPI** | 2.3.0 | API 문서화 |
| **Gradle** | 8.14.3 | 빌드 도구 |

## 프로젝트 구조

```
src/main/java/bunny/boardhole/
├── BoardHoleApplication.java    # Spring Boot 메인 클래스
├── controller/                  # Controller 레이어
│   ├── HelloController.java     #   - Hello API 컨트롤러
│   └── MemberController.java    #   - Member API 컨트롤러
└── service/                     # Service 레이어
    ├── HelloService.java        #   - Hello 비즈니스 로직
    └── MemberService.java       #   - Member 비즈니스 로직
```

### 레이어드 아키텍처

```
┌─────────────────┐
│   Controller    │  ← HTTP 요청/응답 처리
│    (API 계층)    │
└─────────────────┘
         ↓
┌─────────────────┐
│    Service      │  ← 비즈니스 로직 처리
│  (비즈니스 계층)  │
└─────────────────┘
         ↓
┌─────────────────┐
│   Repository    │  ← 데이터 접근 (향후 추가 예정)
│   (데이터 계층)   │
└─────────────────┘
```

## 시작하기

### 필수 요구사항
- Java 21 이상
- IntelliJ IDEA 또는 VS Code (선택사항)

### 실행 방법

1. **프로젝트 클론**
   ```bash
   git clone <repository-url>
   cd board-hole
   ```

2. **애플리케이션 실행**
   ```bash
   # Gradle Wrapper 사용
   ./gradlew bootRun
   
   # 또는 IDE에서 BoardHoleApplication.java 실행
   ```

3. **실행 확인**
   ```
   애플리케이션: http://localhost:8080
   API 문서: http://localhost:8080/swagger-ui.html
   ```

## 학습 포인트

### 1. Controller 레이어
```java
@RestController  // ← REST API 컨트롤러 선언
public class HelloController {
    
    private final HelloService helloService;  // ← 의존성 주입
    
    @GetMapping("/hello")  // ← HTTP GET 매핑
    public String sayHello() {
        return helloService.sayHello();  // ← Service 계층 호출
    }
}
```

### 2. Service 레이어
```java
@Service  // ← Service 컴포넌트 선언
public class HelloService {
    
    public String sayHello() {  // ← 비즈니스 로직 구현
        return "Hello, World!";
    }
}
```

### 3. 의존성 주입 (DI)
```java
// 생성자 주입 방식 (권장)
private final HelloService helloService;

HelloController(HelloService helloService) {
    this.helloService = helloService;
}
```

## 개발 도구

### H2 데이터베이스 콘솔
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (비어있음)
```

### API 문서 (Swagger)
```
URL: http://localhost:8080/swagger-ui.html
```

## 기여하기

이 프로젝트는 학습 목적으로 만들어졌습니다. 개선사항이나 추가 학습 예제가 있다면 `CONTRIBUTING.md`를 참고해주세요.

## 라이센스

이 프로젝트는 학습 목적으로 자유롭게 사용 가능합니다.