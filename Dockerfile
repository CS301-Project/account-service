FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests
RUN cp target/*.jar app.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/app.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]