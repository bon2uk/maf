FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY . .

ARG MODULE_PATH
RUN mvn clean package -pl ${MODULE_PATH} -am -DskipTests && \
    find ${MODULE_PATH}/target -name "*.jar.original" -delete

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system --gid 1001 appgroup && \
    useradd --system --uid 1001 --gid appgroup appuser

ARG MODULE_PATH
COPY --from=builder /app/${MODULE_PATH}/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
