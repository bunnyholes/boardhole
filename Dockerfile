# Build stage with dependency caching
FROM eclipse-temurin:21-jdk-alpine AS deps
WORKDIR /app

# Copy gradle files for dependency caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .
RUN chmod +x ./gradlew

# Download dependencies - this layer will be cached
RUN ./gradlew dependencies --no-daemon --parallel --build-cache

# Build stage
FROM deps AS build
WORKDIR /app

# Copy source code
COPY src src

# Build application with optimizations
RUN ./gradlew bootJar --no-daemon --parallel --build-cache

# Runtime stage - minimal image
FROM eclipse-temurin:21-jre-alpine AS runtime

# Install timezone data and remove package manager cache
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata && \
    rm -rf /var/cache/apk/*

# Create non-root user
RUN addgroup -g 1000 spring && \
    adduser -D -u 1000 -G spring spring

WORKDIR /app

# Copy JAR from build stage
COPY --from=build --chown=spring:spring /app/build/libs/*.jar app.jar

# Create logs directory with proper permissions
RUN mkdir -p /app/logs && \
    chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Optimized JVM options for containers
ENV JAVA_OPTS="-Xms256m -Xmx512m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseStringDeduplication \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]