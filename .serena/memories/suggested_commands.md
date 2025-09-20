# boardholes - 주요 개발 명령어

## 빌드 및 실행
```bash
# 애플리케이션 실행 (dev 프로필, Docker 자동 시작)
./gradlew bootRun

# 운영 프로필로 실행
./gradlew bootRun --args='--spring.profiles.active=prod'

# 전체 빌드 (품질 검사 포함)
./gradlew build

# 테스트 없이 빌드
./gradlew build -x test

# 클린 빌드
./gradlew clean build
```

## 테스트 실행
```bash
# 모든 테스트 (단위 + E2E)
./gradlew test

# E2E 테스트만 실행
./gradlew test --tests "*" -DincludeTags=e2e

# 특정 패키지 테스트
./gradlew test --tests "bunny.boardhole.board.*"

# 특정 테스트 클래스
./gradlew test --tests BoardControllerTest
```

## 품질 검사
```bash
# SonarCloud 분석 (설정되어 있다면)
./gradlew sonarAnalysis

# 의존성 버전 체크
./gradlew dependencyUpdates
```

## Docker 관리
```bash
# Docker 컨테이너 상태 확인
docker ps

# Docker 컨테이너 종료
docker-compose down

# Docker 컨테이너 및 볼륨 완전 삭제
docker-compose down -v
```

## 유틸리티 명령어 (macOS)
```bash
# Git 상태 확인
git status && git branch

# 파일 검색
find src -name "*.java" -type f

# 코드 패턴 검색
grep -r "pattern" src/

# 디렉토리 구조 보기
tree src/ -I '__pycache__|*.pyc'

# 프로세스 확인
ps aux | grep java

# 포트 사용 확인
lsof -i :8080
```

## API 접근
- 애플리케이션: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API 경로: /api/auth/*, /api/users/*, /api/boards/*