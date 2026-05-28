# Stage 1: Build
FROM gradle:8.14-jdk21 AS builder
WORKDIR /workspace
COPY . .
RUN gradle :control-service:bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/control-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
