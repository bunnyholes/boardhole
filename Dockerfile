# 로컬 개발용 Dockerfile - 컨테이너 내에서 빌드

# Build stage
FROM azul/zulu-openjdk-alpine:25 AS build
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
FROM azul/zulu-openjdk-alpine:25-jre

# 보안을 위한 non-root 유저
RUN addgroup -g 1000 spring && \
    adduser -D -u 1000 -G spring spring

WORKDIR /app

# 애플리케이션 JAR 복사 (빌드 스테이지 산출물)
COPY --chown=spring:spring --from=build /app/build/libs/*.jar app.jar

USER spring:spring

EXPOSE 8080

# 런타임에 JAVA_OPTS와 SPRING_PROFILES_ACTIVE 환경변수 설정 가능
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
