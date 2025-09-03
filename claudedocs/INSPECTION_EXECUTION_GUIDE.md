# IntelliJ 인스펙션 실행 가이드
## 체계적 코드 품질 개선 절차

### 🔧 사전 준비

#### Git 안전 조치
```bash
# 현재 상태 백업
git checkout -b quality-improvement-$(date +%Y%m%d)
git add -A && git commit -m "Backup before quality improvements"
```

#### 도구 확인
- IntelliJ IDEA Ultimate 설치 확인
- Python 3 환경 (XML → JSON 변환용)
- Gradle 빌드 환경

### 📋 Phase 1: 🔴 CRITICAL 보안 문제 해결

#### 1.1 취약한 라이브러리 업데이트 (11개 문제)

**파일**: `build.gradle:62`  
**문제**: CVE-2025-48924 (commons-lang3:3.17.0)

**해결 절차**:
```bash
# 1. 현재 의존성 확인
./gradlew dependencies | grep commons-lang3

# 2. 최신 안전 버전 확인
# https://mvnrepository.com/artifact/org.apache.commons/commons-lang3

# 3. build.gradle 수정
# implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.11' 
# → 최신 안전 버전으로 교체

# 4. 의존성 업데이트 테스트
./gradlew build --info
```

**검증 체크리스트**:
- [ ] 빌드 성공
- [ ] 125개 테스트 통과
- [ ] 보안 스캔 통과
- [ ] API 문서 정상 작동

#### 1.2 DataFlow NPE 위험 해결 (10개)

**주요 파일들**:
- `GlobalExceptionHandlerTest.java:74,97,118`
- 기타 테스트 파일들

**해결 패턴**:
```java
// Before (위험)
result.get()

// After (안전)
result.orElse(defaultValue)
// 또는
if (result.isPresent()) {
    result.get()
}
```

### 📋 Phase 2: 🔴 신뢰성 문제 해결

#### 2.1 Repository @NonNullApi 호환성 (32개)

**문제 패턴**: Spring의 @NonNullApi와 Bean Validation 충돌

**해결 전략**:
1. **검토 우선**: 실제로 null이 전달될 수 있는지 확인
2. **Spring 패턴 준수**: Spring Data JPA 규칙 따르기  
3. **테스트 검증**: 각 수정 후 관련 테스트 실행

**주요 파일**:
- `BoardRepository.java:23,27`
- `UserRepository.java`  
- `EmailVerificationRepository.java`

### 📋 Phase 3: 🟡 HIGH 코드 정리

#### 3.1 자동화 정리 (109개)

**사용하지 않는 Import 정리**:
```bash
# IntelliJ에서 일괄 처리
Code → Optimize Imports → Whole project
```

**사용하지 않는 코드 정리**:
- IntelliJ "Unused Declaration" 인스펙션 활용
- 메서드별 사용 여부 확인 후 제거

#### 3.2 수동 검토 필요 항목
- **AuthCommandService**: login/logout 메서드가 실제로 미사용인지 확인
- **BoardHoleApplication**: 메인 클래스 미사용 경고 (false positive일 가능성)

### 📋 Phase 4-5: 🟢🟢 설정 및 문서 정리

#### 4.1 Properties 파일 정리 (186개)
```bash
# 미사용 validation 메시지 식별
grep -r "jakarta.validation.constraints" src/main/java/

# 실제 사용하는 메시지만 유지
# ValidationMessages.properties 정리
```

#### 5.1 문서화 개선 (157개)
- 마크다운 형식 표준화
- 영어 철자 검사 및 교정
- 헤더 참조 오류 수정

---

## 🎯 실행 순서 및 체크포인트

### 실행 타임라인
```
Day 1 Morning (2-3시간):
├─ Phase 1: 🔴 보안 문제 해결
├─ 중간 검증: 테스트 + 보안 스캔  
└─ 체크포인트: Git 커밋

Day 1 Afternoon (1-2시간):  
├─ Phase 2: 🔴 신뢰성 문제 해결
├─ 중간 검증: 테스트 실행
└─ 체크포인트: Git 커밋

Day 2 Morning (1시간):
├─ Phase 3: 🟡 코드 정리
├─ 자동화 도구 활용
└─ 체크포인트: Git 커밋

Day 2 Afternoon (1시간):
├─ Phase 4-5: 🟢⚪ 설정/문서 정리  
├─ 최종 검증: 전체 테스트
└─ 완료: Pull Request 생성
```

### 품질 게이트

#### 각 Phase 완료 기준
```bash
# 1. 테스트 통과 확인
./gradlew test

# 2. Checkstyle 검증  
./gradlew checkstyleMain checkstyleTest

# 3. 빌드 성공 확인
./gradlew build

# 4. Git 상태 정리
git status  # clean working directory
```

#### 최종 완료 기준
- [ ] **보안**: 취약점 0개
- [ ] **테스트**: 125개 테스트 100% 통과  
- [ ] **빌드**: 경고 없이 성공
- [ ] **품질**: Checkstyle 규칙 준수
- [ ] **문서**: 일관된 형식 및 정확성

---

## 📝 구현 절차서

### 단계별 실행 명령어

#### Phase 1 실행
```bash
# 보안 스캔 현황 확인
./gradlew dependencyCheckAnalyze

# 취약점 해결 후 재검증  
./gradlew build test
```

#### Phase 2 실행  
```bash
# 특정 파일 테스트
./gradlew test --tests "*Repository*"
./gradlew test --tests "*GlobalExceptionHandler*"
```

#### Phase 3 실행
```bash  
# Import 최적화 (IntelliJ CLI)
idea64 optimize-imports --project-path .

# 미사용 코드 검사
./gradlew checkstyleMain | grep "unused"
```

#### Phase 4-5 실행
```bash
# Properties 파일 검증
grep -r "jakarta.validation" src/main/resources/
grep -r "messages\." src/main/java/

# 문서 형식 검증
markdownlint docs/ *.md
```

### 🚨 비상 대응 절차

#### 테스트 실패 시
1. 실패한 테스트 상세 분석
2. 수정 사항 롤백
3. 점진적 적용으로 전환
4. 근본 원인 해결

#### 빌드 실패 시  
1. 의존성 충돌 확인
2. 호환성 매트릭스 검토
3. 안전한 버전으로 다운그레이드
4. 단계별 업데이트 적용

---

## 📊 성과 측정

### KPI 지표
- **보안 스코어**: CVE 개수 (목표: 0개)
- **코드 품질**: 인스펙션 문제 개수 (목표: <100개)  
- **테스트 커버리지**: 현재 95% 유지
- **빌드 시간**: 현재 시간 유지 또는 개선

### 완료 후 재인스펙션
```bash
# IntelliJ 전체 코드 인스펙션 재실행
idea64 inspect --project-path . --profile All_Inspections --output xml-updated/

# 개선 효과 측정
python3 parse_inspections_enhanced.py  # 업데이트된 결과로
```

**성공 지표**: 전체 문제 수 1,059개 → 100개 이하 (90% 개선)