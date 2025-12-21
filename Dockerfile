# 1. Etapa de Construcción (Maven)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Etapa de Ejecución (Java)
FROM openjdk:17-jdk-slim
COPY --from=build /app/target/fittrack-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]