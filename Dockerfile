# 로컬 개발용 Dockerfile - 컨테이너 내에서 빌드

# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Gradle wrapper 복사
COPY gradlew .
COPY gradle gradle
RUN chmod +x ./gradlew

# 빌드 파일 복사
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# BuildKit 캐시 마운트를 사용한 의존성 다운로드
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# BuildKit 캐시 마운트를 사용한 JAR 빌드
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드 스테이지에서 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]