# Contributing to Board Hole

이 프로젝트에 기여해주셔서 감사합니다! 이 문서는 프로젝트 기여 가이드라인을 설명합니다.

## 📋 목차

1. [개발 환경 설정](#개발-환경-설정)
2. [브랜치 전략](#브랜치-전략)
3. [커밋 컨벤션](#커밋-컨벤션)
4. [개발 워크플로우](#개발-워크플로우)
5. [자동 버전 관리](#자동-버전-관리)
6. [코드 스타일](#코드-스타일)
7. [테스트](#테스트)
8. [Pull Request 가이드](#pull-request-가이드)
9. [릴리즈 프로세스](#릴리즈-프로세스)

## 🚀 개발 환경 설정

### 필수 요구사항
- Java 21
- Gradle 8.x
- Git
- IDE (IntelliJ IDEA 권장)

### 초기 설정
```bash
# 저장소 클론
git clone https://github.com/your-org/board-hole.git
cd board-hole

# 커밋 템플릿 설정
git config commit.template .gitmessage

# 의존성 설치 및 빌드
./gradlew clean build

# 테스트 실행
./gradlew test
```

## 🌲 브랜치 전략

### 브랜치 구조
```
main (production)
├── develop (개발 통합)
│   ├── feature/기능명
│   ├── bugfix/버그명
│   ├── chore/작업명
│   └── test/테스트명
├── release/버전
└── hotfix/긴급수정
```

### 브랜치 규칙

#### Main Branch (`main`, `master`)
- **용도**: 프로덕션 배포 브랜치
- **보호**: 직접 푸시 금지, PR만 허용
- **자동화**: 머지 시 자동 릴리즈 생성

#### Develop Branch (`develop`)
- **용도**: 개발 기능 통합 브랜치
- **자동화**: 머지마다 자동 버전 증가
- **버전 형식**: `X.Y.Z-dev.N`

#### Feature Branches (`feature/*`)
- **용도**: 새 기능 개발
- **타겟**: develop 브랜치로 PR
- **예시**: `feature/user-authentication`

#### Bugfix Branches (`bugfix/*`)
- **용도**: 버그 수정
- **타겟**: develop 브랜치로 PR
- **예시**: `bugfix/login-error`

#### Release Branches (`release/*`)
- **용도**: 릴리즈 준비
- **생성**: develop에서 분기
- **버전 형식**: `X.Y.Z-rc.N`
- **예시**: `release/1.2.0`

#### Hotfix Branches (`hotfix/*`)
- **용도**: 프로덕션 긴급 수정
- **생성**: main에서 분기
- **타겟**: main과 develop 모두에 PR
- **예시**: `hotfix/critical-security-fix`

## 📝 커밋 컨벤션

### 커밋 메시지 형식
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type (필수)
| Type | 설명 | 버전 영향 |
|------|------|----------|
| `feat` | 새로운 기능 추가 | MINOR ⬆️ |
| `fix` | 버그 수정 | PATCH ⬆️ |
| `feat!` | Breaking Change | MAJOR ⬆️ |
| `fix!` | Breaking Change | MAJOR ⬆️ |
| `docs` | 문서 변경 | - |
| `style` | 코드 포맷팅 | - |
| `refactor` | 코드 리팩토링 | - |
| `test` | 테스트 추가/수정 | - |
| `chore` | 빌드/설정 변경 | - |
| `perf` | 성능 개선 | - |
| `ci` | CI 설정 변경 | - |
| `build` | 빌드 시스템 변경 | - |
| `revert` | 커밋 되돌리기 | - |

### 커밋 예시
```bash
# 기능 추가 (MINOR 버전 증가)
feat(auth): add refresh token support

# 버그 수정 (PATCH 버전 증가)
fix(board): resolve pagination error

# Breaking Change (MAJOR 버전 증가)
feat!(api): change authentication header format

BREAKING CHANGE: Authorization header now requires Bearer prefix
```

## 🔄 개발 워크플로우

### 1. 기능 개발
```bash
# develop 브랜치에서 시작
git checkout develop
git pull origin develop

# feature 브랜치 생성
git checkout -b feature/my-feature

# 개발 및 커밋
git add .
git commit  # 템플릿 사용

# 푸시
git push origin feature/my-feature
```

### 2. Pull Request 생성
1. GitHub에서 PR 생성
2. develop 브랜치를 타겟으로 설정
3. PR 템플릿 작성
4. CI 통과 확인

### 3. 코드 리뷰
- 최소 1명의 리뷰어 승인 필요
- CI 모든 체크 통과
- 충돌 해결

### 4. 머지
- Squash and merge 권장
- 자동 버전 증가 확인

## 🔢 자동 버전 관리

### 버전 체계
- **Semantic Versioning**: `MAJOR.MINOR.PATCH`
- **Pre-release**: `-dev.N`, `-rc.N`, `-alpha.N`, `-beta.N`

### 자동 버전 규칙

#### Develop 브랜치
- 모든 머지마다 자동 버전 증가
- `feat:` → MINOR 증가
- `fix:` → PATCH 증가
- `feat!:` → MAJOR 증가
- 예: `1.2.3-dev.1` → `1.2.3-dev.2`

#### Release 브랜치
- RC 버전 자동 생성
- 예: `1.2.3-rc.1` → `1.2.3-rc.2`

#### Main 브랜치
- 정식 버전 릴리즈
- GitHub Release 자동 생성
- JAR 아티팩트 업로드
- 예: `1.2.3`

## 💅 코드 스타일

### Java 코드 스타일
- Google Java Style Guide 준수
- Checkstyle로 자동 검증
- 2개 이상 임포트는 와일드카드 사용

### 자동 검증
```bash
# Checkstyle 실행
./gradlew checkstyleMain checkstyleTest

# SpotBugs 실행
./gradlew spotbugsMain

# 모든 검증 실행
./gradlew check
```

## 🧪 테스트

### 테스트 유형
- **Unit Tests**: 단위 테스트
- **Integration Tests**: 통합 테스트
- **Architecture Tests**: 아키텍처 검증

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 테스트만
./gradlew test --tests "*.Critical*"

# 테스트 커버리지
./gradlew jacocoTestReport
```

### 테스트 요구사항
- 새 기능은 테스트 필수
- 버그 수정은 재현 테스트 포함
- 커버리지 목표: 80% 이상

## 🎯 Pull Request 가이드

### PR 체크리스트
- [ ] 커밋 컨벤션 준수
- [ ] 테스트 통과
- [ ] Checkstyle 통과
- [ ] SpotBugs 통과
- [ ] 문서 업데이트 (필요시)
- [ ] CHANGELOG 업데이트 불필요 (자동 생성)

### PR 템플릿
```markdown
## 📋 변경 사항
- 기능/버그 설명

## 🔍 변경 유형
- [ ] feat: 새로운 기능
- [ ] fix: 버그 수정
- [ ] docs: 문서 변경
- [ ] refactor: 리팩토링

## ✅ 테스트
- [ ] 단위 테스트 추가/수정
- [ ] 통합 테스트 추가/수정
- [ ] 수동 테스트 완료

## 📝 추가 정보
- 관련 이슈: #123
```

## 🚀 릴리즈 프로세스

### 1. Release 준비
```bash
# release 브랜치 생성
git checkout -b release/1.2.0 develop

# 자동으로 RC 버전 생성됨
# 1.2.0-rc.1
```

### 2. Release 테스트
- 스테이징 환경 배포
- QA 테스트 수행
- 버그 수정 (RC 버전 증가)

### 3. Production 릴리즈
1. release → main PR 생성
2. 승인 및 머지
3. 자동 릴리즈 생성:
   - 정식 버전 태그
   - GitHub Release
   - JAR 아티팩트
   - CHANGELOG

### 4. Hotfix (긴급 수정)
```bash
# hotfix 브랜치 생성
git checkout -b hotfix/critical-fix main

# 수정 및 푸시
# 자동으로 main과 develop에 PR 생성
```

## 📊 CI/CD 파이프라인

### GitHub Actions Workflows

#### feature-ci.yml
- **트리거**: feature/* → develop PR
- **검증**: 
  - 브랜치 이름 규칙
  - 커밋 메시지 형식
  - 빌드 및 테스트
  - 코드 품질

#### develop-versioning.yml
- **트리거**: develop 푸시/머지
- **동작**: 자동 버전 증가
- **태그**: `dev-X.Y.Z-dev.N`

#### release-prepare.yml
- **트리거**: release/* 푸시
- **동작**: RC 버전 생성
- **결과**: Draft Release

#### main-release.yml
- **트리거**: main 푸시/머지
- **동작**: 정식 릴리즈
- **결과**: GitHub Release + JAR

#### hotfix.yml
- **트리거**: hotfix/* 푸시
- **동작**: 긴급 릴리즈
- **PR**: main + develop

## 🤝 도움 요청

### 이슈 생성
- 버그 리포트
- 기능 제안
- 질문

### 디스커션
- 아키텍처 논의
- 개선 제안
- 일반 토론

### 연락처
- 메인테이너: @username
- 이메일: team@example.com

---

**Happy Contributing! 🎉**