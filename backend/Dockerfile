FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml ./pom.xml
COPY services ./services

RUN mvn -f services/auth-service/pom.xml -DskipTests package

