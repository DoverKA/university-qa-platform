FROM maven:3.9.11-eclipse-temurin-11 AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src
COPY application-example.yml ./application-example.yml

RUN mvn -B -DskipTests package

FROM eclipse-temurin:11-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
