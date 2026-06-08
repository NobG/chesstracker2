FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN mvn -B -DskipTests dependency:go-offline

COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S chesstracker2 && adduser -S chesstracker2 -G chesstracker2

WORKDIR /app
COPY --from=build /workspace/target/chesstracker2-0.1.0-SNAPSHOT.jar /app/chesstracker2.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

USER chesstracker2
ENTRYPOINT ["java", "-jar", "/app/chesstracker2.jar"]
