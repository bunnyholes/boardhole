# 코드 품질 관리 설정 가이드

## 📋 개요
이 프로젝트는 다층 품질 관리 시스템을 사용합니다:
- **로컬 검사**: 즉시 피드백 (Checkstyle, PMD, SpotBugs)
- **클라우드 분석**: 종합 분석 (SonarCloud)
- **스타일 선택**: 프로젝트 커스텀 또는 Google Style

## 🚀 빠른 시작

### 1. SonarCloud 설정
```bash
# 1. https://sonarcloud.io 가입 (GitHub 계정 연동)
# 2. 새 프로젝트 생성
# 3. 토큰 생성 (Account Settings > Security)
# 4. GitHub Secrets에 SONAR_TOKEN 추가
```

### 2. 로컬 환경 설정
```bash
# gradle/quality-gates.gradle 적용
echo "apply from: 'gradle/quality-gates.gradle'" >> build.gradle

# 품질 검사 실행
./gradlew qualityGate
```

## 🎯 사용 시나리오

### 시나리오 1: 기본 프로젝트 스타일
```bash
# 현재 프로젝트 규칙 사용 (기본)
./gradlew build
./gradlew qualityCheck  # 모든 품질 검사
```

### 시나리오 2: Google Style 적용
```bash
# Google Java Style로 검사
./gradlew build -PuseGoogleStyle
./gradlew checkstyleMain -PuseGoogleStyle
```

### 시나리오 3: SonarCloud 분석
```bash
# 로컬에서 SonarCloud 분석 실행
export SONAR_TOKEN=your_token_here
./gradlew sonar

# 또는 토큰을 직접 전달
./gradlew sonar -Dsonar.token=your_token_here
```

### 시나리오 4: CI/CD 통합 검사
```bash
# GitHub Actions에서 자동 실행 (PR 및 Push 시)
# .github/workflows/quality-check.yml 참조
```

### 시나리오 5: 전체 품질 분석
```bash
# 로컬 + SonarCloud 모두 실행
./gradlew fullQualityAnalysis -Dsonar.token=your_token
```

## 📊 품질 기준

### 로컬 품질 게이트
| 도구 | 기준 | 실패 조건 |
|------|------|-----------|
| Checkstyle | 경고 0개 | maxWarnings = 0 |
| PMD | Priority 1 위반 0개 | 빌드 실패 |
| SpotBugs | Low 레벨 이상 | 모든 버그 검출 |
| JaCoCo | 라인 커버리지 60% | 경고만 |

### SonarCloud 품질 게이트
| 메트릭 | 기준 | 설명 |
|--------|------|------|
| 버그 | 0개 | 새 코드에 버그 없음 |
| 취약점 | 0개 | 보안 취약점 없음 |
| 코드 스멜 | A 등급 | 유지보수성 |
| 커버리지 | 80% | 새 코드 커버리지 |
| 중복 | 3% 미만 | 중복 코드 비율 |

## 🔧 커스터마이징

### 프로젝트 특화 규칙 추가
```xml
<!-- config/checkstyle/checkstyle.xml -->
<module name="RegexpSinglelineJava">
    <property name="format" value="당신의 패턴"/>
    <property name="message" value="위반 메시지"/>
</module>
```

### SonarCloud 규칙 조정
1. SonarCloud 대시보드 > Administration > Quality Profiles
2. 새 프로파일 생성 또는 기존 프로파일 확장
3. 규칙 활성화/비활성화
4. build.gradle에서 프로파일 지정:
```gradle
property 'sonar.qualityProfile', 'Your Profile Name'
```

## 📈 리포트 확인

### 로컬 리포트
```bash
# HTML 리포트 위치
open build/reports/checkstyle/main.html
open build/reports/pmd/main.html
open build/reports/spotbugs/main.html
open build/reports/jacoco/test/html/index.html
```

### SonarCloud 대시보드
```
https://sonarcloud.io/project/overview?id=your-project-key
```

## 🚨 문제 해결

### 문제: Checkstyle 버전 충돌
```gradle
// 해결: 버전 명시
checkstyle {
    toolVersion = '10.12.5'
}
```

### 문제: SonarCloud Lombok 인식 실패
```gradle
// 해결: Lombok 애노테이션 설정
property 'sonar.java.lombok.addLombokGeneratedAnnotation', 'true'
```

### 문제: Google Style 너무 엄격함
```bash
# 해결: 경고만 표시하도록 설정
./gradlew checkstyleMain -PuseGoogleStyle --continue
```

## 🎯 권장 워크플로우

### 개발 중
1. 코드 작성
2. `./gradlew checkstyleMain` - 즉시 스타일 검사
3. `./gradlew test` - 테스트 실행
4. 커밋

### PR 전
1. `./gradlew qualityGate` - 전체 로컬 검사
2. 문제 수정
3. PR 생성 → GitHub Actions 자동 검사

### 릴리즈 전
1. `./gradlew fullQualityAnalysis` - 전체 분석
2. SonarCloud 대시보드 확인
3. 품질 게이트 통과 확인

## 📚 참고 자료
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [Checkstyle Configuration](https://checkstyle.org/config.html)
- [PMD Rule Reference](https://pmd.github.io/latest/pmd_rules_java.html)
- [SpotBugs Bug Patterns](https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html)