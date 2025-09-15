# 작업 완료 시 실행할 명령어

## 코드 작성 후 필수 단계

### 1. 빌드 검증
```bash
# 전체 빌드 (품질 검사 포함)
./gradlew build

# 빌드 실패 시 테스트 없이 컴파일 확인
./gradlew build -x test
```

### 2. 테스트 실행
```bash
# 모든 테스트 실행
./gradlew test

# E2E 테스트 포함
./gradlew test --tests "*" -DincludeTags=e2e

# 특정 도메인 테스트
./gradlew test --tests "bunny.boardhole.[domain].*"
```

### 3. 품질 검사
```bash
# Qodana 정적 분석 (설정된 경우)
# CI/CD에서 자동 실행되나 로컬에서도 가능

# 의존성 보안 취약점 체크
./gradlew dependencyUpdates
```

### 4. 통합 테스트 (Docker 필요)
```bash
# Docker 데몬 실행 확인
docker --version

# 애플리케이션 시작 테스트
./gradlew bootRun
# Ctrl+C로 종료 후 다음 단계

# API 접근 테스트
curl http://localhost:8080/swagger-ui/index.html
```

### 5. Git 작업
```bash
# 변경사항 확인
git status
git diff

# 스테이징
git add .

# 커밋 (의미 있는 메시지)
git commit -m "feat: [기능명] 구현"

# 또는 기존 패턴 따라
git commit -m "type(scope): description"
```

## 실패 시 대응

### 빌드 실패
- 컴파일 오류: Java 21 문법 확인
- 의존성 문제: `./gradlew --refresh-dependencies`

### 테스트 실패
- 단위 테스트: Mock 설정 확인
- E2E 테스트: Docker 컨테이너 상태 확인 `docker ps`
- DB 관련: Testcontainers 포트 충돌 확인

### Docker 문제
- 컨테이너 정리: `docker-compose down -v`
- 이미지 재다운로드: `docker-compose pull`
- 포트 충돌: `lsof -i :8080`

## 코드 리뷰 준비
- [ ] 빌드 성공
- [ ] 모든 테스트 통과  
- [ ] Null 안전성 확인 (@NullMarked)
- [ ] 아키텍처 규칙 준수
- [ ] API 문서 업데이트 (필요시)
- [ ] 로깅 적절성 확인