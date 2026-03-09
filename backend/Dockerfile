FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY . .

ARG MODULE_PATH
# Збираємо модуль і одразу видаляємо .jar.original файли, щоб лишився тільки один правильний JAR
RUN mvn clean package -pl ${MODULE_PATH} -am -DskipTests && \
    find ${MODULE_PATH}/target -name "*.jar.original" -delete

FROM eclipse-temurin:21-jre
WORKDIR /app

ARG MODULE_PATH
# Тепер тут гарантовано лише один JAR файл
COPY --from=builder /app/${MODULE_PATH}/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
