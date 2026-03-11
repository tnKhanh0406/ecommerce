FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/*.war app.war

ENTRYPOINT ["java","-jar","app.war"]