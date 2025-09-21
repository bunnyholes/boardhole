# Thymeleaf 개발 환경 개선 가이드

## 🚀 현재 적용된 설정

### 1. Hot Reload 활성화 완료
프로젝트에 이미 Spring Boot DevTools가 설정되어 있고, Thymeleaf 캐시가 비활성화되어 있습니다.

**build.gradle:**
```gradle
developmentOnly 'org.springframework.boot:spring-boot-devtools'
```

**application.yml & application-dev.yml:**
```yaml
spring:
  thymeleaf:
    cache: false  # 템플릿 캐시 비활성화로 hot reload 활성화
  devtools:
    livereload:
      enabled: true
    restart:
      enabled: true
```

### 2. 사용 방법
1. **애플리케이션 실행:** `./gradlew bootRun`
2. **템플릿 수정:** `src/main/resources/templates/` 디렉토리의 HTML 파일 수정
3. **브라우저 새로고침:** 변경사항이 즉시 반영됨 (서버 재시작 불필요)

## 🔧 IDE 플러그인 및 도구

### IntelliJ IDEA Ultimate (권장)
IntelliJ IDEA Ultimate에는 **Thymeleaf 지원이 기본 탑재**되어 있습니다:

#### 주요 기능:
- ✅ **자동완성:** th:* 속성, Spring EL 표현식
- ✅ **문법 검증:** 실시간 오류 감지
- ✅ **코드 네비게이션:** 컨트롤러 ↔ 템플릿 이동 (Ctrl+Click)
- ✅ **리팩토링 지원:** 변수명 변경 시 자동 업데이트
- ✅ **Fragment 지원:** th:fragment 자동완성 및 참조
- ✅ **Live Edit:** 브라우저에서 실시간 미리보기

#### 설정 방법:
1. **Settings → Editor → File Types**
   - Thymeleaf가 HTML 파일과 연결되어 있는지 확인
2. **Settings → Languages & Frameworks → Thymeleaf**
   - Enable Thymeleaf support 체크
   - Dialect prefix 설정 (th)
3. **Settings → Build, Execution, Deployment → Compiler**
   - "Build project automatically" 체크
4. **Registry 설정 (Ctrl+Shift+A → Registry):**
   - `compiler.automake.allow.when.app.running` 활성화

### IntelliJ IDEA Community Edition
Community Edition은 기본 Thymeleaf 지원이 없지만, 다음 플러그인을 설치할 수 있습니다:

1. **Thymeleaf** (by Jetbrains) - 부분적 지원
   - 기본 문법 하이라이팅
   - 간단한 자동완성
   
설치: Settings → Plugins → Marketplace → "Thymeleaf" 검색

### Visual Studio Code
VS Code용 Thymeleaf 확장:

1. **Thymeleaf** (by Takuma Maruyama)
   - 문법 하이라이팅
   - 코드 스니펫
   - 설치: Extensions → "Thymeleaf" 검색

2. **ThymeLab** (개발 중)
   - 더 풍부한 기능 제공 예정
   - 현재 베타 단계

## 💡 개발 생산성 향상 팁

### 1. Fragment 활용
공통 컴포넌트를 fragment로 분리하여 재사용성 향상:

```html
<!-- fragments/common.html -->
<div th:fragment="header">
    <header>공통 헤더</header>
</div>

<!-- 사용 -->
<div th:replace="fragments/common :: header"></div>
```

### 2. Layout Dialect 추가 (선택사항)
더 강력한 레이아웃 기능이 필요하다면:

```gradle
implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0'
```

### 3. 브라우저 LiveReload 확장
자동 새로고침을 위한 브라우저 확장:
- **Chrome:** LiveReload Extension
- **Firefox:** LiveReload Add-on

설치 후 개발 서버 실행 시 자동으로 페이지가 새로고침됩니다.

### 4. Thymeleaf 디버깅 모드
application-dev.yml에 추가:

```yaml
logging:
  level:
    org.thymeleaf: DEBUG
    org.thymeleaf.TemplateEngine.CONFIG: TRACE
```

### 5. 개발용 더미 데이터
컨트롤러에서 개발용 더미 데이터 제공:

```java
@Profile("dev")
@Configuration
public class DevDataConfig {
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // 개발용 샘플 데이터 초기화
        };
    }
}
```

## 🎯 자주 사용하는 Thymeleaf 표현식

### 기본 표현식
```html
<!-- 변수 출력 -->
<span th:text="${user.name}">이름</span>

<!-- 조건문 -->
<div th:if="${user.isAdmin()}">관리자 메뉴</div>
<div th:unless="${user.isAdmin()}">일반 사용자 메뉴</div>

<!-- 반복문 -->
<tr th:each="board : ${boards}">
    <td th:text="${board.title}">제목</td>
</tr>

<!-- URL 생성 -->
<a th:href="@{/boards/{id}(id=${board.id})}">상세보기</a>

<!-- Fragment 포함 -->
<div th:replace="~{fragments/header :: header}"></div>
```

### Spring Security 통합
```html
<!-- 인증 상태 확인 -->
<div sec:authorize="isAuthenticated()">
    환영합니다, <span sec:authentication="name">사용자</span>님!
</div>

<!-- 권한 확인 -->
<div sec:authorize="hasRole('ADMIN')">
    관리자 전용 메뉴
</div>
```

## 🚨 주의사항

1. **프로덕션 환경에서는 반드시 캐시 활성화**
   ```yaml
   spring:
     thymeleaf:
       cache: true  # 프로덕션에서는 true
   ```

2. **정적 리소스 위치**
   - CSS/JS: `src/main/resources/static/`
   - 템플릿: `src/main/resources/templates/`

3. **Fragment 네이밍 규칙**
   - 파일명: `fragments/[기능].html`
   - Fragment명: 명확하고 재사용 가능한 이름 사용

## 📚 추가 리소스

- [Thymeleaf 공식 문서](https://www.thymeleaf.org/documentation.html)
- [Spring Boot + Thymeleaf 가이드](https://spring.io/guides/gs/serving-web-content/)
- [Thymeleaf + Spring Security](https://www.thymeleaf.org/doc/articles/springsecurity.html)

---

이 설정으로 Thymeleaf 개발이 훨씬 편리해졌습니다. 템플릿을 수정하면 브라우저를 새로고침하는 것만으로 변경사항을 확인할 수 있습니다.