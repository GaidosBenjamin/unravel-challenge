FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn

COPY pom.xml .
COPY unravel-api/pom.xml unravel-api/pom.xml
COPY unravel-service/pom.xml unravel-service/pom.xml
COPY unravel-integration-tests/pom.xml unravel-integration-tests/pom.xml

RUN ./mvnw -B dependency:go-offline

COPY unravel-api unravel-api
COPY unravel-service unravel-service

RUN ./mvnw clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/unravel-service/target/*fat.jar app.jar