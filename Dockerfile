FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn
COPY src ./src

RUN chmod +x mvnw
RUN ./mvnw clean package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.war app.war

ENTRYPOINT ["java", "-jar", "app.war"]