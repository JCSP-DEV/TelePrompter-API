FROM maven:3.9.9-eclipse-temurin-21 as build

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=build app/target/demo-0.0.1-SNAPSHOT.jar /app/demo-0.0.1-SNAPSHOT.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "-Ddb.password=root", "-Ddb.user=root", "-Ddb.url=jdbc:mysql://mysql:3306/testtfg", "/app/demo-0.0.1-SNAPSHOT.jar"]