# 품질 도구 점진적 개선 로드맵

## 현재 설정 (경고 모드)

모든 품질 도구가 경고만 출력하고 빌드는 계속 진행됩니다.

### 커버리지 임계값 (현재)
- 전체: 60% (현재 64%)
- 브랜치: 40% 
- 메서드: 50%

### 목표 임계값 (75% 달성 후)
- 전체: 80%
- 브랜치: 70%
- 메서드: 75%

## 점진적 개선 계획

### 1단계: 현재 위반사항 파악
- PMD: 1105개 위반사항
- SpotBugs: 보안/노출 관련 위반사항
- 커버리지: 64% (목표 80%)

### 2단계: 우선순위 수정
1. 보안 관련 위반사항 (SpotBugs SECURITY)
2. 성능 관련 위반사항 (PMD PERFORMANCE)
3. 정확성 관련 위반사항 (CORRECTNESS)
4. 커버리지 증대

### 3단계: 임계값 단계적 상향 조정
- 70% 달성 → 65% 설정
- 75% 달성 → 70% 설정
- 80% 달성 → ignoreFailures = false로 변경

## 설정 변경 방법

### 엄격 모드 활성화 (75% 이상 달성 시)

```gradle
// build.gradle에서 다음 값들을 변경:

pmd {
    ignoreFailures = false  // 빌드 실패 활성화
}

spotbugs {
    ignoreFailures = false  // 빌드 실패 활성화
}

checkstyle {
    ignoreFailures = false  // 빌드 실패 활성화
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80  // 최종 목표 80%
            }
        }
        // 기타 규칙들도 목표값으로 조정
    }
}
```

## 품질 지표 모니터링

### 일일 체크리스트
- [ ] `./gradlew qualityCheck` 실행
- [ ] 리포트 확인: build/reports/
- [ ] 위반사항 개수 추적
- [ ] 커버리지 비율 확인

### 주간 목표
- PMD 위반사항 10% 감소
- 커버리지 2% 증가
- SpotBugs 보안 이슈 우선 수정