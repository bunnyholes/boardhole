# Generated via JReleaser Docker packager
FROM eclipse-temurin:21-jre-alpine

ARG APP_USER=spring
ARG APP_UID=1000
ARG APP_GID=1000
ARG APP_HOME=/app
ARG APP_JAR={{distributionArtifactFile}}

RUN addgroup -g ${APP_GID} ${APP_USER} \
    && adduser -D -u ${APP_UID} -G ${APP_USER} ${APP_USER}

WORKDIR ${APP_HOME}

COPY --chown=${APP_USER}:${APP_USER} {{distributionArtifactFile}} app.jar

USER ${APP_USER}:${APP_USER}

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
